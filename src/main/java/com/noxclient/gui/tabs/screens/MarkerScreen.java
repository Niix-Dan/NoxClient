/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.tabs.screens;

import com.noxclient.gui.utils.Cell;
import com.noxclient.gui.widgets.WWidget;
import com.noxclient.gui.widgets.containers.WContainer;
import com.noxclient.gui.GuiTheme;
import com.noxclient.gui.WindowScreen;
import com.noxclient.systems.modules.render.marker.BaseMarker;

public class MarkerScreen extends WindowScreen {
    private final BaseMarker marker;
    private WContainer settingsContainer;

    public MarkerScreen(GuiTheme theme, BaseMarker marker) {
        super(theme, marker.name.get());

        this.marker = marker;
    }

    @Override
    public void initWidgets() {
        // Settings
        if (marker.settings.groups.size() > 0) {
            settingsContainer = add(theme.verticalList()).expandX().widget();
            settingsContainer.add(theme.settings(marker.settings)).expandX();
        }

        // Custom widget
        WWidget widget = getWidget(theme);

        if (widget != null) {
            add(theme.horizontalSeparator()).expandX();
            Cell<WWidget> cell = add(widget);
            if (widget instanceof WContainer) cell.expandX();
        }
    }

    @Override
    public void tick() {
        super.tick();

        marker.settings.tick(settingsContainer, theme);
    }

    public WWidget getWidget(GuiTheme theme) {
        return null;
    }
}
