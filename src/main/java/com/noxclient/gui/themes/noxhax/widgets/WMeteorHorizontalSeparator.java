/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.themes.noxhax.widgets;

import com.noxclient.gui.renderer.GuiRenderer;
import com.noxclient.gui.widgets.WHorizontalSeparator;

import com.noxclient.gui.themes.noxhax.NoxHaxGuiTheme;
import com.noxclient.gui.themes.noxhax.NoxHaxWidget;

public class WMeteorHorizontalSeparator extends WHorizontalSeparator implements NoxHaxWidget {
    public WMeteorHorizontalSeparator(String text) {
        super(text);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (text == null) renderWithoutText(renderer);
        else renderWithText(renderer);
    }

    private void renderWithoutText(GuiRenderer renderer) {
        NoxHaxGuiTheme theme = theme();
        double s = theme.scale(1);
        double w = width / 2;

        renderer.quad(x, y + s, w, s, theme.separatorEdges.get(), theme.separatorCenter.get());
        renderer.quad(x + w, y + s, w, s, theme.separatorCenter.get(), theme.separatorEdges.get());
    }

    private void renderWithText(GuiRenderer renderer) {
        NoxHaxGuiTheme theme = theme();
        double s = theme.scale(2);
        double h = theme.scale(1);

        double textStart = Math.round(width / 2.0 - textWidth / 2.0 - s);
        double textEnd = s + textStart + textWidth + s;

        double offsetY = Math.round(height / 2.0);

        renderer.quad(x, y + offsetY, textStart, h, theme.separatorEdges.get(), theme.separatorCenter.get());
        renderer.text(text, x + textStart + s, y, theme.separatorText.get(), false);
        renderer.quad(x + textEnd, y + offsetY, width - textEnd, h, theme.separatorCenter.get(), theme.separatorEdges.get());
    }
}
