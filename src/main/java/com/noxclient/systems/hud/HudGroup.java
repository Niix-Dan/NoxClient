/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.hud;

import java.util.Objects;

public class HudGroup {
    public final String title;

    public HudGroup(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HudGroup hudGroup = (HudGroup) o;
        return Objects.equals(title, hudGroup.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title);
    }
}
