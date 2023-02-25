/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.renderer;

public class ShaderMesh extends Mesh {
    private final Shader shader;

    public ShaderMesh(Shader shader, DrawMode drawMode, Attrib... attributes) {
        super(drawMode, attributes);

        this.shader = shader;
    }

    @Override
    protected void beforeRender() {
        shader.bind();
    }
}
