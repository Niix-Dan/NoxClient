/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.themes.meteor.widgets.pressable;

import com.noxclient.gui.widgets.pressable.WFavorite;
import com.noxclient.gui.themes.meteor.MeteorWidget;
import com.noxclient.utils.render.color.Color;

public class WMeteorFavorite extends WFavorite implements MeteorWidget {
    public WMeteorFavorite(boolean checked) {
        super(checked);
    }

    @Override
    protected Color getColor() {
        return theme().favoriteColor.get();
    }
}
