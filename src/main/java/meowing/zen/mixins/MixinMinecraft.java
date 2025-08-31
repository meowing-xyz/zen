package meowing.zen.mixins;

import meowing.zen.events.EventBus;
import meowing.zen.events.KeyEvent;
import meowing.zen.features.general.RemoveSelfieCam;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Shadow public GameSettings gameSettings;

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;dispatchKeypresses()V"), cancellable = true)
    public void zen$keyPress(CallbackInfo ci) {
        int key = (Keyboard.getEventKey() == 0) ? (Keyboard.getEventCharacter() + 256) : Keyboard.getEventKey();
        if (Keyboard.getEventKeyState()) {
            if (EventBus.INSTANCE.post(new KeyEvent.Press(key))) ci.cancel();
        } else {
            if (EventBus.INSTANCE.post(new KeyEvent.Release(key))) ci.cancel();
        }

        if (RemoveSelfieCam.INSTANCE.isEnabled() && this.gameSettings.keyBindTogglePerspective.isPressed()) {
            this.gameSettings.thirdPersonView = (this.gameSettings.thirdPersonView == 0) ? 1 : 0;
        }
    }
}