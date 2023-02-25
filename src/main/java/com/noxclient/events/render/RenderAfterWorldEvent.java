/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.render;

public class RenderAfterWorldEvent {
    private static final RenderAfterWorldEvent INSTANCE = new RenderAfterWorldEvent();

    public static RenderAfterWorldEvent get() {
        return INSTANCE;
    }
}
