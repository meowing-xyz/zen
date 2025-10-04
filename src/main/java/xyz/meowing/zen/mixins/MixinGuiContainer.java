package xyz.meowing.zen.mixins;

import xyz.meowing.zen.Zen;
import xyz.meowing.zen.events.EventBus;
import xyz.meowing.zen.events.GuiEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainer.class)
public class MixinGuiContainer {
    @Unique
    private static final Minecraft zen$mc = Minecraft.getMinecraft();
    @Shadow
    public Container inventorySlots;

    @Inject(method = "handleMouseClick(Lnet/minecraft/inventory/Slot;III)V", at = @At("HEAD"), cancellable = true)
    private void zen$onHandleMouseClick(Slot slot, int slotId, int clickedButton, int mode, CallbackInfo ci) {
        if (EventBus.INSTANCE.post(new GuiEvent.Slot.Click(slot, (GuiContainer) zen$mc.currentScreen, this.inventorySlots, slotId, clickedButton, mode))) {
            ci.cancel();
        }
    }

    @Inject(method = "mouseClicked(III)V", at = @At("HEAD"), cancellable = true)
    private void zen$onMousePressed(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        try {
            if (EventBus.INSTANCE.post(new GuiEvent.Mouse.Press(mouseX, mouseY, mouseButton, (GuiContainer) zen$mc.currentScreen))) {
                ci.cancel();
            }
        } catch (Exception e) {
            Zen.LOGGER.error("[Zen] Caught error in mouseClicked: {}", String.valueOf(e));
        }
    }

    @Inject(method = "mouseReleased(III)V", at = @At("HEAD"), cancellable = true)
    private void zen$onMouseReleased(int mouseX, int mouseY, int state, CallbackInfo ci) {
        try {
            if (EventBus.INSTANCE.post(new GuiEvent.Mouse.Release(mouseX, mouseY, Mouse.getEventButton(), (GuiContainer) zen$mc.currentScreen))) {
                ci.cancel();
            }
        } catch (Exception e) {
            Zen.LOGGER.error("[Zen] Caught error in mouseReleased: {}", String.valueOf(e));
        }
    }

    @Inject(method = "mouseClickMove(IIIJ)V", at = @At("HEAD"), cancellable = true)
    private void zen$onMouseMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick, CallbackInfo ci) {
        try {
            if (EventBus.INSTANCE.post(new GuiEvent.Mouse.Move(mouseX, mouseY, clickedMouseButton, (GuiContainer) zen$mc.currentScreen))) {
                ci.cancel();
            }
        } catch (Exception e) {
            Zen.LOGGER.error("[Zen] Caught error in mouseClickMove: {}", String.valueOf(e));
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
        if (zen$mc.currentScreen != null && this.inventorySlots != null) {
            if (EventBus.INSTANCE.post(new GuiEvent.Close((GuiContainer) zen$mc.currentScreen, this.inventorySlots))) {
                ci.cancel();
            }
        }
    }
}