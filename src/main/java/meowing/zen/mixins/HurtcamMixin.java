package meowing.zen.mixins;

import meowing.zen.events.EventBus;
import meowing.zen.events.HurtCamEvent;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class HurtcamMixin {
    @Inject(method = "hurtCameraEffect(F)V", at = @At("HEAD"), cancellable = true)
    private void onHurtCameraEffect(float partialTicks, CallbackInfo ci) {
        HurtCamEvent event = new HurtCamEvent(partialTicks);
        EventBus.INSTANCE.post(event);
        if (event.isCancelled()) ci.cancel();
    }
}
