package com.noxclient.systems.modules.combat;

import com.noxclient.events.render.Render3DEvent;
import com.noxclient.events.world.TickEvent;
import com.noxclient.renderer.ShapeMode;
import com.noxclient.settings.*;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import com.noxclient.utils.BEntityUtils;
import com.noxclient.utils.BPlayerUtils;
import com.noxclient.utils.entity.EntityUtils;
import com.noxclient.utils.entity.SortPriority;
import com.noxclient.utils.entity.TargetUtils;
import com.noxclient.utils.player.FindItemResult;
import com.noxclient.utils.player.InvUtils;
import com.noxclient.utils.player.Rotations;
import com.noxclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class BurrowBreaker extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");


    // General
    private final Setting<Double> targetRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("target-range")
        .description("The radius in which players get targeted.")
        .defaultValue(5)
        .min(0)
        .sliderMax(7)
        .build()
    );

    private final Setting<Double> mineRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("mine-range")
        .description("How far away can you mine the burrow block.")
        .defaultValue(4.5)
        .min(0)
        .sliderMax(7)
        .build()
    );

    private final Setting<Boolean> autoSwitch = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-switch")
        .description("Auto switches to a pickaxe when enabled.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Automatically rotates you towards the city block.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> retry = sgGeneral.add(new BoolSetting.Builder()
        .name("retry")
        .description("Retry mining the block after a certain ticks if there is still a block.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> retryTicks = sgGeneral.add(new IntSetting.Builder()
        .name("retry-ticks")
        .description("The amount of ticks before retrying.")
        .defaultValue(50)
        .min(1)
        .sliderRange(1,100)
        .visible(retry::get)
        .build()
    );

    private final Setting<Boolean> chatInfo = sgGeneral.add(new BoolSetting.Builder()
        .name("chat-info")
        .description("Sends a message when it is trying to burrow mine someone.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> debug = sgGeneral.add(new BoolSetting.Builder()
        .name("debug")
        .defaultValue(false)
        .build()
    );


    // Rendering
    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("render")
        .description("Renders a block overlay where the obsidian will be placed.")
        .defaultValue(true)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The color of the sides of the blocks being rendered.")
        .defaultValue(new SettingColor(230, 75, 100, 10))
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The color of the lines of the blocks being rendered.")
        .defaultValue(new SettingColor(230, 75, 100, 255))
        .build()
    );


    public BurrowBreaker() {
        super(Categories.Combat, "burrow-breaker", "Automatically mines enemy's burrow block.");
    }


    private PlayerEntity target;
    private BlockPos blockPosTarget;
    private boolean mining;
    private boolean sentMessage;
    private int ticksPassed;


    @Override
    public void onActivate() {
        mining = false;
        sentMessage = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (TargetUtils.isBadTarget(target, targetRange.get())) {
            PlayerEntity search = TargetUtils.getPlayerTarget(targetRange.get(), SortPriority.LowestDistance);
            if (search != target) sentMessage = false;
            target = search;
        }

        if (TargetUtils.isBadTarget(target, targetRange.get())) {
            target = null;
            blockPosTarget = null;
            toggle();
            return;
        }

        if (BEntityUtils.isBurrowed(target, BEntityUtils.BlastResistantType.Mineable)) {
            blockPosTarget = BEntityUtils.playerPos(target);
        } else blockPosTarget = null;

        if (blockPosTarget == null) {
            error("No target block found... disabling.");
            toggle();
            mining = false;
            target = null;
            return;
        }

        if (BPlayerUtils.distanceFromEye(blockPosTarget) > mineRange.get()) {
            error("Target block out of reach... disabling.");
            toggle();
            return;
        }

        FindItemResult pickaxe = InvUtils.find(itemStack -> itemStack.getItem() == Items.DIAMOND_PICKAXE || itemStack.getItem() == Items.NETHERITE_PICKAXE);

        if (!pickaxe.isHotbar()) {
            error("No pickaxe found... disabling.");
            toggle();
            return;
        }

        if (autoSwitch.get()) InvUtils.swap(pickaxe.slot(), false);

        if (rotate.get()) Rotations.rotate(Rotations.getYaw(blockPosTarget), Rotations.getPitch(blockPosTarget), () -> mine(blockPosTarget));
        else mine(blockPosTarget);

        if (!sentMessage && chatInfo.get()) {
            warning("Attempting to burrow mine %s.", target.getEntityName());
            sentMessage = true;
        }

        if (retry.get()) {
            if (ticksPassed > 0) ticksPassed--;
            if (blockPosTarget != null && ticksPassed <= 0) {
                warning("Retrying...");
                toggle();
                toggle();
            }
        }

        if (debug.get()) info(String.valueOf(ticksPassed));

    }


    private void mine(BlockPos blockPos) {
        if (!mining) {
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.UP));
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));
            mining = true;
            ticksPassed = retryTicks.get();
        }
    }

    @Override
    public String getInfoString() {
        return EntityUtils.getName(target);
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!render.get() || blockPosTarget == null || !mining) return;
        event.renderer.box(blockPosTarget, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
    }
}
