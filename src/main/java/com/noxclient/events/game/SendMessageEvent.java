/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.game;

import com.noxclient.events.Cancellable;

public class SendMessageEvent extends Cancellable {
    private static final SendMessageEvent INSTANCE = new SendMessageEvent();

    public String message;

    public static SendMessageEvent get(String message) {
        INSTANCE.setCancelled(false);
        INSTANCE.message = message;
        return INSTANCE;
    }
}


