/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.tabs.screens;

import com.noxclient.gui.widgets.WAccount;
import com.noxclient.gui.widgets.containers.WContainer;
import com.noxclient.gui.widgets.containers.WHorizontalList;
import com.noxclient.gui.widgets.pressable.WButton;
import com.noxclient.NoxClient;
import com.noxclient.gui.GuiTheme;
import com.noxclient.gui.WindowScreen;
import com.noxclient.systems.accounts.Account;
import com.noxclient.systems.accounts.Accounts;
import com.noxclient.systems.accounts.MicrosoftLogin;
import com.noxclient.systems.accounts.types.MicrosoftAccount;
import com.noxclient.utils.misc.NbtUtils;
import com.noxclient.utils.network.MeteorExecutor;
import org.jetbrains.annotations.Nullable;

public class AccountsScreen extends WindowScreen {
    public AccountsScreen(GuiTheme theme) {
        super(theme, "Accounts");
    }

    @Override
    public void initWidgets() {
        // Accounts
        for (Account<?> account : Accounts.get()) {
            WAccount wAccount = add(theme.account(this, account)).expandX().widget();
            wAccount.refreshScreenAction = this::reload;
        }

        // Add account
        WHorizontalList l = add(theme.horizontalList()).expandX().widget();

        addButton(l, "Cracked", () -> NoxClient.mc.setScreen(new AddCrackedAccountScreen(theme, this)));
        addButton(l, "Altening", () -> NoxClient.mc.setScreen(new AddAlteningAccountScreen(theme, this)));
        addButton(l, "Microsoft", () -> {
            locked = true;

            MicrosoftLogin.getRefreshToken(refreshToken -> {
                locked = false;

                if (refreshToken != null) {
                    MicrosoftAccount account = new MicrosoftAccount(refreshToken);
                    addAccount(null, this, account);
                }
            });
        });
    }

    private void addButton(WContainer c, String text, Runnable action) {
        WButton button = c.add(theme.button(text)).expandX().widget();
        button.action = action;
    }

    public static void addAccount(@Nullable AddAccountScreen screen, AccountsScreen parent, Account<?> account) {
        if (screen != null) screen.locked = true;

        MeteorExecutor.execute(() -> {
            if (account.fetchInfo()) {
                account.getCache().loadHead();

                Accounts.get().add(account);
                if (account.login()) Accounts.get().save();

                if (screen != null) {
                    screen.locked = false;
                    screen.close();
                }

                parent.reload();

                return;
            }

            if (screen != null) screen.locked = false;
        });
    }

    @Override
    public boolean toClipboard() {
        return NbtUtils.toClipboard(Accounts.get());
    }

    @Override
    public boolean fromClipboard() {
        return NbtUtils.fromClipboard(Accounts.get());
    }
}
