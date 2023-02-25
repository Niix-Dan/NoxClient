/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.modules.movement;

import com.noxclient.mixininterface.IVec3d;
import com.noxclient.settings.DoubleSetting;
import com.noxclient.settings.Setting;
import com.noxclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;
import com.noxclient.events.entity.LivingEntityMoveEvent;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import com.noxclient.utils.player.PlayerUtils;

public class EntityFly extends Module {
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

    public EntityFly() {
        super(Categories.Movement, "entity-fly", "Allows you to fly with any entity.");
    }

    @EventHandler
    private void onLivingEntityMove(LivingEntityMoveEvent event) {
        if (event.entity.getPrimaryPassenger() != mc.player) return;

        // Update Yaw
        event.entity.setYaw(mc.player.getYaw());

        // Horizontal Movement
        Vec3d vel = PlayerUtils.getHorizontalVelocity(speed.get());
        double velX = vel.getX();
        double velY = 0;
        double velZ = vel.getZ();

        // Vertical Movement
        if (mc.options.jumpKey.isPressed()) velY += verticalSpeed.get() / 20;
        if (mc.options.sprintKey.isPressed()) velY -= verticalSpeed.get() / 20;
        else velY -= fallSpeed.get() / 20;

        // Apply Velocity
        ((IVec3d) event.entity.getVelocity()).set(velX, velY, velZ);
    }
}
