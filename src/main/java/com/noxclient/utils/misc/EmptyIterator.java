/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.utils.misc;

import java.util.Iterator;

public class EmptyIterator<T> implements Iterator<T> {
    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public T next() {
        return null;
    }
}
