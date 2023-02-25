package com.noxclient.systems.modules.chat;

import com.noxclient.NoxClient;
import com.noxclient.events.game.SendMessageEvent;
import com.noxclient.settings.Setting;
import com.noxclient.settings.SettingGroup;
import com.noxclient.settings.StringListSetting;
import com.noxclient.settings.StringSetting;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PlayerListEntry;

import java.util.List;

public class GroupChat extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // General

    private final Setting<List<String>> players = sgGeneral.add(new StringListSetting.Builder()
        .name("players")
        .description("Determines which players to message.")
        .build()
    );

    private final Setting<String> command = sgGeneral.add(new StringSetting.Builder()
        .name("command")
        .description("How the message command is set up on the server.")
        .defaultValue("/msg %player% %message%")
        .build()
    );

    public GroupChat() {
        super(Categories.Chat, "group-chat", "Talks with people in groups privately using /msg.");
    }

    @EventHandler
    private void onMessageSend(SendMessageEvent event) {
        for(String playerString: players.get()) {
            for(PlayerListEntry onlinePlayer: NoxClient.mc.getNetworkHandler().getPlayerList()) {
                if (onlinePlayer.getProfile().getName().equalsIgnoreCase(playerString)) {
                    NoxClient.mc.player.sendChatMessage(command.get().replace("%player%", onlinePlayer.getProfile().getName()).replace("%message%", event.message), null);
                    break;
                }
            }
        }

        event.cancel();
    }
}
