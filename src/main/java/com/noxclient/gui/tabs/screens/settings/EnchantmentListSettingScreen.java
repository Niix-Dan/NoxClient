/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.tabs.screens.settings;

import com.noxclient.gui.widgets.WWidget;
import com.noxclient.gui.GuiTheme;
import com.noxclient.settings.Setting;
import com.noxclient.utils.misc.Names;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class EnchantmentListSettingScreen extends LeftRightListSettingScreen<Enchantment> {
    public EnchantmentListSettingScreen(GuiTheme theme, Setting<List<Enchantment>> setting) {
        super(theme, "Select Enchantments", setting, setting.get(), Registry.ENCHANTMENT);
    }

    @Override
    protected WWidget getValueWidget(Enchantment value) {
        return theme.label(getValueName(value));
    }

    @Override
    protected String getValueName(Enchantment value) {
        return Names.get(value);
    }
}
