/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.settings;

import com.noxclient.gui.GuiTheme;
import com.noxclient.gui.WidgetScreen;
import com.noxclient.utils.misc.IChangeable;
import com.noxclient.utils.misc.ICopyable;
import com.noxclient.utils.misc.ISerializable;
import net.minecraft.block.Block;

public interface IBlockData<T extends ICopyable<T> & ISerializable<T> & IChangeable & IBlockData<T>> {
    WidgetScreen createScreen(GuiTheme theme, Block block, BlockDataSetting<T> setting);
}
