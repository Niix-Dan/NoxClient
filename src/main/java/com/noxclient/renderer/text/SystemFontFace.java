/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.renderer.text;

import com.noxclient.utils.render.FontUtils;

import java.io.InputStream;
import java.nio.file.Path;

public class SystemFontFace extends FontFace {
    private final Path path;

    public SystemFontFace(FontInfo info, Path path) {
        super(info);

        this.path = path;
    }

    @Override
    public InputStream toStream() {
        if (!path.toFile().exists()) {
            throw new RuntimeException("Tried to load font that no longer exists.");
        }

        InputStream in = FontUtils.stream(path.toFile());
        if (in == null) throw new RuntimeException("Failed to load font from " + path + ".");
        return in;
    }

    @Override
    public String toString() {
        return super.toString() + " (" + path.toString() + ")";
    }
}
