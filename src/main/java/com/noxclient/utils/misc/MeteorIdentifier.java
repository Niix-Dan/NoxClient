/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.utils.misc;

import com.noxclient.NoxClient;
import net.minecraft.util.Identifier;

public class MeteorIdentifier extends Identifier {
    public MeteorIdentifier(String path) {
        super(NoxClient.MOD_ID, path);
    }
}
