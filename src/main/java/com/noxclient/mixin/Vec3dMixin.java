/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.mixin;

import com.noxclient.mixininterface.IVec3d;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Vec3d.class)
public class Vec3dMixin implements IVec3d {
    @Shadow @Final @Mutable public double x;
    @Shadow @Final @Mutable public double y;
    @Shadow @Final @Mutable public double z;

    @Override
    public void set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void setXZ(double x, double z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public void setY(double y) {
        this.y = y;
    }
}
