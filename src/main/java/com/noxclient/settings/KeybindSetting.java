/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.settings;

import com.noxclient.NoxClient;
import com.noxclient.gui.widgets.WKeybind;
import com.noxclient.events.meteor.KeyEvent;
import com.noxclient.events.meteor.MouseButtonEvent;
import com.noxclient.utils.misc.Keybind;
import com.noxclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.nbt.NbtCompound;

import java.util.function.Consumer;

public class KeybindSetting extends Setting<Keybind> {
    private final Runnable action;
    public WKeybind widget;

    public KeybindSetting(String name, String description, Keybind defaultValue, Consumer<Keybind> onChanged, Consumer<Setting<Keybind>> onModuleActivated, IVisible visible, Runnable action) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);

        this.action = action;
        NoxClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onKeyBinding(KeyEvent event) {
        if (event.action == KeyAction.Release && widget != null && widget.onAction(true, event.key)) event.cancel();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onMouseButtonBinding(MouseButtonEvent event) {
        if (event.action == KeyAction.Release && widget != null && widget.onAction(false, event.button)) event.cancel();
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onKey(KeyEvent event) {
        if (event.action == KeyAction.Release && get().matches(true, event.key) && (module == null || module.isActive()) && action != null) {
            action.run();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action == KeyAction.Release && get().matches(false, event.button) && (module == null || module.isActive()) && action != null) {
            action.run();
        }
    }

    @Override
    public void resetImpl() {
        if (value == null) value = defaultValue.copy();
        else value.set(defaultValue);

        if (widget != null) widget.reset();
    }

    @Override
    protected Keybind parseImpl(String str) {
        try {
            return Keybind.fromKey(Integer.parseInt(str.trim()));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @Override
    protected boolean isValueValid(Keybind value) {
        return true;
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        tag.put("value", get().toTag());

        return tag;
    }

    @Override
    public Keybind load(NbtCompound tag) {
        get().fromTag(tag.getCompound("value"));

        return get();
    }

    public static class Builder extends SettingBuilder<Builder, Keybind, KeybindSetting> {
        private Runnable action;

        public Builder() {
            super(Keybind.none());
        }

        public Builder action(Runnable action) {
            this.action = action;
            return this;
        }

        @Override
        public KeybindSetting build() {
            return new KeybindSetting(name, description, defaultValue, onChanged, onModuleActivated, visible, action);
        }
    }
}
