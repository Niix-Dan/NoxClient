/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.world;

public class ConnectToServerEvent {
    private static final ConnectToServerEvent INSTANCE = new ConnectToServerEvent();

    public static ConnectToServerEvent get() {
        return INSTANCE;
    }
}
