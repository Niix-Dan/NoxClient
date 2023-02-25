/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.utils.render.postprocess;

import com.noxclient.NoxClient;
import com.noxclient.mixin.WorldRendererAccessor;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;

public abstract class EntityShader extends PostProcessShader {
    private Framebuffer prevBuffer;

    @Override
    protected void preDraw() {
        WorldRenderer worldRenderer = NoxClient.mc.worldRenderer;
        WorldRendererAccessor wra = (WorldRendererAccessor) worldRenderer;
        prevBuffer = worldRenderer.getEntityOutlinesFramebuffer();
        wra.setEntityOutlinesFramebuffer(framebuffer);
    }

    @Override
    protected void postDraw() {
        if (prevBuffer == null) return;

        WorldRenderer worldRenderer = NoxClient.mc.worldRenderer;
        WorldRendererAccessor wra = (WorldRendererAccessor) worldRenderer;
        wra.setEntityOutlinesFramebuffer(prevBuffer);
        prevBuffer = null;
    }

    public void endRender() {
        endRender(() -> ((OutlineVertexConsumerProvider) vertexConsumerProvider).draw());
    }
}
