/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.utils.misc.input;

import org.lwjgl.glfw.GLFW;

public enum KeyAction {
    Press,
    Repeat,
    Release;

    public static KeyAction get(int action) {
        if (action == GLFW.GLFW_PRESS) return Press;
        else if (action == GLFW.GLFW_RELEASE) return Release;
        else return Repeat;
    }
}
