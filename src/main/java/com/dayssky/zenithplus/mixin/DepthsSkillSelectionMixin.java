package com.dayssky.zenithplus.mixin;

import com.dayssky.zenithplus.clear.SidearmHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

@Mixin(AbstractContainerScreen.class)
public class DepthsSkillSelectionMixin {
    @Inject(method = "slotClicked", at = @At("HEAD"))
    private void onSlotClicked(Slot slot, int slotId, int button, ClickType type, CallbackInfo ci) {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        String title = screen.getTitle().getString();
        if (!"Select an Ability".equals(title) && !"Select an Upgrade".equals(title)) return;
        if (slot == null || !slot.hasItem()) return;

        SidearmHelper.handleSkillSelectionClick(slot.getItem());
    }
}
