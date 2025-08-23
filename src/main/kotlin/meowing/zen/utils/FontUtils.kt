package meowing.zen.utils

import meowing.zen.Zen.Companion.mc
import net.minecraft.client.gui.FontRenderer
import net.minecraft.util.ResourceLocation

object FontUtils {
    private var minecraftSmoothFont = FontRenderer(
        mc.gameSettings,
        ResourceLocation("zen", "fonts/smooth_ascii.png"),
        mc.renderEngine,
        false
    )

    init {
        minecraftSmoothFont.onResourceManagerReload(mc.resourceManager)
    }

    fun getFontRenderer(): FontRenderer = minecraftSmoothFont
}