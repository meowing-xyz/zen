package meowing.zen

import meowing.zen.config.zenconfig
import meowing.zen.config.command
import meowing.zen.events.AreaEvent
import meowing.zen.events.EventBus
import meowing.zen.events.EntityJoinEvent
import meowing.zen.events.GuiCloseEvent
import meowing.zen.events.GuiOpenEvent
import meowing.zen.events.SubAreaEvent
import meowing.zen.feats.carrying.carrycommand
import meowing.zen.feats.Feature
import meowing.zen.feats.FeatureLoader
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.Location
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent

@Mod(modid = "zen", name = "Zen", version = "1.8.9", useMetadata = true, clientSideOnly = true)
class Zen {
    private var eventCall: EventBus.EventCall? = null
    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        config = zenconfig()
        FeatureLoader.init()
        Location.initialize()
        eventCall = EventBus.register<EntityJoinEvent> ({ event ->
            if (event.entity == Minecraft.getMinecraft().thePlayer) {
                ChatUtils.addMessage("§c[Zen] §fMod loaded - §c${FeatureLoader.getModuleCount()} §ffeatures")
                eventCall?.unregister()
                eventCall = null
                UpdateChecker.checkForUpdates()
            }
        })
        EventBus.register<GuiOpenEvent> ({ event ->
            if (event.screen is GuiInventory) isInInventory = true
        })
        EventBus.register<GuiCloseEvent> ({
            isInInventory = false
        })
        EventBus.register<AreaEvent> ({
            for (feat in features)
                feat.update()
        })
        EventBus.register<SubAreaEvent>({
            for (feat in features)
                feat.update()
        })
        ClientCommandHandler.instance.registerCommand(command())
        ClientCommandHandler.instance.registerCommand(carrycommand())
    }

    companion object {
        val features = mutableListOf<Feature>()
        val mc = Minecraft.getMinecraft()
        var isInInventory = false
        lateinit var config: zenconfig

        fun registerListener(configKey: String, instance: Any) {
            val toggleRegistration = {
                val isEnabled = config.javaClass.getDeclaredField(configKey).get(config) as Boolean
                if (instance is Feature) {
                    instance.onToggle(isEnabled)
                } else {
                    if (isEnabled) MinecraftForge.EVENT_BUS.register(instance)
                    else MinecraftForge.EVENT_BUS.unregister(instance)
                }
            }
            config.registerListener(configKey, toggleRegistration)
            toggleRegistration()
        }

        fun addFeature(feature: Feature) {
            features.add(feature)
        }
    }
}