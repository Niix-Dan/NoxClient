/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.packets;

import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;

public class PlaySoundPacketEvent {

    private static final PlaySoundPacketEvent INSTANCE = new PlaySoundPacketEvent();

    public PlaySoundS2CPacket packet;

    public static PlaySoundPacketEvent get(PlaySoundS2CPacket packet) {
        INSTANCE.packet = packet;
        return INSTANCE;
    }
}
