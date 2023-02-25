/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.entity;

import com.noxclient.events.Cancellable;
import net.minecraft.item.ItemStack;

public class DropItemsEvent extends Cancellable {
    private static final DropItemsEvent INSTANCE = new DropItemsEvent();

    public ItemStack itemStack;

    public static DropItemsEvent get(ItemStack itemStack) {
        INSTANCE.setCancelled(false);
        INSTANCE.itemStack = itemStack;
        return INSTANCE;
    }
}
