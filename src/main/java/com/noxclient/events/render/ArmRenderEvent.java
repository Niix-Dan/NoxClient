/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.events.render;


import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;

public class ArmRenderEvent {
    public static ArmRenderEvent INSTANCE = new ArmRenderEvent();

    public MatrixStack matrix;
    public Hand hand;

    public static ArmRenderEvent get(Hand hand, MatrixStack matrices) {
        INSTANCE.matrix = matrices;
        INSTANCE.hand = hand;

        return INSTANCE;
    }
}
