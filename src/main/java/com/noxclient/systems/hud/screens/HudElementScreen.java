/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.hud.screens;

import com.noxclient.gui.GuiTheme;
import com.noxclient.gui.WindowScreen;
import com.noxclient.gui.widgets.containers.WContainer;
import com.noxclient.gui.widgets.containers.WHorizontalList;
import com.noxclient.gui.widgets.pressable.WCheckbox;
import com.noxclient.gui.widgets.pressable.WMinus;
import com.noxclient.settings.BoolSetting;
import com.noxclient.settings.EnumSetting;
import com.noxclient.settings.SettingGroup;
import com.noxclient.settings.Settings;
import com.noxclient.systems.hud.HudElement;
import com.noxclient.systems.hud.XAnchor;
import com.noxclient.systems.hud.YAnchor;
import com.noxclient.utils.misc.NbtUtils;
import net.minecraft.nbt.NbtCompound;

import static com.noxclient.utils.Utils.getWindowWidth;

public class HudElementScreen extends WindowScreen {
    private final HudElement element;

    private WContainer settingsC1, settingsC2;
    private final Settings settings;

    public HudElementScreen(GuiTheme theme, HudElement element) {
        super(theme, element.info.title);

        this.element = element;

        settings = new Settings();
        SettingGroup sg = settings.createGroup("Anchors");
        sg.add(new BoolSetting.Builder()
            .name("auto-anchors")
            .description("Automatically assigns anchors based on the position.")
            .defaultValue(true)
            .onModuleActivated(booleanSetting -> booleanSetting.set(element.autoAnchors))
            .onChanged(aBoolean -> {
                if (aBoolean) element.box.updateAnchors();
                element.autoAnchors = aBoolean;
            })
            .build()
        );
        sg.add(new EnumSetting.Builder<XAnchor>()
            .name("x-anchor")
            .description("Horizontal anchor.")
            .defaultValue(XAnchor.Left)
            .visible(() -> !element.autoAnchors)
            .onModuleActivated(xAnchorSetting -> xAnchorSetting.set(element.box.xAnchor))
            .onChanged(element.box::setXAnchor)
            .build()
        );
        sg.add(new EnumSetting.Builder<YAnchor>()
            .name("y-anchor")
            .description("Vertical anchor.")
            .defaultValue(YAnchor.Top)
            .visible(() -> !element.autoAnchors)
            .onModuleActivated(yAnchorSetting -> yAnchorSetting.set(element.box.yAnchor))
            .onChanged(element.box::setYAnchor)
            .build()
        );
    }

    @Override
    public void initWidgets() {
        // Description
        add(theme.label(element.info.description, getWindowWidth() / 2.0));

        // Settings
        if (element.settings.sizeGroups() > 0) {
            element.settings.onActivated();

            settingsC1 = add(theme.verticalList()).expandX().widget();
            settingsC1.add(theme.settings(element.settings)).expandX();
        }

        // Anchors
        settings.onActivated();

        settingsC2 = add(theme.verticalList()).expandX().widget();
        settingsC2.add(theme.settings(settings)).expandX();

        add(theme.horizontalSeparator()).expandX();

        // Bottom
        WHorizontalList bottomList = add(theme.horizontalList()).expandX().widget();

        //   Active
        bottomList.add(theme.label("Active:"));
        WCheckbox active = bottomList.add(theme.checkbox(element.isActive())).widget();
        active.action = () -> {
            if (element.isActive() != active.checked) element.toggle();
        };

        //   Remove
        WMinus remove = bottomList.add(theme.minus()).expandCellX().right().widget();
        remove.action = () -> {
            element.remove();
            close();
        };
    }

    @Override
    public void tick() {
        super.tick();

        if (settingsC1 != null) {
            element.settings.tick(settingsC1, theme);
        }

        settings.tick(settingsC2, theme);
    }

    @Override
    protected void onRenderBefore(float delta) {
        HudEditorScreen.renderElements();
    }

    @Override
    public boolean toClipboard() {
        return NbtUtils.toClipboard(element.info.title, element.toTag());
    }

    @Override
    public boolean fromClipboard() {
        NbtCompound clipboard = NbtUtils.fromClipboard(element.toTag());

        if (clipboard != null) {
            element.fromTag(clipboard);
            return true;
        }

        return false;
    }

}

