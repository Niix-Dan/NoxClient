/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.mixin;

import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BufferRenderer.class)
public interface BufferRendererAccessor {
    @Accessor("currentVertexBuffer")
    static void setCurrentVertexBuffer(VertexBuffer vertexBuffer) {}
}
