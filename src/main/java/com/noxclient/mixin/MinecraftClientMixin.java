/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.mixin;

import com.noxclient.NoxClient;
import com.noxclient.events.entity.player.ItemUseCrosshairTargetEvent;
import com.noxclient.events.game.GameLeftEvent;
import com.noxclient.events.game.OpenScreenEvent;
import com.noxclient.events.game.ResourcePacksReloadedEvent;
import com.noxclient.events.game.WindowResizedEvent;
import com.noxclient.events.world.TickEvent;
import com.noxclient.gui.WidgetScreen;
import com.noxclient.mixininterface.IMinecraftClient;
import com.noxclient.systems.config.Config;
import com.noxclient.systems.modules.Modules;
import com.noxclient.systems.modules.player.MultiTask;
import com.noxclient.systems.modules.render.UnfocusedCPU;
import com.noxclient.utils.Utils;
import com.noxclient.utils.misc.MeteorStarscript;
import com.noxclient.utils.network.OnlinePlayers;
import meteordevelopment.starscript.Script;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(value = MinecraftClient.class, priority = 1001)
public abstract class MinecraftClientMixin implements IMinecraftClient {
    @Unique private boolean doItemUseCalled;
    @Unique private boolean rightClick;
    @Unique private long lastTime;
    @Unique private boolean firstFrame;

    @Shadow public ClientWorld world;
    @Shadow @Final public Mouse mouse;
    @Shadow @Final private Window window;
    @Shadow public Screen currentScreen;

    @Shadow protected abstract void doItemUse();
    @Shadow public abstract Profiler getProfiler();
    @Shadow public abstract boolean isWindowFocused();

    @Shadow
    @Nullable
    public ClientPlayerInteractionManager interactionManager;

    @Shadow
    public @Nullable ClientPlayerEntity player;
    @Shadow
    @Final
    public GameOptions options;
    @Shadow
    private boolean doAttack() {
        return false;
    }
    @Shadow
    public HitResult crosshairTarget;

    @Redirect(method = "handleBlockBreaking", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"), require = 0)
    public boolean breakBlock(ClientPlayerEntity cpim) {
        if(Modules.get().get(MultiTask.class).isActive()) {
            return false;
        }
        return cpim.isUsingItem();
    }

    @Redirect(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;isBreakingBlock()Z"), require = 0)
    public boolean itemBreak(ClientPlayerInteractionManager cpim) {
        if(Modules.get().get(MultiTask.class).isActive()) {
            return false;
        }
        return cpim.isBreakingBlock();
    }

    @Redirect(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"), require = 0)
    public boolean attackCheck(ClientPlayerEntity instance) {
        MultiTask multi = Modules.get().get(MultiTask.class);
        if(multi == null) return player.isUsingItem();
        if(multi.isActive()) {
            while(this.options.attackKey.wasPressed()) {
                this.doAttack();
            }

            while(this.options.useKey.wasPressed()) {
                this.doItemUse();
            }
        }
        return player.isUsingItem();
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        NoxClient.INSTANCE.onInitializeClient();
        firstFrame = true;
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void onPreTick(CallbackInfo info) {
        OnlinePlayers.update();
        doItemUseCalled = false;

        getProfiler().push(NoxClient.MOD_ID + "_pre_update");
        NoxClient.EVENT_BUS.post(TickEvent.Pre.get());
        getProfiler().pop();

        if (rightClick && !doItemUseCalled && interactionManager != null) doItemUse();
        rightClick = false;
    }

    @Inject(at = @At("TAIL"), method = "tick")
    private void onTick(CallbackInfo info) {
        getProfiler().push(NoxClient.MOD_ID + "_post_update");
        NoxClient.EVENT_BUS.post(TickEvent.Post.get());
        getProfiler().pop();
    }

    @Inject(method = "doItemUse", at = @At("HEAD"))
    private void onDoItemUse(CallbackInfo info) {
        doItemUseCalled = true;
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"))
    private void onDisconnect(Screen screen, CallbackInfo info) {
        if (world != null) {
            NoxClient.EVENT_BUS.post(GameLeftEvent.get());
        }
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo info) {
        if (screen instanceof WidgetScreen) screen.mouseMoved(mouse.getX() * window.getScaleFactor(), mouse.getY() * window.getScaleFactor());

        OpenScreenEvent event = OpenScreenEvent.get(screen);
        NoxClient.EVENT_BUS.post(event);

        if (event.isCancelled()) info.cancel();
    }

    @Redirect(method = "doItemUse", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;crosshairTarget:Lnet/minecraft/util/hit/HitResult;", ordinal = 1))
    private HitResult doItemUseMinecraftClientCrosshairTargetProxy(MinecraftClient client) {
        return NoxClient.EVENT_BUS.post(ItemUseCrosshairTargetEvent.get(client.crosshairTarget)).target;
    }

    @Inject(method = "reloadResources(Z)Ljava/util/concurrent/CompletableFuture;", at = @At("RETURN"), cancellable = true)
    private void onReloadResourcesNewCompletableFuture(boolean force, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        cir.setReturnValue(cir.getReturnValue().thenRun(() -> NoxClient.EVENT_BUS.post(ResourcePacksReloadedEvent.get())));
    }

    @ModifyArg(method = "updateWindowTitle", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;setTitle(Ljava/lang/String;)V"))
    private String setTitle(String original) {
        if (Config.get() == null || !Config.get().customWindowTitle.get()) return original;

        String customTitle = Config.get().customWindowTitleText.get();
        Script script = MeteorStarscript.compile(customTitle);

        if (script != null) {
            String title = MeteorStarscript.run(script);
            if (title != null) customTitle = title;
        }

        return customTitle;
    }

    @Inject(method = "onResolutionChanged", at = @At("TAIL"))
    private void onResolutionChanged(CallbackInfo info) {
        NoxClient.EVENT_BUS.post(WindowResizedEvent.get());
    }

    @Inject(method = "getFramerateLimit", at = @At("HEAD"), cancellable = true)
    private void onGetFramerateLimit(CallbackInfoReturnable<Integer> info) {
        if (Modules.get().isActive(UnfocusedCPU.class) && !isWindowFocused()) info.setReturnValue(1);
    }

    // Time delta

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(CallbackInfo info) {
        long time = System.currentTimeMillis();

        if (firstFrame) {
            lastTime = time;
            firstFrame = false;
        }

        Utils.frameTime = (time - lastTime) / 1000.0;
        lastTime = time;
    }

    // Interface

    @Override
    public void rightClick() {
        rightClick = true;
    }
}
