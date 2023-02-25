/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.mixininterface;

import com.noxclient.utils.misc.Vec4;
import net.minecraft.util.math.Vec3d;

public interface IMatrix4f {
    void multiplyMatrix(Vec4 v, Vec4 out);

    Vec3d mul(Vec3d vec);
}
