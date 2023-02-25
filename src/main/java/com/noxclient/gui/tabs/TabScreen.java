/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.tabs;

import com.noxclient.gui.utils.Cell;
import com.noxclient.gui.widgets.WWidget;
import com.noxclient.gui.GuiTheme;
import com.noxclient.gui.WidgetScreen;

public abstract class TabScreen extends WidgetScreen {
    public final Tab tab;

    public TabScreen(GuiTheme theme, Tab tab) {
        super(theme, tab.name);

        this.tab = tab;
    }

    public <T extends WWidget> Cell<T> addDirect(T widget) {
        return super.add(widget);
    }
}
