package meowing.zen.utils

import meowing.zen.Zen.Companion.mc
import meowing.zen.events.EventBus
import meowing.zen.events.RenderEvent
import net.minecraft.client.renderer.GlStateManager
import java.util.*

object TitleUtils {
    private data class TitleData(
        val title: String?,
        val subtitle: String?,
        val fadeIn: Int,
        val stay: Int,
        val fadeOut: Int,
        val scale: Float = 2.0f
    )

    private val titleQueue = LinkedList<TitleData>()
    private var currentTitle: TitleData? = null
    private var currentTime = 0
    private var totalDuration = 0

    init {
        EventBus.register<RenderEvent.HUD> ({ render() })
    }

    fun showTitle(title: String?, subtitle: String?, duration: Int, scale: Float = 2.0f) {
        titleQueue.offer(TitleData(title, subtitle, 500, duration, 500, scale))
        if (currentTitle == null) nextTitle()
    }

    fun showTitle(title: String?, subtitle: String?, fadeIn: Int, stay: Int, fadeOut: Int, scale: Float = 2.0f) {
        titleQueue.offer(TitleData(title, subtitle, fadeIn, stay, fadeOut, scale))
        if (currentTitle == null) nextTitle()
    }

    private fun nextTitle() {
        currentTitle = titleQueue.poll()
        currentTime = 0
        totalDuration = currentTitle?.let { it.fadeIn + it.stay + it.fadeOut } ?: 0
    }

    private fun render() {
        val title = currentTitle ?: return

        if (currentTime >= totalDuration) {
            nextTitle()
            return
        }

        val alpha = when {
            currentTime < title.fadeIn -> currentTime.toFloat() / title.fadeIn
            currentTime < title.fadeIn + title.stay -> 1.0f
            else -> 1.0f - ((currentTime - title.fadeIn - title.stay).toFloat() / title.fadeOut)
        }

        val scale = title.scale * (0.8f + 0.2f * alpha)
        val centerX = mc.displayWidth / 4f
        val centerY = mc.displayHeight / 4f

        GlStateManager.color(1.0f, 1.0f, 1.0f, alpha)

        val hasTitle = title.title != null
        val hasSubtitle = title.subtitle != null

        when {
            hasTitle && hasSubtitle -> {
                val titleWidth = mc.fontRendererObj.getStringWidth(title.title) * scale
                val titleX = centerX - titleWidth / 2
                val titleY = centerY - (mc.fontRendererObj.FONT_HEIGHT * scale) / 2 - 2 * scale
                Render2D.renderStringWithShadow(title.title, titleX, titleY, scale)

                val subScale = scale * 0.7f
                val subtitleWidth = mc.fontRendererObj.getStringWidth(title.subtitle) * subScale
                val subtitleX = centerX - subtitleWidth / 2
                val subtitleY = centerY + (mc.fontRendererObj.FONT_HEIGHT * subScale) / 2 + 2 * scale
                Render2D.renderStringWithShadow(title.subtitle, subtitleX, subtitleY, subScale)
            }
            hasTitle -> {
                val titleWidth = mc.fontRendererObj.getStringWidth(title.title) * scale
                val titleX = centerX - titleWidth / 2
                val titleY = centerY - (mc.fontRendererObj.FONT_HEIGHT * scale) / 2
                Render2D.renderStringWithShadow(title.title, titleX, titleY, scale)
            }
            hasSubtitle -> {
                val subScale = scale * 0.7f
                val subtitleWidth = mc.fontRendererObj.getStringWidth(title.subtitle) * subScale
                val subtitleX = centerX - subtitleWidth / 2
                val subtitleY = centerY - (mc.fontRendererObj.FONT_HEIGHT * subScale) / 2
                Render2D.renderStringWithShadow(title.subtitle, subtitleX, subtitleY, subScale)
            }
        }

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        currentTime++
    }
}