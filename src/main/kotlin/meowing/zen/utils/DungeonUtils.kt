package meowing.zen.utils

import meowing.zen.Zen.Companion.mc
import meowing.zen.events.EventBus
import meowing.zen.events.AreaEvent
import meowing.zen.events.TablistEvent
import meowing.zen.events.WorldEvent
import meowing.zen.utils.Utils.removeFormatting
import kotlin.math.floor

object DungeonUtils {
    private val cryptsRegex = "^ Crypts: (\\d+)$".toRegex()
    private val cataRegex = "^ Catacombs (\\d+):".toRegex()
    private val playerInfoRegex = "^[^\\x00-\\x7F]?(?:\\[\\d+] )?(?:\\[\\w+] )?(\\w{1,16})(?: [^\\x00-\\x7F]+)? \\((\\w+) ?(([IVXLCDM]+))?\\)$".toRegex()
    private var crypts = 0
    private var currentClass: String? = null
    private var currentLevel = 0
    private val players = mutableMapOf<String, PlayerData>()
    private var cryptsTab: EventBus.EventCall? = null
    private var cataLevel = 0
    data class PlayerData(val name: String, val className: String, val level: Int)

    init {
        EventBus.register<AreaEvent.Main> ({ event ->
            val inCatacombs = event.area.equals("catacombs", true)

            if (inCatacombs && cryptsTab == null) {
                cryptsTab = EventBus.register<TablistEvent> ({ tabEvent ->
                    tabEvent.packet.entries.forEach { entry ->
                        val text = entry.displayName?.unformattedText?.removeFormatting() ?: return@forEach

                        cryptsRegex.find(text)?.let {
                            crypts = it.groupValues[1].toIntOrNull() ?: crypts
                        }

                        playerInfoRegex.find(text)?.let { match ->
                            val playerName = match.groupValues[1]
                            val className = match.groupValues[2]
                            val levelStr = match.groupValues[4]
                            val level = if (levelStr.isNotEmpty()) Utils.decodeRoman(levelStr) else 0

                            players[playerName] = PlayerData(playerName, className, level)

                            if (playerName == mc.thePlayer.name) {
                                currentClass = className
                                currentLevel = level
                            }
                        }
                    }
                })
            }

            if (!inCatacombs) {
                cryptsTab?.unregister()
                cryptsTab = null
                reset()
            }
        })

        EventBus.register<TablistEvent> ({ event ->
            event.packet.entries.forEach { entry ->
                val text = entry.displayName?.unformattedText?.removeFormatting() ?: return@forEach
                cataRegex.find(text)?.let { match ->
                    val cata = match.groupValues[1].toIntOrNull()
                    if (cata != null) cataLevel = cata
                }
            }
        })

        EventBus.register<WorldEvent.Unload> ({
            cryptsTab?.unregister()
            cryptsTab = null
            reset()
        })
    }

    private fun reset() {
        crypts = 0
        currentClass = null
        currentLevel = 0
        players.clear()
    }

    fun getCryptCount(): Int = crypts

    fun getCurrentClass(): String? = currentClass

    fun getCurrentLevel(): Int = currentLevel

    fun isMage(): Boolean = currentClass == "Mage"

    fun getPlayerClass(playerName: String): String? = players[playerName]?.className

    fun isDuplicate(className: String): Boolean = players.values.count { it.className.equals(className, true) } > 1

    fun getMageReduction(cooldown: Double, checkClass: Boolean = false): Double {
        if (checkClass && currentClass != "Mage") return cooldown

        val multiplier = if (isDuplicate("mage")) 1 else 2
        return cooldown * (0.75 - (floor(currentLevel / 2.0) / 100.0) * multiplier)
    }

    // TODO: Use api for cata level and calc
    fun getCurrentCata(): Int = cataLevel
}
