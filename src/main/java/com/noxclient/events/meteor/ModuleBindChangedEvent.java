/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.meteor;

import com.noxclient.systems.modules.Module;

public class ModuleBindChangedEvent {
    private static final ModuleBindChangedEvent INSTANCE = new ModuleBindChangedEvent();

    public Module module;

    public static ModuleBindChangedEvent get(Module module) {
        INSTANCE.module = module;
        return INSTANCE;
    }
}
