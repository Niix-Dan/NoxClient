/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.modules.render;

import com.noxclient.NoxClient;
import com.noxclient.gui.WidgetScreen;
import com.noxclient.renderer.Framebuffer;
import com.noxclient.renderer.GL;
import com.noxclient.renderer.PostProcessRenderer;
import com.noxclient.renderer.Shader;
import com.noxclient.settings.*;
import com.noxclient.events.game.WindowResizedEvent;
import com.noxclient.events.render.RenderAfterWorldEvent;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import meteordevelopment.orbit.listeners.ConsumerListener;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

public class Blur extends Module {
    public enum Mode {
        Fancy,
        Fast
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgScreens = settings.createGroup("Screens");

    // General

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("Which mode the blur should use.")
        .defaultValue(Mode.Fancy)
        .build()
    );

    private final Setting<Integer> radius = sgGeneral.add(new IntSetting.Builder()
        .name("radius")
        .description("How large the blur should be.")
        .defaultValue(4)
        .min(1)
        .sliderRange(1, 32)
        .build()
    );

    private final Setting<Integer> fadeTime = sgGeneral.add(new IntSetting.Builder()
        .name("fade-time")
        .description("How long the fade will last in milliseconds.")
        .defaultValue(100)
        .min(0)
        .sliderMax(500)
        .build()
    );

    // Screens

    private final Setting<Boolean> meteor = sgScreens.add(new BoolSetting.Builder()
        .name("meteor")
        .description("Applies blur to Meteor screens.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> inventories = sgScreens.add(new BoolSetting.Builder()
        .name("inventories")
        .description("Applies blur to inventory screens.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> chat = sgScreens.add(new BoolSetting.Builder()
        .name("chat")
        .description("Applies blur when in chat.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> other = sgScreens.add(new BoolSetting.Builder()
        .name("other")
        .description("Applies blur to all other screen types.")
        .defaultValue(true)
        .build()
    );

    private Shader shader;
    private Framebuffer fbo1, fbo2;

    private boolean enabled;
    private long fadeEndAt;

    public Blur() {
        super(Categories.Render, "blur", "Blurs background when in GUI screens.");

        // The listeners need to run even when the module is not enabled
        NoxClient.EVENT_BUS.subscribe(new ConsumerListener<>(WindowResizedEvent.class, event -> {
            if (fbo1 != null) {
                fbo1.resize();
                fbo2.resize();
            }
        }));

        NoxClient.EVENT_BUS.subscribe(new ConsumerListener<>(RenderAfterWorldEvent.class, event -> onRenderAfterWorld()));
    }

    private void onRenderAfterWorld() {
        // Enable / disable with fading
        boolean shouldRender = shouldRender();
        long time = System.currentTimeMillis();

        if (enabled) {
            if (!shouldRender) {
                if (fadeEndAt == -1) fadeEndAt = System.currentTimeMillis() + fadeTime.get();

                if (time >= fadeEndAt) {
                    enabled = false;
                    fadeEndAt = -1;
                }
            }
        }
        else {
            if (shouldRender) {
                enabled = true;
                fadeEndAt = System.currentTimeMillis() + fadeTime.get();
            }
        }

        if (!enabled) return;

        // Initialize shader and framebuffer if running for the first time
        if (shader == null) {
            shader = new Shader("blur.vert", "blur.frag");
            fbo1 = new Framebuffer();
            fbo2 = new Framebuffer();
        }

        // Prepare stuff for rendering
        int sourceTexture = mc.getFramebuffer().getColorAttachment();

        shader.bind();
        shader.set("u_Size", mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight());
        shader.set("u_Texture", 0);

        // Update progress
        double progress = 1;

        if (time < fadeEndAt) {
            if (shouldRender) progress = 1 - (fadeEndAt - time) / fadeTime.get().doubleValue();
            else progress = (fadeEndAt - time) / fadeTime.get().doubleValue();
        }
        else {
            fadeEndAt = -1;
        }

        // Render the blur
        shader.set("u_Radius", Math.floor(radius.get() * progress));

        PostProcessRenderer.beginRender();

        fbo1.bind();
        GL.bindTexture(sourceTexture);
        shader.set("u_Direction", 1.0, 0.0);
        PostProcessRenderer.render();

        if (mode.get() == Mode.Fancy) fbo2.bind();
        else fbo2.unbind();
        GL.bindTexture(fbo1.texture);
        shader.set("u_Direction", 0.0, 1.0);
        PostProcessRenderer.render();

        if (mode.get() == Mode.Fancy) {
            fbo1.bind();
            GL.bindTexture(fbo2.texture);
            shader.set("u_Direction", 1.0, 0.0);
            PostProcessRenderer.render();

            fbo2.unbind();
            GL.bindTexture(fbo1.texture);
            shader.set("u_Direction", 0.0, 1.0);
            PostProcessRenderer.render();
        }

        PostProcessRenderer.endRender();
    }

    private boolean shouldRender() {
        if (!isActive()) return false;
        Screen screen = mc.currentScreen;

        if (screen instanceof WidgetScreen) return meteor.get();
        if (screen instanceof HandledScreen) return inventories.get();
        if (screen instanceof ChatScreen) return chat.get();
        if (screen != null) return other.get();

        return false;
    }
}
