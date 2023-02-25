/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.entity;

import net.minecraft.entity.Entity;

public class EntityRemovedEvent {
    private static final EntityRemovedEvent INSTANCE = new EntityRemovedEvent();

    public Entity entity;

    public static EntityRemovedEvent get(Entity entity) {
        INSTANCE.entity = entity;
        return INSTANCE;
    }
}
