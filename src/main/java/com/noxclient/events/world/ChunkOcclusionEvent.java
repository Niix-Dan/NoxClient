/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.world;

import com.noxclient.events.Cancellable;

public class ChunkOcclusionEvent extends Cancellable {
    private static final ChunkOcclusionEvent INSTANCE = new ChunkOcclusionEvent();

    public static ChunkOcclusionEvent get() {
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
