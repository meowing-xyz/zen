package meowing.zen.mixins;

import meowing.zen.Zen;
import meowing.zen.events.EventBus;
import meowing.zen.events.GuiEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainer.class)
public class MixinGuiContainer {
    @Unique private static final Minecraft zen$mc = Minecraft.getMinecraft();
    @Shadow public Container inventorySlots;

    @Inject(method = "handleMouseClick(Lnet/minecraft/inventory/Slot;III)V", at = @At("HEAD"), cancellable = true)
    private void zen$onHandleMouseClick(Slot slot, int slotId, int clickedButton, int mode, CallbackInfo ci) {
        if (EventBus.INSTANCE.post(new GuiEvent.Slot.Click(slot, (GuiContainer) zen$mc.currentScreen, this.inventorySlots, slotId, clickedButton, mode))) {
            ci.cancel();
        }
    }

    @Inject(method = "drawSlot", at = @At("HEAD"))
    private void zen$preSlotDrawnEvent(Slot slotIn, CallbackInfo ci) {
        try {
            if (zen$mc.currentScreen != null) {
                EventBus.INSTANCE.post(new GuiEvent.Slot.RenderPre(slotIn, (GuiContainer) zen$mc.currentScreen));
            }
        } catch (Exception e) {
            Zen.LOGGER.error("[Zen] Caught error in drawSlot$pre: {}", String.valueOf(e));
        }
    }

    @Inject(method = "drawSlot", at = @At("TAIL"))
    private void zen$postSlotDrawnEvent(Slot slotIn, CallbackInfo ci) {
        try {
            if (zen$mc.currentScreen != null) {
                GlStateManager.translate(0, 0, 275f);
                EventBus.INSTANCE.post(new GuiEvent.Slot.RenderPost(slotIn, (GuiContainer) zen$mc.currentScreen));
                GlStateManager.translate(0, 0, -275f);
            }
        } catch (Exception e) {
            Zen.LOGGER.error("[Zen] Caught error in drawSlot$post: {}", String.valueOf(e));
        }
    }

    @Inject(method = "keyTyped", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;closeScreen()V", shift = At.Shift.BEFORE), cancellable = true)
    private void zen$onCloseWindow(CallbackInfo ci) {
        if (EventBus.INSTANCE.post(new GuiEvent.Close((GuiContainer) zen$mc.currentScreen, this.inventorySlots))) {
            ci.cancel();
        }
    }
}
