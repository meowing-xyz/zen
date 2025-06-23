package meowing.zen.utils

import meowing.zen.events.EventBus
import meowing.zen.events.AreaEvent
import meowing.zen.events.TablistEvent
import meowing.zen.events.WorldUnloadEvent
import meowing.zen.utils.Utils.removeFormatting

object DungeonUtils {
    private val regex = "^ Crypts: (\\d+)$".toRegex()
    private var crypts = 0
    private var crypttab: EventBus.EventCall? = null

    init {
        EventBus.register<AreaEvent> { event ->
            if (event.area.equals("catacombs", true)) {
                if (crypttab == null) {
                    crypttab = EventBus.register<TablistEvent> { event ->
                        crypts = event.packet.entries
                            .asSequence()
                            .mapNotNull { it.displayName?.unformattedText?.removeFormatting() }
                            .mapNotNull { regex.find(it)?.groupValues?.getOrNull(1)?.toIntOrNull() }
                            .firstOrNull() ?: crypts
                    }
                }
            } else {
                crypttab?.unregister()
                crypttab = null
                crypts = 0
            }
        }

        EventBus.register<WorldUnloadEvent> {
            crypttab?.unregister()
            crypttab = null
            crypts = 0
        }
    }

    fun getCryptCount(): Int = crypts
}
