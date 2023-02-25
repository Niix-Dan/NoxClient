/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.utils;

import com.noxclient.gui.widgets.WWidget;
import com.noxclient.gui.GuiTheme;
import com.noxclient.settings.Settings;

public interface SettingsWidgetFactory {
    WWidget create(GuiTheme theme, Settings settings, String filter);
}
