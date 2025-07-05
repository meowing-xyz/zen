package meowing.zen.feats.noclutter

import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils.removeFormatting

object hidedamage : Feature("hidedamage", area = "catacombs") {
    private val regex = "^.?\\d[\\d,.]+.*?$".toRegex()

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("No clutter", "Dungeons", ConfigElement(
                "hidedamage",
                "Hide damage in dungeons",
                "Hides the damage nametag in dungeons.",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<EntityEvent.Join> { event ->
            TickUtils.scheduleServer(1) {
                if (regex.matches(event.entity.name.removeFormatting())) event.entity.setDead()
            }
        }
    }
}