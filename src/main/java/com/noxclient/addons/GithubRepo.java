/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.addons;

public record GithubRepo(String owner, String name, String branch) {
    public GithubRepo(String owner, String name) {
        this(owner, name, "master");
    }

    public String getOwnerName() {
        return owner + "/" + name;
    }
}
