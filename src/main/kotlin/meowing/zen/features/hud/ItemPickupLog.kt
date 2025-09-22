package meowing.zen.features.hud

import meowing.zen.Zen
import meowing.zen.api.ItemAPI
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.PacketEvent
import meowing.zen.events.RenderEvent
import meowing.zen.features.Feature
import meowing.zen.hud.HUDManager
import meowing.zen.utils.ItemUtils.skyblockID
import meowing.zen.utils.NumberUtils.abbreviateNumber
import meowing.zen.utils.NumberUtils.formatNumber
import meowing.zen.utils.Render2D
import meowing.zen.utils.StringUtils.getRegexGroups
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S2FPacketSetSlot
import java.awt.Color
import kotlin.math.abs

@Zen.Module
object ItemPickupLog : Feature("itempickuplog") {
    private const val name = "Item Pickup Log"
    private var ignoreStacksRegex = listOf("""^§8Quiver.*""".toRegex(), """^§aSkyBlock Menu §7\(Click\)""".toRegex(), """^§bMagical Map""".toRegex())
    private val abbreviateNumbers by ConfigDelegate<Boolean>("itempickuplogabbreviate")
    private val npcSellingStackRegex = """(.*) §8x\d+""".toRegex()
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("HUD", "Item Pickup Log", ConfigElement(
                "itempickuplog",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("HUD", "Item Pickup Log", "Options", ConfigElement(
                "itempickuplogabbreviate",
                "Abbreviate Numbers",
                ElementType.Switch(false)
            ))
    }

    private var previousInventory = mutableMapOf<String, Int>()
    private var currentInventory = mutableMapOf<String, Int>()
    private var items = mutableMapOf<String, Pair<String, ItemStack>>()
    var displayLines = mutableMapOf<String, PickupEntry>()

    override fun initialize() {
        HUDManager.register(name, "§a+5 §fPotato §6$16\n§c-4 §fHay Bale §6$54")

        register<PacketEvent.ReceivedPost> { event ->
            if (event.packet is S2FPacketSetSlot) {
                currentInventory = getCurrentInventoryState()?.toMutableMap() ?: return@register
                compareInventories(previousInventory, currentInventory)
                previousInventory = currentInventory
            }
        }

        register<RenderEvent.Text> { _ ->
            if (HUDManager.isEnabled(name)) render()
        }
    }

    private fun render() {
        val x = HUDManager.getX(name)
        val y = HUDManager.getY(name)
        val scale = HUDManager.getScale(name)

        val currentTime = System.currentTimeMillis()
        val visibleEntries = displayLines.values.filter { !it.isExpired() && it.count != 0 }
        val sortedEntries = visibleEntries.sortedByDescending { it.lastUpdated }

        sortedEntries.forEachIndexed { index, entry ->
            val targetY = (10f * scale) * index
            entry.updatePosition(targetY, currentTime)

            val alpha = entry.getAlpha()
            if (alpha <= 0) return@forEachIndexed

            val colorSymbol = if (entry.count < 0) "§c-" else "§3+"
            val count = if (abbreviateNumbers) abs(entry.count).abbreviateNumber() else abs(entry.count).formatNumber()
            var display = "$colorSymbol$count §e${entry.itemName}"

            val priceInfo = ItemAPI.getItemInfo(entry.itemId)
            val price = (priceInfo?.get("bazaarSell")?.asDouble ?: priceInfo?.get("lowestBin")?.asDouble ?: 0.0) * entry.count

            if (price != 0.0) {
                val formattedPrice = if (abbreviateNumbers) abs(price).abbreviateNumber() else abs(price).formatNumber()
                display += " §6$$formattedPrice"
            }

            val color = Color(255, 255, 255, alpha)
            Render2D.renderString(display, x, y + entry.animatedY, scale, color.rgb)
        }

        displayLines = displayLines.filterValues { !it.isExpired() } as MutableMap<String, PickupEntry>
    }

    private fun getCurrentInventoryState(): Map<String, Int>? {
        val inventoryState = mutableMapOf<String, Int>()
        val mainInventory = player?.inventory?.mainInventory ?: return null

        loop@ for (element in mainInventory) {
            if (element == null) continue
            var displayName = element.displayName ?: "Empty slot"

            for (regex in ignoreStacksRegex) {
                if (displayName.matches(regex)) {
                    continue@loop
                }
            }

            if (displayName.matches(npcSellingStackRegex)) {
                displayName = displayName.getRegexGroups(npcSellingStackRegex)!![1]!!.value
            }
            items[displayName] = Pair(element.skyblockID, element)

            val itemCount = element.stackSize
            inventoryState.merge(displayName, itemCount, Int::plus)
        }

        return inventoryState
    }

    private fun compareInventories(previous: Map<String, Int>, current: Map<String, Int>) {
        if (previous.isEmpty() || current.isEmpty()) return

        current.forEach { (displayName, currentCount) ->
            val previousCount = previous[displayName] ?: 0
            val countDifference = currentCount - previousCount

            if (countDifference != 0) {
                val itemName = if (displayName != "Empty slot") displayName else getPreviousItemName()
                val itemEntry = items[itemName] ?: return
                val (itemID, _) = itemEntry
                val entry = displayLines.getOrPut(itemName) { PickupEntry(itemName, itemID) }
                entry.count += countDifference
                entry.lastUpdated = System.currentTimeMillis()
                displayLines[itemName] = entry
            }
        }

        previous.forEach { (displayName, previousCount) ->
            val currentCount = current[displayName] ?: 0
            if (previousCount > 0 && currentCount == 0) {
                val itemName = if (displayName != "Empty slot") displayName else getPreviousItemName()
                val itemEntry = items[itemName] ?: return
                val (itemID, _) = itemEntry
                val entry = displayLines.getOrPut(itemName) { PickupEntry(itemName, itemID) }

                entry.count -= previousCount
                entry.lastUpdated = System.currentTimeMillis()
                displayLines[itemName] = entry
            }
        }
    }

    private fun getPreviousItemName(): String {
        val previousItem = previousInventory.entries.find { it.key != "Empty slot" && it.value == 0 }
        return previousItem?.key ?: "Unknown item"
    }

    class PickupEntry(val itemName: String = "", var itemId: String = "") {
        var count: Int = 0
        var lastUpdated: Long = 0
        var animatedY: Float = 0f
        var targetY: Float = 0f
        private var lastTargetUpdate: Long = 0

        private val fadeInDuration = 300L
        private val fadeOutDuration = 300L
        private val totalDuration = 5_000L
        private val positionLerpSpeed = 0.15f

        fun updatePosition(newTargetY: Float, currentTime: Long) {
            if (targetY != newTargetY) {
                targetY = newTargetY
                lastTargetUpdate = currentTime
            }

            val deltaTime = (currentTime - lastTargetUpdate).coerceAtMost(50L)
            val lerpFactor = (positionLerpSpeed * deltaTime / 16.67f).coerceIn(0f, 1f)
            animatedY += (targetY - animatedY) * lerpFactor
        }

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
    }
}