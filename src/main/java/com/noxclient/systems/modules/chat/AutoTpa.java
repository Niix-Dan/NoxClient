package com.noxclient.systems.modules.chat;

import com.noxclient.NoxClient;
import com.noxclient.events.game.ReceiveMessageEvent;
import com.noxclient.settings.*;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;

import meteordevelopment.orbit.EventHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

import java.util.List;

public class AutoTpa extends Module {
    public enum Mode {
        All,
        Blacklist,
        Whitelist
    }

    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    private final Setting<Enum<Mode>> mode = sgGeneral.add(new EnumSetting.Builder<Enum<Mode>>()
        .name("mode")
        .defaultValue(Mode.Whitelist)
        .build()
    );

    private final Setting<List<String>> list = sgGeneral.add(new StringListSetting.Builder()
        .name("list")
        .visible(() -> !mode.get().equals(Mode.All))
        .build()
    );

    private final Setting<String> msg = sgGeneral.add(new StringSetting.Builder()
        .name("request-text")
        .description("The text required to run the tpa accept command")
        .defaultValue("has requested to teleport to you")
        .build()
    );

    private final Setting<String> command = sgGeneral.add(new StringSetting.Builder()
        .name("tpaaccept-command")
        .description("tpaccept")
        .defaultValue("tpaccept")
        .build()
    );

    public AutoTpa() {
        super(Categories.Chat, "auto-tpa", "Automatically accepts tpa requests.");
    }

    @EventHandler
    private void onMessageReceive(ReceiveMessageEvent event) {
        if (event.isModified() || event.isCancelled() || mc.player == null) return;
        Text text = event.getMessage();

        String message = text.getString();
        int idx = message.indexOf(' ');
        if (idx == -1) return;

        String name = message.substring(0, idx);
        message = message.substring(idx);

        if (!message.contains(msg.get())) return;
        if (!mode.get().equals(Mode.All)) {
            NoxClient.LOG.info(name);
            if (list.get().contains(name)) {
                if (mode.get().equals(Mode.Whitelist)) {
                    mc.player.sendCommand(command.get());
                }
            }
        } else {
            mc.player.sendCommand(command.get());
        }
    }
}
