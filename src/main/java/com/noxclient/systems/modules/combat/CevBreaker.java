/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.modules.combat;

import com.noxclient.events.entity.player.FinishUsingItemEvent;
import com.noxclient.events.world.TickEvent;
import com.noxclient.mixin.ClientPlayerInteractionManagerAccessor;
import com.noxclient.mixininterface.IVec3d;
import com.noxclient.settings.*;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import com.noxclient.utils.entity.SortPriority;
import com.noxclient.utils.entity.TargetUtils;
import com.noxclient.utils.misc.Vec3;
import com.noxclient.utils.player.ChatUtils;
import com.noxclient.utils.player.FindItemResult;
import com.noxclient.utils.player.InvUtils;
import com.noxclient.utils.player.PlayerUtils;
import com.noxclient.utils.world.BlockUtils;
import com.noxclient.utils.world.CardinalDirection;
import meteordevelopment.orbit.EventHandler;
import com.noxclient.utils.Task;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.block.Blocks;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.noxclient.utils.BlockInfo.getState;

public class CevBreaker extends Module {
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


    public CevBreaker() {
        super(Categories.Combat, "cev-breaker", "Places obsidian on top of people and explodes crystals on top of their heads after destroying the obsidian.");
    }

    private FindItemResult pickaxe, crystal, obsidian, gap;
    private EndCrystalEntity crystalEntity;
    private BlockPos breakPos, prevPos;
    private PlayerEntity target;
    private int ticks = 0;
    private boolean isEating;

    private final Task obsidianTask = new Task();
    private final Task crystalTask = new Task();
    private final Task startTask = new Task();


    private final Vec3d vec3dRayTraceEnd = new Vec3d(0, 0, 0);
    private RaycastContext raycastContext;
    private final Vec3d vec3d = new Vec3d(0, 0, 0);
    private final Vec3d playerEyePos = new Vec3d(0, 0, 0);
    private final Vec3 vec3 = new Vec3();


    @Override
    public void onActivate() {
        ticks = 0;

        raycastContext = new RaycastContext(new Vec3d(0, 0, 0), new Vec3d(0, 0, 0), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);

        breakPos = null;
        prevPos = new BlockPos(0,1,0);
        crystalEntity = null;

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
        crystal = InvUtils.findInHotbar(Items.END_CRYSTAL);
        obsidian = InvUtils.findInHotbar(Items.OBSIDIAN);

        target = TargetUtils.getPlayerTarget(targetRange.get(), SortPriority.LowestDistance);
        if (TargetUtils.isBadTarget(target, targetRange.get())) {
            warning("Target is null");
            toggle();
            return;
        }

        if (!pickaxe.found() || !crystal.found() || !obsidian.found()) {
            ChatUtils.info("Items: " + (!pickaxe.found() ? Formatting.RED + "pickaxe, " : Formatting.GREEN + "pickaxe, ") + (!crystal.found() ? Formatting.RED + "crystal, " : Formatting.GREEN + "crystal, ") + (!obsidian.found() ? Formatting.RED + "obsidian. " : Formatting.GREEN + "obsidian. "));
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

        BlockPos crystalPos = target.getBlockPos().up().up();
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof EndCrystalEntity) {
                BlockPos entityPos = entity.getBlockPos();

                for (Direction direction : Direction.values()) {
                    if (direction.equals(Direction.DOWN)) continue;

                    if (entityPos.equals(crystalPos.offset(direction))) crystalEntity = (EndCrystalEntity) entity;
                }
            }
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
                ticks++;
                if (ticks == delay.get() - 3 && ninja.get()) {
                    obsidianTask.run(() -> {
                        BlockUtils.place(breakPos, obsidian, 50, rotate.get());
                        if (debug.get()) info("obsidian placed");
                    });
                }
                if (ticks == delay.get() - 2) {
                    crystalTask.run(() -> {
                        int prevSlot = mc.player.getInventory().selectedSlot;
                        if (mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL) mc.player.getInventory().selectedSlot = crystal.slot();
                        //new BlockHitResult(mc.player.getPos(), Direction.DOWN, breakPos, true)


                        mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(crystal.getHand(), getPlaceInfo(breakPos), 0));
                        //BlockUtils.place(breakPos, crystal, false, 0, false, true, false);
                        //mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(mc.player.getPos(), Direction.DOWN, breakPos, true), 0));
                        //mc.interactionManager.interactBlock(mc.player, crystal.getHand(), new BlockHitResult(mc.player.getPos(), Direction.DOWN, breakPos, true));

                        mc.player.getInventory().selectedSlot = prevSlot;
                        if (debug.get()) info("crystal placed");
                    });
                }
                if (ticks == delay.get() - 1) {
                    mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, breakPos, Direction.UP));
                }
                if (ticks >= delay.get() + 1) {
                    if (crystalEntity != null && getState(breakPos).isAir()) {
                        if (debug.get()) info("crystal attacked");
                        mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(crystalEntity, mc.player.isSneaking()));
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
            obsidianTask.run(() -> {
                if (debug.get()) info("obsidian placed");
                BlockUtils.place(breakPos, obsidian, 50, rotate.get());
                prevPos = breakPos;
                if (debug.get()) info(prevPos.toShortString() + " = " + breakPos.toShortString());
            });

            if (mc.world.getBlockState(breakPos).getBlock() == Blocks.OBSIDIAN) {
                mc.player.getInventory().selectedSlot = pickaxe.slot();
                mc.interactionManager.updateBlockBreakingProgress(breakPos, Direction.UP);

                float progress = ((ClientPlayerInteractionManagerAccessor) mc.interactionManager).getBreakingProgress();
                if (progress < breakProgress.get()) return;

                crystalTask.run(() -> {
                    if (debug.get()) info("crystal placed");
                    int prevSlot = mc.player.getInventory().selectedSlot;
                    if (mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL) mc.player.getInventory().selectedSlot = crystal.slot();

                    mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(crystal.getHand(), getPlaceInfo(breakPos), 0));
                    //BlockUtils.place(breakPos, crystal, false, 0, false, true, false);
                    //mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(mc.player.getPos(), Direction.DOWN, breakPos, true), 0));
                    //mc.interactionManager.interactBlock(mc.player, crystal.getHand(), new BlockHitResult(mc.player.getPos(), Direction.DOWN, breakPos, true));

                    mc.player.getInventory().selectedSlot = prevSlot;
                });
            }

            if (crystalEntity != null && getState(breakPos).isAir()) {
                if (debug.get()) info("crystal attacked");
                mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(crystalEntity, mc.player.isSneaking()));
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, breakPos, Direction.UP));
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

    private BlockHitResult getPlaceInfo(BlockPos blockPos) {
        ((IVec3d) vec3d).set(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());
/*
        for (Direction side : Direction.values()) {
            ((IVec3d) vec3dRayTraceEnd).set(
                blockPos.getX() + 0.5 + side.getVector().getX() * 0.5,
                blockPos.getY() + 0.5 + side.getVector().getY() * 0.5,
                blockPos.getZ() + 0.5 + side.getVector().getZ() * 0.5
            );

            ((IRaycastContext) raycastContext).set(vec3d, vec3dRayTraceEnd, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
            BlockHitResult result = mc.world.raycast(raycastContext);

            if (result != null && result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals(blockPos)) {
                return result;
            }
        }*/

        Direction side = blockPos.getY() > vec3d.y ? Direction.DOWN : Direction.UP;
        return new BlockHitResult(vec3d, side, blockPos, false);
    }
    private void reset() {
        obsidianTask.reset();
        crystalTask.reset();
        crystalEntity = null;
    }

    @Override
    public String getInfoString() {
        return target != null ? target.getGameProfile().getName() : null; // adds target name to the module array list
    }
}
