/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.themes.meteor.widgets;

import com.noxclient.gui.renderer.GuiRenderer;
import com.noxclient.gui.widgets.WWidget;
import com.noxclient.gui.widgets.containers.WWindow;
import com.noxclient.gui.themes.meteor.MeteorWidget;

public class WMeteorWindow extends WWindow implements MeteorWidget {
    public WMeteorWindow(WWidget icon, String title) {
        super(icon, title);
    }

    @Override
    protected WHeader header(WWidget icon) {
        return new WMeteorHeader(icon);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (expanded || animProgress > 0) {
            renderer.quad(x, y + header.height, width, height - header.height, theme().backgroundColor.get());
        }
    }

    private class WMeteorHeader extends WHeader {
        public WMeteorHeader(WWidget icon) {
            super(icon);
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            renderer.quad(this, theme().accentColor.get());
        }
    }
}
