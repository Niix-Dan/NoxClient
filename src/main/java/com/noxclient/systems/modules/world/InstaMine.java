package com.noxclient.systems.modules.world;

import com.noxclient.events.entity.player.StartBreakingBlockEvent;
import com.noxclient.events.render.Render3DEvent;
import com.noxclient.events.world.TickEvent;
import com.noxclient.renderer.ShapeMode;
import com.noxclient.settings.*;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import com.noxclient.utils.player.Rotations;
import com.noxclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class InstaMine extends Module {
    private final BlockPos.Mutable blockPos = new BlockPos.Mutable(0, -1, 0);

    private Direction direction;

    private int ticks;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    // General

    private final Setting<Integer> tickDelay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("The delay between breaks.")
        .defaultValue(0)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );

    private final Setting<Boolean> pick = sgGeneral.add(new BoolSetting.Builder()
        .name("only-pick")
        .description("Only tries to mine the block if you are holding a pickaxe.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Faces the blocks being mined server side.")
        .defaultValue(true)
        .build()
    );

    // Render

    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("render")
        .description("Renders a block overlay on the block being broken.")
        .defaultValue(true)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("Determines how the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The color of the sides of the blocks being rendered.")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The color of the lines of the blocks being rendered.")
        .defaultValue(new SettingColor(255, 0, 0))
        .build()
    );

    public InstaMine() {
        super(Categories.World,"insta-mine", "Attempts to instantly mine blocks.");
    }

    @Override
    public void onActivate() {
        ticks = 0;
        blockPos.set(0, -1, 0);
    }

    @EventHandler
    private void onStartBreakingBlock(StartBreakingBlockEvent event) {
        direction = event.direction;
        blockPos.set(event.blockPos);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (ticks >= tickDelay.get()) {
            ticks = 0;

            if (shouldMine()) {
                if (rotate.get()) Rotations.rotate(Rotations.getYaw(blockPos), Rotations.getPitch(blockPos), () -> mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction)));
                else mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));
                mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
        } else ticks++;
    }

    private boolean shouldMine() {
        if (blockPos.getY() == -1) return false;
        if (mc.world.getBlockState(blockPos).getHardness(mc.world, blockPos) < 0) return false;
        if (mc.world.getBlockState(blockPos).isAir()) return false;
        return !pick.get() || (mc.player.getMainHandStack().getItem() == Items.DIAMOND_PICKAXE || mc.player.getMainHandStack().getItem() == Items.NETHERITE_PICKAXE);
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!render.get() || !shouldMine()) return;
        event.renderer.box(blockPos, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
    }
}
