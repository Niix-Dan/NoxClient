/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.themes.noxhax.widgets.pressable;


import com.noxclient.gui.widgets.pressable.WFavorite;
import com.noxclient.gui.themes.noxhax.NoxHaxWidget;
import com.noxclient.utils.render.color.Color;

public class WMeteorFavorite extends WFavorite implements NoxHaxWidget {
    public WMeteorFavorite(boolean checked) {
        super(checked);
    }

    @Override
    protected Color getColor() {
        return theme().favoriteColor.get();
    }
}
