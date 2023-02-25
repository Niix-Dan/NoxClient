/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.entity.player;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BreakBlockEvent {
    private static final BreakBlockEvent INSTANCE = new BreakBlockEvent();

    public BlockPos blockPos;

    public BlockState getBlockState(World world) {
        return world.getBlockState(blockPos);
    }

    public static BreakBlockEvent get(BlockPos blockPos) {
        INSTANCE.blockPos = blockPos;
        return INSTANCE;
    }
}
