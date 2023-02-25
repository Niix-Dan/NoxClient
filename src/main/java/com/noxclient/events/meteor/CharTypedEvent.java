/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.meteor;

import com.noxclient.events.Cancellable;

public class CharTypedEvent extends Cancellable {
    private static final CharTypedEvent INSTANCE = new CharTypedEvent();

    public char c;

    public static CharTypedEvent get(char c) {
        INSTANCE.setCancelled(false);
        INSTANCE.c = c;
        return INSTANCE;
    }
}
