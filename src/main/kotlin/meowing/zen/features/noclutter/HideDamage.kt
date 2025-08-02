package meowing.zen.features.noclutter

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import meowing.zen.features.Feature
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils.removeFormatting

@Zen.Module
object HideDamage : Feature("hidedamage", area = "catacombs") {
    private val regex = "[✧✯]?(\\d{1,3}(?:,\\d{3})*[⚔+✧❤♞☄✷ﬗ✯]*)".toRegex()

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("No clutter", "Hide damage in dungeons", ConfigElement(
                "hidedamage",
                "Hide damage in dungeons",
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        register<EntityEvent.Join> { event ->
            TickUtils.scheduleServer(1) {
                if (regex.matches(event.entity.name.removeFormatting())) event.entity.setDead()
            }
        }
    }
}