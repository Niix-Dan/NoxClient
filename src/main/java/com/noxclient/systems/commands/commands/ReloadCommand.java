/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.noxclient.renderer.Fonts;
import com.noxclient.systems.Systems;
import com.noxclient.systems.commands.Command;
import com.noxclient.utils.network.Capes;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ReloadCommand extends Command {
    public ReloadCommand() {
        super("reload", "Reloads the config, modules, friends, macros, accounts, capes and fonts.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            Systems.load();
            Capes.init();
            Fonts.refresh();

            return SINGLE_SUCCESS;
        });
    }
}
