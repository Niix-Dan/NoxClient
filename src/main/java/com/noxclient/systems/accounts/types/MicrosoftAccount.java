/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.accounts.types;

import com.noxclient.systems.accounts.Account;
import com.noxclient.systems.accounts.AccountType;
import com.noxclient.systems.accounts.MicrosoftLogin;
import net.minecraft.client.util.Session;

import java.util.Optional;

public class MicrosoftAccount extends Account<MicrosoftAccount> {
    public MicrosoftAccount(String refreshToken) {
        super(AccountType.Microsoft, refreshToken);
    }

    @Override
    public boolean fetchInfo() {
        return auth() != null;
    }

    @Override
    public boolean login() {
        super.login();

        String token = auth();
        if (token == null) return false;

        cache.loadHead();

        setSession(new Session(cache.username, cache.uuid, token, Optional.empty(), Optional.empty(), Session.AccountType.MSA));
        return true;
    }

    private String auth() {
        MicrosoftLogin.LoginData data = MicrosoftLogin.login(name);
        if (!data.isGood()) return null;

        name = data.newRefreshToken;
        cache.username = data.username;
        cache.uuid = data.uuid;

        return data.mcToken;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MicrosoftAccount)) return false;
        return ((MicrosoftAccount) o).name.equals(this.name);
    }
}
