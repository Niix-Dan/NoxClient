/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.widgets;

public class WVerticalSeparator extends WWidget {
    @Override
    protected void onCalculateSize() {
        width = theme.scale(3);
        height = 1;
    }
}
