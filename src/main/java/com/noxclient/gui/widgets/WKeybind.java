/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.widgets;

import com.noxclient.gui.renderer.GuiRenderer;
import com.noxclient.gui.widgets.containers.WHorizontalList;
import com.noxclient.gui.widgets.pressable.WButton;
import com.noxclient.systems.modules.Modules;
import com.noxclient.utils.misc.Keybind;

public class WKeybind extends WHorizontalList {
    public Runnable action;
    public Runnable actionOnSet;

    private WLabel label;

    private final Keybind keybind;
    private final Keybind defaultValue;
    private boolean listening;

    public WKeybind(Keybind keybind, Keybind defaultValue) {
        this.keybind = keybind;
        this.defaultValue = defaultValue;
    }

    @Override
    public void init() {
        label = add(theme.label("")).widget();

        WButton set = add(theme.button("Set")).widget();
        set.action = () -> {
            listening = true;
            label.set(appendBindText("..."));

            if (actionOnSet != null) actionOnSet.run();
        };

        WButton reset = add(theme.button(GuiRenderer.RESET)).expandCellX().right().widget();
        reset.action = this::resetBind;

        refreshLabel();
    }

    public boolean onAction(boolean isKey, int value) {
        if (listening && keybind.canBindTo(isKey, value)) {
            keybind.set(isKey, value);
            reset();

            return true;
        }

        return false;
    }

    public void resetBind() {
        keybind.set(defaultValue);
        reset();
    }

    public void reset() {
        listening = false;
        refreshLabel();
        if (Modules.get().isBinding()) {
            Modules.get().setModuleToBind(null);
        }
    }

    private void refreshLabel() {
        label.set(appendBindText(keybind.toString()));
    }

    private String appendBindText(String text) {
        return "Bind: " + text;
    }
}
