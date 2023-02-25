/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.mixin;

import com.noxclient.systems.modules.Modules;
import com.noxclient.systems.modules.misc.ServerSpoof;
import net.minecraft.client.util.ProfileKeys;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.network.encryption.Signer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ProfileKeys.class)
public class ProfileKeysMixin {
    @Inject(method = "getSigner", at = @At("HEAD"), cancellable = true)
    private void onGetSigner(CallbackInfoReturnable<Signer> info) {
        if (Modules.get().get(ServerSpoof.class).noSignatures()) info.setReturnValue(null);
    }

    @Inject(method = "getPublicKey", at = @At("HEAD"), cancellable = true)
    private void onGetPublicKey(CallbackInfoReturnable<Optional<PlayerPublicKey>> info) {
        if (Modules.get().get(ServerSpoof.class).noSignatures()) info.setReturnValue(Optional.empty());
    }

    @Inject(method = "getPublicKey", at = @At("HEAD"), cancellable = true)
    private void onGetPublicKeyData(CallbackInfoReturnable<Optional<PlayerPublicKey.PublicKeyData>> info) {
        if (Modules.get().get(ServerSpoof.class).noSignatures()) info.setReturnValue(Optional.empty());
    }
}
