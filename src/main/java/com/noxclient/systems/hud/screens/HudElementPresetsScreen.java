/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.hud.screens;

import com.noxclient.gui.GuiTheme;
import com.noxclient.gui.WindowScreen;
import com.noxclient.gui.widgets.containers.WHorizontalList;
import com.noxclient.gui.widgets.input.WTextBox;
import com.noxclient.gui.widgets.pressable.WPlus;
import com.noxclient.systems.hud.Hud;
import com.noxclient.systems.hud.HudElementInfo;
import com.noxclient.utils.Utils;

public class HudElementPresetsScreen extends WindowScreen {
    private final HudElementInfo<?> info;
    private final int x, y;

    private final WTextBox searchBar;
    private HudElementInfo<?>.Preset firstPreset;

    public HudElementPresetsScreen(GuiTheme theme, HudElementInfo<?> info, int x, int y) {
        super(theme, "Select preset for " + info.title);

        this.info = info;
        this.x = x + 9;
        this.y = y;

        searchBar = theme.textBox("");
        searchBar.action = () -> {
            clear();
            initWidgets();
        };

        enterAction = () -> {
            Hud.get().add(firstPreset, x, y);
            close();
        };
    }

    @Override
    public void initWidgets() {
        firstPreset = null;

        // Search bar
        add(searchBar).expandX();
        searchBar.setFocused(true);

        // Presets
        for (HudElementInfo<?>.Preset preset : info.presets) {
            if (!Utils.searchTextDefault(preset.title, searchBar.get(), false)) continue;

            WHorizontalList l = add(theme.horizontalList()).expandX().widget();

            l.add(theme.label(preset.title));

            WPlus add = l.add(theme.plus()).expandCellX().right().widget();
            add.action = () -> {
                Hud.get().add(preset, x, y);
                close();
            };

            if (firstPreset == null) firstPreset = preset;
        }
    }

    @Override
    protected void onRenderBefore(float delta) {
        HudEditorScreen.renderElements();
    }
}
