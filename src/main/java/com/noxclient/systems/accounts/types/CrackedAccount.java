/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.accounts.types;

import com.noxclient.systems.accounts.Account;
import com.noxclient.systems.accounts.AccountType;
import net.minecraft.client.util.Session;

import java.util.Optional;

public class CrackedAccount extends Account<CrackedAccount> {
    public CrackedAccount(String name) {
        super(AccountType.Cracked, name);
    }

    @Override
    public boolean fetchInfo() {
        cache.username = name;
        return true;
    }

    @Override
    public boolean login() {
        super.login();

        cache.loadHead();
        setSession(new Session(name, "", "", Optional.empty(), Optional.empty(), Session.AccountType.MOJANG));
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CrackedAccount)) return false;
        return ((CrackedAccount) o).getUsername().equals(this.getUsername());
    }
}
