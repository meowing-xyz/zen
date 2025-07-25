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
import meowing.zen.events.RenderEvent
import meowing.zen.events.WorldEvent
import meowing.zen.feats.Feature
import meowing.zen.feats.FeatureLoader
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.DataUtils
import meowing.zen.utils.Render3D
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils.partialTicks
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.event.ClickEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent

@Mod(modid = "zen", name = "Zen", version = "1.8.9", useMetadata = true, clientSideOnly = true)
class Zen {
    data class PersistentData (val isFirstInstall: Boolean = true)
    private var eventCall: EventBus.EventCall? = null
    private lateinit var FirstInstall: DataUtils<PersistentData>

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

        FirstInstall = DataUtils("zen-data", PersistentData())

        eventCall = EventBus.register<EntityEvent.Join> ({ event ->
            if (event.entity == mc.thePlayer) {
                ChatUtils.addMessage(
                    "$prefix §fMod loaded - §c${FeatureLoader.getModuleCount()} §ffeatures",
                    "§c${FeatureLoader.getLoadtime()}ms §8- §c${FeatureLoader.getCommandCount()} commands"
                )
                val data = FirstInstall.getData()
                if (data.isFirstInstall) {
                    ChatUtils.addMessage("$prefix §fThanks for installing Zen!")
                    ChatUtils.addMessage("§7> §fUse §c/zen §fto open the config or §c/zenhud §fto edit HUD elements")
                    ChatUtils.addMessage("§7> §cDiscord:§b [Discord]", "Discord server", ClickEvent.Action.OPEN_URL, "https://discord.gg/KPmHQUC97G")
                    FirstInstall.setData(data.copy(isFirstInstall = false))
                    FirstInstall.save()
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

        EventBus.register<RenderEvent.LivingEntity.Post> ({ event ->
            if (mc.theWorld == null) return@register
            Render3D.drawString(event.entity.entityId.toString(), event.entity.positionVector, partialTicks)
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

            feature.addConfig(configUI)
        }

        fun openConfig() {
            mc.displayGuiScreen(configUI)
        }
    }
}