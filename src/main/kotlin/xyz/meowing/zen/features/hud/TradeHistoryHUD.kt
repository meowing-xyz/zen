package xyz.meowing.zen.features.hud

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.CramSiblingConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.universal.UMatrixStack
import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.mc
import xyz.meowing.zen.api.ItemAPI
import xyz.meowing.zen.api.TradeAPI
import xyz.meowing.zen.config.ui.core.CustomFontProvider
import xyz.meowing.zen.ui.components.ItemComponent
import xyz.meowing.zen.utils.CommandUtils
import xyz.meowing.zen.utils.FontUtils
import xyz.meowing.zen.utils.ItemUtils.skyblockID
import xyz.meowing.zen.utils.NumberUtils.abbreviateNumber
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.command.ICommandSender
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.JsonToNBT
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.client.config.GuiUtils
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@Zen.Command
object TradeHistoryCommand : CommandUtils("tradelogs", aliases = listOf("zentl", "zentrades")) {
    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        TickUtils.schedule(2) {
            mc.displayGuiScreen(TradeHistoryHUD())
        }
    }
}

class TradeHistoryHUD : WindowScreen(ElementaVersion.V2, newGuiScale = 2) {
    private val theme = object {
        val bg = Color(8, 12, 16, 255)
        val element = Color(12, 16, 20, 255)
        val accent = Color(100, 245, 255, 255)
        val accent2 = Color(80, 200, 220, 255)
        val divider = Color(70, 80, 90, 255)
        val give = Color(255, 85, 85, 255)
        val receive = Color(85, 255, 85, 255)
        val warning = Color(255, 170, 0, 255)
    }

    private lateinit var scrollComponent: ScrollComponent
    private lateinit var tradesContainer: UIContainer
    private val formatter = DecimalFormat("#,###")
    private val timeFormatter = SimpleDateFormat("HH:mm")
    private var searchQuery = ""
    private var tooltipElements: MutableMap<UIComponent, Set<String>> = mutableMapOf()
    private var stackElements: MutableMap<UIComponent, ItemStack?> = mutableMapOf()

    private fun UIComponent.addTooltip(tooltip: Set<String>, stack: ItemStack? = null) {
        tooltipElements[this] = tooltip
        stackElements[this] = stack
    }

    override fun onDrawScreen(matrixStack: UMatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.onDrawScreen(matrixStack, mouseX, mouseY, partialTicks)

        for (element in tooltipElements.keys) {
            if (element.isHovered()) {
                val tooltip = tooltipElements[element]?.toMutableList() ?: mutableListOf()
                val item = stackElements[element] ?: ItemStack(Items.apple)

                val event = ItemTooltipEvent(item, mc.thePlayer, tooltip, false)
                MinecraftForge.EVENT_BUS.post(event)

                GuiUtils.drawHoveringText(
                    event.toolTip,
                    mouseX, mouseY,
                    window.getWidth().toInt(), window.getHeight().toInt(),
                    -1, FontUtils.getFontRenderer()
                )
            }
        }
    }

    init {
        buildGui()
        updateTrades()
    }

    private fun createBlock(radius: Float): UIRoundedRectangle = UIRoundedRectangle(radius)

    private fun buildGui() {
        val border = createBlock(4f).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 90.percent()
            height = 90.percent()
        }.setColor(theme.accent2) childOf window

        val main = createBlock(4f).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent() - 2.pixels()
            height = 100.percent() - 2.pixels()
        }.setColor(theme.bg) childOf border

        createHeader(main)
        createContent(main)
    }

    private fun createHeader(parent: UIComponent) {
        val header = UIContainer().constrain {
            x = 0.percent()
            y = 0.percent()
            width = 100.percent()
            height = 40.pixels()
        } childOf parent

        UIText("§lTrade History").constrain {
            x = 12.pixels()
            y = 12.pixels()
            textScale = 2.0.pixels()
        }.setColor(theme.accent).setFontProvider(CustomFontProvider) childOf header

        val searchContainer = createBlock(3f).constrain {
            x = 12.pixels(true)
            y = CenterConstraint()
            width = 200.pixels()
            height = 24.pixels()
        }.setColor(theme.element) childOf header

        val searchInput = (UITextInput("Search trades...").constrain {
            x = 8.pixels()
            y = CenterConstraint()
            width = 100.percent() - 16.pixels()
            height = 14.pixels()
        }.setColor(Color.LIGHT_GRAY).setFontProvider(CustomFontProvider) childOf searchContainer) as UITextInput

        searchInput.onKeyType { _, _ ->
            searchQuery = searchInput.getText()
            updateTrades()
        }

        searchContainer.onMouseClick {
            searchInput.grabWindowFocus()
        }

        createBlock(0f).constrain {
            x = 0.percent()
            y = 100.percent() - 1.pixels()
            width = 100.percent()
            height = 1.pixels()
        }.setColor(theme.accent2) childOf header
    }

    private fun createContent(parent: UIComponent) {
        val contentPanel = UIContainer().constrain {
            x = 8.pixels()
            y = 48.pixels()
            width = 100.percent() - 16.pixels()
            height = 100.percent() - 56.pixels()
        } childOf parent

        scrollComponent = ScrollComponent().constrain {
            x = 4.pixels()
            y = 4.pixels()
            width = 100.percent() - 8.pixels()
            height = 100.percent() - 8.pixels()
        } childOf contentPanel

        tradesContainer = UIContainer().constrain {
            width = 100.percent()
            height = 100.percent()
        } childOf scrollComponent

        window.onMouseScroll {
            it.stopImmediatePropagation()
            scrollComponent.mouseScroll(it.delta)
        }
    }

    private fun updateTrades() {
        tradesContainer.clearChildren()
        tooltipElements.clear()
        stackElements.clear()

        val tradeHistory = TradeAPI.getTradeHistory()
        if (tradeHistory.entrySet().isEmpty()) {
            createEmptyState()
            return
        }

        val sortedDates = tradeHistory.entrySet().map { it.key }.sortedByDescending { it }

        sortedDates.forEach { date ->
            val trades = tradeHistory.getAsJsonArray(date)
            if (trades.size() > 0 && dateShouldShow(date, trades)) {
                createDateSection(date, trades)
            }
        }
    }

    private fun createEmptyState() {
        val emptyContainer = UIContainer().constrain {
            x = 0.percent()
            y = 50.percent()
            width = 100.percent()
            height = 60.pixels()
        } childOf tradesContainer

        UIText("No trades recorded yet").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 1.5.pixels()
        }.setColor(theme.accent2).setFontProvider(CustomFontProvider) childOf emptyContainer
    }

    private fun createDateSection(date: String, trades: JsonArray) {
        val dateContainer = UIContainer().constrain {
            x = 0.percent()
            y = CramSiblingConstraint(12f)
            width = 100.percent()
            height = 40.pixels()
        } childOf tradesContainer

        val dateHeader = createBlock(3f).constrain {
            x = 0.percent()
            y = 0.percent()
            width = 100.percent()
            height = 35.pixels()
        }.setColor(theme.element) childOf dateContainer

        UIText(date).constrain {
            x = 12.pixels()
            y = CenterConstraint()
            textScale = 1.3.pixels()
        }.setColor(theme.accent).setFontProvider(CustomFontProvider) childOf dateHeader

        UIText("${trades.size()} trades").constrain {
            x = 12.pixels(true)
            y = CenterConstraint()
            textScale = 1.0.pixels()
        }.setColor(theme.accent2).setFontProvider(CustomFontProvider) childOf dateHeader

        val dateTooltip = createDateSummary(date, trades)
        dateHeader.addTooltip(dateTooltip)

        val horizontalScroll = ScrollComponent(horizontalScrollEnabled = true, verticalScrollEnabled = false).constrain {
            x = 0.percent()
            y = CramSiblingConstraint(4f)
            width = 100.percent()
            height = 160.pixels()
        } childOf tradesContainer

        horizontalScroll.onMouseScroll {
            if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                it.stopImmediatePropagation()
                scrollComponent.scrollTo(verticalOffset = (scrollComponent.verticalOffset + it.delta * 20).toFloat())
            }
        }

        val scrollbar = createBlock(3f).constrain {
            width = 100.percent()
            height = 4.pixels()
            y = 100.percent() - 4.pixels()
            color = theme.divider.constraint
        } childOf horizontalScroll.parent

        horizontalScroll.setHorizontalScrollBarComponent(scrollbar, true)

        trades.reversed().forEach { tradeElement ->
            val trade = tradeElement.asJsonObject
            if (matchesSearch(trade, date)) {
                createTradeCard(horizontalScroll, trade)
            }
        }
    }

    private fun createDateSummary(date: String, trades: JsonArray): Set<String> {
        var coinChange = 0L
        val itemChanges = mutableMapOf<String, Int>()

        trades.forEach { tradeElement ->
            val trade = tradeElement.asJsonObject
            coinChange += trade.get("theirCoins").asLong - trade.get("yourCoins").asLong

            trade.getAsJsonArray("yourItems").forEach { item ->
                val itemObj = item.asJsonObject
                val stack = createItemStack(itemObj)
                val key = stack.displayName.removeFormatting()
                itemChanges[key] = itemChanges.getOrDefault(key, 0) - stack.stackSize
            }

            trade.getAsJsonArray("theirItems").forEach { item ->
                val itemObj = item.asJsonObject
                val stack = createItemStack(itemObj)
                val key = stack.displayName.removeFormatting()
                itemChanges[key] = itemChanges.getOrDefault(key, 0) + stack.stackSize
            }
        }

        val tooltip = mutableSetOf<String>()
        tooltip.add("§e§l$date Summary")
        tooltip.add("§7Coin Change: ${if (coinChange >= 0) "§a+" else "§c"}${formatter.format(coinChange)}")

        if (itemChanges.isNotEmpty()) {
            tooltip.add("§7Item Changes:")
            itemChanges.forEach { (item, change) ->
                if (change != 0) {
                    tooltip.add(" ${if (change > 0) "§a+" else "§c"}$change §7$item")
                }
            }
        }

        return tooltip
    }

    private fun createTradeCard(parent: UIComponent, trade: JsonObject) {
        val outline = createBlock(3f).constrain {
            x = SiblingConstraint(8f)
            y = 0.percent()
            width = 220.pixels()
            height = 150.pixels()
        }.setColor(theme.accent) childOf parent

        val card = createBlock(3f).constrain {
            x = 0.percent() + 1.pixels()
            y = 0.percent() + 1.pixels()
            width = 100.percent() - 2.pixels()
            height = 100.percent() - 2.pixels()
        }.setColor(theme.element) childOf outline

        val timestamp = trade.get("timestamp").asLong
        val username = trade.get("username").asString
        val yourCoins = trade.get("yourCoins").asLong
        val theirCoins = trade.get("theirCoins").asLong
        val yourItems = trade.getAsJsonArray("yourItems")
        val theirItems = trade.getAsJsonArray("theirItems")

        val header = UIContainer().constrain {
            x = 0.percent()
            y = 0.percent()
            width = 100.percent()
            height = 30.pixels()
        } childOf card

        UIText("§b$username").constrain {
            x = 8.pixels()
            y = CenterConstraint()
            textScale = 1.1.pixels()
        }.setColor(theme.accent2).setFontProvider(CustomFontProvider) childOf header

        UIText(timeFormatter.format(Date(timestamp))).constrain {
            x = 8.pixels(true)
            y = CenterConstraint()
            textScale = 0.9.pixels()
        }.setColor(theme.accent2).setFontProvider(CustomFontProvider) childOf header

        createBlock(0f).constrain {
            x = 4.pixels()
            y = 100.percent() - 1.pixels()
            width = 100.percent() - 8.pixels()
            height = 1.pixels()
        }.setColor(theme.divider) childOf header

        val contentArea = UIContainer().constrain {
            x = 0.percent()
            y = 32.pixels()
            width = 100.percent()
            height = 118.pixels()
        } childOf card

        val leftSide = UIContainer().constrain {
            x = 4.pixels()
            y = 0.percent()
            width = 50.percent() - 6.pixels()
            height = 100.percent()
        } childOf contentArea

        val rightSide = UIContainer().constrain {
            x = 50.percent() + 2.pixels()
            y = 0.percent()
            width = 50.percent() - 6.pixels()
            height = 100.percent()
        } childOf contentArea

        val yourCustomWorth = createTradeSide(leftSide, "You Gave", yourItems, yourCoins, theme.give, trade, "yourCustomValue")
        val theirCustomWorth = createTradeSide(rightSide, "You Received", theirItems, theirCoins, theme.receive, trade, "theirCustomValue")

        UIBlock().constrain {
            x = 50.percent()
            y = 0.percent() - 2.pixels()
            width = 2.pixels()
            height = 88.percent()
        }.setColor(theme.divider) childOf contentArea

        val profitContainer = UIContainer().constrain {
            x = CenterConstraint()
            y = 100.percent() - 12.pixels()
            width = 0.pixels()
            height = 12.pixels()
        } childOf card

        updateProfitText(profitContainer, yourCustomWorth, theirCustomWorth)
    }

    private fun updateProfitText(container: UIComponent, yourWorth: Long, theirWorth: Long) {
        container.clearChildren()
        val profit = theirWorth - yourWorth
        val profitText = if (profit >= 0) "§a+${profit.abbreviateNumber()}" else "§c${profit.abbreviateNumber()}"

        UIText("Profit: $profitText").constrain {
            x = CenterConstraint()
            y = 0.pixels()
            textScale = 0.8.pixels()
        }.setColor(if (profit >= 0) theme.receive else theme.give).setFontProvider(CustomFontProvider) childOf container
    }

    private fun createTradeSide(parent: UIComponent, title: String, items: JsonArray, coins: Long, color: Color, trade: JsonObject, customValueKey: String): Long {
        UIText(title).constrain {
            x = CenterConstraint()
            y = 4.pixels()
            textScale = 0.9.pixels()
        }.setColor(color).setFontProvider(CustomFontProvider) childOf parent

        val itemsGrid = UIContainer().constrain {
            x = 0.percent()
            y = 16.pixels()
            width = 100.percent()
            height = 64.pixels()
        } childOf parent

        var itemWorth = 0L

        items.forEachIndexed { index, itemElement ->
            val itemObj = itemElement.asJsonObject
            val stack = createItemStack(itemObj)
            itemWorth += getItemValue(stack) * stack.stackSize

            if (index < 8) {
                val itemComponent = ItemComponent(stack, 14f).constrain {
                    x = (index % 4 * 16 + 2).pixels()
                    y = (index / 4 * 16 + 2).pixels()
                    width = 14.pixels()
                    height = 14.pixels()
                } childOf itemsGrid

                val tooltip = mutableSetOf<String>()
                tooltip.add(stack.displayName)
                tooltip.add("§7Count: ${stack.stackSize}")

                val itemValue = getItemValue(stack)
                if (itemValue > 0) {
                    tooltip.add("§7Value: §6${(itemValue * stack.stackSize).abbreviateNumber()}")
                }

                itemComponent.addTooltip(tooltip, stack)
            }
        }

        if (items.size() > 8) {
            UIText("§7+${items.size() - 8} more").constrain {
                x = 0.percent()
                y = 82.pixels()
                textScale = 0.7.pixels()
            }.setColor(theme.accent2).setFontProvider(CustomFontProvider) childOf parent
        }

        val totalWorth = itemWorth + coins
        var currentWorth = totalWorth

        if (trade.has(customValueKey)) {
            currentWorth = trade.get(customValueKey).asLong
        }

        if (coins > 0) {
            UIText("§6${coins.abbreviateNumber()} coins").constrain {
                x = CenterConstraint()
                y = 100.percent() - 25.pixels()
                textScale = 0.8.pixels()
            }.setColor(theme.warning).setFontProvider(CustomFontProvider) childOf parent
        }

        return currentWorth
    }

    private fun getItemValue(stack: ItemStack): Long {
        val itemId = stack.skyblockID
        if (itemId.isEmpty()) return 0L
        val itemInfo = ItemAPI.getItemInfo(itemId) ?: return 0L

        return when {
            itemInfo.has("lowestBin") && itemInfo.get("lowestBin").asLong > 0 -> itemInfo.get("lowestBin").asLong
            itemInfo.has("bazaarSell") && itemInfo.get("bazaarSell").asDouble > 0 -> itemInfo.get("bazaarSell").asDouble.toLong()
            itemInfo.has("bazaarBuy") && itemInfo.get("bazaarBuy").asDouble > 0 -> itemInfo.get("bazaarBuy").asDouble.toLong()
            itemInfo.has("avgLowestBin") && itemInfo.get("avgLowestBin").asLong > 0 -> itemInfo.get("avgLowestBin").asLong
            itemInfo.has("npcSell") && itemInfo.get("npcSell").asLong > 1 -> itemInfo.get("npcSell").asLong
            else -> 0L
        }
    }

    private fun createItemStack(itemObj: JsonObject): ItemStack {
        val stack = ItemStack(
            Item.getByNameOrId(itemObj.get("id").asString),
            itemObj.get("count").asInt
        )
        stack.itemDamage = itemObj.get("damage").asInt
        if (itemObj.get("nbt").asString != "null") {
            stack.tagCompound = JsonToNBT.getTagFromJson(itemObj.get("nbt").asString)
        }
        return stack
    }

    private fun dateShouldShow(date: String, trades: JsonArray): Boolean {
        if (searchQuery.isEmpty()) return true
        if (date.contains(searchQuery, ignoreCase = true)) return true

        trades.forEach { tradeElement ->
            val trade = tradeElement.asJsonObject
            if (matchesSearch(trade, date)) return true
        }
        return false
    }

    private fun matchesSearch(trade: JsonObject, date: String): Boolean {
        if (searchQuery.isEmpty()) return true
        if (date.contains(searchQuery, ignoreCase = true)) return true
        if (trade.get("username").asString.contains(searchQuery, ignoreCase = true)) return true

        listOf("yourItems", "theirItems").forEach { itemsKey ->
            trade.getAsJsonArray(itemsKey).forEach { itemElement ->
                val stack = createItemStack(itemElement.asJsonObject)
                if (stack.displayName.removeFormatting().contains(searchQuery, ignoreCase = true)) return true
            }
        }

        return false
    }
}