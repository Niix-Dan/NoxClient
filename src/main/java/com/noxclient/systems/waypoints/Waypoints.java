/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.waypoints;

import com.noxclient.NoxClient;
import com.noxclient.renderer.text.TextRenderer;
import com.noxclient.events.game.GameJoinedEvent;
import com.noxclient.events.game.GameLeftEvent;
import com.noxclient.events.render.Render2DEvent;
import com.noxclient.systems.System;
import com.noxclient.systems.Systems;
import com.noxclient.systems.modules.Modules;
import com.noxclient.systems.modules.render.WaypointsModule;
import com.noxclient.utils.Utils;
import com.noxclient.utils.files.StreamUtils;
import com.noxclient.utils.misc.NbtUtils;
import com.noxclient.utils.misc.Vec3;
import com.noxclient.utils.player.PlayerUtils;
import com.noxclient.utils.render.NametagUtils;
import com.noxclient.utils.render.color.Color;
import com.noxclient.utils.world.Dimension;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Waypoints extends System<Waypoints> implements Iterable<Waypoint> {
    public static final String[] BUILTIN_ICONS = {"square", "circle", "triangle", "star", "diamond", "skull"};
    private static final Color TEXT = new Color(255, 255, 255);

    public final Map<String, AbstractTexture> icons = new ConcurrentHashMap<>();

    public Map<String, Waypoint> waypoints = new ConcurrentHashMap<>();

    public Waypoints() {
        super(null);
    }

    public static Waypoints get() {
        return Systems.get(Waypoints.class);
    }

    @Override
    public void init() {
        File iconsFolder = new File(new File(NoxClient.FOLDER, "waypoints"), "icons");
        iconsFolder.mkdirs();

        for (String builtinIcon : BUILTIN_ICONS) {
            File iconFile = new File(iconsFolder, builtinIcon + ".png");
            if (!iconFile.exists()) copyIcon(iconFile);
        }

        File[] files = iconsFolder.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.getName().endsWith(".png")) {
                try {
                    String name = file.getName().replace(".png", "");
                    AbstractTexture texture = new NativeImageBackedTexture(NativeImage.read(new FileInputStream(file)));
                    icons.put(name, texture);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean add(Waypoint waypoint) {
        Waypoint added = waypoints.put(waypoint.name.get().toLowerCase(Locale.ROOT), waypoint);
        if (added != null) {
            save();
        }

        return added != null;
    }

    public boolean remove(Waypoint waypoint) {
        Waypoint removed = waypoints.remove(waypoint.name.get().toLowerCase(Locale.ROOT));
        if (removed != null) {
            save();
        }

        return removed != null;
    }

    public Waypoint get(String name) {
        return waypoints.get(name.toLowerCase(Locale.ROOT));
    }

    @EventHandler
    private void onGameJoined(GameJoinedEvent event) {
        load();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onGameDisconnected(GameLeftEvent event) {
        waypoints.clear();
    }

    public static boolean checkDimension(Waypoint waypoint) {
        Dimension playerDim = PlayerUtils.getDimension();
        Dimension waypointDim = waypoint.dimension.get();

        if (playerDim == waypointDim) return true;
        if (!waypoint.opposite.get()) return false;

        boolean playerOpp = playerDim == Dimension.Overworld || playerDim == Dimension.Nether;
        boolean waypointOpp = waypointDim == Dimension.Overworld || waypointDim == Dimension.Nether;

        return playerOpp && waypointOpp;
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        WaypointsModule module = Modules.get().get(WaypointsModule.class);
        if (!module.isActive()) return;

        TextRenderer text = TextRenderer.get();
        Vec3 center = new Vec3(NoxClient.mc.getWindow().getFramebufferWidth() / 2.0, NoxClient.mc.getWindow().getFramebufferHeight() / 2.0, 0);
        int textRenderDist = module.textRenderDistance.get();

        for (Waypoint waypoint : this) {
            // Continue if this waypoint should not be rendered
            if (!waypoint.visible.get() || !checkDimension(waypoint)) continue;

            // Calculate distance
            BlockPos blockPos = waypoint.getPos();
            Vec3 pos = new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
            double dist = PlayerUtils.distanceToCamera(pos.x, pos.y, pos.z);

            // Continue if this waypoint should not be rendered
            if (dist > waypoint.maxVisible.get()) continue;
            if (!NametagUtils.to2D(pos, 1)) continue;

            // Calculate alpha and distance to center of the screen
            double distToCenter = pos.distanceTo(center);
            double a = 1;

            if (dist < 20) {
                a = (dist - 10) / 10;
                if (a < 0.01) continue;
            }

            // Render
            NametagUtils.scale = waypoint.scale.get() - 0.2;
            NametagUtils.begin(pos);

            // Render icon
            waypoint.renderIcon(-16, -16, a, 32);

            // Render text if cursor is close enough
            if (distToCenter <= textRenderDist) {
                // Setup text rendering
                int preTextA = TEXT.a;
                TEXT.a *= a;
                text.begin();

                // Render name
                text.render(waypoint.name.get(), -text.getWidth(waypoint.name.get()) / 2, -16 - text.getHeight(), TEXT, true);

                // Render distance
                String distText = String.format("%d blocks", (int) Math.round(dist));
                text.render(distText, -text.getWidth(distText) / 2, 16, TEXT, true);

                // End text rendering
                text.end();
                TEXT.a = preTextA;
            }

            NametagUtils.end();
        }
    }

    @Override
    public File getFile() {
        if (!Utils.canUpdate()) return null;
        return new File(new File(NoxClient.FOLDER, "waypoints"), Utils.getWorldName() + ".nbt");
    }

    public boolean isEmpty() {
        return waypoints.isEmpty();
    }

    @Override
    public Iterator<Waypoint> iterator() {
        return waypoints.values().iterator();
    }

    public ListIterator<Waypoint> iteratorReverse() {
        return new ArrayList<>(waypoints.values()).listIterator(waypoints.size());
    }

    private void copyIcon(File file) {
        StreamUtils.copy(Waypoints.class.getResourceAsStream("/assets/" + NoxClient.MOD_ID + "/textures/icons/waypoints/" + file.getName()), file);
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.put("waypoints", NbtUtils.listToTag(waypoints.values()));
        return tag;
    }

    @Override
    public Waypoints fromTag(NbtCompound tag) {
        Map<String, Waypoint> fromNbt = NbtUtils.listFromTag(tag.getList("waypoints", 10), Waypoint::new).stream().collect(Collectors.toMap(o -> o.name.get().toLowerCase(Locale.ROOT), o -> o));
        this.waypoints = new ConcurrentHashMap<>(fromNbt);

        return this;
    }
}
