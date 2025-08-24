package meowing.zen.features

import meowing.zen.Zen.Command
import meowing.zen.Zen.Companion.LOGGER
import meowing.zen.Zen.Module
import meowing.zen.utils.TimeUtils
import meowing.zen.utils.TimeUtils.millis
import net.minecraft.command.ICommand
import net.minecraftforge.client.ClientCommandHandler
import org.reflections.Reflections

object FeatureLoader {
    private var moduleCount = 0
    private var commandCount = 0
    private var loadtime: Long = 0

    fun init() {
        val reflections = Reflections("meowing.zen")

        val features = reflections.getTypesAnnotatedWith(Module::class.java)
        val starttime = TimeUtils.now
        val categoryOrder = listOf("general", "slayers", "dungeons", "meowing", "rift", "noclutter")

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
                LOGGER.error("Error initializing module-${clazz.name}: $e")
                e.printStackTrace()
            }
        }

        val commands = reflections.getTypesAnnotatedWith(Command::class.java)
        commands.forEach { commandClass ->
            try {
                val commandInstance = commandClass.getDeclaredField("INSTANCE").get(null) as ICommand
                ClientCommandHandler.instance.registerCommand(commandInstance)
                commandCount++
            } catch (e: Exception) {
                LOGGER.error("Error initializing command-${commandClass.name}: $e")
                e.printStackTrace()
            }
        }

        loadtime = starttime.since.millis
    }

    fun getModuleCount(): Int = moduleCount
    fun getCommandCount(): Int = commandCount
    fun getLoadtime(): Long = loadtime
}