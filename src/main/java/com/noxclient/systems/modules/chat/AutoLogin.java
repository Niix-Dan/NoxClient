package com.noxclient.systems.modules.chat;

import com.noxclient.NoxClient;
import com.noxclient.events.game.ReceiveMessageEvent;
import com.noxclient.events.world.TickEvent;
import com.noxclient.settings.*;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;

import java.util.ArrayList;
import java.util.List;

public class AutoLogin extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // General

    private final Setting<String> regPass = sgGeneral.add(new StringSetting.Builder()
        .name("register_command")
        .description("The command to auto register.")
        .defaultValue("/register noxontop noxontop")
        .build()
    );

    private final Setting<String> logPass = sgGeneral.add(new StringSetting.Builder()
        .name("login_command")
        .description("The command to auto log in.")
        .defaultValue("/login noxontop")
        .build()
    );

    private final Setting<Integer> dls = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("The delay to waits in ticks before sent login command.")
        .defaultValue(1)
        .min(1)
        .max(120)
        .sliderMin(1)
        .build()
    );
    private final Setting<List<String>> registerMessages = sgGeneral.add(new StringListSetting.Builder()
        .name("register-messages")
        .description("The messages to verify if the server needs to register.")
        .defaultValue(List.of("/register ", "/register <password>", "/register <password> <password>"))
        .build()
    );

    private final Setting<List<String>> loginMessages = sgGeneral.add(new StringListSetting.Builder()
        .name("login-messages")
        .description("The messages to verify if the server needs to log in.")
        .defaultValue(List.of("/login ", "/login <password>"))
        .build()
    );

    public AutoLogin() {
        super(Categories.Chat, "auto-login", "Automatically logs into servers that use /login.");
    }

    private int delay = 0;
    private boolean sentLogin = false;
    private String cmd = "";

    @Override
    public void onActivate() {
        sentLogin = false;

        delay = dls.get();
    }

    @Override
    public void onDeactivate() {
        delay = dls.get();
    }

    @EventHandler
    private void onTick(TickEvent.Pre e) {
        if(sentLogin) {
            if(delay >= 0) {
                delay-=1;
            } else {
                NoxClient.mc.player.sendCommand(cmd.replace("/", ""));
                cmd = null;
                delay = dls.get();
                sentLogin = false;
            }
        }
    }

    private boolean hasMsg(String msg, List<String> msgs) {
        boolean has = false;
        for(String txt : msgs) {
            if(msg.contains(txt)) has = true;
        }
        return has;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onMessageReceive(ReceiveMessageEvent event) {
        if(hasMsg(event.getMessage().getString(), loginMessages.get())) {
            sentLogin = true;
            cmd = logPass.get();
        } else if(hasMsg(event.getMessage().getString(), registerMessages.get())) {
            sentLogin = true;
            cmd = regPass.get();
        }
    }
}
