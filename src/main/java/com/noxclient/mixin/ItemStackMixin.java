/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.mixin;

import com.noxclient.NoxClient;
import com.noxclient.events.entity.player.FinishUsingItemEvent;
import com.noxclient.events.entity.player.StoppedUsingItemEvent;
import com.noxclient.events.game.ItemStackTooltipEvent;
import com.noxclient.systems.modules.Modules;
import com.noxclient.systems.modules.render.BetterTooltips;
import com.noxclient.utils.Utils;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "getTooltip", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onGetTooltip(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> info, List<Text> list) {
        if (Utils.canUpdate()) {
            NoxClient.EVENT_BUS.post(ItemStackTooltipEvent.get((ItemStack) (Object) this, list));
        }
    }

    @Inject(method = "finishUsing", at = @At("HEAD"))
    private void onFinishUsing(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> info) {
        if (user == NoxClient.mc.player) {
            NoxClient.EVENT_BUS.post(FinishUsingItemEvent.get((ItemStack) (Object) this));
        }
    }

    @Inject(method = "onStoppedUsing", at = @At("HEAD"))
    private void onStoppedUsing(World world, LivingEntity user, int remainingUseTicks, CallbackInfo info) {
        if (user == NoxClient.mc.player) {
            NoxClient.EVENT_BUS.post(StoppedUsingItemEvent.get((ItemStack) (Object) this));
        }
    }

    @Inject(method = "getHideFlags", at = @At("HEAD"), cancellable = true)
    private void onGetHideFlags(CallbackInfoReturnable<Integer> cir) {
        if (Modules.get().get(BetterTooltips.class).alwaysShow()) {
            cir.setReturnValue(0);
        }
    }
}
