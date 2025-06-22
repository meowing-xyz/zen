package meowing.zen.utils

import meowing.zen.events.*
import meowing.zen.utils.Utils.removeEmotes
import net.minecraft.network.play.server.S38PacketPlayerListItem
import net.minecraft.network.play.server.S3EPacketTeams
import net.minecraftforge.common.MinecraftForge

// Taken from devonian 
// https://github.com/Synnerz/devonian/blob/main/src/main/kotlin/com/github/synnerz/devonian/utils/Location.kt
object Location {
    private val areaRegex = "^(?:Area|Dungeon): ([\\w ]+)$".toRegex()
    private val subAreaRegex = "^ ([⏣ф]) .*".toRegex()

    var area: String? = null
    var subarea: String? = null

    fun initialize() {
        MinecraftForge.EVENT_BUS.register(this)
        EventBus.register<PacketEvent.Received>({ event ->
            when (val packet = event.packet) {
                is S38PacketPlayerListItem -> {
                    if (packet.action == S38PacketPlayerListItem.Action.UPDATE_DISPLAY_NAME || packet.action == S38PacketPlayerListItem.Action.ADD_PLAYER) {
                        packet.entries?.forEach { entry ->
                            val displayName = entry.displayName?.unformattedText ?: return@forEach
                            val line = displayName.removeEmotes()
                            if (areaRegex.matches(line)) {
                                val newArea = areaRegex.find(line)?.groupValues?.get(1) ?: return@forEach
                                if (newArea != area) {
                                    EventBus.post(AreaEvent(newArea))
                                    area = newArea.lowercase()
                                }
                            }
                        }
                    }
                }
                is S3EPacketTeams -> {
                    val teamPrefix = packet.prefix
                    val teamSuffix = packet.suffix
                    if (teamPrefix.isEmpty() || teamSuffix.isEmpty()) return@register
                    val line = "$teamPrefix$teamSuffix"
                    if (subAreaRegex.matches(line) && line != subarea) {
                        EventBus.post(SubAreaEvent(line))
                        subarea = line.lowercase()
                    }
                }
            }
        })
    }
}
