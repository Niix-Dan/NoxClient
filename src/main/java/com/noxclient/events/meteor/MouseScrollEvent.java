/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.meteor;

import com.noxclient.events.Cancellable;

public class MouseScrollEvent extends Cancellable {
    private static final MouseScrollEvent INSTANCE = new MouseScrollEvent();

    public double value;

    public static MouseScrollEvent get(double value) {
        INSTANCE.setCancelled(false);
        INSTANCE.value = value;

        return INSTANCE;
    }
}
