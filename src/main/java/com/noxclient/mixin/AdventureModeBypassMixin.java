package com.noxclient.mixin;

import com.noxclient.systems.modules.Modules;
import com.noxclient.systems.modules.world.AntiAdventureMode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(PlayerEntity.class)
public class AdventureModeBypassMixin {
    @Inject(method = "isBlockBreakingRestricted", at = @At("HEAD"), cancellable = true)
    private void isBlockBreakingRestricted(World world, BlockPos pos, GameMode gameMode, CallbackInfoReturnable<Boolean> ci) {
        if(Modules.get().isActive(AntiAdventureMode.class)) {
            ci.setReturnValue(false);
        }
    }

    @Inject(method = "canModifyBlocks", at = @At("HEAD"), cancellable = true)
    public void canModifyBlocks(CallbackInfoReturnable<Boolean> ci) {
        if(Modules.get().isActive(AntiAdventureMode.class)) {
            ci.setReturnValue(true);
        }
    }
    @Inject(method = "canPlaceOn", at = @At("HEAD"), cancellable = true)
    public void canPlaceOn(BlockPos pos, Direction facing, ItemStack stack, CallbackInfoReturnable<Boolean> ci) {
        if(Modules.get().isActive(AntiAdventureMode.class)) {
            ci.setReturnValue(true);
        }
    }

}
