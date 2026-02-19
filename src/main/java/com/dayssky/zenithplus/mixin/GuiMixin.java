package com.dayssky.zenithplus.mixin;

import com.dayssky.zenithplus.utils.ChatUtils;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "setTitle", at = @At("HEAD"))
    private void onTitle(Component title, CallbackInfo ci) {
        ChatUtils.handleTitle(title);
    }

    @Inject(method = "setSubtitle", at = @At("HEAD"))
    private void onSubtitle(Component subtitle, CallbackInfo ci) {
        ChatUtils.handleSubtitle(subtitle);
    }
}
