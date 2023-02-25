/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.mixin;

import com.noxclient.systems.modules.player.IgnoreBorder;
import com.noxclient.systems.modules.Modules;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldBorder.class)
public abstract class WorldBorderMixin {
    @Inject(method = "canCollide", at = @At("HEAD"), cancellable = true)
    private void canCollide(Entity entity, Box box, CallbackInfoReturnable<Boolean> infoR) {
        if (Modules.get().isActive(IgnoreBorder.class)) {
            infoR.setReturnValue(false);
        }
    }
    @Inject(method = "contains(Lnet/minecraft/util/math/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
    private void contains(BlockPos pos, CallbackInfoReturnable<Boolean> infoR) {
        if (Modules.get().isActive(IgnoreBorder.class)) {
            infoR.setReturnValue(true);
        }
    }
}
