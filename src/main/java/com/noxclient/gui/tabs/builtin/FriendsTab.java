/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.tabs.builtin;

import com.noxclient.gui.widgets.containers.WHorizontalList;
import com.noxclient.gui.widgets.containers.WTable;
import com.noxclient.gui.widgets.input.WTextBox;
import com.noxclient.gui.widgets.pressable.WMinus;
import com.noxclient.gui.widgets.pressable.WPlus;
import com.noxclient.gui.GuiTheme;
import com.noxclient.gui.tabs.Tab;
import com.noxclient.gui.tabs.TabScreen;
import com.noxclient.gui.tabs.WindowTabScreen;
import com.noxclient.systems.friends.Friend;
import com.noxclient.systems.friends.Friends;
import com.noxclient.utils.misc.NbtUtils;
import com.noxclient.utils.network.MeteorExecutor;
import net.minecraft.client.gui.screen.Screen;

public class FriendsTab extends Tab {
    public FriendsTab() {
        super("[ Friends ]");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new FriendsScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof FriendsScreen;
    }

    private static class FriendsScreen extends WindowTabScreen {
        public FriendsScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);
        }

        @Override
        public void initWidgets() {
            WTable table = add(theme.table()).expandX().minWidth(400).widget();
            initTable(table);

            add(theme.horizontalSeparator()).expandX();

            // New
            WHorizontalList list = add(theme.horizontalList()).expandX().widget();

            WTextBox nameW = list.add(theme.textBox("", (text, c) -> c != ' ')).expandX().widget();
            nameW.setFocused(true);

            WPlus add = list.add(theme.plus()).widget();
            add.action = () -> {
                String name = nameW.get().trim();
                Friend friend = new Friend(name);

                if (Friends.get().add(friend)) {
                    MeteorExecutor.execute(() -> {
                        friend.updateInfo();
                        nameW.set("");
                        reload();
                    });
                }
            };

            enterAction = add.action;
        }

        private void initTable(WTable table) {
            table.clear();
            if (Friends.get().isEmpty()) return;

            for (Friend friend : Friends.get()) {
                //table.add(theme.texture(32, 32, friend.headTexture.needsRotate() ? 90 : 0, friend.headTexture));
                table.add(theme.label(friend.name));

                WMinus remove = table.add(theme.minus()).expandCellX().right().widget();
                remove.action = () -> {
                    Friends.get().remove(friend);
                    reload();
                };

                table.row();
            }
        }

        @Override
        public boolean toClipboard() {
            return NbtUtils.toClipboard(Friends.get());
        }

        @Override
        public boolean fromClipboard() {
            return NbtUtils.fromClipboard(Friends.get());
        }
    }
}
