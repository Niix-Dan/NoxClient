/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.utils.misc.text;

import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.Style;

public class StringCharacterVisitor implements CharacterVisitor {

    public StringBuilder result = new StringBuilder();

    @Override
    public boolean accept(int index, Style style, int j) {
        result.append((char)j);
        return true;
    }
}
