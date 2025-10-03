package xyz.meowing.zen.config

import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.mc
import xyz.meowing.zen.hud.HUDEditor
import xyz.meowing.zen.utils.CommandUtils
import xyz.meowing.zen.utils.TickUtils
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos

@Zen.Command
object ConfigCommand : CommandUtils(
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