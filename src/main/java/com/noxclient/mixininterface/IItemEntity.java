/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.mixininterface;

import net.minecraft.util.math.Vec3d;

public interface IItemEntity {
    Vec3d getRotation();
    void setRotation(Vec3d rotation);
}
