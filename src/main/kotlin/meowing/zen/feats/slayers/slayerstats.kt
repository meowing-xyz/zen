package meowing.zen.feats.slayers

import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import cc.polyfrost.oneconfig.hud.TextHud
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer

object slayerstats : Feature("slayerstats") {
    var kills = 0
    private var sessionStart = System.currentTimeMillis()
    private var totalKillTime = 0L

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

class slayerstatshud : TextHud(true, 10, 150) {
    override fun getLines(lines: MutableList<String>, example: Boolean) {
        if (example) {
            lines.addAll(listOf(
                "§c[Zen] §f§lSlayer Stats: ",
                "§7> §bTotal bosses§f: §c15",
                "§7> §bBosses/hr§f: §c12",
                "§7> §bAvg. kill§f: §c45.2s"
            ))
            return
        }

        if (slayerstats.kills > 0) {
            lines.addAll(listOf(
                "§c[Zen] §f§lSlayer Stats: ",
                "§7> §bTotal bosses§f: §c${slayerstats.kills}",
                "§7> §bBosses/hr§f: §c${slayerstats.getBPH()}",
                "§7> §bAvg. kill§f: §c${slayerstats.getAVG()}"
            ))
        }
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