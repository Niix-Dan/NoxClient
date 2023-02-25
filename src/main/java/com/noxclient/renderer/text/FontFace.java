/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.renderer.text;

import java.io.InputStream;

public abstract class FontFace {
    public final FontInfo info;

    protected FontFace(FontInfo info) {
        this.info = info;
    }

    public abstract InputStream toStream();

    @Override
    public String toString() {
        return info.toString();
    }
}
