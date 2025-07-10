package meowing.zen.feats

import meowing.zen.config.ConfigCommand
import meowing.zen.feats.carrying.carrycommand
import meowing.zen.feats.general.calculator
import meowing.zen.feats.slayers.SlayerStatsCommand
import meowing.zen.utils.LocationUtils
import meowing.zen.utils.DungeonUtils
import net.minecraftforge.client.ClientCommandHandler
import org.reflections.Reflections

object FeatureLoader {
    private val commands = arrayOf(
        ConfigCommand(),
        carrycommand(),
        calculator(),
        SlayerStatsCommand()
    )

    private var moduleCount = 0
    private var moduleErr = 0
    private var commandCount = 0
    private var loadtime: Long = 0

    fun init() {
        val starttime = System.currentTimeMillis()

        try {
            val reflections = Reflections("meowing.zen.feats")
            val featureClasses = reflections.getSubTypesOf(Feature::class.java)

            featureClasses.forEach { clazz ->
                try {
                    val constructor = clazz.getDeclaredConstructor()
                    constructor.isAccessible = true
                    constructor.newInstance()
                    moduleCount++
                } catch (e: Exception) {
                    System.err.println("[Zen] Error initializing ${clazz.simpleName}: $e")
                    e.printStackTrace()
                    moduleErr++
                }
            }
        } catch (e: Exception) {
            System.err.println("[Zen] Error during reflection scan: $e")
            e.printStackTrace()
            moduleErr++
        }

        commands.forEach { command ->
            ClientCommandHandler.instance.registerCommand(command)
            commandCount++
        }

        DungeonUtils
        LocationUtils
        loadtime = System.currentTimeMillis() - starttime
    }

    fun getModuleCount(): Int = moduleCount
    fun getModuleErr(): Int = moduleErr
    fun getCommandCount(): Int = commandCount
    fun getLoadtime(): Long = loadtime
}