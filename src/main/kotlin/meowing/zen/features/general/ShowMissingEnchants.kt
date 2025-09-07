package meowing.zen.features.general

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.runBlocking
import meowing.zen.Zen
import meowing.zen.api.NEUApi
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ItemTooltipEvent
import meowing.zen.features.Feature
import meowing.zen.utils.ItemUtils.extraAttributes
import meowing.zen.utils.Utils.removeFormatting
import org.apache.commons.lang3.text.WordUtils
import org.lwjgl.input.Keyboard

@Zen.Module
object ShowMissingEnchants : Feature("showmissingenchants", true) {
    private var enchantsData: JsonObject? = null
    private var enchantPools: JsonArray? = null
    private val itemNameRegex = Regex("""\b(?:COMMON|UNCOMMON|RARE|EPIC|LEGENDARY|MYTHIC|SPECIAL|VERY SPECIAL|DIVINE)\b.*\b([A-Z]+)\b""")
    private val poolIgnoreCache = mutableMapOf<Set<String>, Set<String>>()
    private val itemEnchantCache = mutableMapOf<String, JsonArray?>()
    private val tooltipCache = mutableMapOf<ItemCacheKey, List<String>>()

    data class ItemCacheKey(
        val itemUuid: String,
        val enchantIds: Set<String>
    )

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Show Missing Enchants", ConfigElement(
                "showmissingenchants",
                "Show Missing Enchants",
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        loadConstants()

        register<ItemTooltipEvent> { event ->
            if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || enchantsData == null || enchantPools == null) return@register

            val extraAttributes = event.itemStack.extraAttributes ?: return@register
            if (!extraAttributes.hasKey("enchantments", 10)) return@register

            val enchantIds = extraAttributes.getCompoundTag("enchantments").keySet
            if (enchantIds.isEmpty()) return@register

            val itemUUID = extraAttributes.getString("uuid")
            if (itemUUID.isEmpty()) return@register

            val cacheKey = ItemCacheKey(itemUUID, enchantIds)

            tooltipCache[cacheKey]?.let { cachedLines ->
                event.lines.clear()
                event.lines.addAll(cachedLines)
                return@register
            }

            if (!hasEnchantInTooltip(event.lines, enchantIds)) return@register

            val allItemEnchs = getItemEnchantsFromCache(event.lines) ?: return@register

            val emptyLineIndex = findEmptyLineAfterEnchants(event.lines, enchantIds)
            if (emptyLineIndex == -1) return@register

            val ignoreSet = poolIgnoreCache.getOrPut(enchantIds) { getIgnoreFromPool(enchantIds) }
            val missing = allItemEnchs.asSequence()
                .map { it.asString }
                .filter { !it.startsWith("ultimate_") && !ignoreSet.contains(it) && !enchantIds.contains(it) }
                .toList()

            if (missing.isNotEmpty()) {
                addMissingEnchantsToTooltip(event.lines, missing, emptyLineIndex)
                tooltipCache[cacheKey] = ArrayList(event.lines)
                if (tooltipCache.size > 50) tooltipCache.clear()
            }
        }
    }

    private fun loadConstants() {
        runBlocking {
            try {
                val constants = NEUApi.NeuConstantData.getData().getAsJsonObject("enchants")
                enchantsData = constants?.getAsJsonObject("enchants")
                enchantPools = constants?.getAsJsonArray("enchant_pools")
                LOGGER.info("Loaded enchants in ShowMissingEnchants")
            } catch (e: Exception) {
                LOGGER.warn("Failed to load enchants in ShowMissingEnchants: $e")
            }
        }
    }

    private fun getIgnoreFromPool(enchantIds: Set<String>): Set<String> {
        val ignoreFromPool = mutableSetOf<String>()
        enchantPools?.forEach { poolElement ->
            val poolEnchants = poolElement.asJsonArray.map { it.asString }.toSet()
            if (poolEnchants.any { enchantIds.contains(it) }) {
                ignoreFromPool.addAll(poolEnchants)
            }
        }
        return ignoreFromPool
    }

    private fun getItemEnchantsFromCache(tooltip: List<String>): JsonArray? {
        val itemName = extractItemNameFromTooltip(tooltip) ?: return null
        if (enchantsData == null) return null

        return itemEnchantCache.getOrPut(itemName) {
            enchantsData?.entrySet()?.find { (key, _) ->
                itemName.contains(key, ignoreCase = true)
            }?.value?.asJsonArray
        }
    }

    private fun extractItemNameFromTooltip(tooltip: List<String>): String? {
        if (tooltip.isEmpty()) return null

        for (i in tooltip.indices.reversed()) {
            val cleanLine = tooltip[i].removeFormatting().trim()
            if (cleanLine.isEmpty()) continue

            val match = itemNameRegex.find(cleanLine)
            if (match != null) {
                val itemName = match.groupValues[1]
                if (itemName.isNotEmpty()) {
                    return itemName
                }
            }
        }

        return null
    }

    private fun hasEnchantInTooltip(tooltip: List<String>, enchantIds: Set<String>): Boolean {
        val enchantNames by lazy { enchantIds.map { WordUtils.capitalizeFully(it.replace("_", " ")) } }
        return tooltip.any { line -> enchantNames.any { line.contains(it) } }
    }

    private fun findEmptyLineAfterEnchants(tooltip: List<String>, enchantIds: Set<String>): Int {
        val enchantNames by lazy { enchantIds.map { WordUtils.capitalizeFully(it.replace("_", " ")) } }
        var foundEnchantSection = false

        for (i in tooltip.indices) {
            val line = tooltip[i]
            if (enchantNames.any { line.contains(it) }) {
                foundEnchantSection = true
            } else if (foundEnchantSection && line.removeFormatting().trim().isEmpty()) {
                return i
            }
        }
        return -1
    }

    private fun addMissingEnchantsToTooltip(tooltip: MutableList<String>, missing: List<String>, insertIndex: Int) {
        val lines = mutableListOf("")
        val sb = StringBuilder("§cMissing: ")
        var lineLength = 9

        missing.forEachIndexed { index, enchantId ->
            val enchantName = WordUtils.capitalizeFully(enchantId.replace("_", " "))
            val separator = if (index == 0) "" else ", "
            val newLength = lineLength + separator.length + enchantName.length

            if (index > 0 && newLength > 40) {
                lines.add(sb.toString())
                sb.clear().append("§7$enchantName")
                lineLength = enchantName.length
            } else {
                sb.append("$separator§7$enchantName")
                lineLength = newLength
            }
        }

        if (sb.isNotEmpty()) lines.add(sb.toString())
        tooltip.addAll(insertIndex, lines)
    }
}