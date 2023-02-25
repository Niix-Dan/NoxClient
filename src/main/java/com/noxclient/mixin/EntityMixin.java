/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.mixin;

import com.noxclient.NoxClient;
import com.noxclient.events.entity.LivingEntityMoveEvent;
import com.noxclient.events.entity.player.JumpVelocityMultiplierEvent;
import com.noxclient.events.entity.player.PlayerMoveEvent;
import com.noxclient.systems.modules.Modules;
import com.noxclient.systems.modules.combat.Hitboxes;
import com.noxclient.systems.modules.movement.NoSlow;
import com.noxclient.systems.modules.movement.Velocity;
import com.noxclient.systems.modules.render.ESP;
import com.noxclient.systems.modules.render.NoRender;
import com.noxclient.utils.Utils;
import com.noxclient.utils.entity.fakeplayer.FakePlayerEntity;
import com.noxclient.utils.render.postprocess.PostProcessShaders;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public World world;

    @Shadow public abstract BlockPos getBlockPos();
    @Shadow protected abstract BlockPos getVelocityAffectingPos();

    @Redirect(method = "updateMovementInFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;getVelocity(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d updateMovementInFluidFluidStateGetVelocity(FluidState state, BlockView world, BlockPos pos) {
        Vec3d vec = state.getVelocity(world, pos);

        Velocity velocity = Modules.get().get(Velocity.class);
        if ((Object) this == NoxClient.mc.player && velocity.isActive() && velocity.liquids.get()) {
            vec = vec.multiply(velocity.getHorizontal(velocity.liquidsHorizontal), velocity.getVertical(velocity.liquidsVertical), velocity.getHorizontal(velocity.liquidsHorizontal));
        }

        return vec;
    }

    @ModifyArgs(method = "pushAwayFrom(Lnet/minecraft/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    private void onPushAwayFrom(Args args, Entity entity) {
        Velocity velocity = Modules.get().get(Velocity.class);

        // Velocity
        if ((Object) this == NoxClient.mc.player && velocity.isActive() && velocity.entityPush.get()) {
            double multiplier = velocity.entityPushAmount.get();
            args.set(0, (double) args.get(0) * multiplier);
            args.set(2, (double) args.get(2) * multiplier);
        }
        // FakePlayerEntity
        else if (entity instanceof FakePlayerEntity player && player.doNotPush) {
            args.set(0, 0.0);
            args.set(2, 0.0);
        }
    }

    @Inject(method = "getJumpVelocityMultiplier", at = @At("HEAD"), cancellable = true)
    private void onGetJumpVelocityMultiplier(CallbackInfoReturnable<Float> info) {
        if ((Object) this == NoxClient.mc.player) {
            float f = world.getBlockState(getBlockPos()).getBlock().getJumpVelocityMultiplier();
            float g = world.getBlockState(getVelocityAffectingPos()).getBlock().getJumpVelocityMultiplier();
            float a = f == 1.0D ? g : f;

            JumpVelocityMultiplierEvent event = NoxClient.EVENT_BUS.post(JumpVelocityMultiplierEvent.get());
            info.setReturnValue(a * event.multiplier);
        }
    }

    @Inject(method = "move", at = @At("HEAD"))
    private void onMove(MovementType type, Vec3d movement, CallbackInfo info) {
        if ((Object) this == NoxClient.mc.player) {
            NoxClient.EVENT_BUS.post(PlayerMoveEvent.get(type, movement));
        } else if ((Object) this instanceof LivingEntity) {
            NoxClient.EVENT_BUS.post(LivingEntityMoveEvent.get((LivingEntity) (Object) this, movement));
        }
    }

    @Inject(method = "getTeamColorValue", at = @At("HEAD"), cancellable = true)
    private void onGetTeamColorValue(CallbackInfoReturnable<Integer> info) {
        if (PostProcessShaders.rendering) {
            info.setReturnValue(Modules.get().get(ESP.class).getColor((Entity) (Object) this).getPacked());
        }
    }

    @Redirect(method = "getVelocityMultiplier", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;"))
    private Block getVelocityMultiplierGetBlockProxy(BlockState blockState) {
        if ((Object) this != NoxClient.mc.player) return blockState.getBlock();
        if (blockState.getBlock() == Blocks.SOUL_SAND && Modules.get().get(NoSlow.class).soulSand()) return Blocks.STONE;
        if (blockState.getBlock() == Blocks.HONEY_BLOCK && Modules.get().get(NoSlow.class).honeyBlock()) return Blocks.STONE;
        return blockState.getBlock();
    }

    @Inject(method = "isInvisibleTo(Lnet/minecraft/entity/player/PlayerEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void isInvisibleToCanceller(PlayerEntity player, CallbackInfoReturnable<Boolean> info) {
        if (!Utils.canUpdate()) return;
        if (Modules.get().get(NoRender.class).noInvisibility() || Modules.get().get(ESP.class).shouldDrawOutline((Entity) (Object) this)) info.setReturnValue(false);
    }

    @Inject(method = "getTargetingMargin", at = @At("HEAD"), cancellable = true)
    private void onGetTargetingMargin(CallbackInfoReturnable<Float> info) {
        double v = Modules.get().get(Hitboxes.class).getEntityValue((Entity) (Object) this);
        if (v != 0) info.setReturnValue((float) v);
    }

    @Inject(method = "isInvisibleTo", at = @At("HEAD"), cancellable = true)
    private void onIsInvisibleTo(PlayerEntity player, CallbackInfoReturnable<Boolean> info) {
        if (player == null) info.setReturnValue(false);
    }
}
