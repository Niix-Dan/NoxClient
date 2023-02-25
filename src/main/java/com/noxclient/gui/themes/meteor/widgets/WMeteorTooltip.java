/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.themes.meteor.widgets;

import com.noxclient.gui.renderer.GuiRenderer;
import com.noxclient.gui.widgets.WTooltip;
import com.noxclient.gui.themes.meteor.MeteorWidget;

public class WMeteorTooltip extends WTooltip implements MeteorWidget {
    public WMeteorTooltip(String text) {
        super(text);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        renderer.quad(this, theme().backgroundColor.get());
    }
}
