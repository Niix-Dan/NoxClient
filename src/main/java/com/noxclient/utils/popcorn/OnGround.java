package com.noxclient.utils.popcorn;

import com.noxclient.NoxClient;
import com.noxclient.events.packets.PacketEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class OnGround {
    private boolean onGround;
    public OnGround() {
        NoxClient.EVENT_BUS.subscribe(this);
        this.onGround = false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPacket(PacketEvent.Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket) {
            onGround = ((PlayerMoveC2SPacket) event.packet).isOnGround();
        }
    }

    public boolean isOnGround() {return onGround;}
}
