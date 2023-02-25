/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.noxclient.NoxClient;
import com.noxclient.mixininterface.IMatrix4f;
import com.noxclient.utils.Utils;
import com.noxclient.utils.misc.Vec3;
import com.noxclient.utils.misc.Vec4;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;

public class NametagUtils {
    private static final Vec4 vec4 = new Vec4();
    private static final Vec4 mmMat4 = new Vec4();
    private static final Vec4 pmMat4 = new Vec4();
    private static final Vec3 camera = new Vec3();
    private static final Vec3 cameraNegated = new Vec3();
    private static Matrix4f model;
    private static Matrix4f projection;
    private static double windowScale;

    public static double scale;

    public static void onRender(MatrixStack matrices, Matrix4f projection) {
        model = matrices.peek().getPositionMatrix().copy();
        NametagUtils.projection = projection;

        camera.set(NoxClient.mc.gameRenderer.getCamera().getPos());
        cameraNegated.set(camera);
        cameraNegated.negate();

        windowScale = NoxClient.mc.getWindow().calculateScaleFactor(1, false);
    }

    public static boolean to2D(Vec3 pos, double scale) {
        NametagUtils.scale = getScale(pos) * scale;

        vec4.set(cameraNegated.x + pos.x, cameraNegated.y + pos.y, cameraNegated.z + pos.z, 1);

        ((IMatrix4f) (Object) model).multiplyMatrix(vec4, mmMat4);
        ((IMatrix4f) (Object) projection).multiplyMatrix(mmMat4, pmMat4);

        if (pmMat4.w <= 0.0f) return false;

        pmMat4.toScreen();
        double x = pmMat4.x * NoxClient.mc.getWindow().getFramebufferWidth();
        double y = pmMat4.y * NoxClient.mc.getWindow().getFramebufferHeight();

        if (Double.isInfinite(x) || Double.isInfinite(y)) return false;

        pos.set(x / windowScale, NoxClient.mc.getWindow().getFramebufferHeight() - y / windowScale, pmMat4.z);
        return true;
    }

    public static void begin(Vec3 pos) {
        MatrixStack matrices = RenderSystem.getModelViewStack();

        matrices.push();
        matrices.translate(pos.x, pos.y, 0);
        matrices.scale((float) scale, (float) scale, 1);
    }

    public static void end() {
        RenderSystem.getModelViewStack().pop();
    }

    private static double getScale(Vec3 pos) {
        double dist = camera.distanceTo(pos);
        return Utils.clamp(1 - dist * 0.01, 0.5, Integer.MAX_VALUE);
    }
}
