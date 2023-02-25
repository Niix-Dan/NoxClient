/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.utils.entity.fakeplayer;

import com.mojang.authlib.GameProfile;
import com.noxclient.NoxClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class FakePlayerEntity extends OtherClientPlayerEntity {
    public boolean doNotPush, hideWhenInsideCamera;

    public FakePlayerEntity(PlayerEntity player, String name, float health, boolean copyInv) {
        super(NoxClient.mc.world, new GameProfile(UUID.randomUUID(), name), player.getPublicKey());

        copyPositionAndRotation(player);

        prevYaw = getYaw();
        prevPitch = getPitch();
        headYaw = player.headYaw;
        prevHeadYaw = headYaw;
        bodyYaw = player.bodyYaw;
        prevBodyYaw = bodyYaw;

        Byte playerModel = player.getDataTracker().get(PlayerEntity.PLAYER_MODEL_PARTS);
        dataTracker.set(PlayerEntity.PLAYER_MODEL_PARTS, playerModel);

        getAttributes().setFrom(player.getAttributes());
        setPose(player.getPose());

        capeX = getX();
        capeY = getY();
        capeZ = getZ();

        if (health <= 20) {
            setHealth(health);
        } else {
            setHealth(health);
            setAbsorptionAmount(health - 20);
        }

        if (copyInv) getInventory().clone(player.getInventory());
    }

    public void spawn() {
        unsetRemoved();
        NoxClient.mc.world.addEntity(getId(), this);
    }

    public void despawn() {
        NoxClient.mc.world.removeEntity(getId(), RemovalReason.DISCARDED);
        setRemoved(RemovalReason.DISCARDED);
    }

    @Nullable
    @Override
    protected PlayerListEntry getPlayerListEntry() {
        if (cachedScoreboardEntry == null) {
            cachedScoreboardEntry = NoxClient.mc.getNetworkHandler().getPlayerListEntry(NoxClient.mc.player.getUuid());
        }

        return cachedScoreboardEntry;
    }
}
