package meowing.zen.hud

import meowing.zen.Zen.Companion.mc
import meowing.zen.utils.TickUtils
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer

class HUDCommand : CommandBase() {
    override fun getCommandName() = "zenhud"
    override fun getCommandUsage(sender: ICommandSender?) = "/zenhud - Opens the Zen HUD Editor"
    override fun getRequiredPermissionLevel() = 0

    override fun processCommand(sender: ICommandSender?, args: Array<out String?>?) {
        if (sender is EntityPlayer) {
            TickUtils.schedule(1) {
                mc.displayGuiScreen(HUDEditor())
            }
        }
    }
}