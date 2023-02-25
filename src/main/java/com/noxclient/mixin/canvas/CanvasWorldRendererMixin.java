/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.mixin.canvas;

import grondag.canvas.render.world.CanvasWorldRenderer;
import com.noxclient.systems.modules.Modules;
import com.noxclient.systems.modules.render.BlockSelection;
import com.noxclient.utils.render.postprocess.PostProcessShaders;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CanvasWorldRenderer.class, remap = false)
public class CanvasWorldRendererMixin {
    @ModifyVariable(method = "renderWorld", at = @At("LOAD"), name = "blockOutlines")
    private boolean renderWorld_blockOutlines(boolean blockOutlines) {
        if (Modules.get().isActive(BlockSelection.class)) return false;
        return blockOutlines;
    }

    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void onRenderHead(MatrixStack viewMatrixStack, float tickDelta, long frameStartNanos, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo info) {
        PostProcessShaders.beginRender();
    }

    // Injected through ASM because mixins are fucking retarded and don't work outside of development environment for this one injection
    /*@Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/OutlineVertexConsumerProvider;draw()V", shift = At.Shift.AFTER))
    private void onRenderOutlines(CallbackInfo info) {
        PostProcessShaders.endRender();
    }*/
}
