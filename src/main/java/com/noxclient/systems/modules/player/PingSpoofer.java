package com.noxclient.systems.modules.player;

import com.noxclient.events.game.GameJoinedEvent;
import com.noxclient.events.packets.PacketEvent;
import com.noxclient.events.world.TickEvent;
import com.noxclient.settings.BoolSetting;
import com.noxclient.settings.IntSetting;
import com.noxclient.settings.Setting;
import com.noxclient.settings.SettingGroup;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import net.minecraft.network.packet.s2c.play.KeepAliveS2CPacket;

public class PingSpoofer extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> strict = sgGeneral.add(new BoolSetting.Builder()
        .name("strict")
        .description("Responds as fast as possible to keep alive packets send by the server.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> ping = sgGeneral.add(new IntSetting.Builder()
        .name("ping")
        .description("The ping to set.")
        .defaultValue(250)
        .min(0)
        .sliderMin(100)
        .sliderMax(1000)
        .noSlider()
        .visible(() -> !strict.get())
        .build()
    );

    private final Setting<Integer> deviation = sgGeneral.add(new IntSetting.Builder()
        .name("deviation")
        .description("Randomize the ping by this amount.")
        .defaultValue(0)
        .min(0)
        .sliderMin(0)
        .sliderMax(50)
        .noSlider()
        .visible(() -> !strict.get())
        .build()
    );

    public PingSpoofer() {
        super(Categories.Player, "ping-spoof", "Modify your ping.");
    }

    private long id;
    private long timer;
    private int nextPing;

    @Override
    public void onActivate() {

        id = -1;
        timer = System.currentTimeMillis();
        nextPing = getPing();
    }


    private int getPing() {
        if (deviation.get() == 0) return ping.get();
        return (int) (Math.random() * (deviation.get() * 2) - deviation.get() + ping.get());
    }


    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        if (System.currentTimeMillis() - timer >= nextPing && id >= 0 && !strict.get()) {
            mc.getNetworkHandler().sendPacket(new KeepAliveC2SPacket(id));
            nextPing = getPing();
        }
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof KeepAliveC2SPacket packet && id != packet.getId()) {
            if (strict.get()) {
                event.cancel();
            } else if (ping.get() != 0) {
                id = packet.getId();
                timer = System.currentTimeMillis();

                event.cancel();
            }
        }
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (strict.get() && event.packet instanceof KeepAliveS2CPacket packet) {
            id = packet.getId();
            mc.getNetworkHandler().sendPacket(new KeepAliveC2SPacket(id));
        }
    }

    @Override
    public String getInfoString() {
        if (mc != null && mc.player != null && mc.getNetworkHandler() != null) {
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            return entry != null ? entry.getLatency() + "ms" : null;
        }

        return null;
    }
}
