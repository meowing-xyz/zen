package meowing.zen.config

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.hud.HUDEditor
import meowing.zen.utils.CommandUtils
import meowing.zen.utils.TickUtils
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.BlockPos

class ConfigCommand : CommandUtils(
    "zen",
    "Opens the Config GUI",
    listOf("ma", "meowaddons")
) {
    override fun processCommand(sender: ICommandSender?, args: Array<out String?>?) {
        if (args != null && args.isNotEmpty()) {
            when (args[0]?.lowercase()) {
                "hud" -> {
                    TickUtils.schedule(1) {
                        mc.displayGuiScreen(HUDEditor())
                    }
                    return
                }
            }
        }

        TickUtils.schedule(1) {
            Zen.openConfig()
        }
    }

    override fun getTabCompletions(sender: ICommandSender, args: Array<String>, pos: BlockPos): List<String> {
        if (args.size == 1) return listOf("hud").filter { it.startsWith(args[0].lowercase()) }
        return emptyList()
    }
}