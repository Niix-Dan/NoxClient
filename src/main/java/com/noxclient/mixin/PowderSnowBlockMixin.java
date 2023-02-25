/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.mixin;

import com.noxclient.NoxClient;
import com.noxclient.systems.modules.Modules;
import com.noxclient.systems.modules.movement.Jesus;
import net.minecraft.block.PowderSnowBlock;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PowderSnowBlock.class)
public class PowderSnowBlockMixin {
    @Inject(method = "canWalkOnPowderSnow", at = @At("HEAD"), cancellable = true)
    private static void onCanWalkOnPowderSnow(Entity entity, CallbackInfoReturnable<Boolean> info) {
        if (entity == NoxClient.mc.player && Modules.get().get(Jesus.class).canWalkOnPowderSnow()) info.setReturnValue(true);
    }
}
