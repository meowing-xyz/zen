package meowing.zen.feats

import meowing.zen.Zen.Module
import meowing.zen.config.ConfigCommand
import meowing.zen.feats.carrying.carrycommand
import meowing.zen.feats.general.calculator
import meowing.zen.feats.slayers.SlayerStatsCommand
import meowing.zen.utils.DungeonUtils
import meowing.zen.utils.LocationUtils
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
    private var commandCount = 0
    private var loadtime: Long = 0

    fun init() {
        val features = Reflections("meowing.zen").getTypesAnnotatedWith(Module::class.java)
        val starttime = System.currentTimeMillis()
        val categoryOrder = listOf("general", "slayers", "dungeons", "meowing", "noclutter")

        features.sortedWith(compareBy<Class<*>> { clazz ->
            val packageName = clazz.`package`.name
            val category = packageName.substringAfterLast(".")
            val normalizedCategory = if (category == "carrying") "slayers" else category
            categoryOrder.indexOf(normalizedCategory).takeIf { it != -1 } ?: Int.MAX_VALUE
        }.thenBy { it.name }).forEach { clazz ->
            try {
                Class.forName(clazz.name)
                moduleCount++
            } catch (e: Exception) {
                System.err.println("[Zen] Error initializing ${clazz.name}: $e")
                e.printStackTrace()
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

    fun getModuleCount(): Int = moduleCount
    fun getCommandCount(): Int = commandCount
    fun getLoadtime(): Long = loadtime
}