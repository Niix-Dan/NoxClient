/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.meteor;

import com.noxclient.events.Cancellable;
import com.noxclient.utils.misc.input.KeyAction;

public class MouseButtonEvent extends Cancellable {
    private static final MouseButtonEvent INSTANCE = new MouseButtonEvent();

    public int button;
    public KeyAction action;

    public static MouseButtonEvent get(int button, KeyAction action) {
        INSTANCE.setCancelled(false);
        INSTANCE.button = button;
        INSTANCE.action = action;
        return INSTANCE;
    }
}
