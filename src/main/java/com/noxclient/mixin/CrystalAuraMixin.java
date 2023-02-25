package com.noxclient.mixin;

import com.noxclient.systems.modules.Modules;
import com.noxclient.systems.modules.combat.CrystalAura;
import net.minecraft.block.BlockState;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.noxclient.systems.modules.world.AdvPlacer;



@Mixin(BlockItem.class)
public class CrystalAuraMixin {
    @Inject(method = "canPlace", at = @At("HEAD"), cancellable = true)
    private void canPlace(ItemPlacementContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);

        if(Modules.get().isActive(AdvPlacer.class)) {
            cir.setReturnValue(true);
        } else if(Modules.get().isActive(CrystalAura.class)) {
            if(blockState.getBlock().asItem() == Items.END_CRYSTAL) {
                cir.setReturnValue(true);
            }
        }
    }
}
