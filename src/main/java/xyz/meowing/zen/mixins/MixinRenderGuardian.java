package xyz.meowing.zen.mixins;

import xyz.meowing.zen.events.EventBus;
import xyz.meowing.zen.events.RenderEvent;
import net.minecraft.client.renderer.entity.RenderGuardian;
import net.minecraft.entity.monster.EntityGuardian;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGuardian.class)
public class MixinRenderGuardian {
    @Inject(method = "doRender(Lnet/minecraft/entity/monster/EntityGuardian;DDDFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Tessellator;getInstance()Lnet/minecraft/client/renderer/Tessellator;"), cancellable = true)
    private void zen$guardianLaserRender(EntityGuardian entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        if (EventBus.INSTANCE.post(new RenderEvent.GuardianLaser(entity, entity.getTargetedEntity()))) ci.cancel();
    }
}