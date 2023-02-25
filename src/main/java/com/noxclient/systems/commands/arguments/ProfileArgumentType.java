/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.commands.arguments;

import com.google.common.collect.Streams;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.noxclient.systems.profiles.Profile;
import com.noxclient.systems.profiles.Profiles;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.command.CommandSource.suggestMatching;

public class ProfileArgumentType implements ArgumentType<String> {
    private static final DynamicCommandExceptionType NO_SUCH_PROFILE = new DynamicCommandExceptionType(name -> Text.literal("Profile with name " + name + " doesn't exist."));

    private static final Collection<String> EXAMPLES = List.of("pvp.meteorclient.com", "anarchy");

    public static ProfileArgumentType create() {
        return new ProfileArgumentType();
    }

    public static Profile get(CommandContext<?> context) {
        return Profiles.get().get(context.getArgument("profile", String.class));
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String argument = reader.getRemaining();
        reader.setCursor(reader.getTotalLength());
        if (Profiles.get().get(argument) == null) throw NO_SUCH_PROFILE.create(argument);

        return argument;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return suggestMatching(Streams.stream(Profiles.get()).map(profile -> profile.name.get()), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
