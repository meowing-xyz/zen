package xyz.meowing.zen.features.slayers

import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.prefix
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.ChatEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.ChatUtils
import xyz.meowing.zen.utils.TitleUtils
import xyz.meowing.zen.utils.Utils
import xyz.meowing.zen.utils.Utils.removeFormatting

@Zen.Module
object MinibossSpawn : Feature("minibossspawn", true) {
    private val showTitle by ConfigDelegate<Boolean>("minibossspawntitle")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Slayers", "Miniboss spawn alert", ConfigElement(
                "minibossspawn",
                "Miniboss spawn alert",
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Slayers", "Miniboss spawn alert", "Options", ConfigElement(
                "minibossspawntitle",
                "Show Title",
                ElementType.Switch(true)
            ))
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val message = event.event.message.unformattedText.removeFormatting()

            if (message.contains("SLAYER MINI-BOSS") && message.contains("has spawned!")) {
                val minibossName = extractMinibossName(message)

                if (minibossName != null) {
                    Utils.playSound("mob.cat.meow", 1f, 1f)
                    ChatUtils.addMessage("$prefix §b$minibossName §fspawned.")

                    if (showTitle) {
                        TitleUtils.showTitle("§b$minibossName", "§fMiniboss Spawned!", 2000, scale = 3.0f)
                    }
                }
            }
        }
    }

    private fun extractMinibossName(message: String): String? {
        val parts = message.split(" ")
        val miniBossIndex = parts.indexOfFirst { it.equals("MINI-BOSS", ignoreCase = true) }

        if (miniBossIndex != -1 && miniBossIndex + 1 < parts.size) {
            val nameParts = mutableListOf<String>()
            for (i in miniBossIndex + 1 until parts.size) {
                if (parts[i].equals("has", ignoreCase = true)) break
                nameParts.add(parts[i])
            }
            return nameParts.joinToString(" ").trim()
        }
        return null
    }
}