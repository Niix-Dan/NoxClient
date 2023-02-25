/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.mixininterface;

import net.minecraft.item.ItemStack;

// Using accessor causes a stackoverflow for some fucking reason
public interface IAbstractFurnaceScreenHandler {
    boolean isItemSmeltable(ItemStack itemStack);
}
