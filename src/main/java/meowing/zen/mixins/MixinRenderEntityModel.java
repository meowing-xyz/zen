package meowing.zen.mixins;

import meowing.zen.events.EventBus;
import meowing.zen.events.RenderEvent;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RendererLivingEntity.class)
public class MixinRenderEntityModel <T extends EntityLivingBase> {
    @Shadow
    protected ModelBase mainModel;

    @Inject(method = "renderLayers", at = @At("RETURN"))
    private void onRenderLayers(
            T entitylivingbaseIn,
            float p_177093_2_,
            float p_177093_3_,
            float partialTicks,
            float p_177093_5_,
            float p_177093_6_,
            float p_177093_7_,
            float p_177093_8_,
            CallbackInfo ci
    ) {
        EventBus.INSTANCE.post(
                new RenderEvent.EntityModel(
                    entitylivingbaseIn,
                    mainModel,
                    p_177093_2_,
                    p_177093_3_,
                    p_177093_5_,
                    p_177093_6_,
                    p_177093_7_,
                    p_177093_8_
                )
        );
    }
}