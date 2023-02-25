package com.noxclient.systems.modules.chat;

import com.noxclient.NoxClient;
import com.noxclient.settings.*;
import com.noxclient.events.world.TickEvent;
import com.noxclient.systems.friends.Friends;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import com.noxclient.utils.Utils;
import com.noxclient.utils.player.ChatUtils;
import com.noxclient.utils.player.DamageUtils;
import com.noxclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;

public class ChatNotify extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();


    private final Setting<Boolean> _health = sgGeneral.add(new BoolSetting.Builder()
        .name("health")
        .description("Sends a message when your health is lower or equal to a value.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> health = sgGeneral.add(new IntSetting.Builder()
        .name("health-value")
        .description("Automatically sends a message when health is lower or equal to this value.")
        .defaultValue(6)
        .range(0, 20)
        .sliderMax(20)
        .visible(_health::get)
        .build()
    );

    private final Setting<String> health_msg = sgGeneral.add(new StringSetting.Builder()
        .name("health-message")
        .description("Automatically sends a message in chat when health is lower or equal the value above.")
        .defaultValue("Low Health! | ####")
        .visible(_health::get)
        .build()
    );

    private final Setting<Boolean> smart = sgGeneral.add(new BoolSetting.Builder()
        .name("smart")
        .description("Sends a message when you're about to take enough damage to kill you.")
        .defaultValue(false)
        .visible(_health::get)
        .build()
    );

    private final Setting<String> smart_msg = sgGeneral.add(new StringSetting.Builder()
        .name("smart-message")
        .description("Automatically sends a message in chat when you're about to take enough damage to kill you.")
        .defaultValue("Low Health! | ####")
        .visible2(smart::get, _health::get)
        .build()
    );

    private final Setting<Boolean> onlyTrusted = sgGeneral.add(new BoolSetting.Builder()
        .name("only-trusted")
        .description("Sends a message when a player not on your friends list appears in render distance.")
        .defaultValue(false)
        .build()
    );

    private final Setting<String> onlyTrusted_msg = sgGeneral.add(new StringSetting.Builder()
        .name("only-trusted-message")
        .description("Automatically sends a message in chat when a player not on your friends list appears in render distance.")
        .defaultValue("Unknown player! | ####")
        .visible(onlyTrusted::get)
        .build()
    );

    private final Setting<Boolean> instantDeath = sgGeneral.add(new BoolSetting.Builder()
        .name("32K")
        .description("Sends a message when a player near you can instantly kill you.")
        .defaultValue(false)
        .build()
    );

    private final Setting<String> instantDeath_msg = sgGeneral.add(new StringSetting.Builder()
        .name("32k-message")
        .description("Automatically sends a message in chat when a player near you can instantly kill you.")
        .defaultValue("32k Nearby! | ####")
        .visible(instantDeath::get)
        .build()
    );

    private final Setting<Boolean> crystalLog = sgGeneral.add(new BoolSetting.Builder()
        .name("crystal-nearby")
        .description("Sends a message when a crystal appears near you.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("How close a crystal has to be to you before you sends a message.")
        .defaultValue(4)
        .range(1, 10)
        .sliderMax(5)
        .visible(crystalLog::get)
        .build()
    );

    private final Setting<String> crystalLog_msg = sgGeneral.add(new StringSetting.Builder()
        .name("crystal-message")
        .description("Automatically sends a message in chat when a crystal appears near you.")
        .defaultValue("Crystals Nearby! | ####")
        .visible(crystalLog::get)
        .build()
    );

    public ChatNotify() {
        super(Categories.Chat, "chat-notify", "Automatically sends a message in chat when a certain requirements are met.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (_health.get() && mc.player.getHealth() <= health.get()) {
            info("[Health] Health was lower than " + health.get() + ".");
            if(health_msg.get() != "" && health_msg.isVisible()) ChatUtils.sendPlayerMsg(health_msg.get());
        }
        if(smart.get() && mc.player.getHealth() + mc.player.getAbsorptionAmount() - PlayerUtils.possibleHealthReductions() < health.get()){
            info("[Smart] Health was going to be lower than " + health.get() + ".");
            if(smart_msg.get() != "" && smart_msg.isVisible()) ChatUtils.sendPlayerMsg(smart_msg.get());
        }


        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity && entity.getUuid() != mc.player.getUuid()) {
                if (onlyTrusted.get() && entity != mc.player && !Friends.get().isFriend((PlayerEntity) entity)) {
                    info("[OnlyTrusted] A non-trusted player appeared in your render distance.");
                    if(onlyTrusted_msg.get() != "" && onlyTrusted_msg.isVisible()) ChatUtils.sendPlayerMsg(onlyTrusted_msg.get());
                    break;
                }
                if (mc.player.distanceTo(entity) < 8 && instantDeath.get() && DamageUtils.getSwordDamage((PlayerEntity) entity, true)
                    > mc.player.getHealth() + mc.player.getAbsorptionAmount()) {
                    info("[32k] Anti-32k measures.");
                    if(instantDeath_msg.get() != "" && instantDeath_msg.isVisible()) ChatUtils.sendPlayerMsg(instantDeath_msg.get());
                    break;
                }
            }

            if (entity instanceof EndCrystalEntity && mc.player.distanceTo(entity) < range.get() && crystalLog.get()) {
                info("[Crystal] End Crystal appeared within specified range.");
                if(crystalLog_msg.get() != "" && crystalLog_msg.isVisible()) ChatUtils.sendPlayerMsg(crystalLog_msg.get());
            }
        }
    }

    private class StaticListener {
        @EventHandler
        private void healthListener(TickEvent.Post event) {
            if (isActive()) disableHealthListener();

            else if (Utils.canUpdate()
                && !mc.player.isDead()
                && mc.player.getHealth() >= health.get()) {
                toggle();
                disableHealthListener();
            }
        }
    }

    private final ChatNotify.StaticListener staticListener = new ChatNotify.StaticListener();

    private void enableHealthListener(){
        NoxClient.EVENT_BUS.subscribe(staticListener);
    }
    private void disableHealthListener(){
        NoxClient.EVENT_BUS.unsubscribe(staticListener);
    }
}
