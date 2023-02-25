package com.noxclient.systems.modules.movement;

import com.noxclient.events.world.TickEvent;
import com.noxclient.settings.DoubleSetting;
import com.noxclient.settings.Setting;
import com.noxclient.settings.SettingGroup;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class Glide extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Double> fallSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("fall-speed")
        .description("Fall speed.")
        .defaultValue(0.125)
        .min(0.005)
        .sliderRange(0.005, 0.25)
        .build()
    );

    public final Setting<Double> moveSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("move-speed")
        .description("Horizontal movement factor.")
        .defaultValue(1.2)
        .min(1)
        .sliderRange(1, 5)
        .build()
    );

    public final Setting<Double> minHeight = sgGeneral.add(new DoubleSetting.Builder()
        .name("min-height")
        .description("Won't glide when you are too close to the ground.")
        .defaultValue(0)
        .min(0)
        .sliderRange(0, 2)
        .build()
    );

    public Glide() {
        super(Categories.Movement, "glide", "Yeehaw!");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        ClientPlayerEntity player = mc.player;
        Vec3d v = player.getVelocity();

        if(player.isOnGround() || player.isTouchingWater() || player.isInLava() || player.isClimbing() || v.y >= 0) return;

        if(minHeight.get() > 0)
        {
            Box box = player.getBoundingBox();
            box = box.union(box.offset(0, -minHeight.get(), 0));
            if(!mc.world.isSpaceEmpty(box)) return;
        }

        player.setVelocity(v.x, Math.max(v.y, -fallSpeed.get()), v.z);
        player.airStrafingSpeed *= moveSpeed.get();
    }
}
