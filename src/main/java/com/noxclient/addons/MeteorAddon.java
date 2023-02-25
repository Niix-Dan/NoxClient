/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.addons;

import com.noxclient.utils.render.color.Color;

public abstract class MeteorAddon {
    /** This field is automatically assigned from fabric.mod.json file. */
    public String name;

    /** This field is automatically assigned from fabric.mod.json file. */
    public String[] authors;

    /** This field is automatically assigned from the meteor-client:color property in fabric.mod.json file. */
    public final Color color = new Color(255, 255, 255);

    public abstract void onInitialize();

    public void onRegisterCategories() {}

    public abstract String getPackage();

    public String getWebsite() {
        return null;
    }

    public GithubRepo getRepo() {
        return null;
    }

    public String getCommit() {
        return null;
    }
}
