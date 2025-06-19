package meowing.zen.feats.slayers

import meowing.zen.Zen
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.TickScheduler
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.Minecraft
import net.minecraftforge.event.entity.EntityJoinWorldEvent
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

    fun handleNametagUpdate(entityId: Int) {
        nametagID = entityId
    }

    @SubscribeEvent
    fun onEntitySpawn(event: EntityJoinWorldEvent) {
        if (nametagID == -1) return

        TickScheduler.scheduleServer(2) {
            val entityName = event.entity.name?.removeFormatting() ?: return@scheduleServer
            val vengMatch = veng.matcher(entityName)

            if (vengMatch.matches()) {
                val spawnedEntity = mc.theWorld?.getEntityByID(event.entity.entityId) ?: return@scheduleServer
                val nametagEntity = mc.theWorld?.getEntityByID(nametagID) ?: return@scheduleServer

                if (spawnedEntity.getDistanceToEntity(nametagEntity) <= 5) {
                    val numStr = vengMatch.group(0).replace("ﬗ", "").replace(",", "")
                    numStr.toLongOrNull()?.let { num ->
                        if (num > 500000) ChatUtils.addMessage("§c[Zen] §fVeng DMG: §c${vengMatch.group(0).replace("ﬗ", "")}")
                    }
                }
            }
        }
    }
}