/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.tabs.builtin;

import com.noxclient.gui.renderer.GuiRenderer;
import com.noxclient.gui.widgets.containers.WHorizontalList;
import com.noxclient.gui.widgets.pressable.WButton;
import com.noxclient.gui.widgets.pressable.WCheckbox;
import com.noxclient.gui.GuiTheme;
import com.noxclient.gui.tabs.Tab;
import com.noxclient.gui.tabs.TabScreen;
import com.noxclient.gui.tabs.WindowTabScreen;
import com.noxclient.systems.hud.Hud;
import com.noxclient.systems.hud.screens.HudEditorScreen;
import com.noxclient.utils.misc.NbtUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.NbtCompound;
import com.noxclient.NoxClient;

public class HudTab extends Tab {
    public HudTab() {
        super("[ HUD ]");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new HudScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof HudScreen;
    }

    public static class HudScreen extends WindowTabScreen {
        private final Hud hud;

        public HudScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);

            hud = Hud.get();
            hud.settings.onActivated();
        }

        @Override
        public void initWidgets() {
            add(theme.settings(hud.settings)).expandX();

            add(theme.horizontalSeparator()).expandX();

            WButton openEditor = add(theme.button("Edit")).expandX().widget();
            openEditor.action = () -> NoxClient.mc.setScreen(new HudEditorScreen(theme));

            WHorizontalList buttons = add(theme.horizontalList()).expandX().widget();
            buttons.add(theme.button("Clear")).expandX().widget().action = hud::clear;
            buttons.add(theme.button("Reset to default elements")).expandX().widget().action = hud::resetToDefaultElements;

            add(theme.horizontalSeparator()).expandX();

            WHorizontalList bottom = add(theme.horizontalList()).expandX().widget();

            bottom.add(theme.label("Active: "));
            WCheckbox active = bottom.add(theme.checkbox(hud.active)).expandCellX().widget();
            active.action = () -> hud.active = active.checked;

            WButton resetSettings = bottom.add(theme.button(GuiRenderer.RESET)).widget();
            resetSettings.action = hud.settings::reset;
        }

        @Override
        protected void onRenderBefore(float delta) {
            HudEditorScreen.renderElements();
        }

        @Override
        public boolean toClipboard() {
            return NbtUtils.toClipboard("hud-settings", hud.settings.toTag());
        }

        @Override
        public boolean fromClipboard() {
            NbtCompound clipboard = NbtUtils.fromClipboard(hud.settings.toTag());

            if (clipboard != null) {
                hud.settings.fromTag(clipboard);
                return true;
            }

            return false;
        }
    }
}
