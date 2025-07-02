package meowing.zen.feats

import meowing.zen.config.GUICommand
import meowing.zen.feats.carrying.carrycommand
import meowing.zen.feats.general.calculator
import meowing.zen.feats.slayers.slayerstatsreset
import meowing.zen.hud.HUDCommand
import meowing.zen.utils.LocationUtils
import meowing.zen.utils.DungeonUtils
import net.minecraftforge.client.ClientCommandHandler

object FeatureLoader {
    private val features = arrayOf(
        "meowing.automeow",
        "meowing.meowdeathsounds",
        "meowing.meowsounds",
        "general.guildmessage",
        "general.partymessage",
        "general.guildjoinleave",
        "general.friendjoinleave",
        "general.betterah",
        "general.betterbz",
        "general.customsize",
        "general.worldage",
        "general.nohurtcam",
        "general.blockoverlay",
        "general.entityhighlight",
        "slayers.MetadataHandler",
        "slayers.slayertimer",
        "slayers.slayerhighlight",
        "slayers.vengdmg",
        "slayers.vengtimer",
        "slayers.slayerstats",
        "carrying.carrycounter",
        "dungeons.bloodtimer",
        "dungeons.termtracker",
        "dungeons.keyalert",
        "dungeons.keyhighlight",
        "dungeons.partyfinder",
        "dungeons.serverlagtimer",
        "dungeons.firefreeze",
        "dungeons.cryptreminder",
        "dungeons.architectdraft",
        "noclutter.hidedamage",
        "noclutter.hidedeathani",
        "noclutter.hidefallingblocks",
        "noclutter.hidenonstarmobs",
        "noclutter.noendermantp"
    )

    private val commands = arrayOf(
        GUICommand(),
        carrycommand(),
        calculator(),
        slayerstatsreset(),
        HUDCommand()
    )

    private var moduleCount = 0
    private var moduleErr = 0
    private var commandCount = 0
    private var loadtime: Long = 0

    fun init() {
        val starttime = System.currentTimeMillis()
        features.forEach { className ->
            try {
                val fullClassName = "meowing.zen.feats.$className"
                Class.forName(fullClassName)
                moduleCount++
            } catch (e: Exception) {
                System.err.println("[Zen] Error initializing $className: $e")
                e.printStackTrace()
                moduleErr++
            }
        }

        commands.forEach { command ->
            ClientCommandHandler.instance.registerCommand(command)
            commandCount++
        }

        DungeonUtils
        LocationUtils
        loadtime = System.currentTimeMillis() - starttime
    }

    fun getModuleCount(): Int = moduleCount + 2 // Mixin feats
    fun getModuleErr(): Int = moduleErr
    fun getCommandCount(): Int = commandCount
    fun getLoadtime(): Long = loadtime
}