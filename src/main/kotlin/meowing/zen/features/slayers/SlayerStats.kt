package meowing.zen.features.slayers

import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.features.Feature
import meowing.zen.hud.HUDManager
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.CommandUtils
import meowing.zen.utils.Render2D
import meowing.zen.utils.TimeUtils
import meowing.zen.utils.TimeUtils.millis
import net.minecraft.command.ICommandSender
import kotlin.time.Duration

@Zen.Module
object SlayerStats : Feature("slayerstats") {
    private const val name = "SlayerStats"
    private var kills = 0
    private var sessionStart = TimeUtils.now
    private var totalKillTime = Duration.ZERO

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Slayers", "Slayer stats", ConfigElement(
                "slayerstats",
                "Slayer stats",
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Slayers", "Slayer stats", "", ConfigElement(
                "",
                null,
                ElementType.TextParagraph("Shows slayer statistics such as total bosses killed, bosses per hour, and average kill time. §c/slayerstats reset §rto reset stats.")
            ))
    }

    override fun initialize() {
        HUDManager.register("SlayerStats", "$prefix §f§lSlayer Stats: \n§7> §bTotal bosses§f: §c15\n§7> §bBosses/hr§f: §c12\n§7> §bAvg. kill§f: §c45.2s")

        register<RenderEvent.Text> {
            if (HUDManager.isEnabled("SlayerStats")) render()
        }
    }

    fun addKill(killtime: Duration) {
        kills++
        totalKillTime += killtime
    }

    private fun getBPH(): Int {
        val sessionDuration = sessionStart.since
        return if (sessionDuration.millis > 0) (kills * 3600000 / sessionDuration.millis).toInt() else 0
    }

    private fun getAVG() = "${(totalKillTime.millis / kills / 1000.0).format(1)}s"

    fun reset() {
        kills = 0
        sessionStart = TimeUtils.now
        totalKillTime = Duration.ZERO
        ChatUtils.addMessage("$prefix §fSlayer stats reset!")
    }

    private fun render() {
        val x = HUDManager.getX(name)
        val y = HUDManager.getY(name)
        val scale = HUDManager.getScale(name)
        val lines = getLines()

        if (lines.isNotEmpty()) {
            var currentY = y
            for (line in lines) {
                Render2D.renderString(line, x, currentY, scale)
                currentY += fontRenderer.FONT_HEIGHT + 2
            }
        }
    }

    private fun getLines(): List<String> {
        if (kills > 0) {
            return listOf(
                "$prefix §f§lSlayer Stats: ",
                "§7> §bTotal bosses§f: §c${kills}",
                "§7> §bBosses/hr§f: §c${getBPH()}",
                "§7> §bAvg. kill§f: §c${getAVG()}"
            )
        }
        return emptyList()
    }
    private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)
}

@Zen.Command
object SlayerStatsCommand : CommandUtils(
    "slayerstats",
    "/slayerstats reset - Resets slayer statistics",
    listOf("zenslayers")
) {
    override fun processCommand(sender: ICommandSender?, args: Array<out String?>?) {
        val stringArgs = args?.filterNotNull()?.toTypedArray() ?: return

        if (stringArgs.size == 1 && stringArgs[0] == "reset") SlayerStats.reset()
        else ChatUtils.addMessage("$prefix §fCommand: §c/slayerstats reset")
    }
}