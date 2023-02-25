/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.commands.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.noxclient.systems.commands.arguments.FriendArgumentType;
import com.noxclient.systems.commands.arguments.PlayerListEntryArgumentType;
import com.noxclient.systems.commands.Command;
import com.noxclient.systems.friends.Friend;
import com.noxclient.systems.friends.Friends;
import com.noxclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class FriendsCommand extends Command {
    public FriendsCommand() {
        super("friends", "Manages friends.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("add")
            .then(argument("player", PlayerListEntryArgumentType.create())
                .executes(context -> {
                    GameProfile profile = PlayerListEntryArgumentType.get(context).getProfile();
                    Friend friend = new Friend(profile.getName(), profile.getId());

                    if (Friends.get().add(friend)) info("Added (highlight)%s (default)to friends.", friend.name);
                    else error("Already friends with that player.");

                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("remove")
            .then(argument("friend", FriendArgumentType.create())
                .executes(context -> {
                    Friend friend = FriendArgumentType.get(context);
                    if (friend == null) {
                        error("Not friends with that player.");
                        return SINGLE_SUCCESS;
                    }

                    if (Friends.get().remove(friend)) info("Removed (highlight)%s (default)from friends.", friend.name);
                    else error("Failed to remove that friend.");

                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("list").executes(context -> {
                info("--- Friends ((highlight)%s(default)) ---", Friends.get().count());
                Friends.get().forEach(friend -> ChatUtils.info("(highlight)" + friend.name));
                return SINGLE_SUCCESS;
            })
        );
    }
}
