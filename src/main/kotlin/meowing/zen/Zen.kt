package meowing.zen

import meowing.zen.config.ConfigAccessor
import meowing.zen.config.ZenConfig
import meowing.zen.config.ui.ConfigUI
import meowing.zen.events.AreaEvent
import meowing.zen.events.EntityEvent
import meowing.zen.events.EventBus
import meowing.zen.events.GuiEvent
import meowing.zen.feats.Feature
import meowing.zen.feats.FeatureLoader
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.DataUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.event.ClickEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent

data class firstInstall(val isFirstInstall: Boolean = true)

@Mod(modid = "zen", name = "Zen", version = "1.8.9", useMetadata = true, clientSideOnly = true)
class Zen {
    private var eventCall: EventBus.EventCall? = null
    private lateinit var dataUtils: DataUtils<firstInstall>
    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        configUI = ZenConfig()
        config = ConfigAccessor(configUI)
        FeatureLoader.init()
        dataUtils = DataUtils("zen-data", firstInstall())
        eventCall = EventBus.register<EntityEvent.Join> ({ event ->
            if (event.entity == Minecraft.getMinecraft().thePlayer) {
                ChatUtils.addMessage(
                    "§c[Zen] §fMod loaded - §c${FeatureLoader.getModuleCount() + 1} §ffeatures",
                    "§c${FeatureLoader.getLoadtime()}ms §8- §c${FeatureLoader.getCommandCount()} commands §7| §c10 utils"
                )
                eventCall?.unregister()
                eventCall = null
                val data = dataUtils.getData()
                if (data.isFirstInstall) {
                    ChatUtils.addMessage("§c[Zen] §fThanks for installing Zen!")
                    ChatUtils.addMessage("§7> §fUse §c/zen §fto open the config or §c/zenhud §fto edit HUD elements")
                    ChatUtils.addMessage("§7> §cDiscord:§b [Discord]", "Discord server", ClickEvent.Action.OPEN_URL, "https://discord.gg/KPmHQUC97G")
                }
                dataUtils.setData(data.copy(isFirstInstall = false))
                dataUtils.save()
                UpdateChecker.checkForUpdates()
            }
        })
        EventBus.register<GuiEvent.Open> ({ event ->
            if (event.screen is GuiInventory) isInInventory = true
        })
        EventBus.register<GuiEvent.Close> ({
            isInInventory = false
        })
        EventBus.register<AreaEvent.Main> ({ updateFeatures() })
        EventBus.register<AreaEvent.Sub> ({ updateFeatures() })
    }

    companion object {
        val features = mutableListOf<Feature>()
        val mc = Minecraft.getMinecraft()
        var isInInventory = false
        private lateinit var configUI: ConfigUI
        lateinit var config: ConfigAccessor

        private fun updateFeatures() {
            features.forEach { it.update() }
        }

        fun registerListener(configKey: String, instance: Any) {
            configUI.registerListener(configKey) { newValue ->
                val isEnabled = newValue as? Boolean ?: false
                if (instance is Feature) {
                    instance.onToggle(isEnabled)
                } else {
                    if (isEnabled) MinecraftForge.EVENT_BUS.register(instance)
                    else MinecraftForge.EVENT_BUS.unregister(instance)
                }
            }
        }

        fun registerCallback(configKey: String, callback: (Any) -> Unit) {
            configUI.registerListener(configKey) { newValue ->
                callback(newValue)
            }
        }

        fun addFeature(feature: Feature) {
            features.add(feature)
        }

        fun openConfig() {
            mc.displayGuiScreen(configUI)
        }
    }
}