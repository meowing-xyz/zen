package meowing.zen.mixins;

import meowing.zen.feats.noclutter.nothunder;
import net.minecraft.client.renderer.entity.RenderLightningBolt;
import net.minecraft.entity.effect.EntityLightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderLightningBolt.class)
public class MixinRenderLightningBolt {
    @Inject(method = "doRender(Lnet/minecraft/entity/effect/EntityLightningBolt;DDDFF)V", at = @At("HEAD"), cancellable = true)
    private void zen$cancelLightningRender(EntityLightningBolt entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        if (nothunder.INSTANCE.isEnabled()) ci.cancel();
    }
}