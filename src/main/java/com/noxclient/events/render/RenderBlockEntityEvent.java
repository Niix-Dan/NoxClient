/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.render;

import com.noxclient.events.Cancellable;
import net.minecraft.block.entity.BlockEntity;

public class RenderBlockEntityEvent extends Cancellable {
    private static final RenderBlockEntityEvent INSTANCE = new RenderBlockEntityEvent();

    public BlockEntity blockEntity;

    public static RenderBlockEntityEvent get(BlockEntity blockEntity) {
        INSTANCE.setCancelled(false);
        INSTANCE.blockEntity = blockEntity;
        return INSTANCE;
    }
}
