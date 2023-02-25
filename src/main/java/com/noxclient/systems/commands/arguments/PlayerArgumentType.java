/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.noxclient.NoxClient;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PlayerArgumentType implements ArgumentType<PlayerEntity> {
    private static final DynamicCommandExceptionType NO_SUCH_PLAYER = new DynamicCommandExceptionType(name -> Text.literal("Player with name " + name + " doesn't exist."));

    private static final Collection<String> EXAMPLES = List.of("seasnail8169", "MineGame159");

    public static PlayerArgumentType create() {
        return new PlayerArgumentType();
    }

    public static PlayerEntity get(CommandContext<?> context) {
        return context.getArgument("player", PlayerEntity.class);
    }

    @Override
    public PlayerEntity parse(StringReader reader) throws CommandSyntaxException {
        String argument = reader.readString();
        PlayerEntity playerEntity = null;

        for (PlayerEntity p : NoxClient.mc.world.getPlayers()) {
            if (p.getEntityName().equalsIgnoreCase(argument)) {
                playerEntity = p;
                break;
            }
        }
        if (playerEntity == null) throw NO_SUCH_PLAYER.create(argument);

        return playerEntity;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(NoxClient.mc.world.getPlayers().stream().map(PlayerEntity::getEntityName), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
