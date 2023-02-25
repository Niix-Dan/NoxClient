/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.modules.movement;

import com.noxclient.mixininterface.IVec3d;
import com.noxclient.settings.BoolSetting;
import com.noxclient.settings.DoubleSetting;
import com.noxclient.settings.Setting;
import com.noxclient.settings.SettingGroup;
import com.noxclient.events.entity.BoatMoveEvent;
import com.noxclient.events.packets.PacketEvent;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import com.noxclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.VehicleMoveS2CPacket;
import net.minecraft.util.math.Vec3d;

public class BoatFly extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
            .name("speed")
            .description("Horizontal speed in blocks per second.")
            .defaultValue(10)
            .min(0)
            .sliderMax(50)
            .build()
    );

    private final Setting<Double> verticalSpeed = sgGeneral.add(new DoubleSetting.Builder()
            .name("vertical-speed")
            .description("Vertical speed in blocks per second.")
            .defaultValue(6)
            .min(0)
            .sliderMax(20)
            .build()
    );

    private final Setting<Double> fallSpeed = sgGeneral.add(new DoubleSetting.Builder()
            .name("fall-speed")
            .description("How fast you fall in blocks per second.")
            .defaultValue(0.1)
            .min(0)
            .build()
    );

    private final Setting<Boolean> cancelServerPackets = sgGeneral.add(new BoolSetting.Builder()
            .name("cancel-server-packets")
            .description("Cancels incoming boat move packets.")
            .defaultValue(false)
            .build()
    );

    public BoatFly() {
        super(Categories.Movement, "boat-fly", "Transforms your boat into a plane.");
    }

    @EventHandler
    private void onBoatMove(BoatMoveEvent event) {
        if (event.boat.getPrimaryPassenger() != mc.player) return;

        event.boat.setYaw(mc.player.getYaw());

        // Horizontal movement
        Vec3d vel = PlayerUtils.getHorizontalVelocity(speed.get());
        double velX = vel.getX();
        double velY = 0;
        double velZ = vel.getZ();

        // Vertical movement
        if (mc.options.jumpKey.isPressed()) velY += verticalSpeed.get() / 20;
        if (mc.options.sprintKey.isPressed()) velY -= verticalSpeed.get() / 20;
        else velY -= fallSpeed.get() / 20;

        // Apply velocity
        ((IVec3d) event.boat.getVelocity()).set(velX, velY, velZ);
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (event.packet instanceof VehicleMoveS2CPacket && cancelServerPackets.get()) {
            event.cancel();
        }
    }
}
