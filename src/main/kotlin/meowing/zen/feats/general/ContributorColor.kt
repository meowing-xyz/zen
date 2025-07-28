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
    private var contributorData: Map<String, ContributorInfo>? = null

    data class ContributorInfo(
        val displayName: String,
        val highlightColor: List<Int>
    )

    init {
        NetworkUtils.fetchJson<Map<String, Map<String, Any>>>(
            "https://raw.githubusercontent.com/kiwidotzip/zen-data/refs/heads/main/assets/ContributorColor.json",
            onSuccess = { data ->
                contributorData = data.mapValues { (_, info) ->
                    val colorList = (info["highlightColor"] as? List<*>)?.mapNotNull { it as? Int }
                    ContributorInfo(
                        displayName = info["displayName"] as? String ?: "",
                        highlightColor = if (colorList?.size == 4) colorList else listOf(0, 255, 255, 127)
                    )
                }
            },
            onError = {
                contributorData = mapOf(
                    "shikiimori" to ContributorInfo("§dKiwi§r", listOf(255, 0, 255, 127)),
                    "cheattriggers" to ContributorInfo("§cCheater§r", listOf(255, 0, 0, 127)),
                    "Aur0raDye" to ContributorInfo("§5Mango 6 7§r", listOf(170, 0, 170, 127)),
                    "Skyblock_Lobby" to ContributorInfo("§9Skyblock_Lobby§r", listOf(85, 85, 255, 127))
                )
            }
        )

        EventBus.register<RenderEvent.EntityModel> ({ event ->
            contributorData?.get(event.entity.name.removeFormatting())?.let { info ->
                val (r, g, b, a) = info.highlightColor
                OutlineUtils.outlineEntity(event, Color(r, g, b, a))
            }
        })
    }

    @JvmStatic
    fun replace(text: String?): String? {
        if (text == null || contributorData == null) return text

        return contributorData!!.entries.fold(text) { acc, (key, info) ->
            acc.replace(key, info.displayName)
        }
    }
}