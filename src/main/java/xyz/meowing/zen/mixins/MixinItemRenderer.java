package xyz.meowing.zen.mixins;

import xyz.meowing.zen.features.visuals.ItemAnimations;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Aton
 */
@Mixin(value = ItemRenderer.class, priority = 3499)
public class MixinItemRenderer {
    @Shadow
    private ItemStack itemToRender;

    @Inject(method = "transformFirstPersonItem(FF)V", at = @At("HEAD"), cancellable = true)
    public void zen$itemTransform(float equipProgress, float swingProgress, CallbackInfo ci) {
        if (ItemAnimations.INSTANCE.itemTransforHook(equipProgress, swingProgress)) ci.cancel();
    }

    @Inject(method = "doItemUsedTransformations", at = @At("HEAD"), cancellable = true)
    public void zen$useTransform(float swingProgress, CallbackInfo ci) {
        if (ItemAnimations.INSTANCE.scaledSwing(swingProgress)) ci.cancel();
    }

    @Inject(method = "performDrinking", at = @At("HEAD"), cancellable = true)
    public void zen$drinkTransform(AbstractClientPlayer clientPlayer, float partialTicks, CallbackInfo ci) {
        if (ItemAnimations.INSTANCE.rotationlessDrink(clientPlayer, partialTicks)) ci.cancel();
        if (ItemAnimations.INSTANCE.scaledDrinking(clientPlayer, partialTicks, itemToRender)) ci.cancel();
    }
}
