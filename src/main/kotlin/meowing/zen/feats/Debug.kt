package meowing.zen.feats

import meowing.zen.UpdateGUI
import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.Zen.Companion.prefix
import meowing.zen.api.PlayerStats
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.CommandUtils
import meowing.zen.utils.DungeonUtils
import meowing.zen.utils.TickUtils
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
                ChatUtils.addMessage("$prefix §fToggled dev mode.")
            }
            "stats" -> {
                ChatUtils.addMessage(
                    "§cHealth: ${PlayerStats.health} | Max: ${PlayerStats.maxHealth} | §6Absorb: ${PlayerStats.absorption}\n" +
                            "§9Mana: ${PlayerStats.mana} | Max: ${PlayerStats.maxMana} | §3Overflow: ${PlayerStats.overflowMana}\n" +
                            "§dRift Time: ${PlayerStats.riftTimeSeconds} | Max: ${PlayerStats.maxRiftTime}\n" +
                            "§aDefense: ${PlayerStats.defense} | Effective: ${PlayerStats.effectiveHealth} | Effective Max: ${PlayerStats.maxEffectiveHealth}"
                )
            }
            "dgutils" -> {
                ChatUtils.addMessage(
                    "Crypt Count: ${DungeonUtils.getCryptCount()}\n" +
                    "Current Class: ${DungeonUtils.getCurrentClass()} ${DungeonUtils.getCurrentLevel()}\n" +
                    "isMage: ${DungeonUtils.isMage()}"
                )
            }
            "updatechecker" -> {
                TickUtils.schedule(2) {
                    mc.displayGuiScreen(UpdateGUI())
                }
            }
            else -> {
                ChatUtils.addMessage("$prefix §fUsage: §7/§bzendebug §c<toggle|stats|dgutils|updatechecker>")
            }
        }
    }
}