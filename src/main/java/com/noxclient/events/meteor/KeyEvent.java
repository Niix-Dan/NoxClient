/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.meteor;

import com.noxclient.events.Cancellable;
import com.noxclient.utils.misc.input.KeyAction;

public class KeyEvent extends Cancellable {
    private static final KeyEvent INSTANCE = new KeyEvent();

    public int key, modifiers;
    public KeyAction action;

    public static KeyEvent get(int key, int modifiers, KeyAction action) {
        INSTANCE.setCancelled(false);
        INSTANCE.key = key;
        INSTANCE.modifiers = modifiers;
        INSTANCE.action = action;
        return INSTANCE;
    }
}
