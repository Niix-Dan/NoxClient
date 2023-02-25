/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.hud;

import com.noxclient.settings.Settings;
import com.noxclient.systems.hud.screens.HudEditorScreen;
import com.noxclient.utils.Utils;
import com.noxclient.utils.misc.ISerializable;
import com.noxclient.utils.other.Snapper;
import net.minecraft.nbt.NbtCompound;

public abstract class HudElement implements Snapper.Element, ISerializable<HudElement> {
    public final HudElementInfo<?> info;
    private boolean active;

    public final Settings settings = new Settings();
    public final HudBox box = new HudBox(this);

    public boolean autoAnchors = true;
    public int x, y;

    public HudElement(HudElementInfo<?> info) {
        this.info = info;
        this.active = true;
    }

    public boolean isActive() {
        return active;
    }

    public void toggle() {
        active = !active;
    }

    public void setSize(double width, double height) {
        box.setSize(width, height);
    }

    @Override
    public void setPos(int x, int y) {
        if (autoAnchors) {
            box.setPos(x, y);
            box.xAnchor = XAnchor.Left;
            box.yAnchor = YAnchor.Top;
            box.updateAnchors();
        }
        else {
            box.setPos(box.x + (x - this.x), box.y + (y - this.y));
        }

        updatePos();
    }

    @Override
    public void move(int deltaX, int deltaY) {
        box.move(deltaX, deltaY);
        updatePos();
    }

    public void updatePos() {
        x = box.getRenderX();
        y = box.getRenderY();
    }

    protected double alignX(double width, Alignment alignment) {
        return box.alignX(getWidth(), width, alignment);
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return box.width;
    }

    @Override
    public int getHeight() {
        return box.height;
    }

    protected boolean isInEditor() {
        return !Utils.canUpdate() || HudEditorScreen.isOpen();
    }

    public void remove() {
        Hud.get().remove(this);
    }

    public void tick(HudRenderer renderer) {}

    public void render(HudRenderer renderer) {}

    public void onFontChanged() {}

    // Serialization

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.putString("name", info.name);
        tag.putBoolean("active", active);

        tag.put("settings", settings.toTag());
        tag.put("box", box.toTag());

        tag.putBoolean("autoAnchors", autoAnchors);

        return tag;
    }

    @Override
    public HudElement fromTag(NbtCompound tag) {
        settings.reset();

        active = tag.getBoolean("active");

        settings.fromTag(tag.getCompound("settings"));
        box.fromTag(tag.getCompound("box"));

        autoAnchors = tag.getBoolean("autoAnchors");

        x = box.getRenderX();
        y = box.getRenderY();

        return this;
    }
}
