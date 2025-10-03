package xyz.meowing.zen.utils

import xyz.meowing.zen.Zen.Companion.mc
import xyz.meowing.zen.events.AreaEvent
import xyz.meowing.zen.events.EventBus
import xyz.meowing.zen.events.GameEvent
import xyz.meowing.zen.events.PacketEvent
import xyz.meowing.zen.utils.StringUtils.removeEmotes
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.network.play.server.S38PacketPlayerListItem
import net.minecraft.network.play.server.S3EPacketTeams

object LocationUtils {
    private val areaRegex = "^(?:Area|Dungeon): ([\\w ]+)$".toRegex()
    private val subAreaRegex = "^ ([⏣ф]) .*".toRegex()
    private val uselessRegex = "^[⏣ф] ".toRegex()
    private val lock = Any()
    private var cachedAreas = mutableMapOf<String?, Boolean>()
    private var cachedSubareas = mutableMapOf<String?, Boolean>()
    var dungeonFloor: String? = null
        private set
    var dungeonFloorNum: Int? = null
        private set
    var area: String? = null
        private set
    var subarea: String? = null
        private set
    var inSkyblock = false
        private set

    init {
        EventBus.register<PacketEvent.Received> { event ->
            when (val packet = event.packet) {
                is S38PacketPlayerListItem -> {
                    if (packet.action != S38PacketPlayerListItem.Action.UPDATE_DISPLAY_NAME && packet.action != S38PacketPlayerListItem.Action.ADD_PLAYER) return@register
                    packet.entries?.forEach { entry ->
                        val displayName = entry.displayName?.unformattedText ?: return@forEach
                        val line = displayName.removeEmotes()
                        val match = areaRegex.find(line) ?: return@forEach
                        val newArea = match.groupValues[1]
                        if (newArea != area) {
                            synchronized(lock) {
                                EventBus.post(AreaEvent.Main(newArea))
                                area = newArea.lowercase()
                            }
                        }
                    }
                }
                is S3EPacketTeams -> {
                    val teamPrefix = packet.prefix
                    val teamSuffix = packet.suffix
                    if (teamPrefix.isEmpty() || teamSuffix.isEmpty()) return@register

                    val line = teamPrefix + teamSuffix
                    if (!subAreaRegex.matches(line.removeFormatting())) return@register
                    if (line.endsWith("cth") || line.endsWith("ch")) return@register
                    val cleanLine = line.removeFormatting()
                    val cleanSubarea = cleanLine.replace(uselessRegex, "").trim().lowercase()
                    if (cleanSubarea != subarea) {
                        synchronized(lock) {
                            EventBus.post(AreaEvent.Sub(cleanSubarea))
                            subarea = cleanSubarea
                        }
                    }
                    if (cleanLine.contains("The Catacombs (") && !line.contains("Queue")) {
                        dungeonFloor = cleanLine.substringAfter("(").substringBefore(")")
                        dungeonFloorNum =
                            if (dungeonFloor?.contains("M", true) == true) {
                                dungeonFloor?.lastOrNull()?.digitToIntOrNull()?.plus(7) ?: 0
                            } else {
                                dungeonFloor?.lastOrNull()?.digitToIntOrNull() ?: 0
                            }
                    }
                }
            }
        }

        EventBus.register<AreaEvent.Main> {
            synchronized(lock) {
                cachedAreas.clear()
            }
        }

        EventBus.register<AreaEvent.Sub> {
            synchronized(lock) {
                cachedSubareas.clear()
            }
        }

        EventBus.register<GameEvent.Disconnect> {
            reset()
        }

        TickUtils.loop(20) {
            if (mc.theWorld != null) {
                val old = inSkyblock
                inSkyblock = mc.theWorld?.scoreboard?.getObjectiveInDisplaySlot(1)?.name == "SBScoreboard"
                if (old != inSkyblock) EventBus.post(AreaEvent.Skyblock(inSkyblock))
            }
        }
    }

    private fun reset() {
        inSkyblock = false
        dungeonFloor = null
        dungeonFloorNum = null
        subarea = null
        area = null
    }

    fun checkArea(areaLower: String?): Boolean {
        return synchronized(lock) {
            cachedAreas.getOrPut(areaLower) {
                areaLower?.let { area == it } ?: true
            }
        }
    }

    fun checkSubarea(subareaLower: String?): Boolean {
        return synchronized(lock) {
            cachedSubareas.getOrPut(subareaLower) {
                subareaLower?.let { subarea?.contains(it) == true } ?: true
            }
        }
    }
}