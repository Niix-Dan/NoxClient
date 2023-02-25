/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.game;

public class GameLeftEvent {
    private static final GameLeftEvent INSTANCE = new GameLeftEvent();

    public static GameLeftEvent get() {
        return INSTANCE;
    }
}
