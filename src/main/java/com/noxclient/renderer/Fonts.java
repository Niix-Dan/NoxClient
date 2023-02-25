/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.renderer;

import com.noxclient.NoxClient;
import com.noxclient.gui.WidgetScreen;
import com.noxclient.renderer.text.CustomTextRenderer;
import com.noxclient.renderer.text.FontFace;
import com.noxclient.renderer.text.FontFamily;
import com.noxclient.renderer.text.FontInfo;
import com.noxclient.events.meteor.CustomFontChangedEvent;
import com.noxclient.systems.config.Config;
import com.noxclient.utils.PreInit;
import com.noxclient.utils.render.FontUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Fonts {
    public static final String[] BUILTIN_FONTS = { "JetBrains Mono", "Comfortaa", "Tw Cen MT", "Pixelation", "reenie-beanie"};

    public static String DEFAULT_FONT_FAMILY;
    public static FontFace DEFAULT_FONT;

    public static final List<FontFamily> FONT_FAMILIES = new ArrayList<>();
    public static CustomTextRenderer RENDERER;

    @PreInit(dependencies = Shaders.class)
    public static void refresh() {
        FONT_FAMILIES.clear();

        for (String builtinFont : BUILTIN_FONTS) {
            FontUtils.loadBuiltin(FONT_FAMILIES, builtinFont);
        }

        for (String fontPath : FontUtils.getSearchPaths()) {
            FontUtils.loadSystem(FONT_FAMILIES, new File(fontPath));
        }

        FONT_FAMILIES.sort(Comparator.comparing(FontFamily::getName));

        NoxClient.LOG.info("Found {} font families.", FONT_FAMILIES.size());

        DEFAULT_FONT_FAMILY = FontUtils.getBuiltinFontInfo(BUILTIN_FONTS[1]).family();
        DEFAULT_FONT = getFamily(DEFAULT_FONT_FAMILY).get(FontInfo.Type.Regular);

        Config config = Config.get();
        load(config != null ? config.font.get() : DEFAULT_FONT);
    }

    public static void load(FontFace fontFace) {
        if (RENDERER != null && RENDERER.fontFace.equals(fontFace)) return;

        try {
            RENDERER = new CustomTextRenderer(fontFace);
            NoxClient.EVENT_BUS.post(CustomFontChangedEvent.get());
        }
        catch (Exception e) {
            if (fontFace.equals(DEFAULT_FONT)) {
                throw new RuntimeException("Failed to load default font: " + fontFace, e);
            }

            NoxClient.LOG.error("Failed to load font: " + fontFace, e);
            load(Fonts.DEFAULT_FONT);
        }

        if (NoxClient.mc.currentScreen instanceof WidgetScreen && Config.get().customFont.get()) {
            ((WidgetScreen) NoxClient.mc.currentScreen).invalidate();
        }
    }

    public static FontFamily getFamily(String name) {
        for (FontFamily fontFamily : Fonts.FONT_FAMILIES) {
            if (fontFamily.getName().equalsIgnoreCase(name)) {
                return fontFamily;
            }
        }

        return null;
    }
}
