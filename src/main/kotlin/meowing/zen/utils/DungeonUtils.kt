package meowing.zen.utils

import meowing.zen.Zen.Companion.mc
import meowing.zen.events.EventBus
import meowing.zen.events.AreaEvent
import meowing.zen.events.TablistEvent
import meowing.zen.events.WorldEvent
import meowing.zen.utils.Utils.removeFormatting
import kotlin.math.floor

object DungeonUtils {
    private val regex = "^ Crypts: (\\d+)$".toRegex()
    private var crypts = 0
    private var crypttab: EventBus.EventCall? = null

    init {
        EventBus.register<AreaEvent.Main> ({ event ->
            if (event.area.equals("catacombs", true)) {
                if (crypttab == null) {
                    crypttab = EventBus.register<TablistEvent> ({ event ->
                        crypts = event.packet.entries
                            .asSequence()
                            .mapNotNull { it.displayName?.unformattedText?.removeFormatting() }
                            .mapNotNull { regex.find(it)?.groupValues?.getOrNull(1)?.toIntOrNull() }
                            .firstOrNull() ?: crypts
                    })
                }
            } else {
                crypttab?.unregister()
                crypttab = null
                crypts = 0
            }
        })

        EventBus.register<WorldEvent.Unload> ({
            crypttab?.unregister()
            crypttab = null
            crypts = 0
        })
    }

    fun getCryptCount(): Int = crypts

    fun getCooldownReduction(): Int {
        ScoreboardUtils.getSidebarLines(true).forEach { sidebarLine ->
            if (sidebarLine.contains(mc.thePlayer.name)) {
                return runCatching {
                    floor(sidebarLine.split(" ")[2].replace("[^0-9]".toRegex(), "").toInt() / 2.0).toInt()
                }.getOrDefault(0)
            }
        }
        return 0
    }

    fun isMage(): Boolean {
        return ScoreboardUtils.getTabListEntries().any {
            it.removeFormatting().let {
                clean -> clean.contains(mc.thePlayer.name) && clean.contains("Mage")
            }
        }
    }

    fun isUniqueDungeonClass(): Boolean {
        return ScoreboardUtils.getTabListEntries().count { entry ->
            entry.removeFormatting().split(" ").let { args ->
                args.size >= 2 && args[args.size - 2] == "(Mage"
            }
        } == 1
    }
}
