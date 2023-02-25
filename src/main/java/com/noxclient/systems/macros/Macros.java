/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.macros;

import com.noxclient.NoxClient;
import com.noxclient.events.meteor.KeyEvent;
import com.noxclient.events.meteor.MouseButtonEvent;
import com.noxclient.systems.System;
import com.noxclient.systems.Systems;
import com.noxclient.utils.misc.NbtUtils;
import com.noxclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.nbt.NbtCompound;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Macros extends System<Macros> implements Iterable<Macro> {
    private List<Macro> macros = new ArrayList<>();

    public Macros() {
        super("macros");
    }

    public static Macros get() {
        return Systems.get(Macros.class);
    }

    public void add(Macro macro) {
        macros.add(macro);
        NoxClient.EVENT_BUS.subscribe(macro);
        save();
    }

    public List<Macro> getAll() {
        return macros;
    }

    public void remove(Macro macro) {
        if (macros.remove(macro)) {
            NoxClient.EVENT_BUS.unsubscribe(macro);
            save();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onKey(KeyEvent event) {
        if (event.action == KeyAction.Release) return;

        for (Macro macro : macros) {
            if (macro.onAction(true, event.key)) return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onButton(MouseButtonEvent event) {
        if (event.action == KeyAction.Release) return;

        for (Macro macro : macros) {
            if (macro.onAction(false, event.button)) return;
        }
    }

    public boolean isEmpty() {
        return macros.isEmpty();
    }

    @Override
    public Iterator<Macro> iterator() {
        return macros.iterator();
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.put("macros", NbtUtils.listToTag(macros));
        return tag;
    }

    @Override
    public Macros fromTag(NbtCompound tag) {
        for (Macro macro : macros) NoxClient.EVENT_BUS.unsubscribe(macro);

        macros = NbtUtils.listFromTag(tag.getList("macros", 10), Macro::new);

        for (Macro macro : macros) NoxClient.EVENT_BUS.subscribe(macro);
        return this;
    }
}
