package meowing.zen.mixins;

import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static meowing.zen.features.general.ContributorColor.replace;

/*
 * Modified from NoammAddons code
 * Under GPL 3.0 License
 */
@Mixin(value = FontRenderer.class)
public class MixinFontRenderer {
    @ModifyVariable(method = "renderStringAtPos", at = @At("HEAD"), argsOnly = true)
    private String zen$modifyRenderStringAtPos(String text) {
        return replace(text);
    }

    @ModifyVariable(method = "getStringWidth", at = @At(value = "HEAD"), argsOnly = true)
    private String zen$modifyGetStringWidth(String text) {
        return replace(text);
    }
}