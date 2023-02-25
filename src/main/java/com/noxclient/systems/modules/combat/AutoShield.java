/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.modules.combat;

import com.noxclient.events.world.TickEvent;

import com.noxclient.settings.*;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import com.noxclient.utils.KeybindingPresser;
import com.noxclient.utils.Utils;
import com.noxclient.utils.entity.EntityUtils;
import com.noxclient.utils.entity.SortPriority;
import com.noxclient.utils.entity.TargetUtils;
import com.noxclient.utils.player.InvUtils;
import com.noxclient.utils.player.PlayerUtils;
import com.noxclient.utils.player.Rotations;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

public class AutoShield extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Object2BooleanMap<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Entities to block against.")
        .defaultValue(EntityType.PLAYER)
        .build()
    );

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The maximum range the entity can be it.")
        .defaultValue(3)
        .min(0)
        .sliderMax(6)
        .build()
    );

    private final Setting<SortPriority> priority = sgGeneral.add(new EnumSetting.Builder<SortPriority>()
        .name("priority")
        .description("How to filter targets within range.")
        .defaultValue(SortPriority.LowestDistance)
        .build()
    );

    private final KeybindingPresser keybindingPresser = new KeybindingPresser(mc.options.useKey);
    private final List<Entity> targets = new ArrayList<>();

    public AutoShield() {
        super(Categories.Combat, "shield", "Automatically blocks damage.");
    }

    @Override
    public void onDeactivate() {
        keybindingPresser.stopIfPressed();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!InvUtils.testInHands(Items.SHIELD)) keybindingPresser.stopIfPressed();
        TargetUtils.getList(targets, this::entityCheck, priority.get(), 1);

        if (!mc.options.attackKey.isPressed() && !targets.isEmpty()) {
            Entity target = targets.get(0);
            if (!EntityUtils.blockedByShield(target, mc.player)) Rotations.rotate(Rotations.getYaw(target), Rotations.getPitch(target));
            keybindingPresser.use();
        } else {
            boolean using = keybindingPresser.isPressed();
            keybindingPresser.stopIfPressed();
            if (mc.options.attackKey.isPressed()) {
                if (using) Utils.leftClick();
            }
        }
    }

    private boolean entityCheck(Entity entity) {
        if (entity.equals(mc.player) || entity.equals(mc.cameraEntity)) return false;
        if ((entity instanceof LivingEntity && ((LivingEntity) entity).isDead()) || !entity.isAlive()) return false;
        if (PlayerUtils.distanceTo(entity) > range.get()) return false;
        return entities.get().getBoolean(entity.getType());
    }
}
