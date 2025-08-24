package meowing.zen.api

import meowing.zen.Zen
import meowing.zen.events.ChatEvent
import meowing.zen.events.EventBus
import meowing.zen.utils.DataUtils
import meowing.zen.utils.Utils.removeFormatting

@Zen.Module
object PetTracker {
    private val AUTO_PET = "Autopet equipped your \\[Lvl (?<level>\\d+)] (?<name>.*?)! VIEW RULE".toRegex()
    private val PET_LEVEL = "Your (?<name>.*?) leveled up to level (?<newLevel>\\d+)!".toRegex()
    private val PET_ITEM = "Your pet is now holding (?<petItem>.*).".toRegex()
    private val PET_SUMMON = "You summoned your (?<name>.*?)!".toRegex()

    data class PetData(
        var level: Int = 0,
        var name: String = "",
        var item: String = ""
    )

    private val Save = DataUtils("PetTracker", PetData())
    val Data = Save()

    init {
        EventBus.register<ChatEvent.Receive> { event ->
            val message = event.event.message.unformattedText.removeFormatting()

            AUTO_PET.find(message)?.let { match ->
                Save.update {
                    level = match.groups["level"]?.value?.toIntOrNull() ?: 0
                    name = match.groups["name"]?.value ?: ""
                    item = ""
                }
            }

            PET_LEVEL.find(message)?.let { match ->
                Save.update {
                    level = match.groups["newLevel"]?.value?.toIntOrNull() ?: level
                    name = match.groups["name"]?.value ?: name
                }
            }

            PET_SUMMON.find(message)?.let { match ->
                Save.update {
                    name = match.groups["name"]?.value ?: ""
                }
            }

            PET_ITEM.find(message)?.let { match ->
                Save.update {
                    item = match.groups["petItem"]?.value ?: ""
                }
            }

            if (message.startsWith("You despawned your")) {
                Save.update {
                    level = 0
                    name = ""
                    item = ""
                }
            }
        }
    }

    inline val level get() = Data.level
    inline val name get() = Data.name
    inline val item get() = Data.item
}