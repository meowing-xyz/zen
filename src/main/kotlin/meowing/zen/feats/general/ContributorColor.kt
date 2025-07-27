package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.events.EventBus
import meowing.zen.events.RenderEvent
import meowing.zen.utils.NetworkUtils
import meowing.zen.utils.OutlineUtils
import meowing.zen.utils.Utils.removeFormatting
import java.awt.Color

@Zen.Module
object ContributorColor {
    private var map: Map<String, String>? = null
    private val glowColor: Color = Color(0, 255, 255, 127)
    init {
        NetworkUtils.fetchJson<Map<String, String>>(
            "https://raw.githubusercontent.com/kiwidotzip/zen-data/refs/heads/main/assets/ContributorColor.json",
            onSuccess = {
                map = it
            },
            onError = {
                map = mapOf(
                    "shikiimori" to "§dKiwi§r",
                    "cheattriggers" to "§cCheater§r",
                    "Aur0raDye" to "§5Mango 6 7"
                )
            }
        )

        EventBus.register<RenderEvent.EntityModel> ({ event ->
            if (map?.containsKey(event.entity.name.removeFormatting()) == true) {
                OutlineUtils.outlineEntity(event, glowColor)
            }
        })
    }

    @JvmStatic
    fun replace(text: String?): String? {
        if (text == null || map == null) return text

        var newText = text
        map!!.entries.forEach { (key, value) ->
            newText = newText?.replace(key, value)
        }

        return newText
    }
}