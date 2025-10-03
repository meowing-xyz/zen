package xyz.meowing.zen.mixins;

import io.netty.channel.ChannelHandlerContext;
import xyz.meowing.zen.events.EventBus;
import xyz.meowing.zen.events.PacketEvent;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {
    @Inject(method = "channelRead0*", at = @At("HEAD"), cancellable = true)
    private void zen$onReceivePacket(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        if (EventBus.INSTANCE.onPacketReceived(packet)) ci.cancel();
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void zen$onSentPacket(Packet<?> packet, CallbackInfo ci) {
        if (EventBus.INSTANCE.onPacketSent(packet)) ci.cancel();
    }

    @Inject(method = "channelRead0*", at = @At("TAIL"))
    private void zen$onReceivePacketPost(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        EventBus.INSTANCE.post(new PacketEvent.ReceivedPost(packet));
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("TAIL"))
    private void zen$onSentPacketPost(Packet<?> packet, CallbackInfo ci) {
        EventBus.INSTANCE.post(new PacketEvent.SentPost(packet));
    }
}