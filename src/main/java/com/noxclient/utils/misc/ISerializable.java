/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.utils.misc;

import net.minecraft.nbt.NbtCompound;

public interface ISerializable<T> {
    NbtCompound toTag();

    T fromTag(NbtCompound tag);
}
