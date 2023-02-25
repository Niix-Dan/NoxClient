package com.noxclient.systems.modules.beta;

import com.noxclient.events.entity.player.BreakBlockEvent;
import com.noxclient.events.entity.player.InteractBlockEvent;
import com.noxclient.settings.BoolSetting;
import com.noxclient.settings.Setting;
import com.noxclient.settings.SettingGroup;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class NoDesync extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> breakBlock = sgGeneral.add(new BoolSetting.Builder().name("break").description("Anti-desync for block breaking.").defaultValue(true).build());
    private final Setting<Boolean> placeBlock = sgGeneral.add(new BoolSetting.Builder().name("place").description("Anti-desync for block placing.").defaultValue(true).build());

    public NoDesync() {
        super(Categories.Player, "no-desync", "Prevent ghost block placements.");
    }


    @EventHandler
    private void onBlockPlace(InteractBlockEvent event) {
        if (!placeBlock.get()) return;
        BlockPos placePos = event.result.getBlockPos();
        if (placePos != null) mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, placePos, Direction.UP, 0));
    }

    @EventHandler
    private void onBlockBreak(BreakBlockEvent event) {
        if (!breakBlock.get()) return;
        BlockPos breakPos = event.blockPos;
        if (breakPos != null) mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, breakPos, Direction.UP, 0));
    }
}
