/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.render;

public class GetFovEvent {
    private static final GetFovEvent INSTANCE = new GetFovEvent();

    public double fov;

    public static GetFovEvent get(double fov) {
        INSTANCE.fov = fov;
        return INSTANCE;
    }
}
