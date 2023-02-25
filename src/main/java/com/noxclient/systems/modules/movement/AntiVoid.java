/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.modules.movement;

import com.noxclient.settings.EnumSetting;
import com.noxclient.settings.Setting;
import com.noxclient.settings.SettingGroup;
import com.noxclient.events.world.TickEvent;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import com.noxclient.systems.modules.Modules;
import com.noxclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;

public class AntiVoid extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
            .name("mode")
            .description("The method to prevent you from falling into the void.")
            .defaultValue(Mode.Jump)
            .onChanged(a -> onActivate())
            .build()
    );

    private boolean wasFlightEnabled, hasRun;

    public AntiVoid() {
        super(Categories.Movement, "anti-void", "Attempts to prevent you from falling into the void.");
    }

    @Override
    public void onActivate() {
        if (mode.get() == Mode.Flight) wasFlightEnabled = Modules.get().isActive(Flight.class);
    }

    @Override
    public void onDeactivate() {
        if (!wasFlightEnabled && mode.get() == Mode.Flight && Utils.canUpdate() && Modules.get().isActive(Flight.class)) {
            Modules.get().get(Flight.class).toggle();
        }
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        int minY = mc.world.getBottomY();

        if (mc.player.getY() > minY || mc.player.getY() < minY - 15) {
            if (hasRun && mode.get() == Mode.Flight && Modules.get().isActive(Flight.class)) {
                Modules.get().get(Flight.class).toggle();
                hasRun = false;
            }
            return;
        }

        switch (mode.get()) {
            case Flight -> {
                if (!Modules.get().isActive(Flight.class)) Modules.get().get(Flight.class).toggle();
                hasRun = true;
            }
            case Jump -> mc.player.jump();
        }
    }

    public enum Mode {
        Flight,
        Jump
    }
}
