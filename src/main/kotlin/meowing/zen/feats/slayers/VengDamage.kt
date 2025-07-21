package meowing.zen.feats.slayers

import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils.removeFormatting
import java.util.regex.Pattern

@Zen.Module
object VengDamage : Feature("vengdmg") {
    private var nametagID = -1
    private val veng = Pattern.compile("^\\d+(,\\d+)*ﬗ$")

    fun handleNametagUpdate(entityId: Int) {
        nametagID = entityId
    }

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Slayers", "Blaze", ConfigElement(
                "vengdmg",
                "Vengeance damager tracker",
                "Tracks and sends your vegeance damage in the chat.",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<EntityEvent.Join> ({ event ->
            if (nametagID == -1) return@register

            TickUtils.scheduleServer(2) {
                val entityName = event.entity.name?.removeFormatting() ?: return@scheduleServer
                val vengMatch = veng.matcher(entityName)

                if (vengMatch.matches()) {
                    val spawnedEntity = mc.theWorld?.getEntityByID(event.entity.entityId) ?: return@scheduleServer
                    val nametagEntity = mc.theWorld?.getEntityByID(nametagID) ?: return@scheduleServer

                    if (spawnedEntity.getDistanceToEntity(nametagEntity) <= 5) {
                        val numStr = vengMatch.group(0).replace("ﬗ", "").replace(",", "")
                        numStr.toLongOrNull()?.let { num ->
                            if (num > 500000) ChatUtils.addMessage("$prefix §fVeng DMG: §c${vengMatch.group(0).replace("ﬗ", "")}")
                        }
                    }
                }
            }
        })
    }
}