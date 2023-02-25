/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.entity;

import net.minecraft.entity.Entity;

public class EntityDestroyEvent {
    private static final EntityDestroyEvent INSTANCE = new EntityDestroyEvent();

    public Entity entity;

    public static EntityDestroyEvent get(Entity entity) {
        INSTANCE.entity = entity;
        return INSTANCE;
    }
}
