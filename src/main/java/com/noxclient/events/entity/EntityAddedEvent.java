/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.entity;

import net.minecraft.entity.Entity;

public class EntityAddedEvent {
    private static final EntityAddedEvent INSTANCE = new EntityAddedEvent();

    public Entity entity;

    public static EntityAddedEvent get(Entity entity) {
        INSTANCE.entity = entity;
        return INSTANCE;
    }
}
