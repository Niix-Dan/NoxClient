/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.utils.render.color;

import com.noxclient.NoxClient;
import com.noxclient.events.world.TickEvent;
import com.noxclient.gui.GuiThemes;
import com.noxclient.gui.WidgetScreen;
import com.noxclient.settings.ColorSetting;
import com.noxclient.settings.Setting;
import com.noxclient.settings.SettingGroup;
import com.noxclient.systems.config.Config;
import com.noxclient.systems.waypoints.Waypoint;
import com.noxclient.systems.waypoints.Waypoints;
import com.noxclient.utils.PostInit;
import com.noxclient.utils.misc.UnorderedArrayList;
import meteordevelopment.orbit.EventHandler;

import java.util.List;

public class RainbowColors {
    private static final List<Setting<SettingColor>> colorSettings = new UnorderedArrayList<>();
    private static final List<Setting<List<SettingColor>>> colorListSettings = new UnorderedArrayList<>();

    private static final List<SettingColor> colors = new UnorderedArrayList<>();
    private static final List<Runnable> listeners = new UnorderedArrayList<>();

    public static final RainbowColor GLOBAL = new RainbowColor();

    @PostInit
    public static void init() {
        NoxClient.EVENT_BUS.subscribe(RainbowColors.class);
    }

    public static void addSetting(Setting<SettingColor> setting) {
        colorSettings.add(setting);
    }

    public static void addSettingList(Setting<List<SettingColor>> setting) {
        colorListSettings.add(setting);
    }

    public static void removeSetting(Setting<SettingColor> setting) {
        colorSettings.remove(setting);
    }

    public static void removeSettingList(Setting<List<SettingColor>> setting) {
        colorListSettings.remove(setting);
    }

    public static void add(SettingColor color) {
        colors.add(color);
    }

    public static void register(Runnable runnable) {
        listeners.add(runnable);
    }

    @EventHandler
    private static void onTick(TickEvent.Post event) {
        GLOBAL.setSpeed(Config.get().rainbowSpeed.get() / 100);
        GLOBAL.getNext();

        for (Setting<SettingColor> setting : colorSettings) {
            if (setting.module == null || setting.module.isActive()) setting.get().update();
        }

        for (Setting<List<SettingColor>> setting : colorListSettings) {
            if (setting.module == null || setting.module.isActive()) {
                for (SettingColor color : setting.get()) color.update();
            }
        }

        for (SettingColor color : colors) {
            color.update();
        }

        for (Waypoint waypoint : Waypoints.get()) {
            waypoint.color.get().update();
        }

        if (NoxClient.mc.currentScreen instanceof WidgetScreen) {
            for (SettingGroup group : GuiThemes.get().settings) {
                for (Setting<?> setting : group) {
                    if (setting instanceof ColorSetting) ((SettingColor) setting.get()).update();
                }
            }
        }

        for (Runnable listener : listeners) listener.run();
    }
}
