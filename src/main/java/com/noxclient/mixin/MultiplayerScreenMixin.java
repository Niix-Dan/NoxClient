/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.mixin;

import com.noxclient.gui.GuiThemes;
import com.noxclient.gui.tabs.screens.ServerManagerScreen;
import com.noxclient.systems.modules.Modules;
import com.noxclient.systems.modules.misc.NameProtect;
import com.noxclient.systems.proxies.Proxies;
import com.noxclient.systems.proxies.Proxy;
import com.noxclient.utils.render.color.Color;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public class MultiplayerScreenMixin extends Screen {
    private int textColor1;
    private int textColor2;

    private String loggedInAs;
    private int loggedInAsLength;

    public MultiplayerScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        textColor1 = Color.fromRGBA(255, 255, 255, 255);
        textColor2 = Color.fromRGBA(175, 175, 175, 255);

        loggedInAs = "Logged in as ";
        loggedInAsLength = textRenderer.getWidth(loggedInAs);

        addDrawableChild(new ButtonWidget(this.width - 75 - 3, 3, 75, 20, Text.literal("Accounts"), button -> {
            client.setScreen(GuiThemes.get().accountsScreen());
        }));

        addDrawableChild(new ButtonWidget(this.width - 75 - 3 - 75 - 2, 3, 75, 20, Text.literal("Proxies"), button -> {
            client.setScreen(GuiThemes.get().proxiesScreen());
        }));

        addDrawableChild(new ButtonWidget(this.width - 75 - 3 - 75 - 2 - 75 - 2, 3, 75, 20, Text.literal("Servers"), button -> {
            client.setScreen(new ServerManagerScreen(GuiThemes.get(), (MultiplayerScreen) (Object) this));
        }));
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info) {
        float x = 3;
        float y = 3;

        // Logged in as
        textRenderer.drawWithShadow(matrices, loggedInAs, x, y, textColor1);
        textRenderer.drawWithShadow(matrices, Modules.get().get(NameProtect.class).getName(client.getSession().getUsername()), x + loggedInAsLength, y, textColor2);

        y += textRenderer.fontHeight + 2;

        // Proxy
        Proxy proxy = Proxies.get().getEnabled();

        String left = proxy != null ? "Using proxy " : "Not using a proxy";
        String right = proxy != null ? (proxy.name.get() != null && !proxy.name.get().isEmpty() ? "(" + proxy.name.get() + ") " : "") + proxy.address.get() + ":" + proxy.port.get() : null;

        textRenderer.drawWithShadow(matrices, left, x, y, textColor1);
        if (right != null) textRenderer.drawWithShadow(matrices, right, x + textRenderer.getWidth(left), y, textColor2);
    }
}
