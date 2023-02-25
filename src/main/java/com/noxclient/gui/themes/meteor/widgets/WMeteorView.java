/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.themes.meteor.widgets;

import com.noxclient.gui.renderer.GuiRenderer;
import com.noxclient.gui.widgets.containers.WView;
import com.noxclient.gui.themes.meteor.MeteorWidget;

public class WMeteorView extends WView implements MeteorWidget {
    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (canScroll && hasScrollBar) {
            renderer.quad(handleX(), handleY(), handleWidth(), handleHeight(), theme().scrollbarColor.get(handlePressed, handleMouseOver));
        }
    }
}
