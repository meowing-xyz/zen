package meowing.zen

import meowing.zen.config.zenconfig
import meowing.zen.config.command
import meowing.zen.feats.carrying.carrycommand
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.TickScheduler
import net.minecraft.client.Minecraft
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@Mod(modid = "zen", name = "Zen", version = "1.8.9", useMetadata = true, clientSideOnly = true)
class Zen {
    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        config = zenconfig()
        TickScheduler.register()
        val startTime = System.currentTimeMillis()
        FeatLoader.init()
        val loadTime = System.currentTimeMillis() - startTime
        MinecraftForge.EVENT_BUS.register(loadMessage(loadTime))
        ClientCommandHandler.instance.registerCommand(command())
        ClientCommandHandler.instance.registerCommand(carrycommand())
    }
    class loadMessage(private val loadTime: Long) {
        @SubscribeEvent
        fun onEntityJoinWorld(event: EntityJoinWorldEvent) {
            if (event.entity == Minecraft.getMinecraft().thePlayer) {
                ChatUtils.addMessage("§c[Zen] §fMod loaded in §c${loadTime}ms §7| §c${FeatLoader.getModuleCount()} features")
                MinecraftForge.EVENT_BUS.unregister(this)
                UpdateChecker.checkForUpdates()
            }
        }
    }
    companion object {
        val mc = Minecraft.getMinecraft()
        lateinit var config: zenconfig
        fun registerListener(configKey: String, instance: Any) {
            val toggleRegistration = {
                if (config.javaClass.getDeclaredField(configKey).get(config) as Boolean) MinecraftForge.EVENT_BUS.register(instance)
                else MinecraftForge.EVENT_BUS.unregister(instance)
            }
            config.registerListener(configKey, toggleRegistration)
            toggleRegistration()
        }
    }
}