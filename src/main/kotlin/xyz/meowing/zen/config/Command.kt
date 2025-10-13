package xyz.meowing.zen.config

import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.mc
import xyz.meowing.zen.hud.HUDEditor
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.knit.api.command.Commodore

@Zen.Command
object ConfigCommodore : Commodore("zen", "ma", "meowaddons") {
    init {
        runs {
            TickUtils.schedule(1) {
                Zen.openConfig()
            }
        }

        literal("hud").runs {
            TickUtils.schedule(1) {
                mc.displayGuiScreen(HUDEditor())
            }
        }
    }
}