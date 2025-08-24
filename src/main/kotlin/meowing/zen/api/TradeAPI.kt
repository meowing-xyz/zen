package meowing.zen.api

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.events.ChatEvent
import meowing.zen.events.EventBus
import meowing.zen.events.GuiEvent
import meowing.zen.utils.DataUtils
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils
import meowing.zen.utils.Utils.chestName
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.inventory.Container
import net.minecraft.item.ItemStack

@Zen.Module
object TradeAPI {
    data class TradeLogs(val tradeHistory: JsonObject = JsonObject())

    private val save = DataUtils("TradeAPI", TradeLogs())
    private var inTradeMenu = false
    private var lastTradeMenu: Container? = null
    private var tradingWith = ""
    private var tradingWithSub = ""

    private val yourSlots = listOf(0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30)
    private val theirSlots = listOf(5, 6, 7, 8, 14, 15, 16, 17, 23, 24, 25, 26, 32, 33, 34, 35)

    init {
        TickUtils.loop(20) {
            if (mc.theWorld == null) return@loop
            if (mc.currentScreen == null) inTradeMenu = false

            if (tradingWithSub.isNotEmpty()) {
                mc.theWorld!!.playerEntities.find { it.name.contains(tradingWithSub) }?.let {
                    tradingWith = it.name
                }
            }
        }

        EventBus.register<ChatEvent.Receive> { event ->
            if (event.event.message.unformattedText.removeFormatting().startsWith("Trade completed with")) {
                interpretLastTradeMenu()
                inTradeMenu = false
            }
        }

        EventBus.register<GuiEvent.Slot.RenderPost> { event ->
            if (event.slot.slotNumber == 0) {
                inTradeMenu = false
                val tradeSlot = event.gui.inventorySlots?.getSlot(4) ?: return@register
                if (tradeSlot.stack?.displayName?.removeFormatting() != "⇦ Your stuff") return@register

                inTradeMenu = true
                lastTradeMenu = event.gui.inventorySlots
                tradingWithSub = event.gui.chestName.split("You")[1].trim()
            }
        }
    }

    private fun interpretLastTradeMenu() {
        val menu = lastTradeMenu ?: return
        val trade = JsonObject()

        val (yourItems, yourCoins) = processSlots(menu, yourSlots)
        trade.add("yourItems", yourItems)
        trade.addProperty("yourCoins", yourCoins)

        val (theirItems, theirCoins) = processSlots(menu, theirSlots)
        trade.add("theirItems", theirItems)
        trade.addProperty("theirCoins", theirCoins)

        trade.addProperty("timestamp", System.currentTimeMillis())
        trade.addProperty("username", tradingWith)

        val date = Utils.getFormattedDate()
        save.updateAndSave {
            if (!tradeHistory.has(date)) tradeHistory.add(date, JsonArray())
            tradeHistory[date].asJsonArray.add(trade)
        }

        tradingWith = ""
    }

    private fun processSlots(menu: Container, slots: List<Int>): Pair<JsonArray, Long> {
        val items = JsonArray()
        var coins = 0L

        slots.forEach { slot ->
            menu.getSlot(slot).stack?.let { stack ->
                if (stack.displayName.removeFormatting().endsWith("coins")) {
                    coins += parseCoins(stack.displayName.removeFormatting())
                } else {
                    items.add(createItemJson(stack))
                }
            }
        }

        return items to coins
    }

    private fun parseCoins(name: String): Long {
        return when {
            name.endsWith("k coins", true) -> name.dropLast(7).toDouble() * 1_000
            name.endsWith("M coins", true) -> name.dropLast(7).toDouble() * 1_000_000
            name.endsWith("B coins", true) -> name.dropLast(7).toDouble() * 1_000_000_000
            else -> name.replace(" coins", "").toDouble()
        }.toLong()
    }

    private fun createItemJson(stack: ItemStack): JsonObject {
        return JsonObject().apply {
            addProperty("count", stack.stackSize)
            addProperty("damage", stack.itemDamage)
            addProperty("nbt", stack.tagCompound.toString())
            addProperty("id", stack.item.registryName)
        }
    }

    fun getTradeHistory(): JsonObject = save().tradeHistory
}