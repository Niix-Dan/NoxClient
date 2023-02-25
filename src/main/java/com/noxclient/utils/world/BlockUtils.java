/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.utils.world;

import com.noxclient.NoxClient;
import com.noxclient.events.world.TickEvent;
import com.noxclient.mixininterface.IVec3d;
import com.noxclient.utils.player.FindItemResult;
import com.noxclient.utils.player.InvUtils;
import com.noxclient.utils.player.Rotations;
import com.noxclient.utils.PreInit;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.*;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class BlockUtils {
    private static final Vec3d hitPos = new Vec3d(0, 0, 0);

    public static boolean breaking;
    private static boolean breakingThisTick;

    @PreInit
    public static void init() {
        NoxClient.EVENT_BUS.subscribe(BlockUtils.class);
    }

    // Placing

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, int rotationPriority) {
        return place(blockPos, findItemResult, rotationPriority, true);
    }

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, boolean rotate, int rotationPriority) {
        return place(blockPos, findItemResult, rotate, rotationPriority, true);
    }

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, boolean rotate, int rotationPriority, boolean checkEntities) {
        return place(blockPos, findItemResult, rotate, rotationPriority, true, checkEntities);
    }

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, int rotationPriority, boolean checkEntities) {
        return place(blockPos, findItemResult, true, rotationPriority, true, checkEntities);
    }

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, boolean rotate, int rotationPriority, boolean swingHand, boolean checkEntities) {
        return place(blockPos, findItemResult, rotate, rotationPriority, swingHand, checkEntities, true);
    }

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, boolean rotate, int rotationPriority, boolean swingHand, boolean checkEntities, boolean swapBack) {
        if (findItemResult.isOffhand()) {
            return place(blockPos, Hand.OFF_HAND, NoxClient.mc.player.getInventory().selectedSlot, rotate, rotationPriority, swingHand, checkEntities, swapBack);
        } else if (findItemResult.isHotbar()) {
            return place(blockPos, Hand.MAIN_HAND, findItemResult.slot(), rotate, rotationPriority, swingHand, checkEntities, swapBack);
        }
        return false;
    }

    public static boolean place(BlockPos blockPos, Hand hand, int slot, boolean rotate, int rotationPriority, boolean swingHand, boolean checkEntities, boolean swapBack) {
        if (slot < 0 || slot > 8) return false;
        if (!canPlace(blockPos, checkEntities)) return false;

        ((IVec3d) hitPos).set(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);

        BlockPos neighbour;
        Direction side = getPlaceSide(blockPos);

        if (side == null) {
            side = Direction.UP;
            neighbour = blockPos;
        } else {
            neighbour = blockPos.offset(side.getOpposite());
            hitPos.add(side.getOffsetX() * 0.5, side.getOffsetY() * 0.5, side.getOffsetZ() * 0.5);
        }

        Direction s = side;

        if (rotate) {
            Rotations.rotate(Rotations.getYaw(hitPos), Rotations.getPitch(hitPos), rotationPriority, () -> {
                InvUtils.swap(slot, swapBack);

                place(new BlockHitResult(hitPos, s, neighbour, false), hand, swingHand);

                if (swapBack) InvUtils.swapBack();
            });
        } else {
            InvUtils.swap(slot, swapBack);

            place(new BlockHitResult(hitPos, s, neighbour, false), hand, swingHand);

            if (swapBack) InvUtils.swapBack();
        }


        return true;
    }

    private static void place(BlockHitResult blockHitResult, Hand hand, boolean swing) {
        boolean wasSneaking = NoxClient.mc.player.input.sneaking;
        NoxClient.mc.player.input.sneaking = false;

        ActionResult result = NoxClient.mc.interactionManager.interactBlock(NoxClient.mc.player, hand, blockHitResult);

        if (result.shouldSwingHand()) {
            if (swing) NoxClient.mc.player.swingHand(hand);
            else NoxClient.mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));
        }

        NoxClient.mc.player.input.sneaking = wasSneaking;
    }

    public static boolean canPlace(BlockPos blockPos, boolean checkEntities) {
        if (blockPos == null) return false;

        // Check y level
        if (!World.isValid(blockPos)) return false;

        // Check if current block is replaceable
        if (!NoxClient.mc.world.getBlockState(blockPos).getMaterial().isReplaceable()) return false;

        // Check if intersects entities
        return !checkEntities || NoxClient.mc.world.canPlace(NoxClient.mc.world.getBlockState(blockPos), blockPos, ShapeContext.absent());
    }

    public static boolean canPlace(BlockPos blockPos) {
        return canPlace(blockPos, true);
    }

    public static Direction getPlaceSide(BlockPos blockPos) {
        for (Direction side : Direction.values()) {
            BlockPos neighbor = blockPos.offset(side);
            Direction side2 = side.getOpposite();

            BlockState state = NoxClient.mc.world.getBlockState(neighbor);

            // Check if neighbour isn't empty
            if (state.isAir() || isClickable(state.getBlock())) continue;

            // Check if neighbour is a fluid
            if (!state.getFluidState().isEmpty()) continue;

            return side2;
        }

        return null;
    }

    // Breaking

    @EventHandler(priority = EventPriority.HIGHEST + 100)
    private static void onTickPre(TickEvent.Pre event) {
        breakingThisTick = false;
    }

    @EventHandler(priority = EventPriority.LOWEST - 100)
    private static void onTickPost(TickEvent.Post event) {
        if (!breakingThisTick && breaking) {
            breaking = false;
            if (NoxClient.mc.interactionManager != null) NoxClient.mc.interactionManager.cancelBlockBreaking();
        }
    }

    /** Needs to be used in {@link TickEvent.Pre} */
    public static boolean breakBlock(BlockPos blockPos, boolean swing) {
        if (!canBreak(blockPos, NoxClient.mc.world.getBlockState(blockPos))) return false;

        // Creating new instance of block pos because minecraft assigns the parameter to a field and we don't want it to change when it has been stored in a field somewhere
        BlockPos pos = blockPos instanceof BlockPos.Mutable ? new BlockPos(blockPos) : blockPos;

        if (NoxClient.mc.interactionManager.isBreakingBlock()) NoxClient.mc.interactionManager.updateBlockBreakingProgress(pos, Direction.UP);
        else NoxClient.mc.interactionManager.attackBlock(pos, Direction.UP);

        if (swing) NoxClient.mc.player.swingHand(Hand.MAIN_HAND);
        else NoxClient.mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

        breaking = true;
        breakingThisTick = true;

        return true;
    }

    public static boolean canBreak(BlockPos blockPos, BlockState state) {
        if (!NoxClient.mc.player.isCreative() && state.getHardness(NoxClient.mc.world, blockPos) < 0) return false;
        return state.getOutlineShape(NoxClient.mc.world, blockPos) != VoxelShapes.empty();
    }
    public static boolean canBreak(BlockPos blockPos) {
        return canBreak(blockPos, NoxClient.mc.world.getBlockState(blockPos));
    }

    public static boolean canInstaBreak(BlockPos blockPos, BlockState state) {
        return NoxClient.mc.player.isCreative() || state.calcBlockBreakingDelta(NoxClient.mc.player, NoxClient.mc.world, blockPos) >= 1;
    }
    public static boolean canInstaBreak(BlockPos blockPos) {
        return canInstaBreak(blockPos, NoxClient.mc.world.getBlockState(blockPos));
    }

    // Other

    public static boolean isClickable(Block block) {
        return block instanceof CraftingTableBlock
            || block instanceof AnvilBlock
            || block instanceof AbstractButtonBlock
            || block instanceof AbstractPressurePlateBlock
            || block instanceof BlockWithEntity
            || block instanceof BedBlock
            || block instanceof FenceGateBlock
            || block instanceof DoorBlock
            || block instanceof NoteBlock
            || block instanceof TrapdoorBlock;
    }

    public static MobSpawn isValidMobSpawn(BlockPos blockPos, boolean newMobSpawnLightLevel) {
        int spawnLightLimit = newMobSpawnLightLevel ? 0 : 7;
        if (!(NoxClient.mc.world.getBlockState(blockPos).getBlock() instanceof AirBlock) ||
            NoxClient.mc.world.getBlockState(blockPos.down()).getBlock() == Blocks.BEDROCK) return MobSpawn.Never;

        if (!topSurface(NoxClient.mc.world.getBlockState(blockPos.down()))) {
            if (NoxClient.mc.world.getBlockState(blockPos.down()).getCollisionShape(NoxClient.mc.world, blockPos.down()) != VoxelShapes.fullCube()) return MobSpawn.Never;
            if (NoxClient.mc.world.getBlockState(blockPos.down()).isTranslucent(NoxClient.mc.world, blockPos.down())) return MobSpawn.Never;
        }

        if (NoxClient.mc.world.getLightLevel(blockPos, 0) <= spawnLightLimit) return MobSpawn.Potential;
        else if (NoxClient.mc.world.getLightLevel(LightType.BLOCK, blockPos) <= spawnLightLimit) return MobSpawn.Always;

        return MobSpawn.Never;
    }

    public static boolean topSurface(BlockState blockState) {
        if (blockState.getBlock() instanceof SlabBlock && blockState.get(SlabBlock.TYPE) == SlabType.TOP) return true;
        else return blockState.getBlock() instanceof StairsBlock && blockState.get(StairsBlock.HALF) == BlockHalf.TOP;
    }

    public enum MobSpawn {
        Never,
        Potential,
        Always
    }

    private static final ThreadLocal<BlockPos.Mutable> EXPOSED_POS = ThreadLocal.withInitial(BlockPos.Mutable::new);
    public static BlockState getBlockState(BlockPos pos) {
        if (pos == null) return null;
        return NoxClient.mc.world.getBlockState(pos);
    }
    public static boolean isExposed(BlockPos blockPos) {
        for (Direction direction : Direction.values()) {
            if (!NoxClient.mc.world.getBlockState(EXPOSED_POS.get().set(blockPos, direction)).isOpaque()) return true;
        }

        return false;
    }
}
