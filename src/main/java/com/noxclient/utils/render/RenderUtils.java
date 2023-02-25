/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.noxclient.NoxClient;
import com.noxclient.events.render.Render3DEvent;
import com.noxclient.mixininterface.IMatrix4f;
import com.noxclient.renderer.ShapeMode;
import com.noxclient.utils.render.color.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class RenderUtils {
    public static Vec3d center;

    public static boolean visibleHeight(RenderMode renderMode) {
        return (renderMode == RenderMode.UpperSide || renderMode == RenderMode.LowerSide);
    }

    public static boolean visibleSide(ShapeMode shapeMode) {
        return (shapeMode == ShapeMode.Both || shapeMode == ShapeMode.Sides);
    }

    public static boolean visibleLine(ShapeMode shapeMode) {
        return (shapeMode == ShapeMode.Both || shapeMode == ShapeMode.Lines);
    }

    public static void renderQuad(BlockPos block, Direction direction, Render3DEvent event, Color color) {
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        switch (direction) {
            case DOWN -> event.renderer.quadHorizontal(x, y, z, x+1, z+1, color);
            case UP -> event.renderer.quadHorizontal(x, y+1, z, x+1, z+1, color);

            case NORTH -> event.renderer.quadVertical(x,y,z,x+1,y+1,z,color);
            case SOUTH -> event.renderer.quadVertical(x,y,z+1,x+1,y+1,z+1,color);

            case WEST -> event.renderer.quadVertical(x,y,z,x,y+1,z+1,color);
            case EAST -> event.renderer.quadVertical(x+1,y,z,x+1,y+1,z+1,color);
        }
    }

    // Items
    public static void drawItem(ItemStack itemStack, int x, int y, double scale, boolean overlay) {
        //RenderSystem.disableDepthTest();

        MatrixStack matrices = RenderSystem.getModelViewStack();

        matrices.push();
        matrices.scale((float) scale, (float) scale, 1);

        NoxClient.mc.getItemRenderer().renderGuiItemIcon(itemStack, (int) (x / scale), (int) (y / scale));
        if (overlay) NoxClient.mc.getItemRenderer().renderGuiItemOverlay(NoxClient.mc.textRenderer, itemStack, (int) (x / scale), (int) (y / scale), null);

        matrices.pop();
        //RenderSystem.enableDepthTest();
    }

    public static void drawItem(ItemStack itemStack, int x, int y, boolean overlay) {
        drawItem(itemStack, x, y, 1, overlay);
    }

    public static void updateScreenCenter() {
        MinecraftClient mc = MinecraftClient.getInstance();

        Vec3d pos = new Vec3d(0, 0, 1);

        if (mc.options.getBobView().getValue()) {
            MatrixStack bobViewMatrices = new MatrixStack();

            bobView(bobViewMatrices);
            bobViewMatrices.peek().getPositionMatrix().invert();

            pos = ((IMatrix4f) (Object) bobViewMatrices.peek().getPositionMatrix()).mul(pos);
        }

        center = new Vec3d(pos.x, -pos.y, pos.z)
            .rotateX(-(float) Math.toRadians(mc.gameRenderer.getCamera().getPitch()))
            .rotateY(-(float) Math.toRadians(mc.gameRenderer.getCamera().getYaw()))
            .add(mc.gameRenderer.getCamera().getPos());
    }

    private static void bobView(MatrixStack matrices) {
        Entity cameraEntity = MinecraftClient.getInstance().getCameraEntity();

        if (cameraEntity instanceof PlayerEntity playerEntity) {
            float f = MinecraftClient.getInstance().getTickDelta();
            float g = playerEntity.horizontalSpeed - playerEntity.prevHorizontalSpeed;
            float h = -(playerEntity.horizontalSpeed + g * f);
            float i = MathHelper.lerp(f, playerEntity.prevStrideDistance, playerEntity.strideDistance);

            matrices.translate(-(MathHelper.sin(h * 3.1415927f) * i * 0.5), -(-Math.abs(MathHelper.cos(h * 3.1415927f) * i)), 0);
            matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(MathHelper.sin(h * 3.1415927f) * i * 3));
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(Math.abs(MathHelper.cos(h * 3.1415927f - 0.2f) * i) * 5));
        }
    }

    public enum Render {
        Meteor, BedTrap, None;
    }

    public enum RenderMode {
        Box, UpperSide, LowerSide, Shape, Romb, UpperRomb, None;
    }

    public enum Side {
        Default, Upper, Lower;
    }
}

