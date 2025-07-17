package meowing.zen.feats

import meowing.zen.Zen
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.CommandUtils
import net.minecraft.command.ICommandSender

object Debug {
    var debugmode = false
}

@Zen.Command
object DebugCommand : CommandUtils("zendebug") {
    override fun processCommand(sender: ICommandSender?, args: Array<out String?>?) {
        val stringArgs = args?.filterNotNull()?.toTypedArray() ?: return
        if (stringArgs.size == 1 && stringArgs[0].lowercase() == "toggle") {
            Debug.debugmode = !Debug.debugmode
            ChatUtils.addMessage("§c[Zen] §fToggled dev mode.")
        }
    }
}