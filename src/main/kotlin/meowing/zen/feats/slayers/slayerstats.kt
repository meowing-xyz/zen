package meowing.zen.feats.slayers

import meowing.zen.Zen.Companion.mc
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import meowing.zen.hud.HUDEditor
import meowing.zen.hud.HUDElement
import meowing.zen.hud.HUDManager
import meowing.zen.utils.ChatUtils
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.client.event.RenderGameOverlayEvent

object slayerstats : Feature("slayerstats") {
    var kills = 0
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
            if (event.elementType == RenderGameOverlayEvent.ElementType.TEXT && HUDEditor.isEnabled("SlayerStats")) SlayerStatsHUD.render()
        }
    }

    fun addKill(killtime: Long) {
        kills++
        totalKillTime += killtime
    }

    fun getBPH() = (kills * 3600000 / (System.currentTimeMillis() - sessionStart)).toInt()
    fun getAVG() = "${(totalKillTime / kills / 1000.0).format(1)}s"

    fun reset() {
        kills = 0
        sessionStart = System.currentTimeMillis()
        totalKillTime = 0L
        ChatUtils.addMessage("§c[Zen] §fSlayer stats reset!")
    }

    private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)
}

object SlayerStatsHUD {
    private const val name = "SlayerStats"

    fun render() {
        val x = HUDEditor.getX(name)
        val y = HUDEditor.getY(name)
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
        if (slayerstats.kills > 0) {
            return listOf(
                "§c[Zen] §f§lSlayer Stats: ",
                "§7> §bTotal bosses§f: §c${slayerstats.kills}",
                "§7> §bBosses/hr§f: §c${slayerstats.getBPH()}",
                "§7> §bAvg. kill§f: §c${slayerstats.getAVG()}"
            )
        }
        return emptyList()
    }
}

class slayerstatsreset : CommandBase() {
    override fun getCommandName(): String? {
        return "slayerstats"
    }

    override fun getCommandUsage(sender: ICommandSender?): String? {
        return "/slayerstats reset - Resets slayer statistics"
    }

    override fun getRequiredPermissionLevel(): Int {
        return 0
    }

    override fun getCommandAliases(): List<String> {
        return listOf("zenslayers")
    }

    override fun processCommand(sender: ICommandSender?, args: Array<out String?>?) {
        if (sender is EntityPlayer && args?.size == 1 && args[0] == "reset") slayerstats.reset()
        else if (args?.size!! > 1 || args[0] !== "reset") ChatUtils.addMessage("§c[Zen] §fPlease use §c/slayerstats reset")
    }
}