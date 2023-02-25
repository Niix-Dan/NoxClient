/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.mixin;

import net.minecraft.client.resource.ResourceReloadLogger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ResourceReloadLogger.class)
public interface ResourceReloadLoggerAccessor {
    @Accessor("reloadState")
    ResourceReloadLogger.ReloadState getReloadState();
}
