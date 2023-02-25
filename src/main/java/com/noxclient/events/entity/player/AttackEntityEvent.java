/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.entity.player;

import com.noxclient.events.Cancellable;
import net.minecraft.entity.Entity;

public class AttackEntityEvent extends Cancellable {

    private static final AttackEntityEvent INSTANCE = new AttackEntityEvent();

    public Entity entity;

    public static AttackEntityEvent get(Entity entity) {
        INSTANCE.setCancelled(false);
        INSTANCE.entity = entity;
        return INSTANCE;
    }
}
