/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.mixin;

import com.noxclient.utils.misc.FakeClientPlayer;
import com.noxclient.utils.network.Capes;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.noxclient.NoxClient;

@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityMixin {
    @Inject(method = "getCapeTexture", at = @At("HEAD"), cancellable = true)
    private void onGetCapeTexture(CallbackInfoReturnable<Identifier> info) {
        Identifier id = Capes.get((PlayerEntity) (Object) this);
        if (id != null) info.setReturnValue(id);
    }

    // Player model rendering in main menu

    @Inject(method = "getPlayerListEntry", at = @At("HEAD"), cancellable = true)
    private void onGetPlayerListEntry(CallbackInfoReturnable<PlayerListEntry> info) {
        if (NoxClient.mc.getNetworkHandler() == null) info.setReturnValue(FakeClientPlayer.getPlayerListEntry());
    }

    @Inject(method = "isSpectator", at = @At("HEAD"), cancellable = true)
    private void onIsSpectator(CallbackInfoReturnable<Boolean> info) {
        if (NoxClient.mc.getNetworkHandler() == null) info.setReturnValue(false);
    }

    @Inject(method = "isCreative", at = @At("HEAD"), cancellable = true)
    private void onIsCreative(CallbackInfoReturnable<Boolean> info) {
        if (NoxClient.mc.getNetworkHandler() == null) info.setReturnValue(false);
    }
}
