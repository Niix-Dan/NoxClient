package com.noxclient.utils.popcorn;

import com.noxclient.NoxClient;
import com.noxclient.events.packets.PacketEvent;
import com.noxclient.events.render.Render3DEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;

public class Packet {
    int packets;
    float timer;
    int sent;

    public Packet() {
        NoxClient.EVENT_BUS.subscribe(this);
        this.packets = -1;
        this.timer = 0;
        this.sent = 0;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        timer += event.frameTime;
        if (timer >= 0.25) {
            packets = Math.round(sent * 5 / timer);
            timer = 0;
            sent = 0;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPacket(PacketEvent.Send event) {
        sent++;
    }

    public int getSent() {return packets;}
    public double getTimer() {return timer;}
}
