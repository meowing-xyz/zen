package meowing.zen.features.slayers

import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.api.SlayerTracker
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.events.SkyblockEvent
import meowing.zen.features.Feature
import meowing.zen.hud.HUDManager
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.CommandUtils
import meowing.zen.utils.Render2D
import meowing.zen.utils.TimeUtils
import meowing.zen.utils.TimeUtils.millis
import meowing.zen.utils.Utils.toFormattedDuration
import net.minecraft.command.ICommandSender

@Zen.Module
object SlayerStats : Feature("slayerstats", true) {
    private const val name = "SlayerStats"
    private val slayertimer by ConfigDelegate<Boolean>("slayertimer")
    private val slayerstatslines by ConfigDelegate<Set<Int>>("slayerstatslines")

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
                ElementType.TextParagraph("Shows slayer statistics such as total bosses killed, bosses per hour, and average kill time. §c/slayerstats reset §rto reset stats. Requires §eSlayer Timer§r to be enabled.")
            ))
            .addElement("Slayers", "Slayer stats", "Options", ConfigElement(
                "slayerstatslines",
                "",
                ElementType.MultiCheckbox(
                    options = listOf("Show Total Bosses", "Show Bosses/hr", "Show Average kill time", "Show Average spawn time", "Show Total Session time"),
                    default = setOf(0, 1, 2)
                )
            ))
    }

    override fun initialize() {
        HUDManager.register("SlayerStats", "$prefix §f§lSlayer Stats: \n§7> §bTotal bosses§f: §c15\n§7> §bBosses/hr§f: §c12\n§7> §bAvg. kill§f: §c45.2s")

        register<RenderEvent.Text> {
            if (HUDManager.isEnabled("SlayerStats")) render()
        }

        register<SkyblockEvent.Slayer.Death> {
            if (!slayertimer) {
                ChatUtils.addMessage("$prefix §cYou must enable the §eSlayer Timer§c feature for Slayer Stats to work.")
            }
        }
    }


    private fun getBPH(): Int {
        val sessionDuration = SlayerTracker.sessionStart.since.millis
        return if (sessionDuration > 0) (SlayerTracker.sessionKills * 3_600_000 / sessionDuration).toInt() else 0
    }

    private fun getAVG() = "${(SlayerTracker.totalKillTime.millis / SlayerTracker.sessionKills / 1000.0).format(1)}s"

    fun reset() {
        SlayerTracker.reset()
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
        if (SlayerTracker.sessionKills > 0) {
            val list = mutableListOf("$prefix §f§lSlayer Stats: ")

            if(slayerstatslines.contains(4)) {
                val totalTime = TimeUtils.now - SlayerTracker.sessionStart
                val timeString = totalTime.millis.toFormattedDuration(false)
                list.add("§7> §bSession time§f: §c$timeString")
            }
            if(slayerstatslines.contains(0)) list.add("§7> §bTotal bosses§f: §c${SlayerTracker.sessionKills}")
            if(slayerstatslines.contains(1)) list.add("§7> §bBosses/hr§f: §c${getBPH()}")
            if(slayerstatslines.contains(2)) list.add("§7> §bAvg. kill§f: §c${getAVG()}")
            if(slayerstatslines.contains(3)) {
                val avgSpawn = SlayerTracker.totalSpawnTime.millis / SlayerTracker.sessionKills
                list.add("§7> §bAvg. spawn§f: §c${(avgSpawn / 1000.0).format(1)}s")
            }

            return list
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