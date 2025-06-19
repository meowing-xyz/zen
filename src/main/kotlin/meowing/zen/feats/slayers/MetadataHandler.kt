package meowing.zen.feats.slayers

import meowing.zen.Zen
import meowing.zen.events.EntityMetadataUpdateEvent
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.Minecraft
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object MetadataHandler {
    private val mc = Minecraft.getMinecraft()
    private var enabled = false

    @JvmStatic
    fun initialize() {
        Zen.config.registerListener("slayertimer") {
            checkAndToggle()
        }
        Zen.config.registerListener("vengdmg") {
            checkAndToggle()
        }
        checkAndToggle()
    }

    private fun checkAndToggle() {
        val shouldEnable = Zen.config.slayertimer || Zen.config.vengdmg

        if (shouldEnable && !enabled) {
            enabled = true
            MinecraftForge.EVENT_BUS.register(this)
        } else if (!shouldEnable && enabled) {
            enabled = false
            MinecraftForge.EVENT_BUS.unregister(this)
        }
    }

    @SubscribeEvent
    fun onEntityMetadataUpdate(event: EntityMetadataUpdateEvent) {
        val world = mc.theWorld ?: return
        val player = mc.thePlayer ?: return

        event.packet.func_149376_c()?.find { it.dataValueId == 2 && it.`object` is String }?.let { obj ->
            val name = (obj.`object` as String).removeFormatting()
            if (name.contains("Spawned by") && name.endsWith("by: ${player.name}")) {
                val entity = world.getEntityByID(event.packet.entityId) ?: return
                val hasBlackhole = world.loadedEntityList.any {
                    it.name?.removeFormatting()?.lowercase()?.contains("black hole") == true && entity.getDistanceToEntity(it) <= 3
                }

                if (hasBlackhole) return
                if (Zen.config.slayertimer) slayertimer.handleBossSpawn(event.packet.entityId)
                if (Zen.config.vengdmg) vengdmg.handleNametagUpdate(event.packet.entityId)
            }
        }
    }
}