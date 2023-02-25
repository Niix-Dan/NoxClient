/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.modules.movement;

import com.noxclient.settings.DoubleSetting;
import com.noxclient.settings.Setting;
import com.noxclient.settings.SettingGroup;
import com.noxclient.events.world.TickEvent;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

public class ClickTP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> maxDistance = sgGeneral.add(new DoubleSetting.Builder()
            .name("max-distance")
            .description("The maximum distance you can teleport.")
            .defaultValue(5)
            .build()
    );

    public ClickTP() {
        super(Categories.Movement, "click-tp", "Teleports you to the block you click on.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player.isUsingItem()) return;

        if (mc.options.useKey.isPressed()) {
            HitResult hitResult = mc.player.raycast(maxDistance.get(), 1f / 20f, false);

            if (hitResult.getType() == HitResult.Type.ENTITY && mc.player.interact(((EntityHitResult) hitResult).getEntity(), Hand.MAIN_HAND) != ActionResult.PASS) return;

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = ((BlockHitResult) hitResult).getBlockPos(); // get block position
                Direction side = ((BlockHitResult) hitResult).getSide(); // get block side fot the teleport

                if (mc.world.getBlockState(pos).onUse(mc.world, mc.player, Hand.MAIN_HAND, (BlockHitResult) hitResult) != ActionResult.PASS) return;

                BlockState state = mc.world.getBlockState(pos);

                VoxelShape shape = state.getCollisionShape(mc.world, pos);
                if (shape.isEmpty()) shape = state.getOutlineShape(mc.world, pos);

                double height = shape.isEmpty() ? 1 : shape.getMax(Direction.Axis.Y);

                mc.player.setPosition(pos.getX() + 0.5 + side.getOffsetX(), pos.getY() + height, pos.getZ() + 0.5 + side.getOffsetZ());
            }
        }
    }
}
