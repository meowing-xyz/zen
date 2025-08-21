package meowing.zen.api

import meowing.zen.Zen
import meowing.zen.events.ChatEvent
import meowing.zen.events.EventBus
import meowing.zen.utils.Data
import meowing.zen.utils.Utils.removeFormatting

@Zen.Module
object PetTracker {
    private val AUTO_PET = "§cAutopet §eequipped your §7\\[Lvl (?<level>\\d+)](?: §8\\[§6\\d+§8§.✦§8])? §(?<rarityColor>.)(?<name>.*)§e! §a§lVIEW RULE§r".toRegex()
    private val PET_LEVEL = "§aYour §r§(?<rarityColor>.)(?<name>.*?)(?<cosmetic>§r§. ✦)? §r§aleveled up to level §r(?:§.)*(?<newLevel>\\d+)§r§a!§r".toRegex()
    private val PET_ITEM = "§aYour pet is now holding §r§(?<rarityColor>.)(?<petItem>.*)§r§a.§r".toRegex()

    class PetData : Data("PetTracker") {
        var level: Int = 0
        var name: String = ""
        var item: String = ""
    }

    val Save = PetData()

    init {
        EventBus.register<ChatEvent.Receive> { event ->
            val message = event.event.message.formattedText

            AUTO_PET.find(message)?.let { match ->
                Save.level = match.groups["level"]?.value?.toIntOrNull() ?: 0
                Save.name = match.groups["name"]?.value ?: ""
                Save.item = ""
            }

            PET_LEVEL.find(message)?.let { match ->
                Save.level = match.groups["newLevel"]?.value?.toIntOrNull() ?: Save.level
                Save.name = match.groups["name"]?.value ?: Save.name
            }

            PET_ITEM.find(message)?.let { match ->
                Save.item = match.groups["petItem"]?.value ?: ""
            }

            if (message.removeFormatting().startsWith("You despawned your")) {
                Save.level = 0
                Save.name = ""
                Save.item = ""
            }
        }
    }

    inline val level get() = Save.level
    inline val name get() = Save.name
    inline val item get() = Save.item
}