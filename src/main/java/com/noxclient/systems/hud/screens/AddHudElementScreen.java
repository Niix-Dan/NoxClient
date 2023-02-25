/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.hud.screens;

import com.noxclient.NoxClient;
import com.noxclient.gui.GuiTheme;
import com.noxclient.gui.WindowScreen;
import com.noxclient.gui.widgets.WLabel;
import com.noxclient.gui.widgets.containers.WHorizontalList;
import com.noxclient.gui.widgets.containers.WSection;
import com.noxclient.gui.widgets.input.WTextBox;
import com.noxclient.gui.widgets.pressable.WButton;
import com.noxclient.gui.widgets.pressable.WPlus;
import com.noxclient.systems.hud.Hud;
import com.noxclient.systems.hud.HudElementInfo;
import com.noxclient.systems.hud.HudGroup;
import com.noxclient.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.noxclient.NoxClient.mc;

public class AddHudElementScreen extends WindowScreen {
    private final int x, y;
    private final WTextBox searchBar;

    private Object firstObject;

    public AddHudElementScreen(GuiTheme theme, int x, int y) {
        super(theme, "Add Hud element");

        this.x = x;
        this.y = y;

        searchBar = theme.textBox("");
        searchBar.action = () -> {
            clear();
            initWidgets();
        };

        enterAction = () -> runObject(firstObject);
    }

    @Override
    public void initWidgets() {
        firstObject = null;

        // Search bar
        add(searchBar).expandX();
        searchBar.setFocused(true);

        // Group infos
        Hud hud = Hud.get();
        Map<HudGroup, List<Item>> grouped = new HashMap<>();

        for (HudElementInfo<?> info : hud.infos.values()) {
            if (info.hasPresets() && !searchBar.get().isEmpty()) {
                for (HudElementInfo<?>.Preset preset : info.presets) {
                    String title = info.title + "  -  " + preset.title;
                    if (Utils.searchTextDefault(title, searchBar.get(), false)) grouped.computeIfAbsent(info.group, hudGroup -> new ArrayList<>()).add(new Item(title, info.description, preset));
                }
            }
            else if (Utils.searchTextDefault(info.title, searchBar.get(), false)) grouped.computeIfAbsent(info.group, hudGroup -> new ArrayList<>()).add(new Item(info.title, info.description, info));
        }

        // Create widgets
        for (HudGroup group : grouped.keySet()) {
            WSection section = add(theme.section(group.title)).expandX().widget();

            for (Item item : grouped.get(group)) {
                WHorizontalList l = section.add(theme.horizontalList()).expandX().widget();

                WLabel title = l.add(theme.label(item.title)).widget();
                title.tooltip = item.description;

                if (item.object instanceof HudElementInfo.Preset preset) {
                    WPlus add = l.add(theme.plus()).expandCellX().right().widget();
                    add.action = () -> runObject(preset);

                    if (firstObject == null) firstObject = preset;
                }
                else {
                    HudElementInfo<?> info = (HudElementInfo<?>) item.object;

                    if (info.hasPresets()) {
                        WButton open = l.add(theme.button(" > ")).expandCellX().right().widget();
                        open.action = () -> runObject(info);
                    }
                    else {
                        WPlus add = l.add(theme.plus()).expandCellX().right().widget();
                        add.action = () -> runObject(info);
                    }

                    if (firstObject == null) firstObject = info;
                }
            }
        }
    }

    private void runObject(Object object) {
        if (object instanceof HudElementInfo.Preset preset) {
            Hud.get().add(preset, x, y);
            close();
        }
        else {
            HudElementInfo<?> info = (HudElementInfo<?>) object;

            if (info.hasPresets()) {
                HudElementPresetsScreen screen = new HudElementPresetsScreen(theme, info, x, y);
                screen.parent = parent;

                mc.setScreen(screen);
            }
            else {
                Hud.get().add(info, x, y);
                close();
            }
        }
    }

    @Override
    protected void onRenderBefore(float delta) {
        HudEditorScreen.renderElements();
    }

    private record Item(String title, String description, Object object) {}
}
