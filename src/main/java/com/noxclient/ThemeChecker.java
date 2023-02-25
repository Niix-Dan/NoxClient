/*
 * This file is part of the Nox Client.
 */

package com.noxclient;

import com.noxclient.gui.GuiThemes;
import meteordevelopment.discordipc.DiscordIPC;
import meteordevelopment.discordipc.RichPresence;

public class ThemeChecker {
    public static void ThemeCeck() {
        final RichPresence rpc = new RichPresence();

        if (GuiThemes.get().name.equals("NoxHax")) {

            DiscordIPC.start(1070086047880458370L, null);

            rpc.setStart(System.currentTimeMillis() / 1000L);

            String largeText = "Nox Client " + NoxClient.VERSION;
            rpc.setLargeImage("icon", largeText);

            rpc.setDetails("Nox Client v"  + NoxClient.VERSION);

            rpc.setState("Playing with NoxClient");
        } else {
            DiscordIPC.start(1070086047880458370L, null);

            rpc.setStart(System.currentTimeMillis() / 1000L);

            rpc.setDetails("Nox Client v" + NoxClient.VERSION);

            rpc.setState("Playing with NoxClient!");
        }
    }
}
