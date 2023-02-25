/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems;

import com.noxclient.NoxClient;
import com.noxclient.systems.accounts.Accounts;
import com.noxclient.systems.commands.Commands;
import com.noxclient.systems.config.Config;
import com.noxclient.systems.friends.Friends;
import com.noxclient.systems.hud.Hud;
import com.noxclient.systems.macros.Macros;
import com.noxclient.systems.modules.Modules;
import com.noxclient.systems.profiles.Profiles;
import com.noxclient.systems.proxies.Proxies;
import com.noxclient.systems.waypoints.Waypoints;
import com.noxclient.events.game.GameLeftEvent;
import meteordevelopment.orbit.EventHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Systems {
    @SuppressWarnings("rawtypes")
    private static final Map<Class<? extends System>, System<?>> systems = new HashMap<>();
    private static final List<Runnable> preLoadTasks = new ArrayList<>(1);

    public static void addPreLoadTask(Runnable task) {
        preLoadTasks.add(task);
    }

    public static void init() {
        System<?> config = add(new Config());
        config.init();
        config.load();

        add(new Modules());
        add(new Commands());
        add(new Friends());
        add(new Macros());
        add(new Accounts());
        add(new Waypoints());
        add(new Profiles());
        add(new Proxies());
        add(new Hud());

        NoxClient.EVENT_BUS.subscribe(Systems.class);
    }

    private static System<?> add(System<?> system) {
        systems.put(system.getClass(), system);
        NoxClient.EVENT_BUS.subscribe(system);
        system.init();

        return system;
    }

    // save/load

    @EventHandler
    private static void onGameLeft(GameLeftEvent event) {
        save();
    }

    public static void save(File folder) {
        long start = java.lang.System.currentTimeMillis();
        NoxClient.LOG.info("Saving");

        for (System<?> system : systems.values()) system.save(folder);

        NoxClient.LOG.info("Saved in {} milliseconds.", java.lang.System.currentTimeMillis() - start);
    }

    public static void save() {
        save(null);
    }

    public static void load(File folder) {
        long start = java.lang.System.currentTimeMillis();
        NoxClient.LOG.info("Loading");

        for (Runnable task : preLoadTasks) task.run();
        for (System<?> system : systems.values()) system.load(folder);

        NoxClient.LOG.info("Loaded in {} milliseconds", java.lang.System.currentTimeMillis() - start);
    }

    public static void load() {
        load(null);
    }

    @SuppressWarnings("unchecked")
    public static <T extends System<?>> T get(Class<T> klass) {
        return (T) systems.get(klass);
    }
}
