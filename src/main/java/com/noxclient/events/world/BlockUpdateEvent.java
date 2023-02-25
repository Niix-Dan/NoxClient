/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.world;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockUpdateEvent {
    private static final BlockUpdateEvent INSTANCE = new BlockUpdateEvent();

    public BlockPos pos;
    public BlockState oldState, newState;

    public static BlockUpdateEvent get(BlockPos pos, BlockState oldState, BlockState newState) {
        INSTANCE.pos = pos;
        INSTANCE.oldState = oldState;
        INSTANCE.newState = newState;

        return INSTANCE;
    }
}
