/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.renderer;

import org.lwjgl.opengl.GL32C;

public enum DrawMode {
    Lines(2),
    Triangles(3);

    public final int indicesCount;

    DrawMode(int indicesCount) {
        this.indicesCount = indicesCount;
    }

    public int getGL() {
        return switch (this) {
            case Lines ->     GL32C.GL_LINES;
            case Triangles -> GL32C.GL_TRIANGLES;
        };
    }
}
