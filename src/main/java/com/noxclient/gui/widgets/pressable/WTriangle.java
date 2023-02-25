/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.widgets.pressable;

public abstract class WTriangle extends WPressable {
    public double rotation;

    @Override
    protected void onCalculateSize() {
        double s = theme.textHeight();

        width = s;
        height = s;
    }
}
