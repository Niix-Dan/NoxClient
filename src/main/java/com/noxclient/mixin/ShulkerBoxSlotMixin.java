package com.noxclient.mixin;

import com.noxclient.systems.modules.Modules;
import com.noxclient.systems.modules.misc.ShulkersSception;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.ShulkerBoxSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBoxSlot.class)
public class ShulkerBoxSlotMixin {
    @Inject(method = "canInsert", at = @At("HEAD"), cancellable = true)
    private void isItemValid(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if(Modules.get().isActive(ShulkersSception.class)) {
            cir.setReturnValue(true);
        } else {
            cir.setReturnValue(stack.getItem().canBeNested());
        }
    }
}
