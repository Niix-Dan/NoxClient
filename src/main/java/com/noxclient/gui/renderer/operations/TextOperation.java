/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.renderer.operations;

import com.noxclient.gui.renderer.GuiRenderOperation;
import com.noxclient.renderer.text.TextRenderer;

public class TextOperation extends GuiRenderOperation<TextOperation> {
    private String text;
    private TextRenderer renderer;

    public boolean title;

    public TextOperation set(String text, TextRenderer renderer, boolean title) {
        this.text = text;
        this.renderer = renderer;
        this.title = title;

        return this;
    }

    @Override
    protected void onRun() {
        renderer.render(text, x, y, color);
    }
}
