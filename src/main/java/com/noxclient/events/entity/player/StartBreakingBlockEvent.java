/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.entity.player;

import com.noxclient.events.Cancellable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class StartBreakingBlockEvent extends Cancellable {
    private static final StartBreakingBlockEvent INSTANCE = new StartBreakingBlockEvent();

    public BlockPos blockPos;
    public Direction direction;

    public static StartBreakingBlockEvent get(BlockPos blockPos, Direction direction) {
        INSTANCE.setCancelled(false);
        INSTANCE.blockPos = blockPos;
        INSTANCE.direction = direction;
        return INSTANCE;
    }
}
