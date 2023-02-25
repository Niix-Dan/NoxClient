/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.utils.render;

import net.minecraft.client.render.VertexConsumerProvider;

public interface IVertexConsumerProvider extends VertexConsumerProvider {
    void setOffset(double offsetX, double offsetY, double offsetZ);
}
