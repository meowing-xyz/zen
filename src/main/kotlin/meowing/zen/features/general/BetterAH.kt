package meowing.zen.features.general

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.features.Feature
import meowing.zen.utils.ChatUtils.addMessage
import meowing.zen.utils.ChatUtils.formatNumber
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.Minecraft
import java.util.regex.Pattern

@Zen.Module
object BetterAH : Feature("betterah") {
    private val patterns = mapOf(
        "separator" to Pattern.compile("§b-----------------------------------------------------"),
        "purchased" to Pattern.compile("You purchased (.+) for ([\\d,]+) coins!"),
        "sold" to Pattern.compile("\\[Auction] (.+) bought (.+) for ([\\d,]+) coins CLICK"),
        "binStarted" to Pattern.compile("BIN Auction started for (.+)!"),
        "collected" to Pattern.compile("You collected ([\\d,]+) coins from selling (.+) to (?:\\[.+] )?(.+) in an auction!"),
        "auctionStarted" to Pattern.compile("Auction started for (.+)!"),
        "auctionCancelled" to Pattern.compile("You canceled your auction for (.+)!"),
        "playerCollected" to Pattern.compile("\\[.+] .+ collected an auction for [\\d,]+ coins!")
    )
    private val playerName = Minecraft.getMinecraft().thePlayer?.name

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Clean Chat", "Better Messages", ConfigElement(
                "betteralert",
                "Alert",
                ElementType.TextParagraph("BetterAH and BetterBZ have been deprecated and may soon be removed.")
            ))
            .addElement("General", "Clean Chat", "Better Messages", ConfigElement(
                "betterah",
                "Better Auction house",
                ElementType.Switch(false)
            ))
    }
    
    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val text = event.event.message.unformattedText.removeFormatting()

            when {
                patterns["separator"]?.matcher(event.event.message.unformattedText)?.matches() == true -> {
                    event.cancel()
                }

                patterns["purchased"]?.matcher(text)?.takeIf { it.matches() }?.let {
                    ahMessage("Bought §c${it.group(1)} §rfor §6${formatNumber(it.group(2))}§r coins!")
                    true
                } == true -> event.cancel()

                patterns["sold"]?.matcher(text)?.takeIf { it.matches() }?.let {
                    ahMessage("§a${it.group(1)} §rbought §c${it.group(2)} §rfor §6${formatNumber(it.group(3))}§r coins!")
                    true
                } == true -> event.cancel()

                patterns["binStarted"]?.matcher(text)?.takeIf { it.matches() }?.let {
                    ahMessage("§a§lBIN Started!§r §a${playerName}§r is selling §c${it.group(1)}§r!")
                    true
                } == true -> event.cancel()

                patterns["collected"]?.matcher(text)?.takeIf { it.matches() }?.let {
                    ahMessage("Collected §6${formatNumber(it.group(1))}§r coins from §c${it.group(2)} §rto §a${it.group(3)}§r!")
                    true
                } == true -> event.cancel()

                patterns["auctionStarted"]?.matcher(text)?.takeIf { it.matches() }?.let {
                    ahMessage("§a§lAUCTION STARTED!§r §a${playerName}§r started auction for §c${it.group(1)}§r!")
                    true
                } == true -> event.cancel()

                patterns["auctionCancelled"]?.matcher(text)?.takeIf { it.matches() }?.let {
                    ahMessage("§c§lAUCTION CANCELLED!§r §a${playerName}§r cancelled auction for §c${it.group(1)}§r!")
                    true
                } == true -> event.cancel()

                patterns["playerCollected"]?.matcher(text)?.matches() == true -> {
                    event.cancel()
                }
            }
        }
    }

    private fun ahMessage(message: String) = addMessage("§6[AH] §r$message")
}