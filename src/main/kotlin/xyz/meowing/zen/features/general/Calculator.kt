package xyz.meowing.zen.features.general

import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.LOGGER
import xyz.meowing.zen.Zen.Companion.prefix
import xyz.meowing.zen.utils.ChatUtils
import xyz.meowing.knit.api.command.Commodore
import xyz.meowing.knit.api.command.utils.GreedyString
import javax.script.ScriptEngineManager

@Zen.Command
object Calculator : Commodore("zencalc", "calc") {
    init {
        runs { equation: GreedyString ->
            try {
                val engine = ScriptEngineManager(null).getEngineByName("JavaScript") ?: return@runs
                val sanitized = equation.string.replace(Regex("[^0-9+\\-*/().\\s]"), "")
                val result = engine.eval(sanitized) ?: return@runs

                ChatUtils.addMessage("$prefix §b${equation.string} §f= §b$result")
            } catch (e: Exception) {
                ChatUtils.addMessage("$prefix §fInvalid equation.")
                LOGGER.warn("[Zen] Invalid equation: $e")
            }
        }
    }
}