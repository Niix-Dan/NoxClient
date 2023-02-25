/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.renderer.packer;

public class TextureRegion {
    public double x1, y1;
    public double x2, y2;

    public double diagonal;

    public TextureRegion(double width, double height) {
        diagonal = Math.sqrt(width * width + height * height);
    }
}
