package meowing.zen.mixins;

import meowing.zen.Zen;
import meowing.zen.events.EventBus;
import meowing.zen.events.InternalEvent;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiScreen.class)
public class MixinGuiScreen {
    @Unique private int zen$lastMouseX = -1;
    @Unique private int zen$lastMouseY = -1;

    @Inject(method = "mouseClicked(III)V", at = @At("HEAD"), cancellable = true)
    private void zen$onMouseClick(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        try {
            if (EventBus.INSTANCE.post(new InternalEvent.GuiMouse.Click(mouseX, mouseY, mouseButton))) {
                ci.cancel();
            }
        } catch (Exception e) {
            Zen.LOGGER.error("[Zen] Caught error in mouseClicked: {}", String.valueOf(e));
        }
    }

    @Inject(method = "mouseReleased(III)V", at = @At("HEAD"), cancellable = true)
    private void zen$onMouseRelease(int mouseX, int mouseY, int state, CallbackInfo ci) {
        try {
            if (EventBus.INSTANCE.post(new InternalEvent.GuiMouse.Release(mouseX, mouseY, Mouse.getEventButton()))) {
                ci.cancel();
            }
        } catch (Exception e) {
            Zen.LOGGER.error("[Zen] Caught error in mouseReleased: {}", String.valueOf(e));
        }
    }

    @Inject(method = "handleMouseInput()V", at = @At("HEAD"))
    private void zen$onMouseInput(CallbackInfo ci) {
        try {
            int dWheel = Mouse.getEventDWheel();
            if (dWheel != 0) {
                double horizontal = 0.0;
                double vertical = dWheel > 0 ? 1.0 : -1.0;
                EventBus.INSTANCE.post(new InternalEvent.GuiMouse.Scroll(horizontal, vertical));
            }
        } catch (Exception e) {
            Zen.LOGGER.error("[Zen] Caught error in handleMouseInput: {}", String.valueOf(e));
        }
    }

    @Inject(method = "drawScreen(IIF)V", at = @At("HEAD"))
    private void zen$onDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        try {
            if (mouseX != zen$lastMouseX || mouseY != zen$lastMouseY) {
                EventBus.INSTANCE.post(new InternalEvent.GuiMouse.Move(mouseX, mouseY));
                zen$lastMouseX = mouseX;
                zen$lastMouseY = mouseY;
            }
        } catch (Exception e) {
            Zen.LOGGER.error("[Zen] Caught error in drawScreen: {}", String.valueOf(e));
        }
    }

    @Inject(method = "keyTyped(CI)V", at = @At("HEAD"), cancellable = true)
    private void zen$onKeyTyped(char typedChar, int scanCode, CallbackInfo ci) {
        try {
            String keyName = org.lwjgl.input.Keyboard.getKeyName(scanCode);
            if (EventBus.INSTANCE.post(new InternalEvent.GuiKey(keyName, scanCode, typedChar, scanCode))) {
                ci.cancel();
            }
        } catch (Exception e) {
            Zen.LOGGER.error("[Zen] Caught error in keyTyped: {}", String.valueOf(e));
        }
    }
}