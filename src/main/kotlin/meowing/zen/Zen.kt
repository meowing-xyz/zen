package meowing.zen

import meowing.zen.config.zenconfig
import meowing.zen.commands.gui
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
object Zen {
    lateinit var config: zenconfig

    fun registerListener(configKey: String, instance: Any) {
        val toggleRegistration = {
            if (config.javaClass.getDeclaredField(configKey).get(config) as Boolean) MinecraftForge.EVENT_BUS.register(instance)
            else MinecraftForge.EVENT_BUS.unregister(instance)
        }
        config.registerListener(configKey, toggleRegistration)
        toggleRegistration()
    }


    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        config = zenconfig()
        TickScheduler.register()
        val startTime = System.currentTimeMillis()
        FeatLoader.init()
        val loadTime = System.currentTimeMillis() - startTime
        MinecraftForge.EVENT_BUS.register(loadMessage(loadTime))
        ClientCommandHandler.instance.registerCommand(gui())
    }

    class loadMessage(private val loadTime: Long) {
        @SubscribeEvent
        fun onEntityJoinWorld(event: EntityJoinWorldEvent) {
            if (event.entity == Minecraft.getMinecraft().thePlayer && event.world.isRemote) {
                ChatUtils.addMessage(String.format("§c[Zen] §fMod loaded in §c%dms §7| §c%d features", loadTime, FeatLoader.getModuleCount()))
                MinecraftForge.EVENT_BUS.unregister(this)
            }
        }
    }
}