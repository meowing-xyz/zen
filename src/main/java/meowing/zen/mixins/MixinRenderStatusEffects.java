package meowing.zen.mixins;

import meowing.zen.feats.noclutter.hidestatuseffects;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({InventoryEffectRenderer.class})
public class MixinRenderStatusEffects {
    @ModifyVariable(method = "updateActivePotionEffects", at = @At(value = "STORE"))
    public boolean showEffects(boolean hasVisibleEffect) {
        return !hidestatuseffects.INSTANCE.isEnabled();
    }
}
