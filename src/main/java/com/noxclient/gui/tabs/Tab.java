/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.tabs;

import com.noxclient.NoxClient;
import com.noxclient.gui.GuiTheme;
import net.minecraft.client.gui.screen.Screen;

public abstract class Tab {
    public final String name;

    public Tab(String name) {
        this.name = name;
    }

    public void openScreen(GuiTheme theme) {
        TabScreen screen = this.createScreen(theme);
        screen.addDirect(theme.topBar()).top().centerX();
        NoxClient.mc.setScreen(screen);
    }

    public abstract TabScreen createScreen(GuiTheme theme);

    public abstract boolean isScreen(Screen screen);
}
