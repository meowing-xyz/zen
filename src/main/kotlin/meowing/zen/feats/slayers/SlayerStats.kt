package meowing.zen.feats.slayers

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import meowing.zen.hud.HUDManager
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.CommandUtils
import net.minecraft.command.ICommandSender
import net.minecraftforge.client.event.RenderGameOverlayEvent

@Zen.Module
object slayerstats : Feature("slayerstats") {
    private const val name = "SlayerStats"
    private var kills = 0
    private var sessionStart = System.currentTimeMillis()
    private var totalKillTime = 0L

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Slayers", "General", ConfigElement(
                "slayerstats",
                "Slayer stats",
                "Shows stats about your kill times",
                ElementType.Switch(false)
            ))
    }
    
    override fun initialize() {
        HUDManager.register("SlayerStats", "§c[Zen] §f§lSlayer Stats: \n§7> §bTotal bosses§f: §c15\n§7> §bBosses/hr§f: §c12\n§7> §bAvg. kill§f: §c45.2s")

        register<RenderEvent.HUD> { event ->
            if (event.elementType == RenderGameOverlayEvent.ElementType.TEXT && HUDManager.isEnabled("SlayerStats")) render()
        }
    }

    fun addKill(killtime: Long) {
        kills++
        totalKillTime += killtime
    }

    private fun getBPH() = (kills * 3600000 / (System.currentTimeMillis() - sessionStart)).toInt()
    private fun getAVG() = "${(totalKillTime / kills / 1000.0).format(1)}s"

    fun reset() {
        kills = 0
        sessionStart = System.currentTimeMillis()
        totalKillTime = 0L
        ChatUtils.addMessage("§c[Zen] §fSlayer stats reset!")
    }

    private fun render() {
        val x = HUDManager.getX(name)
        val y = HUDManager.getY(name)
        val lines = getLines()

        if (lines.isNotEmpty()) {
            var currentY = y
            for (line in lines) {
                mc.fontRendererObj.drawStringWithShadow(line, x, currentY, 0xFFFFFF)
                currentY += mc.fontRendererObj.FONT_HEIGHT + 2
            }
        }
    }

    private fun getLines(): List<String> {
        if (kills > 0) {
            return listOf(
                "§c[Zen] §f§lSlayer Stats: ",
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

        if (stringArgs.size == 1 && stringArgs[0] == "reset") slayerstats.reset()
        else ChatUtils.addMessage("§c[Zen] §fCommand: §c/slayerstats reset")
    }
}