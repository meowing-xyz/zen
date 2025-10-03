package xyz.meowing.zen.mixins;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2023 Skytils
 */
@Mixin(GuiNewChat.class)
public interface AccessorGuiNewChat {
    @Accessor
    List<ChatLine> getChatLines();

    @Accessor
    List<ChatLine> getDrawnChatLines();

    @Accessor
    int getScrollPos();

    @Invoker
    void invokeSetChatLine(IChatComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly);
}
