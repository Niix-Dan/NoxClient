/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.modules.render;

import com.noxclient.mixininterface.IVec3d;
import com.noxclient.renderer.ShapeMode;
import com.noxclient.settings.*;
import com.noxclient.events.packets.PacketEvent;
import com.noxclient.events.render.Render3DEvent;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import com.noxclient.utils.entity.fakeplayer.FakePlayerEntity;
import com.noxclient.utils.render.WireframeEntityRenderer;
import com.noxclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PopChams extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> onlyOne = sgGeneral.add(new BoolSetting.Builder()
        .name("only-one")
        .description("Only allow one ghost per player.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> renderTime = sgGeneral.add(new DoubleSetting.Builder()
        .name("render-time")
        .description("How long the ghost is rendered in seconds.")
        .defaultValue(1)
        .min(0.1)
        .sliderMax(6)
        .build()
    );

    private final Setting<Double> yModifier = sgGeneral.add(new DoubleSetting.Builder()
        .name("y-modifier")
        .description("How much should the Y position of the ghost change per second.")
        .defaultValue(0.75)
        .sliderRange(-4, 4)
        .build()
    );

    private final Setting<Double> scaleModifier = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale-modifier")
        .description("How much should the scale of the ghost change per second.")
        .defaultValue(-0.25)
        .sliderRange(-4, 4)
        .build()
    );

    private final Setting<Boolean> fadeOut = sgGeneral.add(new BoolSetting.Builder()
        .name("fade-out")
        .description("Fades out the color.")
        .defaultValue(true)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgGeneral.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The side color.")
        .defaultValue(new SettingColor(255, 255, 255, 25))
        .build()
    );

    private final Setting<SettingColor> lineColor = sgGeneral.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The line color.")
        .defaultValue(new SettingColor(255, 255, 255, 127))
        .build()
    );

    private final List<GhostPlayer> ghosts = new ArrayList<>();

    public PopChams() {
        super(Categories.Render, "pop-chams", "Renders a ghost where players pop totem.");
    }

    @Override
    public void onDeactivate() {
        synchronized (ghosts) {
            ghosts.clear();
        }
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (!(event.packet instanceof EntityStatusS2CPacket p)) return;
        if (p.getStatus() != 35) return;

        Entity entity = p.getEntity(mc.world);
        if (!(entity instanceof PlayerEntity player) || entity == mc.player) return;

        synchronized (ghosts) {
            if (onlyOne.get()) ghosts.removeIf(ghostPlayer -> ghostPlayer.uuid.equals(entity.getUuid()));

            ghosts.add(new GhostPlayer(player));
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        synchronized (ghosts) {
            ghosts.removeIf(ghostPlayer -> ghostPlayer.render(event));
        }
    }

    private class GhostPlayer extends FakePlayerEntity {
        private final UUID uuid;
        private double timer, scale = 1;

        public GhostPlayer(PlayerEntity player) {
            super(player, "ghost", 20, false);

            uuid = player.getUuid();
        }

        public boolean render(Render3DEvent event) {
            // Increment timer
            timer += event.frameTime;
            if (timer > renderTime.get()) return true;

            // Y Modifier
            lastRenderY = getY();
            ((IVec3d) getPos()).setY(getY() + yModifier.get() * event.frameTime);

            // Scale Modifier
            scale += scaleModifier.get() * event.frameTime;

            // Fade out
            int preSideA = sideColor.get().a;
            int preLineA = lineColor.get().a;

            if (fadeOut.get()) {
                sideColor.get().a *= 1 - timer / renderTime.get();
                lineColor.get().a *= 1 - timer / renderTime.get();
            }

            // Render
            WireframeEntityRenderer.render(event, this, scale, sideColor.get(), lineColor.get(), shapeMode.get());

            // Restore colors
            sideColor.get().a = preSideA;
            lineColor.get().a = preLineA;

            return false;
        }
    }
}
