/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.modules.combat;

import com.noxclient.settings.DoubleSetting;
import com.noxclient.settings.EntityTypeListSetting;
import com.noxclient.settings.Setting;
import com.noxclient.settings.SettingGroup;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

public class Hitboxes extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Object2BooleanMap<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Which entities to target.")
        .defaultValue(EntityType.PLAYER)
        .build()
    );

    private final Setting<Double> value = sgGeneral.add(new DoubleSetting.Builder()
        .name("expand")
        .description("How much to expand the hitbox of the entity.")
        .defaultValue(0.5)
        .build()
    );

    public Hitboxes() {
        super(Categories.Combat, "hitboxes", "Expands an entity's hitboxes.");
    }

    public double getEntityValue(Entity entity) {
        if (!isActive()) return 0;
        if (entities.get().getBoolean(entity.getType())) return value.get();
        return 0;
    }
}
