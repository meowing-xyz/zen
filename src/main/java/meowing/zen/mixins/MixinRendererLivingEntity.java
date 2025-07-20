package meowing.zen.mixins;

import meowing.zen.events.EventBus;
import meowing.zen.events.RenderEvent;
import meowing.zen.feats.general.CustomTint;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static meowing.zen.Zen.config;

@Mixin(RendererLivingEntity.class)
public class MixinRendererLivingEntity<T extends EntityLivingBase> {
    @Shadow
    protected ModelBase mainModel;

    @Inject(method = "renderLayers", at = @At("RETURN"))
    private void zen$onRenderLayers(T entitylivingbaseIn, float p_177093_2_, float p_177093_3_, float partialTicks, float p_177093_5_, float p_177093_6_, float p_177093_7_, float p_177093_8_, CallbackInfo ci) {
        EventBus.INSTANCE.post(new RenderEvent.EntityModel(entitylivingbaseIn, mainModel, p_177093_2_, p_177093_3_, p_177093_5_, p_177093_6_, p_177093_7_, p_177093_8_));
    }

    /*
     * Modified code from Polyfrost's DamageTint
     * Under LGPL 3.0 License
     */
    @ModifyArg(method = "setBrightness", at = @At(value = "INVOKE", target = "Ljava/nio/FloatBuffer;put(F)Ljava/nio/FloatBuffer;", ordinal = 0))
    private float zen$getRedTint(float f) {
        return CustomTint.INSTANCE.isEnabled() ? config.getCustomtintcolor().getRed() / 255f : f;
    }

    @ModifyArg(method = "setBrightness", at = @At(value = "INVOKE", target = "Ljava/nio/FloatBuffer;put(F)Ljava/nio/FloatBuffer;", ordinal = 1))
    private float zen$getGreenTint(float f) {
        return CustomTint.INSTANCE.isEnabled() ? config.getCustomtintcolor().getGreen() / 255f : f;
    }

    @ModifyArg(method = "setBrightness", at = @At(value = "INVOKE", target = "Ljava/nio/FloatBuffer;put(F)Ljava/nio/FloatBuffer;", ordinal = 2))
    private float zen$getBlueTint(float f) {
        return CustomTint.INSTANCE.isEnabled() ? config.getCustomtintcolor().getBlue() / 255f : f;
    }

    @ModifyArg(method = "setBrightness", at = @At(value = "INVOKE", target = "Ljava/nio/FloatBuffer;put(F)Ljava/nio/FloatBuffer;", ordinal = 3))
    private float zen$getAlphaTint(float f) {
        return CustomTint.INSTANCE.isEnabled() ? config.getCustomtintcolor().getAlpha() / 255f : f;
    }
}