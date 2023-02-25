/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.utils.render.postprocess;

import com.noxclient.systems.modules.Modules;
import com.noxclient.systems.modules.render.StorageESP;
import net.minecraft.entity.Entity;

public class StorageOutlineShader extends PostProcessShader {
    private static StorageESP storageESP;

    public StorageOutlineShader() {
        init("outline");
    }

    @Override
    protected void preDraw() {
        framebuffer.clear(false);
        framebuffer.beginWrite(false);
    }

    @Override
    protected boolean shouldDraw() {
        if (storageESP == null) storageESP = Modules.get().get(StorageESP.class);
        return true;
    }

    @Override
    public boolean shouldDraw(Entity entity) {
        return true;
    }

    @Override
    protected void setUniforms() {
        shader.set("u_Width", storageESP.outlineWidth.get());
        shader.set("u_FillOpacity", storageESP.fillOpacity.get() / 255.0);
        shader.set("u_ShapeMode", storageESP.shapeMode.get().ordinal());
    }
}
