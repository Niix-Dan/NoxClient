/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.mixininterface;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

public interface IPlayerInteractEntityC2SPacket {
    PlayerInteractEntityC2SPacket.InteractType getType();

    Entity getEntity();
}
