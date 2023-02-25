/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.render;

import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;


public class TooltipDataEvent {
    private static final TooltipDataEvent INSTANCE = new TooltipDataEvent();

    public TooltipData tooltipData;
    public ItemStack itemStack;

    public static TooltipDataEvent get(ItemStack itemStack) {
        INSTANCE.tooltipData = null;
        INSTANCE.itemStack = itemStack;
        return INSTANCE;
    }
}
