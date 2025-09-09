package meowing.zen.mixins;

import meowing.zen.features.qol.HideFallingBlocks;
import net.minecraft.client.renderer.entity.RenderFallingBlock;
import net.minecraft.entity.item.EntityFallingBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderFallingBlock.class)
public class MixinRenderFallingBlock {
    @Inject(method = "doRender(Lnet/minecraft/entity/item/EntityFallingBlock;DDDFF)V", at = @At("HEAD"), cancellable = true)
    private void zen$onRenderFallingBlock(EntityFallingBlock entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        if (HideFallingBlocks.INSTANCE.isEnabled()) ci.cancel();
    }
}