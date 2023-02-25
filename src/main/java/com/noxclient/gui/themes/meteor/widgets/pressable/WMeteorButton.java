/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.themes.meteor.widgets.pressable;

import com.noxclient.gui.renderer.GuiRenderer;
import com.noxclient.gui.renderer.packer.GuiTexture;
import com.noxclient.gui.widgets.pressable.WButton;
import com.noxclient.gui.themes.meteor.MeteorGuiTheme;
import com.noxclient.gui.themes.meteor.MeteorWidget;

public class WMeteorButton extends WButton implements MeteorWidget {
    public WMeteorButton(String text, GuiTexture texture) {
        super(text, texture);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        MeteorGuiTheme theme = theme();
        double pad = pad();

        renderBackground(renderer, this, pressed, mouseOver);

        if (text != null) {
            renderer.text(text, x + width / 2 - textWidth / 2, y + pad, theme.textColor.get(), false);
        }
        else {
            double ts = theme.textHeight();
            renderer.quad(x + width / 2 - ts / 2, y + pad, ts, ts, texture, theme.textColor.get());
        }
    }
}
