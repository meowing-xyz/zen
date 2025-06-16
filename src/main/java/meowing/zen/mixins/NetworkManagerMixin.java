package meowing.zen.mixins;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import meowing.zen.utils.TickScheduler;
import meowing.zen.feats.slayers.slayertimer;

@Mixin(NetworkManager.class)
public class NetworkManagerMixin {
    @Inject(method = "channelRead0*", at = @At("HEAD"))
    private void onReceivePacket(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof S32PacketConfirmTransaction)
            TickScheduler.INSTANCE.onServerTick();
        if (packet instanceof S1CPacketEntityMetadata)
            slayertimer.onEntityMetadataUpdate((S1CPacketEntityMetadata) packet);
    }
}