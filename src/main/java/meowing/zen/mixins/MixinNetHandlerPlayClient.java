package meowing.zen.mixins;

import meowing.zen.events.EntityEvent;
import meowing.zen.events.EventBus;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.server.S0FPacketSpawnMob;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {
    @Inject(method = "handleEntityMetadata", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/DataWatcher;updateWatchedObjectsFromList(Ljava/util/List;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void zen$handleEntityMetadata(S1CPacketEntityMetadata packet, CallbackInfo ci, Entity entity) {
        EventBus.INSTANCE.post(new EntityEvent.Metadata(packet, entity));
    }

    @SuppressWarnings("InvalidInjectorMethodSignature") // This does work just fine idk vor
    @Inject(method = "handleSpawnMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;addEntityToWorld(ILnet/minecraft/entity/Entity;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void zen$handleSpawnMob(S0FPacketSpawnMob packet, CallbackInfo ci, double d0, double d1, double d2, float f, float f1, EntityLivingBase entitylivingbase, Entity[] aentity) {
        EventBus.INSTANCE.post(new EntityEvent.Spawn(packet, entitylivingbase));
    }
}