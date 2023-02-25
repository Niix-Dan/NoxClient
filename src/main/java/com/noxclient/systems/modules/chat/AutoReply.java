package com.noxclient.systems.modules.chat;

import com.noxclient.events.packets.PacketEvent;
import com.noxclient.settings.*;
import com.noxclient.systems.friends.Friends;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import com.noxclient.utils.misc.Placeholders;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AutoReply extends Module {

    private final SettingGroup sgKills = settings.getDefaultGroup();

    // Deaths

    private final Setting<List<String>> deathMessages = sgKills.add(new StringListSetting.Builder()
        .name("messages")
        .description("Custom messages to reply (message|reply).")
        .defaultValue(Arrays.asList( "nox|ontop" ))
        .build()
    );

    public AutoReply() {
        super(Categories.Chat, "auto-reply", "Auto reply specific chat messages.");
    }

    @EventHandler
    public void onPacketReadMessage(PacketEvent.Receive event) {
        if (!(event.packet instanceof GameMessageS2CPacket) || mc.player == null || mc.world == null) return;
        if (deathMessages.get().isEmpty()) return;

        String message = ((GameMessageS2CPacket) event.packet).content().getString();

        for(String msg : deathMessages.get()) {
            if(msg.split("|")[0].equals(message)) {
                mc.player.sendChatMessage(msg.split("|")[1], null);
            }
        }
    }
}
