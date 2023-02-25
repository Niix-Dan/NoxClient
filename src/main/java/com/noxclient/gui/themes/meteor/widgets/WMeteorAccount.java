/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.themes.meteor.widgets;

import com.noxclient.gui.widgets.WAccount;
import com.noxclient.gui.WidgetScreen;
import com.noxclient.gui.themes.meteor.MeteorWidget;
import com.noxclient.systems.accounts.Account;
import com.noxclient.utils.render.color.Color;

public class WMeteorAccount extends WAccount implements MeteorWidget {
    public WMeteorAccount(WidgetScreen screen, Account<?> account) {
        super(screen, account);
    }

    @Override
    protected Color loggedInColor() {
        return theme().loggedInColor.get();
    }

    @Override
    protected Color accountTypeColor() {
        return theme().textSecondaryColor.get();
    }
}
