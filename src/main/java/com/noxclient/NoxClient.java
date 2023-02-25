/*
 * This file is part of the Nox Client.
 */

package com.noxclient;

import com.noxclient.gui.GuiThemes;
import com.noxclient.gui.WidgetScreen;
import com.noxclient.gui.tabs.Tabs;
import com.noxclient.addons.AddonManager;
import com.noxclient.addons.MeteorAddon;
import com.noxclient.events.game.OpenScreenEvent;
import com.noxclient.events.meteor.KeyEvent;
import com.noxclient.events.meteor.MouseButtonEvent;
import com.noxclient.events.world.TickEvent;
import com.noxclient.systems.Systems;
import com.noxclient.systems.config.Config;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Modules;
import com.noxclient.systems.modules.misc.DiscordRPC;
import com.noxclient.utils.PostInit;
import com.noxclient.utils.PreInit;
import com.noxclient.utils.ReflectInit;
import com.noxclient.utils.Utils;
import com.noxclient.utils.misc.Version;
import com.noxclient.utils.misc.input.KeyAction;
import com.noxclient.utils.misc.input.KeyBinds;
import com.noxclient.utils.network.OnlinePlayers;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;

public class NoxClient implements ClientModInitializer {
    public static final String MOD_ID = "nox-client";
    public static final ModMetadata MOD_META = FabricLoader.getInstance().getModContainer(MOD_ID).get().getMetadata();
    public final static Version VERSION;
    public final static String DEV_BUILD;
    public static MeteorAddon ADDON;

    public static MinecraftClient mc;
    public static NoxClient INSTANCE;
    public static MinecraftClient CLIENT;
    public static final IEventBus EVENT_BUS = new EventBus();
    public static final File FOLDER = new File(FabricLoader.getInstance().getGameDir().toString(), MOD_ID);
    public static final Logger LOG = LoggerFactory.getLogger("Nox Client");

    static {
        String versionString = MOD_META.getVersion().getFriendlyString();
        if (versionString.contains("-")) versionString = versionString.split("-")[0];

        VERSION = new Version(versionString);
        DEV_BUILD = MOD_META.getCustomValue(NoxClient.MOD_ID + ":devbuild").getAsString();
    }
// a
    @Override
    public void onInitializeClient() {
        if (INSTANCE == null) {
            INSTANCE = this;
            return;
        }


        LOG.info("Initializing Nox Client");

        // Global minecraft client accessor
        mc = MinecraftClient.getInstance();
        CLIENT = MinecraftClient.getInstance();

        // Pre-load
        if (!FOLDER.exists()) {
            FOLDER.getParentFile().mkdirs();
            FOLDER.mkdir();
            Systems.addPreLoadTask(() -> Modules.get().get(DiscordRPC.class).toggle());

        }

        // Register addons
        AddonManager.init();

        // Register event handlers
        EVENT_BUS.registerLambdaFactory(ADDON.getPackage(), (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
        AddonManager.ADDONS.forEach(addon -> EVENT_BUS.registerLambdaFactory(addon.getPackage(), (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup())));

        // Register init classes
        ReflectInit.registerPackages();

        // Pre init
        ReflectInit.init(PreInit.class);

        // Register module categories
        Categories.init();

        // Load systems
        Systems.init();



        // Subscribe after systems are loaded
        EVENT_BUS.subscribe(this);

        // Initialise addons
        AddonManager.ADDONS.forEach(MeteorAddon::onInitialize);

        // Sort modules after addons have added their own
        Modules.get().sortModules();

        // Load configs
        Systems.load();

        // Post init
        ReflectInit.init(PostInit.class);

        // Save on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            OnlinePlayers.leave();
            Systems.save();
            GuiThemes.save();
        }));


    }



    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.currentScreen == null && mc.getOverlay() == null && KeyBinds.OPEN_COMMANDS.wasPressed()) {
            mc.setScreen(new ChatScreen(Config.get().prefix.get()));
        }

    }

    @EventHandler
    private void onKey(KeyEvent event) {
        if (event.action == KeyAction.Press && KeyBinds.OPEN_GUI.matchesKey(event.key, 0)) {
            openGui();
        }
    }

    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action == KeyAction.Press && KeyBinds.OPEN_GUI.matchesMouse(event.button)) {
            openGui();

        }
    }

    private void openGui() {
        if (Utils.canOpenGui()) Tabs.get().get(0).openScreen(GuiThemes.get());
    }

    // Hide HUD

    private boolean wasWidgetScreen, wasHudHiddenRoot;

    @EventHandler(priority = EventPriority.LOWEST)
    private void onOpenScreen(OpenScreenEvent event) {
        boolean hideHud = GuiThemes.get().hideHUD();

        if (hideHud) {
            if (!wasWidgetScreen) wasHudHiddenRoot = mc.options.hudHidden;

            if (event.screen instanceof WidgetScreen) mc.options.hudHidden = true;
            else if (!wasHudHiddenRoot) mc.options.hudHidden = false;
        }

        wasWidgetScreen = event.screen instanceof WidgetScreen;
    }
}
