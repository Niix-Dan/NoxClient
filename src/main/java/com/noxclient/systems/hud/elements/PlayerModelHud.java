/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.hud.elements;

import com.noxclient.NoxClient;
import com.noxclient.settings.*;
import com.noxclient.systems.hud.Hud;
import com.noxclient.systems.hud.HudElement;
import com.noxclient.systems.hud.HudElementInfo;
import com.noxclient.systems.hud.HudRenderer;
import com.noxclient.utils.misc.FakeClientPlayer;
import com.noxclient.utils.render.color.SettingColor;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

public class PlayerModelHud extends HudElement {
    public static final HudElementInfo<PlayerModelHud> INFO = new HudElementInfo<>(Hud.GROUP, "player-model", "Displays a model of your player.", PlayerModelHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgBackground = settings.createGroup("Background");

    // General

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("The scale.")
        .defaultValue(2)
        .min(1)
        .sliderRange(1, 5)
        .build()
    );

    private final Setting<Boolean> copyYaw = sgGeneral.add(new BoolSetting.Builder()
        .name("copy-yaw")
        .description("Makes the player model's yaw equal to yours.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> copyPitch = sgGeneral.add(new BoolSetting.Builder()
        .name("copy-pitch")
        .description("Makes the player model's pitch equal to yours.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> customYaw = sgGeneral.add(new IntSetting.Builder()
        .name("custom-yaw")
        .description("Custom yaw for when copy yaw is off.")
        .defaultValue(0)
        .range(-180, 180)
        .sliderRange(-180, 180)
        .visible(() -> !copyYaw.get())
        .build()
    );

    private final Setting<Integer> customPitch = sgGeneral.add(new IntSetting.Builder()
        .name("custom-pitch")
        .description("Custom pitch for when copy pitch is off.")
        .defaultValue(0)
        .range(-90, 90)
        .sliderRange(-90, 90)
        .visible(() -> !copyPitch.get())
        .build()
    );

    // Background

    private final Setting<Boolean> background = sgBackground.add(new BoolSetting.Builder()
        .name("background")
        .description("Displays background.")
        .defaultValue(false)
        .build()
    );

    private final Setting<SettingColor> backgroundColor = sgBackground.add(new ColorSetting.Builder()
        .name("background-color")
        .description("Color used for the background.")
        .visible(background::get)
        .defaultValue(new SettingColor(25, 25, 25, 50))
        .build()
    );

    public PlayerModelHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        setSize(50 * scale.get(), 75 * scale.get());

        renderer.post(() -> {
            PlayerEntity player = NoxClient.mc.player;
            if (isInEditor()) player = FakeClientPlayer.getPlayer();

            float yaw = copyYaw.get() ? MathHelper.wrapDegrees(player.prevYaw + (player.getYaw() - player.prevYaw) * NoxClient.mc.getTickDelta()) : (float) customYaw.get();
            float pitch = copyPitch.get() ? player.getPitch() : (float) customPitch.get();

            InventoryScreen.drawEntity((int) (x + (25 * scale.get())), (int) (y + (66 * scale.get())), (int) (30 * scale.get()), -yaw, -pitch, player);
        });

        if (background.get()) {
            renderer.quad(this.x, this.y, getWidth(), getHeight(), backgroundColor.get());
        }
    }
}
