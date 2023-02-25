package com.noxclient.systems.commands.commands;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.noxclient.systems.commands.Command;
import com.noxclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class LavaTimeCalculator extends Command {
    public LavaTimeCalculator() {
        super("lavacalc", "Calculates amount of time for lava to flow down. Based on a 45 degree straight staircase at 20 ticks/second.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("TopY", FloatArgumentType.floatArg()).executes(ctx -> {
            float TopY = FloatArgumentType.getFloat(ctx, "TopY");
            float time = (((TopY-64)*60)/20);



            ChatUtils.sendMsg(Text.of("Lava will take "+time+" seconds to go from Y"+TopY+" to Y64(sea level) on a 45degree staircase at 20TPS)."));
            return SINGLE_SUCCESS;
        }));
        builder.then(argument("TopY", FloatArgumentType.floatArg()).then(argument("BottomY",FloatArgumentType.floatArg()).executes(ctx -> {
            float TopY = FloatArgumentType.getFloat(ctx, "TopY");
            float BottomY = FloatArgumentType.getFloat(ctx, "BottomY");
            float time = (((TopY-BottomY)*60)/20);



            ChatUtils.sendMsg(Text.of("Lava will take "+time+" seconds to go from  Y"+TopY+" to Y"+BottomY+" on a 45degree staircase at 20TPS)."));
            return SINGLE_SUCCESS;
        })));
    }
}
