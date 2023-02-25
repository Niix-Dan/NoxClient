/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.modules.movement;

//Created by squidoodly 10/07/2020

import com.noxclient.mixin.ClientPlayerEntityAccessor;
import com.noxclient.mixininterface.IHorseBaseEntity;
import com.noxclient.settings.BoolSetting;
import com.noxclient.settings.Setting;
import com.noxclient.settings.SettingGroup;
import com.noxclient.events.world.TickEvent;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import com.noxclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractHorseEntity;

public class EntityControl extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> maxJump = sgGeneral.add(new BoolSetting.Builder()
            .name("max-jump")
            .description("Sets jump power to maximum.")
            .defaultValue(true)
            .build()
    );

    public EntityControl() {
        super(Categories.Movement, "entity-control", "Lets you control rideable entities without a saddle.");
    }

    @Override
    public void onDeactivate() {
        if (!Utils.canUpdate() || mc.world.getEntities() == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof AbstractHorseEntity) ((IHorseBaseEntity) entity).setSaddled(false);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof AbstractHorseEntity) ((IHorseBaseEntity) entity).setSaddled(true);
        }

        if (maxJump.get()) ((ClientPlayerEntityAccessor) mc.player).setMountJumpStrength(1);
    }
}
