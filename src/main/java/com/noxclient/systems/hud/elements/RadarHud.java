/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.hud.elements;

import java.util.Iterator;


import com.noxclient.NoxClient;
import com.noxclient.renderer.Renderer2D;
import com.noxclient.settings.*;
import com.noxclient.systems.modules.Modules;
import com.noxclient.systems.modules.render.ESP;
import com.noxclient.systems.waypoints.Waypoint;
import com.noxclient.systems.waypoints.Waypoints;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import com.noxclient.systems.hud.HudRenderer;
import com.noxclient.systems.hud.Hud;
import com.noxclient.systems.hud.HudElement;
import com.noxclient.systems.hud.HudElementInfo;
import com.noxclient.utils.misc.Vec3;
import com.noxclient.utils.render.color.SettingColor;

// yes
public class RadarHud extends HudElement {
    public static final HudElementInfo<RadarHud> INFO = new HudElementInfo<>(Hud.GROUP, "radar", "Draws a Radar on your HUD telling you where entities are.", RadarHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SettingColor> backgroundColor = sgGeneral.add(new ColorSetting.Builder()
            .name("background-color")
            .description("Color of background.")
            .defaultValue(new SettingColor(0, 0, 0, 64))
            .build()
    );


    private final Setting<Object2BooleanMap<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
            .name("entities")
            .description("Select specific entities.")
            .defaultValue(EntityType.PLAYER)
            .build()
    );

    private final Setting<Boolean> letters = sgGeneral.add(new BoolSetting.Builder()
            .name("letters")
            .description("Use entity's type first letter.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> showWaypoints = sgGeneral.add(new BoolSetting.Builder()
            .name("waypoints")
            .description("Show waypoints.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
            .name("scale")
            .description("The scale.")
            .defaultValue(1)
            .min(1)
            .sliderRange(0.01, 5)
            .onChanged(aDouble -> calculateSize())
        .build()
    );

    private final Setting<Double> zoom = sgGeneral.add(new DoubleSetting.Builder()
        .name("zoom")
        .description("Radar zoom.")
        .defaultValue(1)
        .min(0.01)
        .sliderRange(0.01, 3)
        .build()
    );

    public RadarHud() {
        super(INFO);
        calculateSize();
    }

    public void calculateSize() {
        setSize(200 * scale.get(), 200 * scale.get());
    }

    @Override
    public void render(HudRenderer renderer) {
        ESP esp = Modules.get().get(ESP.class);
        if (esp == null) return;
        renderer.post(() -> {
            if (NoxClient.mc.player == null) return;
            double width  = getWidth();
            double height = getHeight();
            Renderer2D.COLOR.begin();
            Renderer2D.COLOR.quad(x, y, width, height, backgroundColor.get());
            Renderer2D.COLOR.render(null);
            if (NoxClient.mc.world != null) {
                for (Entity entity : NoxClient.mc.world.getEntities()) {
                    if (!entities.get().getBoolean(entity.getType())) return;
                    double xPos = ((entity.getX() - NoxClient.mc.player.getX()) * scale.get() * zoom.get() + width/2);
                    double yPos = ((entity.getZ() - NoxClient.mc.player.getZ()) * scale.get() * zoom.get()  + height/2);
                    if (xPos < 0 || yPos < 0 || xPos > width - scale.get() || yPos > height - scale.get()) continue;
                    String icon = "*";
                    if (letters.get())
                        icon = entity.getType().getUntranslatedName().substring(0,1).toUpperCase();
                    renderer.text(icon, xPos + x, yPos + y, esp.getColor(entity), false);

                }
            }
            if (showWaypoints.get()) {
                Iterator<Waypoint> waypoints = Waypoints.get().iterator();
                while (waypoints.hasNext()) {
                    Waypoint waypoint = waypoints.next();
                    BlockPos blockPos = waypoint.getPos();
                    Vec3 c = new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
                    Vec3d coords = new Vec3d(c.x, c.y, c.z);
                    double xPos = ((coords.getX() - NoxClient.mc.player.getX()) * scale.get() * zoom.get() + width/2);
                    double yPos = ((coords.getZ() - NoxClient.mc.player.getZ()) * scale.get() * zoom.get()  + height/2);
                    if (xPos < 0 || yPos < 0 || xPos > width - scale.get() || yPos > height - scale.get()) continue;
                    String icon = "*";
                    if (letters.get() && waypoint.name.get().length() > 0)
                        icon = waypoint.name.get().substring(0, 1);
                    renderer.text(icon, xPos + x, yPos + y, waypoint.color.get(), false);
                }
            }
            Renderer2D.COLOR.render(null);
        });

    }

}
