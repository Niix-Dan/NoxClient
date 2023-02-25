/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.modules.render;

import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;

public class UnfocusedCPU extends Module {
    public UnfocusedCPU() {
        super(Categories.Render, "unfocused-cpu", "Will not render anything when your Minecraft window is not focused.");
    }
}
