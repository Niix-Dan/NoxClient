package com.noxclient.mixin;


import com.noxclient.systems.modules.Modules;
import com.noxclient.systems.modules.misc.ShulkersSception;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBoxBlockEntity.class)
public class ShulkerBoxBlockEntityMixin {
    @Inject(method = "canInsert", at = @At("HEAD"), cancellable = true)
    private void canInsert(int slot, ItemStack stack, Direction dir, CallbackInfoReturnable<Boolean> cir) {
        if(Modules.get().isActive(ShulkersSception.class)) {
            cir.setReturnValue(true);
        } else {
            cir.setReturnValue(!(Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock));
        }
    }
}

