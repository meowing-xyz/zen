package meowing.zen.mixins;

import meowing.zen.events.EventBus;
import meowing.zen.events.GuiEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainer.class)
public class MixinGuiContainer {
    @Unique private static final Minecraft zen$mc = Minecraft.getMinecraft();

    @Inject(method = "handleMouseClick(Lnet/minecraft/inventory/Slot;III)V", at = @At("HEAD"), cancellable = true)
    private void onHandleMouseClick(Slot slot, int slotId, int clickedButton, int mode, CallbackInfo ci) {
        if (slot != null && zen$mc.currentScreen != null) {
            if (EventBus.INSTANCE.post(new GuiEvent.SlotClick(slot, (GuiContainer) zen$mc.currentScreen))) {
                ci.cancel();
            }
        }
    }
}
