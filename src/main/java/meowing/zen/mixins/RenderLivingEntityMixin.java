package meowing.zen.mixins;

import meowing.zen.events.EventBus;
import meowing.zen.events.RenderEntityModelEvent;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RendererLivingEntity.class)
public class RenderLivingEntityMixin {
    @Inject(method = "renderModel", at = @At("HEAD"))
    private void onRenderModel(
            EntityLivingBase entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float headYaw,
            float headPitch,
            float scaleFactor,
            CallbackInfo ci
    ) {
        @SuppressWarnings("unchecked")
        RendererLivingEntity<EntityLivingBase> renderer = (RendererLivingEntity<EntityLivingBase>) (Object) this;
        ModelBase model = renderer.getMainModel();
        RenderEntityModelEvent event = new RenderEntityModelEvent(
                entity,
                model,
                limbSwing,
                limbSwingAmount,
                ageInTicks,
                headYaw,
                headPitch,
                scaleFactor
        );
        EventBus.INSTANCE.post(event);
    }
}