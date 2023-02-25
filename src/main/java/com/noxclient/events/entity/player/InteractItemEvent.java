/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.entity.player;

import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class InteractItemEvent {
    private static final InteractItemEvent INSTANCE = new InteractItemEvent();

    public Hand hand;
    public ActionResult toReturn;

    public static InteractItemEvent get(Hand hand) {
        INSTANCE.hand = hand;
        INSTANCE.toReturn = null;

        return INSTANCE;
    }
}
