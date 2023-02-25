/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.mixin.indigo;

import com.noxclient.systems.modules.Modules;
import com.noxclient.systems.modules.render.Xray;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.TerrainRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TerrainRenderContext.class, remap = false)
public class TerrainRenderContextMixin {
    @Inject(method = "tesselateBlock", at = @At("HEAD"), cancellable = true)
    private void onTesselateBlock(BlockState blockState, BlockPos blockPos, BakedModel model, MatrixStack matrixStack, CallbackInfoReturnable<Boolean> info) {
        Xray xray = Modules.get().get(Xray.class);

        if (xray.isActive() && xray.isBlocked(blockState.getBlock(), blockPos)) {
//            info.cancel();
        }
    }
}
