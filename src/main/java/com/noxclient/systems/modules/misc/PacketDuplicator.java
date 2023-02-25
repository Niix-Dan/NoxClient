/*
 * This file is part of the Nox Client.
 */

package com.noxclient.systems.modules.misc;

import com.noxclient.settings.PacketListSetting;
import com.noxclient.settings.Setting;
import com.noxclient.settings.SettingGroup;
import com.noxclient.events.packets.PacketEvent;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import com.noxclient.utils.network.PacketUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.Packet;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class PacketDuplicator extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Set<Class<? extends Packet<?>>>> c2sPackets = sgGeneral.add(new PacketListSetting.Builder()
        .name("C2S-packets")
        .description("Client-to-server packets to duplicate.")
        .filter(aClass -> PacketUtils.getC2SPackets().contains(aClass))
        .build()
    );
    public PacketDuplicator() {
        super(Categories.Misc, "packet-duplicator", "Allows you to duplicates certain packets.");
    }

    private Set<Packet<?>> duplicatedPackets = new HashSet<>();

    private boolean onSendPacketExecuting = false;

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onSendPacket(PacketEvent.Send event) {
        try {
            if (onSendPacketExecuting) {
                return;
            }

            onSendPacketExecuting = true;

            if (c2sPackets.get().contains(event.packet.getClass())) {
                if(!duplicatedPackets.contains(event.packet)) {
                    mc.getNetworkHandler().sendPacket(event.packet);
                    duplicatedPackets.add(event.packet);
                } else {
                    duplicatedPackets.remove(event.packet);
                }
            }
        } catch(Exception err) {
            error("Packet duplicator error: "+err.getMessage());
        } finally {
            onSendPacketExecuting = false;
        }
    }
}

