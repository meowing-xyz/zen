package xyz.meowing.zen.utils

import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos

abstract class CommandUtils(
    private val name: String,
    private val usage: String = "/$name",
    private val aliases: List<String> = emptyList(),
    private val permLevel: Int = 0
) : CommandBase() {
    override fun getCommandName() = name
    override fun getCommandUsage(sender: ICommandSender) = usage
    override fun getCommandAliases() = aliases
    override fun getRequiredPermissionLevel() = permLevel

    override fun addTabCompletionOptions(sender: ICommandSender, args: Array<String>, pos: BlockPos) =
        getTabCompletions(sender, args, pos)

    open fun getTabCompletions(sender: ICommandSender, args: Array<String>, pos: BlockPos): List<String> = emptyList()
}