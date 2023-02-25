/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.mixin;

import com.noxclient.NoxClient;
import com.noxclient.events.entity.EntityDestroyEvent;
import com.noxclient.events.entity.player.PickItemsEvent;
import com.noxclient.events.game.GameJoinedEvent;
import com.noxclient.events.game.GameLeftEvent;
import com.noxclient.events.packets.ContainerSlotUpdateEvent;
import com.noxclient.events.packets.PlaySoundPacketEvent;
import com.noxclient.events.world.ChunkDataEvent;
import com.noxclient.mixininterface.IExplosionS2CPacket;
import com.noxclient.systems.modules.Modules;
import com.noxclient.systems.modules.movement.Velocity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow private ClientWorld world;

    private boolean worldNotNull;

    @Inject(method = "onGameJoin", at = @At("HEAD"))
    private void onGameJoinHead(GameJoinS2CPacket packet, CallbackInfo info) {
        worldNotNull = world != null;
    }

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void onGameJoinTail(GameJoinS2CPacket packet, CallbackInfo info) {
        if (worldNotNull) {
            NoxClient.EVENT_BUS.post(GameLeftEvent.get());
        }

        NoxClient.EVENT_BUS.post(GameJoinedEvent.get());
    }

    @Inject(method = "onPlaySound", at = @At("HEAD"))
    private void onPlaySound(PlaySoundS2CPacket packet, CallbackInfo info) {
        NoxClient.EVENT_BUS.post(PlaySoundPacketEvent.get(packet));
    }

    @Inject(method = "onChunkData", at = @At("TAIL"))
    private void onChunkData(ChunkDataS2CPacket packet, CallbackInfo info) {
        WorldChunk chunk = client.world.getChunk(packet.getX(), packet.getZ());
        NoxClient.EVENT_BUS.post(ChunkDataEvent.get(chunk));
    }

    @Inject(method = "onScreenHandlerSlotUpdate", at = @At("TAIL"))
    private void onContainerSlotUpdate(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo info) {
        NoxClient.EVENT_BUS.post(ContainerSlotUpdateEvent.get(packet));
    }

    @Inject(method = "onEntitiesDestroy", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/EntitiesDestroyS2CPacket;getEntityIds()Lit/unimi/dsi/fastutil/ints/IntList;"))
    private void onEntitiesDestroy(EntitiesDestroyS2CPacket packet, CallbackInfo ci) {
        for (int id : packet.getEntityIds()) {
            NoxClient.EVENT_BUS.post(EntityDestroyEvent.get(client.world.getEntityById(id)));
        }
    }

    @Inject(
        method = "onExplosion",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V",
            shift = At.Shift.AFTER
        )
    )
    private void onExplosionVelocity(ExplosionS2CPacket packet, CallbackInfo ci) {
        Velocity velocity = Modules.get().get(Velocity.class);
        if (!velocity.explosions.get()) return;

        ((IExplosionS2CPacket) packet).setVelocityX((float) (packet.getPlayerVelocityX() * velocity.getHorizontal(velocity.explosionsHorizontal)));
        ((IExplosionS2CPacket) packet).setVelocityY((float) (packet.getPlayerVelocityY() * velocity.getVertical(velocity.explosionsVertical)));
        ((IExplosionS2CPacket) packet).setVelocityZ((float) (packet.getPlayerVelocityZ() * velocity.getHorizontal(velocity.explosionsHorizontal)));
    }

    @Inject(method = "onItemPickupAnimation", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getEntityById(I)Lnet/minecraft/entity/Entity;", ordinal = 0))
    private void onItemPickupAnimation(ItemPickupAnimationS2CPacket packet, CallbackInfo info) {
        Entity itemEntity = client.world.getEntityById(packet.getEntityId());
        Entity entity = client.world.getEntityById(packet.getCollectorEntityId());

        if (itemEntity instanceof ItemEntity && entity == client.player) {
            NoxClient.EVENT_BUS.post(PickItemsEvent.get(((ItemEntity) itemEntity).getStack(), packet.getStackAmount()));
        }
    }
}
