/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.modules;

import com.noxclient.addons.AddonManager;
import com.noxclient.addons.MeteorAddon;
import net.minecraft.item.Items;

public class Categories {
    public static final Category Combat = new Category("Combat", Items.END_CRYSTAL.getDefaultStack());
    public static final Category Player = new Category("Player", Items.WITHER_SKELETON_SKULL.getDefaultStack());
    public static final Category Movement = new Category("Movement", Items.SPLASH_POTION.getDefaultStack());
    public static final Category Render = new Category("Render", Items.CAMPFIRE.getDefaultStack());
    public static final Category World = new Category("World", Items.OBSIDIAN.getDefaultStack());
    public static final Category Misc = new Category("Misc", Items.SNOWBALL.getDefaultStack());
    public static final Category Chat = new Category("Chat", Items.PAPER.getDefaultStack());
    public static final Category Dev = new Category("Dev", Items.COMMAND_BLOCK.getDefaultStack());

    public static boolean REGISTERING;

    public static void init() {
        REGISTERING = true;

        // Meteor
        Modules.registerCategory(Combat);
        Modules.registerCategory(Player);
        Modules.registerCategory(Movement);
        Modules.registerCategory(Render);
        Modules.registerCategory(World);
        Modules.registerCategory(Misc);
        Modules.registerCategory(Chat);
        Modules.registerCategory(Dev);

        // Addons
        AddonManager.ADDONS.forEach(MeteorAddon::onRegisterCategories);

        REGISTERING = false;
    }
}
