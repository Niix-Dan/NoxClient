/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.modules.misc;

import com.noxclient.settings.BoolSetting;
import com.noxclient.settings.Setting;
import com.noxclient.settings.SettingGroup;
import com.noxclient.events.meteor.MouseButtonEvent;
import com.noxclient.settings.StringSetting;
import com.noxclient.systems.friends.Friend;
import com.noxclient.systems.friends.Friends;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import com.noxclient.utils.misc.input.KeyAction;
import com.noxclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE;

public class MiddleClickFriend extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> message = sgGeneral.add(new BoolSetting.Builder()
        .name("message")
        .description("Sends a message to the player when you add them as a friend.")
        .defaultValue(false)
        .build()
    );

    private final Setting<String> msg = sgGeneral.add(new StringSetting.Builder()
        .name("message-content")
        .description("The content of the message.")
        .defaultValue("/msg %player% I just friended you on NoxClient.")
        .visible(message::get)
        .build()
    );

    public MiddleClickFriend() {
        super(Categories.Misc, "middle-click-friend", "Adds or removes a player as a friend via middle click.");
    }

    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action == KeyAction.Press && event.button == GLFW_MOUSE_BUTTON_MIDDLE && mc.currentScreen == null && mc.targetedEntity != null && mc.targetedEntity instanceof PlayerEntity player) {
            if (!Friends.get().isFriend(player)) {
                Friends.get().add(new Friend(player));
                ChatUtils.info("added " + player.getEntityName() + " as a friend.");

                if(message.get()) {
                    ChatUtils.sendPlayerMsg(msg.get().replaceAll("%player%", player.getEntityName()));
                }
            } else {
                ChatUtils.info("removed " + player.getEntityName() + " as a friend.");
                Friends.get().remove(Friends.get().get(player));
            }
        }
    }
}
