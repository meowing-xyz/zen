package meowing.zen.features

import meowing.zen.Zen.Companion.LOGGER
import meowing.zen.utils.TimeUtils
import meowing.zen.utils.TimeUtils.millis
import net.minecraft.command.ICommand
import net.minecraftforge.client.ClientCommandHandler

object FeatureLoader {
    private var moduleCount = 0
    private var commandCount = 0
    private var loadtime: Long = 0

    val featureClassNames = FeatureLoader::class.java.getResourceAsStream("/features.list")?.bufferedReader()?.readLines() ?: emptyList()
    val commandClassNames = FeatureLoader::class.java.getResourceAsStream("/commands.list")?.bufferedReader()?.readLines() ?: emptyList()

    fun init() {
        val starttime = TimeUtils.now

        featureClassNames.mapNotNull { className ->
            try { Class.forName(className); moduleCount++ } catch (e: Exception) {
                LOGGER.error("Error loading module-$className: $e")
                null
            }
        }

        for (className in commandClassNames) {
            try {
                val commandClass = Class.forName(className)
                val commandInstance = commandClass.getDeclaredField("INSTANCE").get(null) as ICommand
                ClientCommandHandler.instance.registerCommand(commandInstance)
                commandCount++
            } catch (e: Exception) {
                LOGGER.error("Error initializing command-$className: $e")
                e.printStackTrace()
            }
        }

        loadtime = starttime.since.millis
    }

    fun getModuleCount(): Int = moduleCount
    fun getCommandCount(): Int = commandCount
    fun getLoadtime(): Long = loadtime
}