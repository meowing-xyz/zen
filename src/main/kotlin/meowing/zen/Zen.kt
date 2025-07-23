package meowing.zen

import meowing.zen.compat.OldConfig
import meowing.zen.config.ConfigAccessor
import meowing.zen.config.ZenConfig
import meowing.zen.config.ui.ConfigUI
import meowing.zen.events.AreaEvent
import meowing.zen.events.EntityEvent
import meowing.zen.events.EventBus
import meowing.zen.events.GameEvent
import meowing.zen.events.GuiEvent
import meowing.zen.events.WorldEvent
import meowing.zen.feats.Feature
import meowing.zen.feats.FeatureLoader
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.DataUtils
import meowing.zen.utils.LocationUtils
import meowing.zen.utils.TickUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.event.ClickEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent

data class firstInstall (val isFirstInstall: Boolean = true)

@Mod(modid = "zen", name = "Zen", version = "1.8.9", useMetadata = true, clientSideOnly = true)
class Zen {
    private var eventCall: EventBus.EventCall? = null
    private lateinit var dataUtils: DataUtils<firstInstall>

    @Target(AnnotationTarget.CLASS)
    annotation class Module

    @Target(AnnotationTarget.CLASS)
    annotation class Command

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        EventBus.post(GameEvent.Load())

        OldConfig.convertConfig(mc.mcDataDir)
        configUI = ZenConfig()
        config = ConfigAccessor(configUI)
        FeatureLoader.init()
        executePendingCallbacks()

        dataUtils = DataUtils("zen-data", firstInstall())

        eventCall = EventBus.register<EntityEvent.Join> ({ event ->
            if (event.entity == mc.thePlayer) {
                ChatUtils.addMessage(
                    "$prefix §fMod loaded - §c${FeatureLoader.getModuleCount()} §ffeatures",
                    "§c${FeatureLoader.getLoadtime()}ms §8- §c${FeatureLoader.getCommandCount()} commands"
                )
                val data = dataUtils.getData()
                if (data.isFirstInstall) {
                    ChatUtils.addMessage("$prefix §fThanks for installing Zen!")
                    ChatUtils.addMessage("§7> §fUse §c/zen §fto open the config or §c/zenhud §fto edit HUD elements")
                    ChatUtils.addMessage("§7> §cDiscord:§b [Discord]", "Discord server", ClickEvent.Action.OPEN_URL, "https://discord.gg/KPmHQUC97G")
                    dataUtils.setData(data.copy(isFirstInstall = false))
                    dataUtils.save()
                }
                UpdateChecker.checkForUpdates()
                eventCall?.unregister()
                eventCall = null
            }
        })

        EventBus.register<GuiEvent.Open> ({ event ->
            if (event.screen is GuiInventory) isInInventory = true
        })

        EventBus.register<GuiEvent.Close> ({
            isInInventory = false
        })

        EventBus.register<AreaEvent.Main> ({
            TickUtils.scheduleServer(1) {
                areaFeatures.forEach { it.update() }
            }
        })

        EventBus.register<AreaEvent.Sub> ({
            TickUtils.scheduleServer(1) {
                subareaFeatures.forEach { it.update() }
            }
        })

        EventBus.register<WorldEvent.Load> ({
            TickUtils.scheduleServer(1) {
                skyblockFeatures.forEach { it.update() }
            }
        })
    }

    @Mod.EventHandler
    fun stop(event: FMLServerStoppingEvent) {
        EventBus.post(GameEvent.Unload())
    }

    companion object {
        private val pendingCallbacks = mutableListOf<Pair<String, (Any) -> Unit>>()
        private val areaFeatures = mutableListOf<Feature>()
        private val subareaFeatures = mutableListOf<Feature>()
        private val skyblockFeatures = mutableListOf<Feature>()
        private lateinit var configUI: ConfigUI
        lateinit var config: ConfigAccessor
        const val prefix = "§7[§bZen§7]"
        val features = mutableListOf<Feature>()
        val mc: Minecraft = Minecraft.getMinecraft()
        var isInInventory = false

        private fun executePendingCallbacks() {
            pendingCallbacks.forEach { (configKey, callback) ->
                configUI.registerListener(configKey, callback)
            }
            pendingCallbacks.clear()
        }

        fun registerListener(configKey: String, instance: Any) {
            val callback: (Any) -> Unit = { _ ->
                when (instance) {
                    is Feature -> instance.update()
                    else -> {
                        val isEnabled = config.getValue(configKey, false)
                        if (isEnabled) MinecraftForge.EVENT_BUS.register(instance)
                        else MinecraftForge.EVENT_BUS.unregister(instance)
                    }
                }
            }

            if (::configUI.isInitialized) {
                configUI.registerListener(configKey, callback)
            } else {
                pendingCallbacks.add(configKey to callback)
            }
        }

        fun registerCallback(configKey: String, callback: (Any) -> Unit) {
            if (::configUI.isInitialized) configUI.registerListener(configKey, callback)
            else pendingCallbacks.add(configKey to callback)
        }

        fun addFeature(feature: Feature) {
            features.add(feature)

            if (feature.hasAreas()) areaFeatures.add(feature)
            if (feature.hasSubareas()) subareaFeatures.add(feature)
            if (feature.checksSkyblock()) skyblockFeatures.add(feature)

            feature.addConfig(configUI)
        }

        fun openConfig() {
            mc.displayGuiScreen(configUI)
        }
    }
}