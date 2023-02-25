/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui;

import com.noxclient.gui.utils.Cell;
import com.noxclient.gui.widgets.WWidget;
import com.noxclient.gui.widgets.containers.WWindow;

public abstract class WindowScreen extends WidgetScreen {
    protected final WWindow window;

    public WindowScreen(GuiTheme theme, WWidget icon, String title) {
        super(theme, title);

        window = super.add(theme.window(icon, title)).center().widget();
        window.view.scrollOnlyWhenMouseOver = false;
    }

    public WindowScreen(GuiTheme theme, String title) {
        this(theme, null, title);
    }

    @Override
    public <W extends WWidget> Cell<W> add(W widget) {
        return window.add(widget);
    }

    @Override
    public void clear() {
        window.clear();
    }
}
