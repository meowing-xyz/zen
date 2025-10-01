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

    val featureClassNames = try {
        FeatureLoader::class.java.classLoader.getResourceAsStream("features.list")?.use { stream ->
            stream.bufferedReader().use { reader ->
                reader.readLines().filter { it.isNotBlank() }
            }
        } ?: emptyList()
    } catch (e: Exception) {
        LOGGER.error("Failed to load features.list: ${e.message}")
        emptyList()
    }

    val commandClassNames = try {
        FeatureLoader::class.java.classLoader.getResourceAsStream("commands.list")?.use { stream ->
            stream.bufferedReader().use { reader ->
                reader.readLines().filter { it.isNotBlank() }
            }
        } ?: emptyList()
    } catch (e: Exception) {
        LOGGER.error("Failed to load commands.list: ${e.message}")
        emptyList()
    }

    fun init() {
        val starttime = TimeUtils.now

        if (featureClassNames.isEmpty() && commandClassNames.isEmpty()) {
            LOGGER.warn("No features or commands found in resource lists!")
        }

        featureClassNames.forEach { className ->
            try {
                Class.forName(className)
                moduleCount++
                LOGGER.debug("Loaded module: $className")
            } catch (e: ClassNotFoundException) {
                LOGGER.error("Module class not found: $className")
            } catch (e: Exception) {
                LOGGER.error("Error loading module $className: ${e.message}")
                e.printStackTrace()
            }
        }

        commandClassNames.forEach { className ->
            try {
                val commandClass = Class.forName(className)
                val commandInstance = commandClass.getDeclaredField("INSTANCE").get(null) as ICommand
                ClientCommandHandler.instance.registerCommand(commandInstance)
                commandCount++
                LOGGER.debug("Loaded command: $className")
            } catch (e: ClassNotFoundException) {
                LOGGER.error("Command class not found: $className")
            } catch (e: Exception) {
                LOGGER.error("Error initializing command $className: ${e.message}")
                e.printStackTrace()
            }
        }

        loadtime = starttime.since.millis
        LOGGER.info("Loaded $moduleCount modules and $commandCount commands in ${loadtime}ms")
    }

    fun getFeatCount(): Int = moduleCount
    fun getCommandCount(): Int = commandCount
    fun getLoadtime(): Long = loadtime
}