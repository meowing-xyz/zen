package meowing.zen.features.general

import meowing.zen.Zen
import meowing.zen.api.ItemApi
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ItemTooltipEvent
import meowing.zen.features.Feature
import meowing.zen.utils.Utils.formatNumber

@Zen.Module
object PriceData : Feature("pricedata") {
    private val displaySet by ConfigDelegate<Set<Int>>("pricedatadisplay")
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
    }

    override fun initialize() {
        register<ItemTooltipEvent> { event ->
            val stack = event.itemStack
            val pricingData = ItemApi.getItemInfo(stack) ?: return@register

            if (0 in displaySet) {
                pricingData.takeIf { it.has("activeBin") || it.has("activeAuc") }?.let {
                    val activeBinNum = if (it.has("activeBin")) it.get("activeBin").asInt else -1
                    val activeAucNum = if (it.has("activeAuc")) it.get("activeAuc").asInt else -1
                    val activeBin = if (activeBinNum != -1) activeBinNum.formatNumber() else "§8N/A"
                    val activeAuc = if (activeAucNum != -1) activeAucNum.formatNumber() else "§8N/A"
                    event.lines.add("§6Active Listings: §a${activeBin} §7[BIN] §8• §b${activeAuc} §7[Auction]")
                }
            }

            if (1 in displaySet) {
                pricingData.takeIf { it.has("binSold") || it.has("aucSold") }?.let {
                    val soldBinNum = if (it.has("binSold")) it.get("binSold").asInt else -1
                    val soldAucNum = if (it.has("aucSold")) it.get("aucSold").asInt else -1
                    val soldBin = if (soldBinNum != -1) soldBinNum.formatNumber() else "§8N/A"
                    val soldAuc = if (soldAucNum != -1) soldAucNum.formatNumber() else "§8N/A"
                    event.lines.add("§6Daily Sales: §a${soldBin} §7[BIN] §8• §b${soldAuc} §7[Auction]")
                }
            }

            if (2 in displaySet) {
                pricingData.takeIf { it.has("avgLowestBin") && it.has("lowestBin") }?.let {
                    val avgLowestBin = it.get("avgLowestBin").asLong.formatNumber()
                    val lowestBin = it.get("lowestBin").asLong.formatNumber()
                    event.lines.add("§6BIN Price: §e${avgLowestBin} §7[Avg] §8• §e${lowestBin} §7[Lowest]")
                }
            }

            if (3 in displaySet) {
                pricingData.takeIf { it.has("avgAucPrice") && it.has("aucPrice") }?.let {
                    val avgAucPrice = it.get("avgAucPrice").asLong.formatNumber()
                    val aucPrice = it.get("aucPrice").asLong.formatNumber()
                    event.lines.add("§6Auction Price: §e${avgAucPrice} §7[Avg] §8• §e${aucPrice} §7[Next]")
                }
            }

            if (4 in displaySet) {
                pricingData.takeIf { it.has("bazaarBuy") || it.has("bazaarSell") }?.let {
                    val multiplier = stack.stackSize
                    val bazaarBuy = it.takeIf { it.has("bazaarBuy") }
                        ?.get("bazaarBuy")
                        ?.asLong
                        ?.times(multiplier)
                        ?.formatNumber() ?: "§8N/A"
                    val bazaarSell = it.takeIf { it.has("bazaarSell") }
                        ?.get("bazaarSell")
                        ?.asLong
                        ?.times(multiplier)
                        ?.formatNumber() ?: "§8N/A"
                    event.lines.add("§6Bazaar: §a${bazaarBuy} §7[Buy] §8• §b${bazaarSell} §7[Sell]")
                }
            }
        }
    }

    override fun onRegister() {
        super.onRegister()
        ItemApi.updateSkyblockItemData()
    }
}