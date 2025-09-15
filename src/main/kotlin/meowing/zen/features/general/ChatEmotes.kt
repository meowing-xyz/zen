package meowing.zen.features.general

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.features.Feature
import meowing.zen.utils.ChatUtils

@Zen.Module
object ChatEmotes : Feature("chatemotes") {
    val HYPIXEL_EMOTES = mapOf(
        "<3" to "❤",
        ":star:" to "✮",
        ":yes:" to "✔",
        ":no:" to "✖",
        ":java:" to "☕",
        ":arrow:" to "➜",
        ":shrug:" to "¯\\_(ツ)_/¯",
        ":tableflip:" to "(╯°□°）╯︵ ┻━┻",
        "o/" to "( ﾟ◡ﾟ)/",
        ":123:" to "123",
        ":totem:" to "◎_◎",
        ":typing:" to "✎...",
        ":maths:" to "√(π+x)=L",
        ":snail:" to "@'-'",
        ":thinking:" to "(0.o?)",
        ":gimme:" to "༼つ ◕_◕ ༽つ",
        ":wizard:" to "('-')⊃━☆ﾟ.*･｡ﾟ",
        ":pvp:" to "⚔",
        ":peace:" to "✌",
        ":oof:" to "OOF",
        ":puffer:" to "<('O')>",
        ":snow:" to "☃",
        ":dog:" to "(ᵔᴥᵔ)",
        ":sloth:" to "( ⬩ ⊝ ⬩ )",
        ":dab:" to "<o/",
        ":cat:" to "ᓚᘏᗢ",
    )

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Chat Emotes", ConfigElement(
                "chatemotes",
                "Automatically replace emote codes with Unicode symbols in chat messages",
                ElementType.Switch(true)
            ), true)
            .addElement("General", "Chat Emotes", "", ConfigElement(
                "",
                null,
                ElementType.TextParagraph("Automatically replace emote codes with Unicode symbols in chat messages, example: <3 becomes ❤, use /emotes to see all supported emotes.")
            ))

    }

    override fun initialize() {
        val emotePattern = HYPIXEL_EMOTES.keys
            .joinToString("|") { Regex.escape(it) }
            .toRegex()

        register<ChatEvent.Send> { event ->
            if (event.chatUtils) return@register

            val newMessage = emotePattern.replace(event.message) { matchResult ->
                HYPIXEL_EMOTES[matchResult.value] ?: matchResult.value
            }

            if (newMessage != event.message) {
                event.cancel()
                ChatUtils.chat(newMessage)
            }
        }
    }
}
