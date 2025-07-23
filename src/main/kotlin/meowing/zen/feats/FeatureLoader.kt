package meowing.zen.feats

import meowing.zen.Zen.Command
import meowing.zen.Zen.Module
import meowing.zen.utils.DungeonUtils
import meowing.zen.utils.LocationUtils
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
        val starttime = System.currentTimeMillis()
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
                System.err.println("[Zen] Error initializing ${clazz.name}: $e")
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
                System.err.println("[Zen] Error initializing ${commandClass.name}: $e")
                e.printStackTrace()
            }
        }

        DungeonUtils
        LocationUtils
        loadtime = System.currentTimeMillis() - starttime
    }

    fun getModuleCount(): Int = moduleCount
    fun getCommandCount(): Int = commandCount
    fun getLoadtime(): Long = loadtime
}