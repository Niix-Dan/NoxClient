/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.modules.movement;

import com.noxclient.settings.DoubleSetting;
import com.noxclient.settings.Setting;
import com.noxclient.settings.SettingGroup;
import com.noxclient.events.world.TickEvent;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;

public class Spider extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
            .name("climb-speed")
            .description("The speed you go up blocks.")
            .defaultValue(0.2)
            .min(0.0)
            .build()
    );

    public Spider() {
        super(Categories.Movement, "spider", "Allows you to climb walls like a spider.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!mc.player.horizontalCollision) return;

        Vec3d velocity = mc.player.getVelocity();
        if (velocity.y >= 0.2) return;

        mc.player.setVelocity(velocity.x, speed.get(), velocity.z);
    }
}
