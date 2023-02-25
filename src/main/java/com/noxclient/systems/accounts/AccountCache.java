/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.accounts;

import com.noxclient.utils.misc.ISerializable;
import com.noxclient.utils.misc.NbtException;
import com.noxclient.utils.render.PlayerHeadTexture;
import com.noxclient.utils.render.PlayerHeadUtils;
import net.minecraft.nbt.NbtCompound;

public class AccountCache implements ISerializable<AccountCache> {
    private PlayerHeadTexture headTexture;
    public String username = "";
    public String uuid = "";

    public PlayerHeadTexture getHeadTexture() {
        return headTexture;
    }

    public void loadHead() {
        headTexture = PlayerHeadUtils.fetchHead(username);
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.putString("username", username);
        tag.putString("uuid", uuid);

        return tag;
    }

    @Override
    public AccountCache fromTag(NbtCompound tag) {
        if (!tag.contains("username") || !tag.contains("uuid")) throw new NbtException();

        username = tag.getString("username");
        uuid = tag.getString("uuid");
        loadHead();

        return this;
    }
}
