package meowing.zen.mixins;

import meowing.zen.events.*;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class NetworkManagerMixin {
    @Inject(method = "channelRead0*", at = @At("HEAD"))
    private void onReceivePacket(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof S32PacketConfirmTransaction && ((S32PacketConfirmTransaction) packet).getActionNumber() <= 0 && !((S32PacketConfirmTransaction) packet).func_148888_e()) {
            MinecraftForge.EVENT_BUS.post(new ServerTickEvent());
            return;
        }

        if (packet instanceof S1CPacketEntityMetadata) {
            MinecraftForge.EVENT_BUS.post(new EntityMetadataUpdateEvent((S1CPacketEntityMetadata) packet));
            return;
        }

        if (packet instanceof S02PacketChat) {
            MinecraftForge.EVENT_BUS.post(new ChatReceiveEvent((S02PacketChat) packet));
            return;
        }

        if (packet instanceof S3EPacketTeams || packet instanceof S3CPacketUpdateScore || packet instanceof S3DPacketDisplayScoreboard) {
            MinecraftForge.EVENT_BUS.post(new ScoreboardEvent(packet));
            return;
        }

        if (packet instanceof S38PacketPlayerListItem && (((S38PacketPlayerListItem) packet).getAction() == S38PacketPlayerListItem.Action.UPDATE_DISPLAY_NAME || ((S38PacketPlayerListItem) packet).getAction() == S38PacketPlayerListItem.Action.ADD_PLAYER)) {
            MinecraftForge.EVENT_BUS.post(new TablistEvent((S38PacketPlayerListItem) packet));
        }
    }
}