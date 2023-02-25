/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.noxclient.systems.commands.Command;
import com.noxclient.systems.modules.Module;
import com.noxclient.systems.modules.Modules;
import com.noxclient.utils.Utils;
import com.noxclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.stream.Collectors;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class BindsCommand extends Command {
    public BindsCommand() {
        super("binds", "List of all bound modules.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            // Modules
            List<Module> modules = Modules.get().getAll().stream()
                    .filter(module -> module.keybind.isSet())
                    .collect(Collectors.toList());

            ChatUtils.info("--- Bound Modules ((highlight)%d(default)) ---", modules.size());

            for (Module module : modules) {
                HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, getTooltip(module));

                MutableText text = Text.literal(module.title).formatted(Formatting.WHITE);
                text.setStyle(text.getStyle().withHoverEvent(hoverEvent));

                MutableText sep = Text.literal(" - ");
                sep.setStyle(sep.getStyle().withHoverEvent(hoverEvent));
                text.append(sep.formatted(Formatting.GRAY));

                MutableText key = Text.literal(module.keybind.toString());
                key.setStyle(key.getStyle().withHoverEvent(hoverEvent));
                text.append(key.formatted(Formatting.GRAY));

                ChatUtils.sendMsg(text);
            }

            return SINGLE_SUCCESS;
        });
    }

    private MutableText getTooltip(Module module) {
        MutableText tooltip = Text.literal(Utils.nameToTitle(module.title)).formatted(Formatting.BLUE, Formatting.BOLD).append("\n\n");
        tooltip.append(Text.literal(module.description).formatted(Formatting.WHITE));

        return tooltip;
    }
}
