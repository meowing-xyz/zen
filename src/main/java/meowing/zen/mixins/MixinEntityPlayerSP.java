package meowing.zen.mixins;

import kotlin.Unit;
import meowing.zen.events.EntityEvent;
import meowing.zen.events.EventBus;
import meowing.zen.events.PacketEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP {
    @Shadow
    protected Minecraft mc;

    @Unique
    private static Integer zen$currentItem = null;

    static {
        EventBus.registerJava(PacketEvent.Received.class, 0, true, event -> {
            if (event.getPacket() instanceof S09PacketHeldItemChange) {
                zen$currentItem = ((S09PacketHeldItemChange) event.getPacket()).getHeldItemHotbarIndex();
            }
            return Unit.INSTANCE;
        });

        EventBus.registerJava(PacketEvent.Sent.class, 0, true, event -> {
            if (event.getPacket() instanceof C09PacketHeldItemChange) {
                zen$currentItem = ((C09PacketHeldItemChange) event.getPacket()).getSlotId();
            }
            return Unit.INSTANCE;
        });
    }

    @Inject(method = "dropOneItem", at = @At("HEAD"), cancellable = true)
    private void onDropItem(boolean dropAll, CallbackInfoReturnable<EntityItem> cir) {
        int slotIndex = (zen$currentItem != null) ? zen$currentItem : mc.thePlayer.inventory.currentItem;
        ItemStack stack = mc.thePlayer.inventory.mainInventory[slotIndex];

        if (stack != null && EventBus.INSTANCE.post(new EntityEvent.ItemToss(stack))) cir.setReturnValue(null);
    }
}