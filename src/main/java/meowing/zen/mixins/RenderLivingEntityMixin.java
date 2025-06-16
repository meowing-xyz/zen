package meowing.zen.mixins;

import meowing.zen.events.RenderEntityModelEvent;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;
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
        RendererLivingEntity<?> renderer = (RendererLivingEntity<?>) (Object) this;
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
        MinecraftForge.EVENT_BUS.post(event);
    }
}
