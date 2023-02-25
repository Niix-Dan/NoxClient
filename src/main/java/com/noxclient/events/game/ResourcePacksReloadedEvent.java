/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.game;

public class ResourcePacksReloadedEvent {
    private static final ResourcePacksReloadedEvent INSTANCE = new ResourcePacksReloadedEvent();

    public static ResourcePacksReloadedEvent get() {
        return INSTANCE;
    }
}
