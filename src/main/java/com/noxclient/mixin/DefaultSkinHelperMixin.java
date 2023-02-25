/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.mixin;

import net.minecraft.client.util.DefaultSkinHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(DefaultSkinHelper.class)
public class DefaultSkinHelperMixin {
    // Player model rendering in main menu
    @Inject(method = "shouldUseSlimModel", at = @At("HEAD"), cancellable = true)
    private static void onShouldUseSlimModel(UUID uuid, CallbackInfoReturnable<Boolean> info) {
        if (uuid == null) info.setReturnValue(false);
    }
}
