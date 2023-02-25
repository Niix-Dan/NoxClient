/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.themes.noxhax.widgets.input;

import com.noxclient.gui.renderer.GuiRenderer;
import com.noxclient.gui.widgets.input.WSlider;

import com.noxclient.gui.themes.noxhax.NoxHaxGuiTheme;
import com.noxclient.gui.themes.noxhax.NoxHaxWidget;

public class WMeteorSlider extends WSlider implements NoxHaxWidget {
    public WMeteorSlider(double value, double min, double max) {
        super(value, min, max);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double valueWidth = valueWidth();

        renderBar(renderer, valueWidth);
        renderHandle(renderer, valueWidth);
    }

    private void renderBar(GuiRenderer renderer, double valueWidth) {
        NoxHaxGuiTheme theme = theme();

        double s = theme.scale(3);
        double handleSize = handleSize();

        double x = this.x + handleSize / 2;
        double y = this.y + height / 2 - s / 2;

        renderer.quad(x, y, valueWidth, s, theme.sliderLeft.get());
        renderer.quad(x + valueWidth, y, width - valueWidth - handleSize, s, theme.sliderRight.get());
    }

    private void renderHandle(GuiRenderer renderer, double valueWidth) {
        NoxHaxGuiTheme theme = theme();
        double s = handleSize();

        renderer.quad(x + valueWidth, y, s, s, GuiRenderer.CIRCLE, theme.sliderHandle.get(dragging, handleMouseOver));
    }
}
