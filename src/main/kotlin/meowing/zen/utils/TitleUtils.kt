package meowing.zen.utils

import meowing.zen.Zen.Companion.mc
import meowing.zen.events.EventBus
import meowing.zen.events.RenderEvent
import meowing.zen.utils.TimeUtils.millis
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import java.util.*

object TitleUtils {
    private data class TitleData(
        val title: String?,
        val subtitle: String?,
        val fadeIn: Int,
        val stay: Int,
        val fadeOut: Int,
        val scale: Float = 4.0f
    )

    private val titleQueue = LinkedList<TitleData>()
    private val fontObj = FontUtils.getFontRenderer()
    private var currentTitle: TitleData? = null
    private var startTime = TimeUtils.zero

    init {
        EventBus.register<RenderEvent.Text> ({ render() })
    }

    fun showTitle(title: String?, subtitle: String?, duration: Int, scale: Float = 4.0f) {
        titleQueue.offer(TitleData(title, subtitle, 200, duration, 200, scale))
        if (currentTitle == null) nextTitle()
    }

    fun showTitle(title: String?, subtitle: String?, fadeIn: Int, stay: Int, fadeOut: Int, scale: Float = 4.0f) {
        titleQueue.offer(TitleData(title, subtitle, fadeIn, stay, fadeOut, scale))
        if (currentTitle == null) nextTitle()
    }

    private fun nextTitle() {
        currentTitle = titleQueue.poll()
        startTime = TimeUtils.now
    }

    private fun render() {
        val title = currentTitle ?: return
        val elapsed = startTime.since.millis.toInt()
        val totalDuration = title.fadeIn + title.stay + title.fadeOut

        if (elapsed >= totalDuration) {
            nextTitle()
            return
        }

        val alpha = when {
            elapsed < title.fadeIn -> elapsed.toFloat() / title.fadeIn
            elapsed < title.fadeIn + title.stay -> 1.0f
            else -> 1.0f - ((elapsed - title.fadeIn - title.stay).toFloat() / title.fadeOut)
        }

        val scale = title.scale * (0.8f + 0.2f * alpha)
        val scaledResolution = ScaledResolution(mc)
        val centerX = scaledResolution.scaledWidth / 2f
        val centerY = scaledResolution.scaledHeight / 2f

        GlStateManager.pushMatrix()
        GlStateManager.color(1.0f, 1.0f, 1.0f, alpha)

        val hasTitle = title.title != null
        val hasSubtitle = title.subtitle != null

        when {
            hasTitle && hasSubtitle -> {
                val titleWidth = fontObj.getStringWidth(title.title) * scale
                val titleX = centerX - titleWidth / 2
                val titleY = centerY - (fontObj.FONT_HEIGHT * scale) / 2 - 2 * scale
                Render2D.renderString(title.title, titleX, titleY, scale)

                val subScale = scale * 0.7f
                val subtitleWidth = fontObj.getStringWidth(title.subtitle) * subScale
                val subtitleX = centerX - subtitleWidth / 2
                val subtitleY = centerY + (fontObj.FONT_HEIGHT * subScale) / 2 + 2 * scale
                Render2D.renderString(title.subtitle, subtitleX, subtitleY, subScale)
            }
            hasTitle -> {
                val titleWidth = fontObj.getStringWidth(title.title) * scale
                val titleX = centerX - titleWidth / 2
                val titleY = centerY - (fontObj.FONT_HEIGHT * scale) / 2
                Render2D.renderString(title.title, titleX, titleY, scale)
            }
            hasSubtitle -> {
                val subScale = scale * 0.7f
                val subtitleWidth = fontObj.getStringWidth(title.subtitle) * subScale
                val subtitleX = centerX - subtitleWidth / 2
                val subtitleY = centerY - (fontObj.FONT_HEIGHT * subScale) / 2
                Render2D.renderString(title.subtitle, subtitleX, subtitleY, subScale)
            }
        }

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        GlStateManager.popMatrix()
    }
}