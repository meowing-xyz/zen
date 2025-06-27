package meowing.zen.mixins;

import meowing.zen.events.EventBus;
import meowing.zen.events.RenderEntityModelEvent;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RendererLivingEntity.class)
public class RenderLivingEntityMixin {
    @Shadow
    protected ModelBase mainModel;

    @Inject(method = "renderLayers", at = @At("RETURN"))
    private void onRenderLayers(
            EntityLivingBase entitylivingbaseIn,
            float p_177093_2_,
            float p_177093_3_,
            float partialTicks,
            float p_177093_5_,
            float p_177093_6_,
            float p_177093_7_,
            float p_177093_8_,
            CallbackInfo ci
    ) {
        RenderEntityModelEvent event = new RenderEntityModelEvent(
                entitylivingbaseIn,
                mainModel,
                p_177093_2_,
                p_177093_3_,
                p_177093_5_,
                p_177093_6_,
                p_177093_7_,
                p_177093_8_
        );
        EventBus.INSTANCE.post(event);
    }
}