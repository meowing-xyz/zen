package meowing.zen.mixins;

import meowing.zen.feats.general.removechatlimit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant;
import net.minecraft.client.gui.GuiNewChat;

@Mixin(GuiNewChat.class)
public class MixinGuiNewChat {
    @ModifyConstant(method = "setChatLine", constant = @Constant(intValue = 100))
    private int zen$removeMessageLimit(int original) {
        return removechatlimit.INSTANCE.isEnabled() ? Integer.MAX_VALUE : original;
    }
}