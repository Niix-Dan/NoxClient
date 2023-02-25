/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.game;

public class GameJoinedEvent {
    private static final GameJoinedEvent INSTANCE = new GameJoinedEvent();

    public static GameJoinedEvent get() {
        return INSTANCE;
    }
}
