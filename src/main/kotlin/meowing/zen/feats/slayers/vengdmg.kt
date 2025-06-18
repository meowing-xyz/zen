package meowing.zen.feats.slayers

import meowing.zen.Zen
import meowing.zen.events.EntityMetadataUpdateEvent
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.TickScheduler
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Pattern

object vengdmg {
    private val mc = Minecraft.getMinecraft()
    private var nametagID = -1
    private val veng = Pattern.compile("^\\d+(,\\d+)*ﬗ$")

    @JvmStatic
    fun initialize() {
        Zen.registerListener("vengdmg", this)
    }

    @SubscribeEvent
    fun onEntityMetadataUpdate(event: EntityMetadataUpdateEvent) {
        event.packet.func_149376_c()?.find { it.dataValueId == 2 && it.`object` is String }?.let { obj ->
            val name = (obj.`object` as String).removeFormatting()
            if (name.contains("Spawned by") && name.endsWith("by: ${mc.thePlayer?.name}")) nametagID = event.packet.entityId
        }
    }

    @SubscribeEvent
    fun onEntitySpawn(event: EntityJoinWorldEvent) {
        TickScheduler.scheduleServer(2) {
            val name = event.entity.name?.removeFormatting() ?: return@scheduleServer
            val vengmatch = veng.matcher(name)
            if (vengmatch.matches() && nametagID != -1) {
                val spawnedEntity = mc.theWorld.getEntityByID(event.entity.entityId) ?: return@scheduleServer
                val nametagEntity = mc.theWorld.getEntityByID(nametagID) ?: return@scheduleServer
                val distance = spawnedEntity.getDistanceToEntity(nametagEntity)
                if (distance <= 5) {
                    val numname = vengmatch.group(0).replace("ﬗ", "")
                    val num = numname.replace(",", "").toLongOrNull()
                    if (num != null && num > 500000) ChatUtils.addMessage("§c[Zen] §fVeng DMG: §c$numname")
                }
            }
        }
    }
}