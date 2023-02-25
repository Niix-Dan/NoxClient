/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.tabs.builtin;

import com.noxclient.gui.GuiTheme;
import com.noxclient.gui.GuiThemes;
import com.noxclient.gui.tabs.Tab;
import com.noxclient.gui.tabs.TabScreen;
import net.minecraft.client.gui.screen.Screen;

public class ModulesTab extends Tab {
    public ModulesTab() {
        super("[ Modules ]");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return theme.modulesScreen();
    }

    @Override
    public boolean isScreen(Screen screen) {
        return GuiThemes.get().isModulesScreen(screen);
    }
}
