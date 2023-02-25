/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.meteor;

@SuppressWarnings("InstantiationOfUtilityClass")
public class CustomFontChangedEvent {
    private static final CustomFontChangedEvent INSTANCE = new CustomFontChangedEvent();

    public static CustomFontChangedEvent get() {
        return INSTANCE;
    }
}
