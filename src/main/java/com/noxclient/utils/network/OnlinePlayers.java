/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.utils.network;

public class OnlinePlayers {
    private static long lastPingTime;

    public static void update() {
        long time = System.currentTimeMillis();

        if (time - lastPingTime > 5 * 60 * 1000) {
            MeteorExecutor.execute(() -> Http.post("https://meteorclient.com/api/online/ping").send());

            lastPingTime = time;
        }
    }

    public static void leave() {
        MeteorExecutor.execute(() -> Http.post("https://meteorclient.com/api/online/leave").send());
    }
}
