/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.game;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

public class ItemStackTooltipEvent {
    private static final ItemStackTooltipEvent INSTANCE = new ItemStackTooltipEvent();

    public ItemStack itemStack;
    public List<Text> list;

    public static ItemStackTooltipEvent get(ItemStack itemStack, List<Text> list) {
        INSTANCE.itemStack = itemStack;
        INSTANCE.list = list;
        return INSTANCE;
    }
}
