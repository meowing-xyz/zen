package meowing.zen.features.general

import meowing.zen.Zen
import meowing.zen.api.ItemAPI
import meowing.zen.api.PartyTracker
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.PacketEvent
import meowing.zen.events.RenderEvent
import meowing.zen.features.Feature
import meowing.zen.hud.HUDManager
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.ItemUtils
import meowing.zen.utils.ItemUtils.skyblockID
import meowing.zen.utils.LoopUtils.setTimeout
import meowing.zen.utils.Render2D
import meowing.zen.utils.Utils.formatNumber
import meowing.zen.utils.Utils.getRegexGroups
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraft.network.play.server.S30PacketWindowItems
import java.awt.Color
import kotlin.math.abs

@Zen.Module
object ItemPickupLog : Feature("itempickuplog") {
    private const val name = "Item Pickup Log"
    private var ignoreStacksRegex = listOf("""^§8Quiver.*""".toRegex(), """^§aSkyBlock Menu §7\(Click\)""".toRegex(), """^§bMagical Map""".toRegex())

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Item Pickup Log", ConfigElement(
                "itempickuplog",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    private var previousInventory = mutableMapOf<String, Int>()
    private var currentInventory = mutableMapOf<String, Int>()

    private var items = mutableMapOf<String, Pair<String, ItemStack>>()

    var displayLines = mutableMapOf<String, PickupEntry>()


    override fun initialize() {
        HUDManager.register(name, "§a+5 §fPotato §6\$16\n§c-4 §fHay Bale §6\$54")

        register<PacketEvent.Received> { event ->
            if(event.packet is S2FPacketSetSlot) {
                setTimeout(100) {
                    currentInventory = getCurrentInventoryState().toMutableMap()

                    ChatUtils.addMessage("Comparing inventories")
                    compareInventories(previousInventory, currentInventory)

                    ChatUtils.addMessage("Setting previous inventory to current")
                    previousInventory = currentInventory
                }
            }
        }

        register<RenderEvent.Text> { event ->
            if (HUDManager.isEnabled(name)) render()
        }
    }

    private fun render() {
        val x = HUDManager.getX(name)
        val y = HUDManager.getY(name)
        val scale = HUDManager.getScale(name)

        // Remove expired entries before drawing
        displayLines = displayLines.filterValues { !it.isExpired() } as MutableMap<String, PickupEntry>
        val sorted = displayLines.toList().sortedByDescending { (_, value) -> value.lastUpdated }

        var drawnEntries = 0
        for ((entryName, entry) in sorted) {
            if (entry.count == 0) continue

            val alpha = entry.getAlpha()
            if (alpha <= 0) continue

            val baseY = (10f * scale) * drawnEntries
            val yWithOffset = entry.getVerticalOffset(baseY)

            val colorSymbol = if (entry.count < 0) "§c-" else "§3+"
            val count = abs(entry.count).formatNumber()
            var display = "$colorSymbol$count §e$entryName"

            val priceInfo = ItemAPI.getItemInfo(entry.itemId)
            val price = (priceInfo?.get("bazaarSell")?.asDouble
                ?: priceInfo?.get("lowestBin")?.asDouble
                ?: 0.0 ) * entry.count

            display += " §6$${abs(price).formatNumber()}"

            val color = Color(255, 255, 255, alpha)
            Render2D.renderStringWithShadow(display, x, y+yWithOffset, scale, color.rgb)

            drawnEntries++
        }
    }

    private fun getCurrentInventoryState(): Map<String, Int> {
        val inventoryState = mutableMapOf<String, Int>()
        val mainInventory = Zen.mc.thePlayer.inventory.mainInventory

        loop@ for (element in mainInventory) {
            if (element == null) continue
            var displayName = element.displayName ?: "Empty slot"
            // Filters
            for (regex in ignoreStacksRegex) {
                if (displayName.matches(regex)) {
                    continue@loop
                }
            }

            // npc shop fix
            val npcSellingStackRegex = """(.*) §8x\d+""".toRegex()
            if (displayName.matches(npcSellingStackRegex)) {
                displayName = displayName.getRegexGroups(npcSellingStackRegex)!![1]!!.value
            }
            items[displayName] = Pair(element.skyblockID, element)

            val itemCount = element.stackSize

            // Merge counts for items with the same display name
            inventoryState.merge(displayName, itemCount, Int::plus)
        }

        return inventoryState
    }

    private fun compareInventories(previous: Map<String, Int>, current: Map<String, Int>) {
        if (previous.isEmpty() || current.isEmpty()) return

        // Compare current inventory with previous inventory
        current.forEach { (displayName, currentCount) ->
            val previousCount = previous[displayName] ?: 0
            val countDifference = currentCount - previousCount

            // Check if there is a change in count
            if (countDifference != 0) {
                val itemName = if (displayName != "Empty slot") displayName else getPreviousItemName()
                val itemEntry = items[itemName] ?: return
                val (itemID, itemStack) = itemEntry
                val entry = displayLines.getOrPut(itemName) { PickupEntry() }
                ChatUtils.addMessage("$itemID $countDifference")
                entry.count += countDifference
                entry.lastUpdated = System.currentTimeMillis()
                entry.itemId = itemID
                displayLines[itemName] = entry
            }
        }

        // Check for items that were removed entirely
        previous.forEach { (displayName, previousCount) ->
            val currentCount = current[displayName] ?: 0
            if (previousCount > 0 && currentCount == 0) {
                val itemName = if (displayName != "Empty slot") displayName else getPreviousItemName()
                val itemEntry = items[itemName] ?: return
                val (itemID, itemStack) = itemEntry
                val entry = displayLines.getOrPut(itemName) { PickupEntry() }
                ChatUtils.addMessage("$itemID STACK DROP")
                entry.count -= 1
                entry.lastUpdated = System.currentTimeMillis()
                entry.itemId = itemID
                displayLines[itemName] = entry
            }
        }
    }

    private fun getPreviousItemName(): String {
        // Look up the item name from the previous inventory
        val previousItem = previousInventory.entries.find { it.key != "Empty slot" && it.value == 0 }
        return previousItem?.key ?: "Unknown item"
    }

    class PickupEntry {
        var count: Int = 0
        var lastUpdated: Long = 0
        var itemId: String = ""

        private val fadeInDuration = 300L
        private val fadeOutDuration = 300L
        private val totalDuration = 5_000L

        fun timeSinceUpdate(): Long = System.currentTimeMillis() - lastUpdated

        fun isExpired(): Boolean = timeSinceUpdate() >= totalDuration

        fun getAlpha(): Int {
            val elapsed = timeSinceUpdate()

            return when {
                elapsed < fadeInDuration -> {
                    val t = elapsed.toFloat() / fadeInDuration
                    (t * 255).toInt().coerceIn(0, 255)
                }
                elapsed > totalDuration - fadeOutDuration -> {
                    val t = (totalDuration - elapsed).toFloat() / fadeOutDuration
                    (t * 255).toInt().coerceIn(0, 255)
                }
                else -> 255
            }
        }

        fun getVerticalOffset(baseY: Float): Float {
            val elapsed = timeSinceUpdate()

            return when {
                elapsed < fadeInDuration -> {
                    val t = 1f - (elapsed.toFloat() / fadeInDuration)
                    baseY + (10f * t) // Slide up into place
                }
                elapsed > totalDuration - fadeOutDuration -> {
                    val t = (elapsed - (totalDuration - fadeOutDuration)).toFloat() / fadeOutDuration
                    baseY + (10f * t) // Slide down while fading out
                }
                else -> baseY
            }
        }
    }

}