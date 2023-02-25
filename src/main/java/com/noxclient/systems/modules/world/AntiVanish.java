/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.modules.world;


import com.google.gson.JsonArray;
import meteordevelopment.orbit.EventHandler;



import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import com.noxclient.events.game.GameLeftEvent;
import com.noxclient.events.packets.PacketEvent;
import com.noxclient.events.world.TickEvent;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import com.noxclient.utils.network.Http;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
// credit to meteor rejects
public class AntiVanish extends Module {

    private final Queue<UUID> toLookup = new ConcurrentLinkedQueue<UUID>();
    private long lastTick = 0;

    public AntiVanish() {
        super(Categories.World, "anti-vanish", "Notifies user when a admin uses /vanish");
    }

    @Override
    public void onDeactivate() {
        toLookup.clear();
    }
    @EventHandler
    public void onLeave(GameLeftEvent event) {
        toLookup.clear();
    }

    @EventHandler
    public void onPacket(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerListS2CPacket) {
            PlayerListS2CPacket packet = (PlayerListS2CPacket) event.packet;
            if (packet.getAction() == PlayerListS2CPacket.Action.UPDATE_LATENCY) {
                try {
                    for (PlayerListS2CPacket.Entry entry : packet.getEntries()) {
                        if (mc.getNetworkHandler().getPlayerListEntry(entry.getProfile().getId()) != null)
                            continue;
                        toLookup.add(entry.getProfile().getId());
                    }
                } catch (Exception ignore) {}
            }
        }
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        long time = mc.world.getTime();
        UUID lookup;

        if (Math.abs(lastTick - time) > 100 && (lookup = toLookup.poll()) != null) {
            try {
                String name = getPlayerNameFromUUID(lookup);
                if (name != null) {
                   warning(name + " has gone into vanish.");
                }
            } catch (Exception ignore) {}
            lastTick = time;
        }
    }

    public String getPlayerNameFromUUID(UUID id) {
        try {
            final NameLookup process = new NameLookup(id, mc);
            final Thread thread = new Thread(process);
            thread.start();
            thread.join();
            return process.getName();
        } catch (Exception ignored) {
            return null;
        }
    }

    public static class NameLookup implements Runnable {
        private final String uuidstr;
        private final UUID uuid;
        private final MinecraftClient mc;
        private volatile String name;

        public NameLookup(final String input, MinecraftClient mc) {
            this.uuidstr = input;
            this.uuid = UUID.fromString(input);
            this.mc = mc;
        }

        public NameLookup(final UUID input, MinecraftClient mc) {
            this.uuid = input;
            this.uuidstr = input.toString();
            this.mc = mc;
        }

        @Override
        public void run() {
            name = this.lookUpName();
        }

        public String lookUpName() {
            PlayerEntity player = null;
            if (mc.world != null) {
                player = mc.world.getPlayerByUuid(uuid);
            }
            if (player == null) {
                final String url = "https://api.mojang.com/user/profiles/" + uuidstr.replace("-", "") + "/names";
                try {
                    JsonArray res = Http.get(url).sendJson(JsonArray.class);
                    return res.get(res.size() - 1).getAsJsonObject().get("name").getAsString();
                } catch (Exception e) {
                    return uuidstr;
                }
            }
            return player.getName().getString();
        }

        public String getName() {
            return this.name;
        }
    }
}

