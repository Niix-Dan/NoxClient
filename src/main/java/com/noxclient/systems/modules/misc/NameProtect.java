/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.modules.misc;

import com.noxclient.settings.Setting;
import com.noxclient.settings.SettingGroup;
import com.noxclient.settings.StringSetting;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;

public class NameProtect extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> name = sgGeneral.add(new StringSetting.Builder()
            .name("name")
            .description("Name to be replaced with.")
            .defaultValue("seasnail")
            .build()
    );

    private String username = "If you see this, something is wrong.";

    public NameProtect() {
        super(Categories.Player, "name-protect", "Hides your name client-side.");
    }

    @Override
    public void onActivate() {
        username = mc.getSession().getUsername();
    }

    public String replaceName(String string) {
        if (string != null && isActive()) {
            return string.replace(username, name.get());
        }

        return string;
    }

    public String getName(String original) {
        if (name.get().length() > 0 && isActive()) {
            return name.get();
        }

        return original;
    }
}
