/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.world;

import com.noxclient.events.Cancellable;
import net.minecraft.client.sound.SoundInstance;

public class PlaySoundEvent extends Cancellable {
    private static final PlaySoundEvent INSTANCE = new PlaySoundEvent();

    public SoundInstance sound;

    public static PlaySoundEvent get(SoundInstance sound) {
        INSTANCE.setCancelled(false);
        INSTANCE.sound = sound;
        return INSTANCE;
    }
}
