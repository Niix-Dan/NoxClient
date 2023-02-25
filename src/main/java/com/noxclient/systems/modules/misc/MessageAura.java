/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.modules.misc;

//Updated by squidoodly 24/07/2020

import com.noxclient.settings.BoolSetting;
import com.noxclient.settings.Setting;
import com.noxclient.settings.SettingGroup;
import com.noxclient.settings.StringSetting;
import com.noxclient.events.entity.EntityAddedEvent;
import com.noxclient.systems.friends.Friends;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import com.noxclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;

public class MessageAura extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> message = sgGeneral.add(new StringSetting.Builder()
            .name("message")
            .description("The specified message sent to the player.")
            .defaultValue("NOX ON TOP")
            .build()
    );

    private final Setting<Boolean> ignoreFriends = sgGeneral.add(new BoolSetting.Builder()
            .name("ignore-friends")
            .description("Will not send any messages to people friended.")
            .defaultValue(false)
            .build()
    );

    public MessageAura() {
        super(Categories.Misc, "message-aura", "Sends a specified message to any player that enters render distance.");
    }

    @EventHandler
    private void onEntityAdded(EntityAddedEvent event) {
        if (!(event.entity instanceof PlayerEntity) || event.entity.getUuid().equals(mc.player.getUuid())) return;

        if (!ignoreFriends.get() || (ignoreFriends.get() && !Friends.get().isFriend((PlayerEntity)event.entity))) {
            ChatUtils.sendPlayerMsg("/msg " + event.entity.getEntityName() + " " + message.get());
        }
    }
}
