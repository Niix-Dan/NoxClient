/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.utils.entity.fakeplayer;

import com.noxclient.NoxClient;

import java.util.ArrayList;
import java.util.List;

public class FakePlayerManager {
    private static final List<FakePlayerEntity> fakePlayers = new ArrayList<>();

    public static void add(String name, float health, boolean copyInv) {
        FakePlayerEntity fakePlayer = new FakePlayerEntity(NoxClient.mc.player, name, health, copyInv);
        fakePlayer.spawn();

        fakePlayers.add(fakePlayer);
    }

    public static void clear() {
        if (fakePlayers.isEmpty()) return;
        fakePlayers.forEach(FakePlayerEntity::despawn);
        fakePlayers.clear();
    }

    public static List<FakePlayerEntity> getPlayers() {
        return fakePlayers;
    }

    public static int size() {
        return fakePlayers.size();
    }
}
