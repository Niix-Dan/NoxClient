/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.utils.render.prompts;

import com.mojang.blaze3d.systems.RenderSystem;
import com.noxclient.NoxClient;
import com.noxclient.gui.GuiTheme;
import com.noxclient.gui.GuiThemes;
import com.noxclient.gui.WindowScreen;
import com.noxclient.gui.widgets.containers.WHorizontalList;
import com.noxclient.gui.widgets.pressable.WButton;
import com.noxclient.gui.widgets.pressable.WCheckbox;
import com.noxclient.systems.config.Config;
import net.minecraft.client.gui.screen.Screen;

import java.util.ArrayList;
import java.util.List;

public class YesNoPrompt {
    private final GuiTheme theme;
    private final Screen parent;

    private String title = "";
    private final List<String> messages = new ArrayList<>();
    private String id = null;

    private Runnable onYes = () -> {};
    private Runnable onNo = () -> {};

    private YesNoPrompt() {
        this(GuiThemes.get(), NoxClient.mc.currentScreen);
    }

    private YesNoPrompt(GuiTheme theme, Screen parent) {
        this.theme = theme;
        this.parent = parent;
    }

    public static YesNoPrompt create() {
        return new YesNoPrompt();
    }

    public static YesNoPrompt create(GuiTheme theme, Screen parent) {
        return new YesNoPrompt(theme, parent);
    }

    public YesNoPrompt title(String title) {
        this.title = title;
        return this;
    }

    public YesNoPrompt message(String message) {
        this.messages.add(message);
        return this;
    }

    public YesNoPrompt message(String message, Object... args) {
        this.messages.add(String.format(message, args));
        return this;
    }

    public YesNoPrompt id(String from) {
        this.id = from;
        return this;
    }

    public YesNoPrompt onYes(Runnable action) {
        this.onYes = action;
        return this;
    }

    public YesNoPrompt onNo(Runnable action) {
        this.onNo = action;
        return this;
    }

    public void show() {
        if (id == null) this.id(this.title);
        if (Config.get().dontShowAgainPrompts.contains(id)) return;

        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> NoxClient.mc.setScreen(new PromptScreen(theme)));
        }
        else {
            NoxClient.mc.setScreen(new PromptScreen(theme));
        }
    }

    private class PromptScreen extends WindowScreen {
        public PromptScreen(GuiTheme theme) {
            super(theme, YesNoPrompt.this.title);

            this.parent = YesNoPrompt.this.parent;
        }

        @Override
        public void initWidgets() {
            for (String line : messages) add(theme.label(line)).expandX();
            add(theme.horizontalSeparator()).expandX();

            WHorizontalList checkboxContainer = add(theme.horizontalList()).expandX().widget();
            WCheckbox dontShowAgainCheckbox = checkboxContainer.add(theme.checkbox(false)).widget();
            checkboxContainer.add(theme.label("Don't show this again.")).expandX();

            WHorizontalList list = add(theme.horizontalList()).expandX().widget();

            WButton yesButton = list.add(theme.button("Yes")).expandX().widget();
            yesButton.action = () -> {
                if (dontShowAgainCheckbox.checked) Config.get().dontShowAgainPrompts.add(id);
                onYes.run();
                close();
            };

            WButton noButton = list.add(theme.button("No")).expandX().widget();
            noButton.action = () -> {
                if (dontShowAgainCheckbox.checked) Config.get().dontShowAgainPrompts.add(id);
                onNo.run();
                close();
            };
        }
    }
}
