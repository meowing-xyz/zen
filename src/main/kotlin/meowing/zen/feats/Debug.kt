package meowing.zen.feats

import meowing.zen.Zen
import meowing.zen.api.PlayerStats
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.CommandUtils
import net.minecraft.command.ICommandSender

object Debug {
    var debugmode = false
}

@Zen.Command
object DebugCommand : CommandUtils("zendebug", aliases = listOf("zd")) {
    override fun processCommand(sender: ICommandSender?, args: Array<out String?>?) {
        val stringArgs = args?.filterNotNull()?.toTypedArray() ?: return
        when (stringArgs.getOrNull(0)?.lowercase()) {
            "toggle" -> {
                Debug.debugmode = !Debug.debugmode
                ChatUtils.addMessage("§c[Zen] §fToggled dev mode.")
            }
            "stats" -> {
                ChatUtils.addMessage(
                    "§cHealth: ${PlayerStats.health} | Max: ${PlayerStats.maxHealth} | §6Absorb: ${PlayerStats.absorption}\n" +
                            "§9Mana: ${PlayerStats.mana} | Max: ${PlayerStats.maxMana} | §3Overflow: ${PlayerStats.overflowMana}\n" +
                            "§dRift Time: ${PlayerStats.riftTimeSeconds} | Max: ${PlayerStats.maxRiftTime}\n" +
                            "§aDefense: ${PlayerStats.defense} | Effective: ${PlayerStats.effectiveHealth} | Effective Max: ${PlayerStats.maxEffectiveHealth}"
                )
            }
            null -> {
                ChatUtils.addMessage("§c[Zen] §fUsage: §7/§bzendebug §c<toggle|stats>")
            }
        }
    }
}