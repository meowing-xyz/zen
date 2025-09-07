package meowing.zen.mixins;

import meowing.zen.features.visuals.ItemAnimations;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Aton
 */
@Mixin(Item.class)
public class MixinItem {
    @Inject(method = "shouldCauseReequipAnimation", at = @At("HEAD"), cancellable = true, remap = false)
    public void zen$overrideReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged, CallbackInfoReturnable<Boolean> ci) {
        if (ItemAnimations.INSTANCE.getCancelReEquip()) {
            if (slotChanged && ItemAnimations.INSTANCE.getShowReEquipWhenSlotsChange()) return;
            ci.setReturnValue(false);
        }
    }
}
