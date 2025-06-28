package meowing.zen.feats.noclutter

import meowing.zen.events.EntityJoinEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.entity.item.EntityArmorStand

object hidenonstarmobs : Feature("hidenonstarmobs", area = "catacombs") {
    private val regex = "^(?:\\[Lv\\d+] )?[\\w ]+ [\\d,.]+\\w(?:/[\\d,.]+\\w)?❤$".toRegex()
    override fun initialize() {
        register<EntityJoinEvent> { event ->
            TickUtils.scheduleServer(2) {
                if (event.entity is EntityArmorStand && regex.matches(event.entity.name.removeFormatting()))
                    event.cancel()
            }
        }
    }
}