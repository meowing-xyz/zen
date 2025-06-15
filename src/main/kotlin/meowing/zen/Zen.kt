package meowing.zen

import meowing.zen.utils.ChatUtils
import net.minecraft.client.Minecraft
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent


@Mod(modid = "zen", name = "Zen", version = "1.8.9", useMetadata = true, clientSideOnly = true)
class Zen {
    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        val startTime = System.currentTimeMillis()
        FeatLoader.init()
        val loadTime = System.currentTimeMillis() - startTime
        MinecraftForge.EVENT_BUS.register(loadMessage(loadTime))
    }
}


class loadMessage(private val loadTime: Long) {
    @SubscribeEvent
    fun onEntityJoinWorld(event: net.minecraftforge.event.entity.EntityJoinWorldEvent) {
        if (event.entity == Minecraft.getMinecraft().thePlayer && event.world.isRemote) {
            ChatUtils.addMessage(String.format("§c[Zen] §fMod loaded in §c%dms §7| §c%d features", loadTime, FeatLoader.getModuleCount()))
            MinecraftForge.EVENT_BUS.unregister(this)
        }
    }
}