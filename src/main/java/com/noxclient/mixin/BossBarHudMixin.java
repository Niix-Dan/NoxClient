/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.mixin;

import com.noxclient.NoxClient;
import com.noxclient.events.render.RenderBossBarEvent;
import com.noxclient.systems.modules.Modules;
import com.noxclient.systems.modules.render.NoRender;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Iterator;

@Mixin(BossBarHud.class)
public class BossBarHudMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(CallbackInfo info) {
        if (Modules.get().get(NoRender.class).noBossBar()) info.cancel();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/Collection;iterator()Ljava/util/Iterator;"))
    public Iterator<ClientBossBar> onRender(Collection<ClientBossBar> collection) {
        RenderBossBarEvent.BossIterator event = NoxClient.EVENT_BUS.post(RenderBossBarEvent.BossIterator.get(collection.iterator()));
        return event.iterator;
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ClientBossBar;getName()Lnet/minecraft/text/Text;"))
    public Text onAsFormattedString(ClientBossBar clientBossBar) {
        RenderBossBarEvent.BossText event = NoxClient.EVENT_BUS.post(RenderBossBarEvent.BossText.get(clientBossBar, clientBossBar.getName()));
        return event.name;
    }

    @ModifyConstant(method = "render", constant = @Constant(intValue = 9, ordinal = 1))
    public int modifySpacingConstant(int j) {
        RenderBossBarEvent.BossSpacing event = NoxClient.EVENT_BUS.post(RenderBossBarEvent.BossSpacing.get(j));
        return event.spacing;
    }
}
