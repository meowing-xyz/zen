package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.utils.ChatUtils.addMessage
import meowing.zen.utils.ChatUtils.formatNumber
import meowing.zen.utils.Utils.removeFormatting
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Pattern

object betterbz {
    private val patterns = mapOf(
        "instaBuy" to Pattern.compile("\\[Bazaar] Bought (\\d+(?:,\\d+)*)x (.+) for (\\d+(?:,\\d+)*) coins!"),
        "buyOrderSetup" to Pattern.compile("\\[Bazaar] Buy Order Setup! (\\d+(?:,\\d+)*)x (.+) for (\\d+(?:,\\d+)*) coins\\."),
        "buyOrderFilled" to Pattern.compile("\\[Bazaar] Your Buy Order for (\\d+(?:,\\d+)*)x (.+) was filled!"),
        "buyOrderCancelled" to Pattern.compile("\\[Bazaar] Cancelled! Refunded (\\d+(?:,\\d+)*) coins from cancelling Buy Order!"),
        "buyOrderClaimed" to Pattern.compile("\\[Bazaar] Claimed (\\d+(?:,\\d+)*)x (.+) worth (\\d+(?:,\\d+)*) coins bought for (\\d+(?:,\\d+)*) each!"),
        "instaSell" to Pattern.compile("\\[Bazaar] Sold (\\d+(?:,\\d+)*)x (.+) for (\\d+(?:,\\d+)*) coins!"),
        "sellOfferSetup" to Pattern.compile("\\[Bazaar] Sell Offer Setup! (\\d+(?:,\\d+)*)x (.+) for (\\d+(?:,\\d+)*) coins\\."),
        "sellOfferFilled" to Pattern.compile("\\[Bazaar] Your Sell Offer for (\\d+(?:,\\d+)*)x (.+) was filled!"),
        "sellOfferCancelled" to Pattern.compile("\\[Bazaar] Cancelled! Refunded (\\d+(?:,\\d+)*)x (.+) from cancelling Sell Offer!"),
        "sellOrderClaimed" to Pattern.compile("\\[Bazaar] Claimed (\\d+(?:,\\d+)*)x (.+) worth (\\d+(?:,\\d+)*) coins sold for (\\d+(?:,\\d+)*) each!")
    )

    @JvmStatic
    fun initialize() = Zen.registerListener("betterbz", this)

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onGameMessage(event: ClientChatReceivedEvent) {
        val text = event.message.unformattedText.removeFormatting()

        when {
            patterns["instaBuy"]?.matcher(text)?.takeIf { it.matches() }?.let {
                bzMessage("§c§lInsta-Bought! §r§c${it.group(1)}x §c${clean(it.group(2))}§r for §6${formatNumber(it.group(3))}§r coins!")
                true
            } == true -> event.isCanceled = true

            patterns["buyOrderSetup"]?.matcher(text)?.takeIf { it.matches() }?.let {
                bzMessage("§c§lBuy Order Setup! §r§c${it.group(1)}x §c${clean(it.group(2))}§r for §6${formatNumber(it.group(3))}§r coins!")
                true
            } == true -> event.isCanceled = true

            patterns["buyOrderFilled"]?.matcher(text)?.takeIf { it.matches() }?.let {
                bzMessage("§a§lBuy Order Filled! §r§c${it.group(1)}x §c${clean(it.group(2))}§r!")
                true
            } == true -> event.isCanceled = true

            patterns["buyOrderCancelled"]?.matcher(text)?.takeIf { it.matches() }?.let {
                bzMessage("§c§lCancelled Order!§r Refunded §6${formatNumber(it.group(1))}§r coins!")
                true
            } == true -> event.isCanceled = true

            patterns["buyOrderClaimed"]?.matcher(text)?.takeIf { it.matches() }?.let {
                val total = formatNumber(it.group(3))
                val per = formatNumber(it.group(4))
                val each = if (total != per) "(§6${per}§r each!)" else ""
                bzMessage("Buy Order Claimed! §c${it.group(1)}x §c${clean(it.group(2))}§r for §6${total}§r coins! ${each}")
                true
            } == true -> event.isCanceled = true

            patterns["instaSell"]?.matcher(text)?.takeIf { it.matches() }?.let {
                bzMessage("§c§lInsta-Sold! §r§c${it.group(1)}x §c${clean(it.group(2))}§r for §6${formatNumber(it.group(3))}§r coins!")
                true
            } == true -> event.isCanceled = true

            patterns["sellOfferSetup"]?.matcher(text)?.takeIf { it.matches() }?.let {
                bzMessage("§c§lSell Offer Setup! §r§c${it.group(1)}x §c${clean(it.group(2))}§r for §6${formatNumber(it.group(3))}§r coins!")
                true
            } == true -> event.isCanceled = true

            patterns["sellOfferFilled"]?.matcher(text)?.takeIf { it.matches() }?.let {
                bzMessage("§a§lSell Offer Filled! §r§c${it.group(1)}x §c${clean(it.group(2))}§r!")
                true
            } == true -> event.isCanceled = true

            patterns["sellOfferCancelled"]?.matcher(text)?.takeIf { it.matches() }?.let {
                bzMessage("§c§lCancelled Order!§r Refunded §6${it.group(1)}x §c${clean(it.group(2))}§r!")
                true
            } == true -> event.isCanceled = true

            patterns["sellOrderClaimed"]?.matcher(text)?.takeIf { it.matches() }?.let {
                val total = formatNumber(it.group(3))
                val per = formatNumber(it.group(4))
                val each = if (total != per) "(§6${per}§r each!)" else ""
                bzMessage("Sell Order Claimed! §c${it.group(1)}x §c${clean(it.group(2))}§r for §6${total}§r coins! ${each}")
                true
            } == true -> event.isCanceled = true
        }
    }

    private fun clean(item: String) = item.replace("ENCHANTED_", "").replace("_", " ").trim()
    private fun bzMessage(message: String) = addMessage("§6[BZ] §r$message")
}