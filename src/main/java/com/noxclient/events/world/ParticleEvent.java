/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.world;

import com.noxclient.events.Cancellable;
import net.minecraft.particle.ParticleEffect;

public class ParticleEvent extends Cancellable {
    private static final ParticleEvent INSTANCE = new ParticleEvent();

    public ParticleEffect particle;

    public static ParticleEvent get(ParticleEffect particle) {
        INSTANCE.setCancelled(false);
        INSTANCE.particle = particle;
        return INSTANCE;
    }
}
