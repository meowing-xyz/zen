package meowing.zen.mixins;

import meowing.zen.events.EntityEvent;
import meowing.zen.events.EventBus;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.MovingObjectPosition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EntityArrow.class)
public class MixinEntityArrow {
    @Shadow public Entity shootingEntity;

    @Inject(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onArrowHitEntity(CallbackInfo ci, MovingObjectPosition movingObjectPosition) {
        if (movingObjectPosition != null && movingObjectPosition.entityHit != null && shootingEntity != null) {
            String shooterName = shootingEntity.getName();
            if (shooterName != null) {
                EventBus.INSTANCE.post(new EntityEvent.ArrowHit(shooterName, movingObjectPosition.entityHit));
            }
        }
    }
}