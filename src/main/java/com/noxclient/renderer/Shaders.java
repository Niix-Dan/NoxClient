/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.renderer;

import com.noxclient.utils.PreInit;

public class Shaders {
    public static Shader POS_COLOR;
    public static Shader POS_TEX_COLOR;
    public static Shader TEXT;

    @PreInit
    public static void init() {
        POS_COLOR = new Shader("pos_color.vert", "pos_color.frag");
        POS_TEX_COLOR = new Shader("pos_tex_color.vert", "pos_tex_color.frag");
        TEXT = new Shader("text.vert", "text.frag");
    }
}
