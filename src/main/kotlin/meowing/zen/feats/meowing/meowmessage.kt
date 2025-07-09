package meowing.zen.feats.meowing

import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import kotlin.random.Random

object meowmessage : Feature("meowmessage") {
    private val variants = listOf("meow", "mew", "mrow", "nyaa", "purr", "mrrp", "meoww", "nya")
    private var isTransforming = false

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI.addElement("Meowing", "Meow chat", ConfigElement(
            "meowmessage",
            "Meow Translator",
            "Adds a TON of meows to your messages.",
            ElementType.Switch(false)
        ))
    }

    override fun initialize() {
        register<ChatEvent.Send> { event ->
            if (isTransforming) return@register
            event.cancel()
            isTransforming = true
            try {
                if (event.message.startsWith("/")) {
                    val parts = event.message.split(" ")
                    if (parts.size > 1) ChatUtils.chat("${parts[0]} ${transform(parts.drop(1).joinToString(" "))}")
                    else ChatUtils.chat(event.message)
                } else {
                    ChatUtils.chat(transform(event.message))
                }
            } finally {
                isTransforming = false
            }
        }
    }

    private fun transform(message: String): String {
        val words = message.split(" ")
        val result = mutableListOf<String>()
        for (word in words) {
            result.add(word)
            if (!word.startsWith("/") && Random.nextBoolean()) {
                result.add(variants.random())
            }
        }
        return result.joinToString(" ")
    }
}