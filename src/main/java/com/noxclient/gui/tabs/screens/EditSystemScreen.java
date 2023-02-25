/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.tabs.screens;

import com.noxclient.gui.widgets.containers.WContainer;
import com.noxclient.gui.widgets.pressable.WButton;
import com.noxclient.gui.GuiTheme;
import com.noxclient.gui.WindowScreen;
import com.noxclient.settings.Settings;

public abstract class EditSystemScreen<T> extends WindowScreen {
    private WContainer settingsContainer;
    protected final T value;
    protected final boolean isNew;
    private final Runnable reload;

    public EditSystemScreen(GuiTheme theme, T value, Runnable reload) {
        super(theme, value == null ? "New" : "Edit");

        this.isNew = value == null;
        this.value = isNew ? create() : value;
        this.reload = reload;
    }

    @Override
    public void initWidgets() {
        settingsContainer = add(theme.verticalList()).expandX().minWidth(400).widget();
        settingsContainer.add(theme.settings(getSettings())).expandX();

        add(theme.horizontalSeparator()).expandX();

        WButton done = add(theme.button(isNew ? "Create" : "Save")).expandX().widget();
        done.action = () -> {
            if (save()) close();
        };

        enterAction = done.action;
    }

    @Override
    public void tick() {
        getSettings().tick(settingsContainer, theme);
    }

    @Override
    protected void onClosed() {
        if (reload != null) reload.run();
    }

    public abstract T create();
    public abstract boolean save();
    public abstract Settings getSettings();
}
