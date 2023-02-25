package com.noxclient.mixin;

import com.noxclient.systems.modules.Modules;
import com.noxclient.systems.modules.misc.VillagerRoller;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.village.TradeOfferList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantScreenHandler.class)
class MerchantScreenHandlerMixin {
    @Inject(at = @At("TAIL"), method = "setOffers(Lnet/minecraft/village/TradeOfferList;)V", cancellable = false)
    public void setOffers(TradeOfferList offers, CallbackInfo ci) {
        VillagerRoller roller = Modules.get().get(VillagerRoller.class);
        roller.triggerTradeCheck(offers);
    }
}
