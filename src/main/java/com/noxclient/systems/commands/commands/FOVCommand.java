/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.commands.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.noxclient.systems.commands.Command;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class FOVCommand extends Command {
    public FOVCommand() {
        super("fov", "Changes your FOV.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("fov", IntegerArgumentType.integer(0, 180)).executes(context -> {
            mc.options.getFov().setValue(context.getArgument("fov", Integer.class));
            return SINGLE_SUCCESS;
        }));
    }
}
