/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.render;

import com.noxclient.utils.Utils;

public class Render2DEvent {
    private static final Render2DEvent INSTANCE = new Render2DEvent();

    public int screenWidth, screenHeight;
    public double frameTime;
    public float tickDelta;

    public static Render2DEvent get(int screenWidth, int screenHeight, float tickDelta) {
        INSTANCE.screenWidth = screenWidth;
        INSTANCE.screenHeight = screenHeight;
        INSTANCE.frameTime = Utils.frameTime;
        INSTANCE.tickDelta = tickDelta;
        return INSTANCE;
    }
}
