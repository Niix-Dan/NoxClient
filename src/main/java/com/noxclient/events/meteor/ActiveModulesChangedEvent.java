/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.meteor;

public class ActiveModulesChangedEvent {
    private static final ActiveModulesChangedEvent INSTANCE = new ActiveModulesChangedEvent();

    public static ActiveModulesChangedEvent get() {
        return INSTANCE;
    }
}
