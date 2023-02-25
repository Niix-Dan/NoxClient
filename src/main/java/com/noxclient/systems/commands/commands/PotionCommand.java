/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.Text;
import com.noxclient.systems.commands.Command;
import com.noxclient.utils.player.ChatUtils;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
// Credit To Wurst For this
public class PotionCommand extends Command {
    public PotionCommand() {
        super("deathpotion", "Gives you a potion that kills you");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            ItemStack stack = createPotionStack();

            if(placeStackInHotbar(stack))
                ChatUtils.info("Potion Created");

            else
                ChatUtils.error("Please clear a slot in your hotbar.");
            return SINGLE_SUCCESS;
        });
    }
    private boolean placeStackInHotbar(ItemStack stack)
    {
        for(int i = 0; i < 9; i++)
        {
            if(!mc.player.getInventory().getStack(i).isEmpty())
                continue;

            mc.player.networkHandler.sendPacket(
                new CreativeInventoryActionC2SPacket(36 + i, stack));
            return true;
        }

        return false;
    }

    public ItemStack createPotionStack()
    {
        ItemStack stack = new ItemStack(Items.SPLASH_POTION);

        NbtCompound effect = new NbtCompound();
        effect.putInt("Amplifier", 125);
        effect.putInt("Duration", 2000);
        effect.putInt("Id", 6);

        NbtList effects = new NbtList();
        effects.add(effect);

        NbtCompound nbt = new NbtCompound();
        nbt.put("CustomPotionEffects", effects);
        stack.setNbt(nbt);

        String name =
            "\u00a7f" + "Potion" + " of \u00a74\u00a7lINSTANT DEATH";
        stack.setCustomName(Text.literal(name));

        return stack;
    }
}
