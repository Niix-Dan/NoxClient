package com.noxclient.systems.modules.chat;

import com.noxclient.events.game.GameJoinedEvent;
import com.noxclient.events.world.TickEvent;
import com.noxclient.gui.GuiTheme;
import com.noxclient.gui.utils.StarscriptTextBoxRenderer;
import com.noxclient.gui.widgets.WWidget;
import com.noxclient.gui.widgets.containers.WTable;
import com.noxclient.gui.widgets.input.WTextBox;
import com.noxclient.gui.widgets.pressable.WMinus;
import com.noxclient.gui.widgets.pressable.WPlus;
import com.noxclient.settings.IntSetting;
import com.noxclient.settings.Setting;
import com.noxclient.settings.SettingGroup;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import com.noxclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;

import java.util.HashMap;

public class JoinCommand extends Module {
    private SettingGroup sgGroup = settings.getDefaultGroup();

    public final Setting<Integer> _delay = sgGroup.add(new IntSetting.Builder()
        .name("delay")
        .description("delay")
        .defaultValue(20)
        .build()
    );

    public final HashMap<String, String> commands = new HashMap<>() {{
        put("hypixel.net", "/lobby");
    }};
    private String currMsgK = "", currMsgV = "";
    private String sendmsg = "";
    private int delay = 0;
    public JoinCommand() {
        super(Categories.Chat, "join-command", "Sends a command or message on chat when you joins a server");
    }

    public void onJoin() {
        ServerInfo info = mc.getCurrentServerEntry();
        if(info != null) {
            ChatUtils.info("obtained server info");
            String ip = info.address;

            commands.forEach((_ip, cmd) -> {
                if(ip.contains(_ip)) {
                    ChatUtils.info("found server ip on auto command! ("+ip+")");
                    sendmsg = cmd;
                }
            });
        } else {
            ChatUtils.info("Cant obtain server info");
        }
    }
    @Override
    public void onActivate() {
        onJoin();
    }

    @Override
    public void onDeactivate() {
        sendmsg = "";
        delay = 0;
    }

    @EventHandler
    public void onTick(TickEvent.Pre e) {
        if(delay <= 0) {
            if(sendmsg != "") {
                ChatUtils.sendPlayerMsg(sendmsg);
                ChatUtils.info("command sent "+sendmsg);
                delay = _delay.get();
                sendmsg = "";
            }
        }
        if(delay > 0) delay--;
    }


    public WWidget getWidget(GuiTheme theme) {
        WTable table = theme.table();
        fillTable(theme, table);
        return table;
    }

    private void fillTable(GuiTheme theme, WTable table) {
        table.clear();
        commands.keySet().forEach((key) -> {
            table.add(theme.label(key)).expandCellX();
            table.add(theme.label(commands.get(key))).expandCellX();
            WMinus delete = table.add(theme.minus()).widget();
            delete.action = () -> {
                commands.remove(key);
                fillTable(theme,table);
            };
            table.row();
        });
        WTextBox textBoxK = table.add(theme.textBox(currMsgK)).minWidth(100).expandX().widget();
        textBoxK.action = () -> {
            currMsgK = textBoxK.get();
        };
        WTextBox textBoxV = table.add(theme.textBox(currMsgV, (text1, c) -> true, StarscriptTextBoxRenderer.class)).minWidth(100).expandX().widget();
        textBoxV.action = () -> {
            currMsgV = textBoxV.get();
        };
        WPlus add = table.add(theme.plus()).widget();
        add.action = () -> {
            if (currMsgK != ""  && currMsgV != "") {
                commands.put(currMsgK, currMsgV);
                currMsgK = ""; currMsgV = "";
                fillTable(theme,table);
            }
        };
        table.row();
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = super.toTag();

        NbtCompound messTag = new NbtCompound();
        commands.keySet().forEach((key) -> {
            messTag.put(key, NbtString.of(commands.get(key)));
        });

        tag.put("commands", messTag);
        return tag;
    }

    @Override
    public Module fromTag(NbtCompound tag) {

        commands.clear();
        if (tag.contains("commands")) {
            NbtCompound msgs = tag.getCompound("commands");
            msgs.getKeys().forEach((key) -> {
                commands.put(key, msgs.getString(key));
            });
        }

        return super.fromTag(tag);
    }
}
