/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.tabs.screens;

import com.noxclient.gui.utils.Cell;
import com.noxclient.gui.widgets.WHorizontalSeparator;
import com.noxclient.gui.widgets.WKeybind;
import com.noxclient.gui.widgets.WWidget;
import com.noxclient.gui.widgets.containers.WContainer;
import com.noxclient.gui.widgets.containers.WHorizontalList;
import com.noxclient.gui.widgets.containers.WSection;
import com.noxclient.gui.widgets.pressable.WButton;
import com.noxclient.gui.widgets.pressable.WCheckbox;
import com.noxclient.gui.widgets.pressable.WFavorite;
import com.noxclient.events.meteor.ModuleBindChangedEvent;
import com.noxclient.gui.GuiTheme;
import com.noxclient.gui.WindowScreen;
import com.noxclient.systems.modules.Module;
import com.noxclient.systems.modules.Modules;
import com.noxclient.utils.misc.NbtUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.nbt.NbtCompound;
import com.noxclient.utils.Utils;

public class ModuleScreen extends WindowScreen {
    private final Module module;

    private WContainer settingsContainer;
    private WKeybind keybind;

    public ModuleScreen(GuiTheme theme, Module module) {
        super(theme, theme.favorite(module.favorite), module.title);
        ((WFavorite) window.icon).action = () -> module.favorite = ((WFavorite) window.icon).checked;

        this.module = module;
    }

    @Override
    public void initWidgets() {
        // Description
        add(theme.label(module.description, Utils.getWindowWidth() / 2.0));

        // Settings
        if (module.settings.groups.size() > 0) {
            settingsContainer = add(theme.verticalList()).expandX().widget();
            settingsContainer.add(theme.settings(module.settings)).expandX();
        }

        // Custom widget
        WWidget widget = module.getWidget(theme);

        if (widget != null) {
            add(theme.horizontalSeparator()).expandX();
            Cell<WWidget> cell = add(widget);
            if (widget instanceof WContainer) cell.expandX();
        }

        // Bind
        WSection section = add(theme.section("Bind", true)).expandX().widget();
        keybind = section.add(theme.keybind(module.keybind)).expandX().widget();
        keybind.actionOnSet = () -> Modules.get().setModuleToBind(module);

        // Toggle on bind release
        WHorizontalList tobr = section.add(theme.horizontalList()).widget();

        tobr.add(theme.label("Toggle on bind release: "));
        WCheckbox tobrC = tobr.add(theme.checkbox(module.toggleOnBindRelease)).widget();
        tobrC.action = () -> module.toggleOnBindRelease = tobrC.checked;

        // Chat feedback
        WHorizontalList cf = section.add(theme.horizontalList()).widget();

        cf.add(theme.label("Chat Feedback: "));
        WCheckbox cfC = cf.add(theme.checkbox(module.chatFeedback)).widget();
        cfC.action = () -> module.chatFeedback = cfC.checked;


        // Bottom
        WSection bottom = add(theme.section("", true)).expandX().widget();

        // Clipboard
        WHorizontalList clip = bottom.add(theme.horizontalList()).expandX().widget();
        clip.add(theme.label("NBT Data: "));
        WButton toclip = clip.add(theme.button("Copy")).widget();
        WButton fromclip = clip.add(theme.button("Paste")).widget();

        toclip.action = () -> toClipboard();
        fromclip.action = () -> fromClipboard();


        //   Active
        WHorizontalList actv = bottom.add(theme.horizontalList()).expandX().widget();
        actv.add(theme.label("Active: "));

        WCheckbox active = actv.add(theme.checkbox(module.isActive())).expandCellX().widget();
        active.action = () -> {
            if (module.isActive() != active.checked) module.toggle();
        };
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !Modules.get().isBinding();
    }

    @Override
    public void tick() {
        super.tick();

        module.settings.tick(settingsContainer, theme);
    }

    @EventHandler
    private void onModuleBindChanged(ModuleBindChangedEvent event) {
        keybind.reset();
    }

    @Override
    public boolean toClipboard() {
        return NbtUtils.toClipboard(module.title, module.toTag());
    }

    @Override
    public boolean fromClipboard() {
        NbtCompound clipboard = NbtUtils.fromClipboard(module.toTag());

        if (clipboard != null) {
            module.fromTag(clipboard);
            return true;
        }

        return false;
    }
}
