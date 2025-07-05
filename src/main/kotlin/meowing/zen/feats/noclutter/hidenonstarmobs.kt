package meowing.zen.feats.noclutter

import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.entity.item.EntityArmorStand

object hidenonstarmobs : Feature("hidenonstarmobs", area = "catacombs") {
    private val regex = "^(?:\\[Lv\\d+] )?[\\w ]+ [\\d,.]+\\w(?:/[\\d,.]+\\w)?❤$".toRegex()

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("No clutter", "Dungeons", ConfigElement(
                "hidenonstarmobs",
                "Hide non-starred mob nametags",
                "Hides non-starred mob's nametags in Dungeons",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<EntityEvent.Join> { event ->
            TickUtils.scheduleServer(1) {
                if (event.entity is EntityArmorStand && regex.matches(event.entity.name.removeFormatting())) event.entity.setDead()
            }
        }
    }
}