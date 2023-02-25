/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.game;

import com.noxclient.events.Cancellable;
import net.minecraft.client.option.Perspective;

public class ChangePerspectiveEvent extends Cancellable {
    private static final ChangePerspectiveEvent INSTANCE = new ChangePerspectiveEvent();

    public Perspective perspective;

    public static ChangePerspectiveEvent get(Perspective perspective) {
        INSTANCE.setCancelled(false);
        INSTANCE.perspective = perspective;
        return INSTANCE;
    }
}
