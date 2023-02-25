/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.renderer;

import com.noxclient.utils.PreInit;
import net.minecraft.client.util.math.MatrixStack;

public class PostProcessRenderer {
    private static Mesh mesh;
    private static final MatrixStack matrices = new MatrixStack();

    @PreInit
    public static void init() {
        mesh = new Mesh(DrawMode.Triangles, Mesh.Attrib.Vec2);
        mesh.begin();

        mesh.quad(
            mesh.vec2(-1, -1).next(),
            mesh.vec2(-1, 1).next(),
            mesh.vec2(1, 1).next(),
            mesh.vec2(1, -1).next()
        );

        mesh.end();
    }

    public static void beginRender() {
        mesh.beginRender(matrices);
    }

    public static void render() {
        mesh.render(matrices);
    }

    public static void endRender() {
        mesh.endRender();
    }
}
