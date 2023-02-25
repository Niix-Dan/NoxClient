package com.noxclient.systems.modules.combat;


import com.noxclient.events.packets.PacketEvent;
import com.noxclient.mixininterface.IPlayerInteractEntityC2SPacket;
import com.noxclient.settings.BoolSetting;
import com.noxclient.settings.Setting;
import com.noxclient.settings.SettingGroup;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;

import com.noxclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;

public class KnockbackPlus extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> ka = sgGeneral.add(new BoolSetting.Builder()
        .name("only-killaura")
        .description("Only performs more KB when using killaura.")
        .defaultValue(false)
        .build()
    );

    private PlayerInteractEntityC2SPacket attackPacket;
    private boolean sendPackets;
    private int sendTimer;
    public KnockbackPlus() {
        super(Categories.Combat, "knockback-plus", "Performs more KB when you hit your target.");
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof IPlayerInteractEntityC2SPacket packet && packet.getType() == PlayerInteractEntityC2SPacket.InteractType.ATTACK) {

            Entity entity = packet.getEntity();

            if (!(entity instanceof LivingEntity) || (entity != Modules.get().get(KillAura.class).getTarget() && ka.get())) return;
            sendPacket();
        }
    }

    @EventHandler
    private void sendPacket() {
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        ClientCommandC2SPacket packet = new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING);

        mc.player.networkHandler.sendPacket(packet);
    }
}
