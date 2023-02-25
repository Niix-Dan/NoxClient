/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.asm;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public abstract class AsmTransformer {
    public final String targetName;

    protected AsmTransformer(String targetName) {
        this.targetName = targetName;
    }

    public abstract void transform(ClassNode klass);

    protected MethodNode getMethod(ClassNode klass, MethodInfo methodInfo) {
        for (MethodNode method : klass.methods) {
            if (methodInfo.equals(method)) return method;
        }

        return null;
    }

    protected static String mapClassName(String name) {
        return FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", name.replace('/', '.'));
    }
}
