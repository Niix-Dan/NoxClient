package com.noxclient.systems.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.noxclient.systems.commands.Command;
import com.noxclient.systems.commands.Commands;
import com.noxclient.utils.player.InvUtils;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.Objects;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class DupeCommand extends Command {
    private static final SimpleCommandExceptionType NOT_SPECTATOR = new SimpleCommandExceptionType(Text.literal("Can't drop items while in spectator."));
    private static final SimpleCommandExceptionType NO_SUCH_ITEM = new SimpleCommandExceptionType(Text.literal("Could not find an item with that name!"));

    public DupeCommand() {
        super("dupe", "Duplicates the item you are holding.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("popbob").executes(context -> popbob()));
        builder.then(literal("11/11").executes(context -> _11()));
    }

    private int popbob() {
        new Thread(() -> {
            for (int i = 0; i < ((9 * 4) - 1); i++) {
                mc.player.getInventory().setStack(i, mc.player.getInventory().getMainHandStack().copy());
                for (int ii = 0; ii < 64; ii++) {
                    mc.player.getInventory().getStack(i).setCount(ii);
                    try {
                        Thread.sleep(10);
                    } catch(InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }).start();
        return SINGLE_SUCCESS;
    }

    private int _11() {
        mc.player.dropSelectedItem(true);
        mc.getNetworkHandler().getConnection().disconnect(Text.of("[NoxClient Controlled Disconnect] Reconnect real quick please"));
        return SINGLE_SUCCESS;
    }

    // Separate interface so exceptions can be thrown from it (which is not the case for Consumer)
    @FunctionalInterface
    private interface PlayerConsumer {
        void accept(ClientPlayerEntity player) throws CommandSyntaxException;
    }

}
