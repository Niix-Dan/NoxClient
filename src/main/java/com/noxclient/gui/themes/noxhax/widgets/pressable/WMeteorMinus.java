/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.themes.noxhax.widgets.pressable;

import com.noxclient.gui.renderer.GuiRenderer;
import com.noxclient.gui.widgets.pressable.WMinus;

import com.noxclient.gui.themes.noxhax.NoxHaxWidget;

public class WMeteorMinus extends WMinus implements NoxHaxWidget {
    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double pad = pad();
        double s = theme.scale(3);

        renderBackground(renderer, this, pressed, mouseOver);
        renderer.quad(x + pad, y + height / 2 - s / 2, width - pad * 2, s, theme().minusColor.get());
    }
}
