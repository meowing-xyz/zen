package xyz.meowing.zen.config.ui.core

import gg.essential.elementa.UIComponent
import gg.essential.elementa.constraints.ConstraintType
import gg.essential.elementa.constraints.resolution.ConstraintVisitor
import gg.essential.elementa.font.FontProvider
import gg.essential.universal.UMatrixStack
import xyz.meowing.zen.utils.FontUtils
import xyz.meowing.zen.utils.Render2D
import java.awt.Color

object CustomFontProvider : FontProvider {
    private val fontRenderer = FontUtils.getFontRenderer()
    override var cachedValue: FontProvider = this
    override var constrainTo: UIComponent? = null
    override var recalculate: Boolean = false

    override fun getStringWidth(string: String, pointSize: Float): Float {
        return fontRenderer.getStringWidth(string).toFloat()
    }

    override fun getStringHeight(string: String, pointSize: Float): Float {
        return fontRenderer.FONT_HEIGHT.toFloat()
    }

    override fun getBaseLineHeight(): Float {
        return fontRenderer.FONT_HEIGHT.toFloat() * 0.8f
    }

    override fun getBelowLineHeight(): Float {
        return fontRenderer.FONT_HEIGHT.toFloat() * 0.2f
    }

    override fun getShadowHeight(): Float {
        return 1f
    }

    override fun visitImpl(visitor: ConstraintVisitor, type: ConstraintType) {}

    override fun drawString(matrixStack: UMatrixStack, string: String, color: Color, x: Float, y: Float, originalPointSize: Float, scale: Float, shadow: Boolean, shadowColor: Color?) {
        val colorInt = (0xFF shl 24) or (color.red shl 16) or (color.green shl 8) or color.blue
        Render2D.renderString(string, x, y, scale, colorInt)
    }
}