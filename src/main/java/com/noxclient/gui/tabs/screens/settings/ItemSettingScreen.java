/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.tabs.screens.settings;

import com.noxclient.gui.widgets.WItemWithLabel;
import com.noxclient.gui.widgets.containers.WTable;
import com.noxclient.gui.widgets.input.WTextBox;
import com.noxclient.gui.widgets.pressable.WButton;
import com.noxclient.gui.GuiTheme;
import com.noxclient.gui.WindowScreen;
import com.noxclient.settings.ItemSetting;
import com.noxclient.utils.misc.Names;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.StringUtils;

public class ItemSettingScreen extends WindowScreen {
    private final ItemSetting setting;

    private WTable table;

    private WTextBox filter;
    private String filterText = "";

    public ItemSettingScreen(GuiTheme theme, ItemSetting setting) {
        super(theme, "Select item");

        this.setting = setting;
    }

    @Override
    public void initWidgets() {
        filter = add(theme.textBox("")).minWidth(400).expandX().widget();
        filter.setFocused(true);
        filter.action = () -> {
            filterText = filter.get().trim();

            table.clear();
            initTable();
        };

        table = add(theme.table()).expandX().widget();
        initTable();
    }

    public void initTable() {
        for (Item item : Registry.ITEM) {
            if (setting.filter != null && !setting.filter.test(item)) continue;
            if (item == Items.AIR) continue;

            WItemWithLabel itemLabel = theme.itemWithLabel(item.getDefaultStack(), Names.get(item));
            if (!filterText.isEmpty() && !StringUtils.containsIgnoreCase(itemLabel.getLabelText(), filterText)) continue;
            table.add(itemLabel);

            WButton select = table.add(theme.button("Select")).expandCellX().right().widget();
            select.action = () -> {
                setting.set(item);
                close();
            };

            table.row();
        }
    }
}
