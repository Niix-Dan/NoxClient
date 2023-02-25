/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.game;

import com.noxclient.events.Cancellable;
import net.minecraft.client.gui.screen.Screen;

public class OpenScreenEvent extends Cancellable {
    private static final OpenScreenEvent INSTANCE = new OpenScreenEvent();

    public Screen screen;

    public static OpenScreenEvent get(Screen screen) {
        INSTANCE.setCancelled(false);
        INSTANCE.screen = screen;
        return INSTANCE;
    }
}
