/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.themes.noxhax.widgets;

import com.noxclient.gui.widgets.WTopBar;
import com.noxclient.gui.themes.noxhax.NoxHaxWidget;
import com.noxclient.utils.render.color.Color;

public class WMeteorTopBar extends WTopBar implements NoxHaxWidget {
    @Override
    protected Color getButtonColor(boolean pressed, boolean hovered) {
        return theme().backgroundColor.get(pressed, hovered);
    }

    @Override
    protected Color getNameColor() {
        return theme().textColor.get();
    }
}
