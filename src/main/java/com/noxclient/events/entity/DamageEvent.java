/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public class DamageEvent {
    private static final DamageEvent INSTANCE = new DamageEvent();

    public LivingEntity entity;
    public DamageSource source;

    public static DamageEvent get(LivingEntity entity, DamageSource source) {
        INSTANCE.entity = entity;
        INSTANCE.source = source;
        return INSTANCE;
    }
}
