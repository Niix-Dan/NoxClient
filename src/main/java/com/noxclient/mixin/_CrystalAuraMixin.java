package com.noxclient.mixin;

import com.noxclient.systems.modules.Modules;
import com.noxclient.systems.modules.combat.CrystalAura;
import com.noxclient.systems.modules.world.AdvPlacer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EndCrystalItem.class)
public class _CrystalAuraMixin {
    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void canPlaceOn(ItemUsageContext context, CallbackInfoReturnable<ActionResult> ci) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);

        if(Modules.get().isActive(CrystalAura.class) || Modules.get().isActive(AdvPlacer.class)) {
            if(!blockState.isOf(Blocks.OBSIDIAN) && !blockState.isOf(Blocks.BEDROCK)) {
                BlockPos blockPos2 = blockPos.up();
                if (!world.isAir(blockPos2)) {
                    ci.setReturnValue(ActionResult.FAIL);
                } else {
                    double d = (double)blockPos2.getX();
                    double e = (double)blockPos2.getY();
                    double f = (double)blockPos2.getZ();
                    List<Entity> list = world.getOtherEntities((Entity)null, new Box(d, e, f, d + 1.0, e + 2.0, f + 1.0));
                    if (!list.isEmpty()) {
                        ci.setReturnValue(ActionResult.FAIL);
                    } else {
                        if (world instanceof ServerWorld) {
                            EndCrystalEntity endCrystalEntity = new EndCrystalEntity(world, d + 0.5, e, f + 0.5);
                            endCrystalEntity.setShowBottom(false);
                            world.spawnEntity(endCrystalEntity);
                            world.emitGameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, blockPos2);
                            EnderDragonFight enderDragonFight = ((ServerWorld)world).getEnderDragonFight();
                            if (enderDragonFight != null) {
                                enderDragonFight.respawnDragon();
                            }
                        }

                        context.getStack().decrement(1);
                        ci.setReturnValue(ActionResult.success(world.isClient));
                    }
                }
            }
        }
    }

}
