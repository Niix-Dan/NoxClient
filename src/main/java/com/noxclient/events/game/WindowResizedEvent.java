/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.game;

public class WindowResizedEvent {
    private static final WindowResizedEvent INSTANCE = new WindowResizedEvent();

    public static WindowResizedEvent get() {
        return INSTANCE;
    }
}
