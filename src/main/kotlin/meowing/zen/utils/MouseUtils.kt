package meowing.zen.utils

import meowing.zen.Zen.Companion.mc
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Mouse

object MouseUtils {
    inline val sr get() = ScaledResolution(mc)

    inline val rawX: Int get() = Mouse.getX()
    inline val rawY: Int get() = mc.displayHeight - Mouse.getY() - 1

    inline val scaledX get() = (Mouse.getX() * sr.scaledWidth / mc.displayWidth).toFloat()
    inline val scaledY get() = (sr.scaledHeight - Mouse.getY() * sr.scaledHeight / mc.displayHeight).toFloat()
}