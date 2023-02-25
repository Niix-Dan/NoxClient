/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.tabs.screens.settings;

import com.noxclient.gui.renderer.GuiRenderer;
import com.noxclient.gui.widgets.containers.WTable;
import com.noxclient.gui.widgets.input.WTextBox;
import com.noxclient.gui.widgets.pressable.WButton;
import com.noxclient.NoxClient;
import com.noxclient.gui.GuiTheme;
import com.noxclient.gui.WindowScreen;
import com.noxclient.settings.BlockDataSetting;
import com.noxclient.settings.IBlockData;
import com.noxclient.utils.misc.IChangeable;
import com.noxclient.utils.misc.ICopyable;
import com.noxclient.utils.misc.ISerializable;
import com.noxclient.utils.misc.Names;
import net.minecraft.block.Block;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BlockDataSettingScreen extends WindowScreen {
    private static final List<Block> BLOCKS = new ArrayList<>(100);

    private final BlockDataSetting<?> setting;

    private WTable table;
    private String filterText = "";

    public BlockDataSettingScreen(GuiTheme theme, BlockDataSetting<?> setting) {
        super(theme, "Configure Blocks");

        this.setting = setting;
    }

    @Override
    public void initWidgets() {
        WTextBox filter = add(theme.textBox("")).minWidth(400).expandX().widget();
        filter.setFocused(true);
        filter.action = () -> {
            filterText = filter.get().trim();

            table.clear();
            initTable();
        };

        table = add(theme.table()).expandX().widget();

        initTable();
    }

    public <T extends ICopyable<T> & ISerializable<T> & IChangeable & IBlockData<T>> void initTable() {
        for (Block block : Registry.BLOCK) {
            T blockData = (T) setting.get().get(block);

            if (blockData != null && blockData.isChanged()) BLOCKS.add(0, block);
            else BLOCKS.add(block);
        }

        for (Block block : BLOCKS) {
            String name = Names.get(block);
            if (!StringUtils.containsIgnoreCase(name, filterText)) continue;

            T blockData = (T) setting.get().get(block);

            table.add(theme.itemWithLabel(block.asItem().getDefaultStack(), Names.get(block))).expandCellX();
            table.add(theme.label((blockData != null && blockData.isChanged()) ? "*" : " "));

            WButton edit = table.add(theme.button(GuiRenderer.EDIT)).widget();
            edit.action = () -> {
                T data = blockData;
                if (data == null) data = (T) setting.defaultData.get().copy();

                NoxClient.mc.setScreen(data.createScreen(theme, block, (BlockDataSetting<T>) setting));
            };

            WButton reset = table.add(theme.button(GuiRenderer.RESET)).widget();
            reset.action = () -> {
                setting.get().remove(block);
                setting.onChanged();

                if (blockData != null && blockData.isChanged()) {
                    table.clear();
                    initTable();
                }
            };

            table.row();
        }

        BLOCKS.clear();
    }
}
