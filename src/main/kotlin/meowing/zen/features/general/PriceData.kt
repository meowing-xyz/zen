package meowing.zen.features.general

import meowing.zen.Zen
import meowing.zen.api.ItemAPI
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ItemTooltipEvent
import meowing.zen.features.Feature
import meowing.zen.utils.Utils.abbreviateNumber
import meowing.zen.utils.Utils.formatNumber

@Zen.Module
object PriceData : Feature("pricedata", true) {
    private val displaySet by ConfigDelegate<Set<Int>>("pricedatadisplay")
    private val abbreviateNumbers by ConfigDelegate<Boolean>("pricedataabbreviatenumber")
    private val displayOptions = listOf(
        "Active Listings",
        "Daily Sales",
        "BIN Price",
        "Auction Price",
        "Bazaar"
    )

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Price Data", ConfigElement(
                "pricedata",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("General", "Price Data", "Options", ConfigElement(
                "pricedatadisplay",
                "Price information to show",
                ElementType.MultiCheckbox(displayOptions, setOf(0, 1, 2, 3, 4))
            ))
            .addElement("General", "Price Data", "Options", ConfigElement(
                "pricedataabbreviatenumber",
                "Abbreviate numbers",
                ElementType.Switch(false)
            ))
    }

    private fun Number.formatPrice(): String = if (abbreviateNumbers) abbreviateNumber() else formatNumber()

    override fun initialize() {
        register<ItemTooltipEvent> { event ->
            val stack = event.itemStack
            val pricingData = ItemAPI.getItemInfo(stack) ?: return@register

            if (0 in displaySet) {
                pricingData.takeIf { it.has("activeBin") || it.has("activeAuc") }?.let {
                    val activeBinNum = if (it.has("activeBin")) it.get("activeBin").asInt else -1
                    val activeAucNum = if (it.has("activeAuc")) it.get("activeAuc").asInt else -1
                    val activeBin = if (activeBinNum != -1) activeBinNum.formatPrice() else "§7N/A"
                    val activeAuc = if (activeAucNum != -1) activeAucNum.formatPrice() else "§7N/A"
                    event.lines.add("§3Active Listings: §e${activeBin} §8[BIN] §7• §e${activeAuc} §8[Auction]")
                }
            }

            if (1 in displaySet) {
                pricingData.takeIf { it.has("binSold") || it.has("aucSold") }?.let {
                    val soldBinNum = if (it.has("binSold")) it.get("binSold").asInt else -1
                    val soldAucNum = if (it.has("aucSold")) it.get("aucSold").asInt else -1
                    val soldBin = if (soldBinNum != -1) soldBinNum.formatPrice() else "§7N/A"
                    val soldAuc = if (soldAucNum != -1) soldAucNum.formatPrice() else "§7N/A"
                    event.lines.add("§3Daily Sales: §e${soldBin} §8[BIN] §7• §e${soldAuc} §8[Auction]")
                }
            }

            if (2 in displaySet) {
                pricingData.takeIf { it.has("avgLowestBin") && it.has("lowestBin") }?.let {
                    val avgLowestBin = it.get("avgLowestBin").asLong.formatPrice()
                    val lowestBin = it.get("lowestBin").asLong.formatPrice()
                    event.lines.add("§3BIN Price: §a${avgLowestBin} §8[Avg] §7• §a${lowestBin} §8[Lowest]")
                }
            }

            if (3 in displaySet) {
                pricingData.takeIf { it.has("avgAucPrice") && it.has("aucPrice") }?.let {
                    val avgAucPrice = it.get("avgAucPrice").asLong.formatPrice()
                    val aucPrice = it.get("aucPrice").asLong.formatPrice()
                    event.lines.add("§3Auction Price: §a${avgAucPrice} §8[Avg] §7• §a${aucPrice} §8[Next]")
                }
            }

            if (4 in displaySet) {
                pricingData.takeIf { it.has("bazaarBuy") || it.has("bazaarSell") }?.let {
                    val multiplier = stack.stackSize
                    val bazaarBuy = it.takeIf { it.has("bazaarBuy") }
                        ?.get("bazaarBuy")
                        ?.asLong
                        ?.times(multiplier)
                        ?.formatPrice() ?: "§7N/A"
                    val bazaarSell = it.takeIf { it.has("bazaarSell") }
                        ?.get("bazaarSell")
                        ?.asLong
                        ?.times(multiplier)
                        ?.formatPrice() ?: "§7N/A"
                    event.lines.add("§3Bazaar: §e${bazaarBuy} §8[Buy] §7• §a${bazaarSell} §8[Sell]")
                }
            }
        }
    }
}