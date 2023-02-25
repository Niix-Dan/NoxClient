package com.noxclient.systems.modules.combat;

import com.noxclient.events.entity.player.FinishUsingItemEvent;
import com.noxclient.events.world.TickEvent;
import com.noxclient.mixin.ClientPlayerInteractionManagerAccessor;
import com.noxclient.settings.*;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import com.noxclient.utils.Task;
import com.noxclient.utils.entity.SortPriority;
import com.noxclient.utils.entity.TargetUtils;
import com.noxclient.utils.player.ChatUtils;
import com.noxclient.utils.player.FindItemResult;
import com.noxclient.utils.player.InvUtils;
import com.noxclient.utils.player.PlayerUtils;
import com.noxclient.utils.world.BlockUtils;
import com.noxclient.utils.world.CardinalDirection;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.noxclient.utils.BlockInfo.getState;

public class MinecartTntAura extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    //private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgNone = settings.createGroup("");


    private final Setting<Integer> targetRange = sgGeneral.add(new IntSetting.Builder().name("target-range").description("The range players can be targeted.").defaultValue(6).sliderRange(0, 7).build());
    private final Setting<Boolean> instant = sgGeneral.add(new BoolSetting.Builder().name("instant").description("Uses instamine exploit.").defaultValue(false).build());
    private final Setting<Double> breakProgress = sgGeneral.add(new DoubleSetting.Builder().name("break-progress").description("Places crystal if break progress of breaking block is higher.").defaultValue(0.95).sliderRange(0, 0.99).visible(() -> !instant.get()).build());
    private final Setting<Boolean> rightClickEat = sgGeneral.add(new BoolSetting.Builder().name("right-click-eat").description("Stops breaking the block and starts eating EGapple.").defaultValue(false).build());
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder().name("delay").description("Delay for breaking block with instamine exploit.").defaultValue(10).sliderRange(5, 20).visible(instant::get).build());
    private final Setting<Boolean> ninja = sgGeneral.add(new BoolSetting.Builder().name("ninja").description("Places obsidian above target head if its time to place crystal.").defaultValue(false).visible(instant::get).build());
    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder().name("rotate").description("Automatically faces towards the blocks being placed.").defaultValue(false).build());
    private final Setting<Boolean> debug = sgGeneral.add(new BoolSetting.Builder().name("debug").description("Sends info in chat about CEV.").defaultValue(false).build());


    public MinecartTntAura() {
        super(Categories.Dev, "minecart-tnt-aura", "Places obsidian on top of people and explodes tnt minecarts on top of their heads after destroying the obsidian.");
    }

    private FindItemResult pickaxe, minecart, obsidian, gap, rail, flint;
    private EndCrystalEntity minecartEntity;
    private BlockPos breakPos, prevPos;
    private PlayerEntity target;
    private int ticks = 0;
    private boolean isEating;

    private final Task obsidianTask = new Task();
    private final Task railTask = new Task();
    private final Task minecartTask = new Task();
    private final Task startTask = new Task();

    @Override
    public void onActivate() {
        ticks = 0;

        breakPos = null;
        prevPos = new BlockPos(0,1,0);
        minecartEntity = null;

        isEating = false;

        startTask.reset();
        reset();
    }

    @Override
    public void onDeactivate() {
        if (breakPos != null) mc.interactionManager.attackBlock(breakPos, Direction.UP);
    }

    @EventHandler
    public void onEat(FinishUsingItemEvent event) {
        if (isEating) {
            mc.options.useKey.setPressed(false);
            isEating = false;
        }
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        assert mc.player != null && mc.world != null && mc.interactionManager != null;

        pickaxe = InvUtils.findInHotbar(Items.NETHERITE_PICKAXE, Items.DIAMOND_PICKAXE);
        minecart = InvUtils.findInHotbar(Items.TNT_MINECART);
        rail = InvUtils.findInHotbar(Items.RAIL);
        obsidian = InvUtils.findInHotbar(Items.OBSIDIAN);
        flint = InvUtils.findInHotbar(Items.FLINT_AND_STEEL);

        target = TargetUtils.getPlayerTarget(targetRange.get(), SortPriority.LowestDistance);
        if (TargetUtils.isBadTarget(target, targetRange.get())) {
            warning("Target is null");
            toggle();
            return;
        }

        if (!pickaxe.found() || !minecart.found() || !obsidian.found() || !rail.found()) {
            ChatUtils.info("Items: "
                + (!pickaxe.found() ? Formatting.RED + "pickaxe, " : Formatting.GREEN + "pickaxe, ")
                + (!minecart.found() ? Formatting.RED + "tnt-minecart, " : Formatting.GREEN + "tnt-minecart, ")
                + (!obsidian.found() ? Formatting.RED + "obsidian, " : Formatting.GREEN + "obsidian, ")
                + (!rail.found() ? Formatting.RED + "rail, " : Formatting.GREEN + "rail, ")
                + (!flint.found() ? Formatting.RED + "flint-and-steel. " : Formatting.GREEN + "flint-and-steel. "));
            toggle();
            return;
        }

        gap = InvUtils.find(Items.ENCHANTED_GOLDEN_APPLE, Items.GOLDEN_APPLE);
        if (rightClickEat.get() && mc.options.useKey.isPressed() && gap.found()) isEating = true;

        if (isEating) {
            mc.player.getInventory().selectedSlot = gap.slot();
            mc.options.useKey.setPressed(true);
            return;
        }

        if (((ClientPlayerInteractionManagerAccessor) mc.interactionManager).getBreakingProgress() <= 0.01) breakPos = validPos(target);

        if (breakPos == null) {
            warning("Position is null");
            toggle();
            return;
        }

        if (instant.get()) {
            if (!ninja.get()) {
                obsidianTask.run(() -> {
                    if (debug.get()) info("obsidian placed");
                    BlockUtils.place(breakPos, obsidian, 50, rotate.get());
                });
            }

            if (mc.world.getBlockState(breakPos).getBlock() == Blocks.OBSIDIAN) {
                mc.player.getInventory().selectedSlot = pickaxe.slot();
                if (!prevPos.equals(breakPos)) {
                    if (debug.get()) info("reset");
                    startTask.reset();
                }
                startTask.run(() -> {
                    mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, breakPos, Direction.UP));
                    prevPos = breakPos;
                    if (debug.get()) info(prevPos.toShortString() + " = " + breakPos.toShortString());
                });
            }

            if (startTask.isCalled()) {

                BlockPos _breakPos = breakPos;
                _breakPos.add(0, 1, 0);

                ticks++;
                if (ticks == delay.get() - 4 && ninja.get()) {
                    obsidianTask.run(() -> {
                        if (debug.get()) info("placing obsidian");
                        BlockUtils.place(breakPos, obsidian, 50, rotate.get());
                        if (debug.get()) info("obsidian placed");
                    });
                }

                if(ticks == delay.get() - 3 && ninja.get()) {
                    railTask.run(() -> {
                        if (debug.get()) info("placing rail");
                        BlockUtils.place(_breakPos, rail, 50, rotate.get());
                        if (debug.get()) info("rail placed");
                    });
                }

                if (ticks == delay.get() - 2) {
                    minecartTask.run(() -> {
                        if (debug.get()) info("placing minecart");
                        int prevSlot = mc.player.getInventory().selectedSlot;
                        if (mc.player.getOffHandStack().getItem() != Items.TNT_MINECART) mc.player.getInventory().selectedSlot = minecart.slot();

                        mc.interactionManager.interactBlock(mc.player, minecart.getHand(), new BlockHitResult(mc.player.getPos(), Direction.DOWN, _breakPos, true));
                        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(mc.player.getPos(), Direction.DOWN, _breakPos, true));

                        mc.player.getInventory().selectedSlot = prevSlot;
                        if (debug.get()) info("minecart placed");
                    });
                }
                if (ticks == delay.get() - 1) {
                    mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, breakPos, Direction.UP));
                }
                if (ticks == delay.get()) {
                    if (minecartEntity != null && getState(breakPos).isAir()) {
                        if (debug.get()) info("minecart ignited");

                        int prevSlot = mc.player.getInventory().selectedSlot;
                        if (mc.player.getOffHandStack().getItem() != Items.FLINT_AND_STEEL) mc.player.getInventory().selectedSlot = flint.slot();

                        mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.interact(minecartEntity, mc.player.isSneaking(), Hand.MAIN_HAND));

                        mc.player.getInventory().selectedSlot = prevSlot;
                        //mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(minecartEntity, mc.player.isSneaking()));
                    }
                    reset();
                    ticks = 0;
                }
            }
        } else {
            if (!prevPos.equals(breakPos)) {
                if (debug.get()) info("reset");
                obsidianTask.reset();
            }

            BlockPos _breakPos = breakPos;
            _breakPos.add(0, 1, 0);

            obsidianTask.run(() -> {
                if (debug.get()) info("obsidian placed");
                BlockUtils.place(breakPos, obsidian, 50, rotate.get());
                prevPos = breakPos;
                if (debug.get()) info(prevPos.toShortString() + " = " + breakPos.toShortString());
            });

            railTask.run(() -> {
                if (debug.get()) info("placing rail");
                BlockUtils.place(_breakPos, rail, 50, rotate.get());
                if (debug.get()) info("rail placed");
            });

            if (mc.world.getBlockState(breakPos).getBlock() == Blocks.OBSIDIAN) {
                mc.player.getInventory().selectedSlot = pickaxe.slot();
                mc.interactionManager.updateBlockBreakingProgress(breakPos, Direction.UP);

                float progress = ((ClientPlayerInteractionManagerAccessor) mc.interactionManager).getBreakingProgress();
                if (progress < breakProgress.get()) return;

                minecartTask.run(() -> {
                    if (debug.get()) info("placing minecart");
                    int prevSlot = mc.player.getInventory().selectedSlot;
                    if (mc.player.getOffHandStack().getItem() != Items.TNT_MINECART) mc.player.getInventory().selectedSlot = minecart.slot();

                    mc.interactionManager.interactBlock(mc.player, minecart.getHand(), new BlockHitResult(mc.player.getPos(), Direction.DOWN, _breakPos, true));
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(mc.player.getPos(), Direction.DOWN, _breakPos, true));

                    mc.player.getInventory().selectedSlot = prevSlot;
                    if (debug.get()) info("minecart placed");
                });
            }

            if (minecartEntity != null && getState(breakPos).isAir()) {
                if (debug.get()) info("minecart ignited");

                int prevSlot = mc.player.getInventory().selectedSlot;
                if (mc.player.getOffHandStack().getItem() != Items.FLINT_AND_STEEL) mc.player.getInventory().selectedSlot = flint.slot();

                mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.interact(minecartEntity, mc.player.isSneaking(), Hand.MAIN_HAND));

                mc.player.getInventory().selectedSlot = prevSlot;
                reset();
            }
        }
    }

    private BlockPos validPos(PlayerEntity player) {
        if (player == null) return null;

        BlockPos pos = player.getBlockPos();
        if (getState(pos.up(3)).isAir() && (getState(pos.up(2)).isOf(Blocks.OBSIDIAN) || getState(pos.up(2)).isOf(Blocks.AIR))) return pos.up(2);
        List<BlockPos> posList = new ArrayList<>();

        for (CardinalDirection direction : CardinalDirection.values()) {
            if (getState(pos.offset(direction.toDirection()).up(2)).isAir() && (getState(pos.offset(direction.toDirection()).up(1)).isOf(Blocks.OBSIDIAN) || getState(pos.offset(direction.toDirection()).up(1)).isOf(Blocks.AIR))) posList.add(pos.offset(direction.toDirection()).up(1));
        }

        posList.sort(Comparator.comparingDouble(PlayerUtils::distanceTo));
        return posList.isEmpty() ? null : posList.get(0);
    }

    private void reset() {
        obsidianTask.reset();
        minecartTask.reset();
        railTask.reset();
        minecartEntity = null;
    }

    @Override
    public String getInfoString() {
        return target != null ? target.getGameProfile().getName() : null; // adds target name to the module array list
    }
}
