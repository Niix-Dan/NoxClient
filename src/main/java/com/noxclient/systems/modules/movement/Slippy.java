/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.modules.movement;

import com.noxclient.settings.BlockListSetting;
import com.noxclient.settings.DoubleSetting;
import com.noxclient.settings.Setting;
import com.noxclient.settings.SettingGroup;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import net.minecraft.block.Block;

import java.util.List;

public class Slippy extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Double> slippness = sgGeneral.add(new DoubleSetting.Builder()
            .name("slippness")
            .description("Decide how slippery blocks should be")
            .min(0.0)
            .max(1.10)
            .sliderMax(1.10)
            .defaultValue(1.02)
            .build()
    );

    public final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
            .name("ignored blocks")
            .description("Decide which blocks not to slip on")
            .defaultValue()
            .build()
    );

    public Slippy() {
        super(Categories.Movement, "slippy", "Makes blocks slippery like ice.");
    }
}
