/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.utils.player;

import com.noxclient.NoxClient;
import com.noxclient.events.entity.player.SendMovementPacketsEvent;
import com.noxclient.events.world.TickEvent;
import com.noxclient.systems.config.Config;
import com.noxclient.utils.PreInit;
import com.noxclient.utils.entity.Target;
import com.noxclient.utils.misc.Pool;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class Rotations {
    private static final Pool<Rotation> rotationPool = new Pool<>(Rotation::new);
    private static final List<Rotation> rotations = new ArrayList<>();
    public static float serverYaw;
    public static float serverPitch;
    public static int rotationTimer;
    private static float preYaw, prePitch;
    private static int i = 0;

    private static Rotation lastRotation;
    private static int lastRotationTimer;
    private static boolean sentLastRotation;
    public static boolean rotating = false;

    @PreInit
    public static void init() {
        NoxClient.EVENT_BUS.subscribe(Rotations.class);
    }

    public static void rotate(double yaw, double pitch, int priority, boolean clientSide, Runnable callback) {
        Rotation rotation = rotationPool.get();
        rotation.set(yaw, pitch, priority, clientSide, callback);

        int i = 0;
        for (; i < rotations.size(); i++) {
            if (priority > rotations.get(i).priority) break;
        }

        rotations.add(i, rotation);
    }

    public static void rotate(double yaw, double pitch, int priority, Runnable callback) {
        rotate(yaw, pitch, priority, false, callback);
    }

    public static void rotate(double yaw, double pitch, Runnable callback) {
        rotate(yaw, pitch, 0, callback);
    }

    public static void rotate(double yaw, double pitch) {
        rotate(yaw, pitch, 0, null);
    }

    private static void resetLastRotation() {
        if (lastRotation != null) {
            rotationPool.free(lastRotation);

            lastRotation = null;
            lastRotationTimer = 0;
        }
    }

    @EventHandler
    private static void onSendMovementPacketsPre(SendMovementPacketsEvent.Pre event) {
        if (NoxClient.mc.cameraEntity != NoxClient.mc.player) return;
        sentLastRotation = false;

        if (!rotations.isEmpty()) {
            rotating = true;
            resetLastRotation();

            Rotation rotation = rotations.get(i);
            setupMovementPacketRotation(rotation);

            if (rotations.size() > 1) rotationPool.free(rotation);

            i++;
        } else if (lastRotation != null) {
            if (lastRotationTimer >= Config.get().rotationHoldTicks.get()) {
                resetLastRotation();
                rotating = false;
            } else {
                setupMovementPacketRotation(lastRotation);
                sentLastRotation = true;

                lastRotationTimer++;
            }
        }
    }

    private static void setupMovementPacketRotation(Rotation rotation) {
        setClientRotation(rotation);
        setCamRotation(rotation.yaw, rotation.pitch);
    }

    private static void setClientRotation(Rotation rotation) {
        preYaw = NoxClient.mc.player.getYaw();
        prePitch = NoxClient.mc.player.getPitch();

        NoxClient.mc.player.setYaw((float) rotation.yaw);
        NoxClient.mc.player.setPitch((float) rotation.pitch);
    }

    @EventHandler
    private static void onSendMovementPacketsPost(SendMovementPacketsEvent.Post event) {
        if (!rotations.isEmpty()) {
            if (NoxClient.mc.cameraEntity == NoxClient.mc.player) {
                rotations.get(i - 1).runCallback();

                if (rotations.size() == 1) lastRotation = rotations.get(i - 1);

                resetPreRotation();
            }

            for (; i < rotations.size(); i++) {
                Rotation rotation = rotations.get(i);

                setCamRotation(rotation.yaw, rotation.pitch);
                if (rotation.clientSide) setClientRotation(rotation);
                rotation.sendPacket();
                if (rotation.clientSide) resetPreRotation();

                if (i == rotations.size() - 1) lastRotation = rotation;
                else rotationPool.free(rotation);
            }

            rotations.clear();
            i = 0;
        } else if (sentLastRotation) {
            resetPreRotation();
        }
    }

    private static void resetPreRotation() {
        NoxClient.mc.player.setYaw(preYaw);
        NoxClient.mc.player.setPitch(prePitch);
    }

    @EventHandler
    private static void onTick(TickEvent.Pre event) {
        rotationTimer++;
    }

    public static double getYaw(Entity entity) {
        return NoxClient.mc.player.getYaw() + MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(entity.getZ() - NoxClient.mc.player.getZ(), entity.getX() - NoxClient.mc.player.getX())) - 90f - NoxClient.mc.player.getYaw());
    }

    public static double getYaw(Vec3d pos) {
        return NoxClient.mc.player.getYaw() + MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(pos.getZ() - NoxClient.mc.player.getZ(), pos.getX() - NoxClient.mc.player.getX())) - 90f - NoxClient.mc.player.getYaw());
    }

    public static double getPitch(Vec3d pos) {
        double diffX = pos.getX() - NoxClient.mc.player.getX();
        double diffY = pos.getY() - (NoxClient.mc.player.getY() + NoxClient.mc.player.getEyeHeight(NoxClient.mc.player.getPose()));
        double diffZ = pos.getZ() - NoxClient.mc.player.getZ();

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        return NoxClient.mc.player.getPitch() + MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)) - NoxClient.mc.player.getPitch());
    }

    public static double getPitch(Entity entity, Target target) {
        double y;
        if (target == Target.Head) y = entity.getEyeY();
        else if (target == Target.Body) y = entity.getY() + entity.getHeight() / 2;
        else y = entity.getY();

        double diffX = entity.getX() - NoxClient.mc.player.getX();
        double diffY = y - (NoxClient.mc.player.getY() + NoxClient.mc.player.getEyeHeight(NoxClient.mc.player.getPose()));
        double diffZ = entity.getZ() - NoxClient.mc.player.getZ();

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        return NoxClient.mc.player.getPitch() + MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)) - NoxClient.mc.player.getPitch());
    }

    public static double getPitch(Entity entity) {
        return getPitch(entity, Target.Body);
    }

    public static double getYaw(BlockPos pos) {
        return NoxClient.mc.player.getYaw() + MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(pos.getZ() + 0.5 - NoxClient.mc.player.getZ(), pos.getX() + 0.5 - NoxClient.mc.player.getX())) - 90f - NoxClient.mc.player.getYaw());
    }

    public static double getPitch(BlockPos pos) {
        double diffX = pos.getX() + 0.5 - NoxClient.mc.player.getX();
        double diffY = pos.getY() + 0.5 - (NoxClient.mc.player.getY() + NoxClient.mc.player.getEyeHeight(NoxClient.mc.player.getPose()));
        double diffZ = pos.getZ() + 0.5 - NoxClient.mc.player.getZ();

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        return NoxClient.mc.player.getPitch() + MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)) - NoxClient.mc.player.getPitch());
    }

    public static void setCamRotation(double yaw, double pitch) {
        serverYaw = (float) yaw;
        serverPitch = (float) pitch;
        rotationTimer = 0;
    }

    private static class Rotation {
        public double yaw, pitch;
        public int priority;
        public boolean clientSide;
        public Runnable callback;

        public void set(double yaw, double pitch, int priority, boolean clientSide, Runnable callback) {
            this.yaw = yaw;
            this.pitch = pitch;
            this.priority = priority;
            this.clientSide = clientSide;
            this.callback = callback;
        }

        public void sendPacket() {
            NoxClient.mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround((float) yaw, (float) pitch, NoxClient.mc.player.isOnGround()));
            runCallback();
        }

        public void runCallback() {
            if (callback != null) callback.run();
        }
    }
}
