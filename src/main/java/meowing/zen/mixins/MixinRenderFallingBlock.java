package meowing.zen.mixins;

import meowing.zen.events.EventBus;
import meowing.zen.events.RenderEvent;
import net.minecraft.client.renderer.entity.RenderFallingBlock;
import net.minecraft.entity.item.EntityFallingBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderFallingBlock.class)
public class MixinRenderFallingBlock {
    @Inject(method = "doRender(Lnet/minecraft/entity/item/EntityFallingBlock;DDDFF)V", at = @At("HEAD"), cancellable = true)
    private void onRenderFallingBlock(EntityFallingBlock entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        RenderEvent.FallingBlock event = new RenderEvent.FallingBlock(entity, x, y, z, entityYaw, partialTicks);
        EventBus.INSTANCE.post(event);
        if (event.isCancelled()) ci.cancel();
    }
}