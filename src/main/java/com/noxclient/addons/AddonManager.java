/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.addons;

import com.noxclient.NoxClient;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;

import java.util.ArrayList;
import java.util.List;

public class AddonManager {
    public static final List<MeteorAddon> ADDONS = new ArrayList<>();

    public static void init() {
        // Meteor pseudo addon
        {
            NoxClient.ADDON = new MeteorAddon() {
                @Override
                public void onInitialize() {}

                @Override
                public String getPackage() {
                    return "com.noxclient";
                }

                @Override
                public String getWebsite() {
                    return " ";
                }

                @Override
                public GithubRepo getRepo() {
                    return new GithubRepo(" ", " ");
                }

                @Override
                public String getCommit() {
                    String commit = NoxClient.MOD_META.getCustomValue(NoxClient.MOD_ID + ":commit").getAsString();
                    return commit.isEmpty() ? null : commit;
                }
            };

            ModMetadata metadata = FabricLoader.getInstance().getModContainer(NoxClient.MOD_ID).get().getMetadata();

            NoxClient.ADDON.name = metadata.getName();
            NoxClient.ADDON.authors = new String[metadata.getAuthors().size()];
            if (metadata.containsCustomValue(NoxClient.MOD_ID + ":color")) {
                NoxClient.ADDON.color.parse(metadata.getCustomValue(NoxClient.MOD_ID + ":color").getAsString());
            }

            int i = 0;
            for (Person author : metadata.getAuthors()) {
                NoxClient.ADDON.authors[i++] = author.getName();
            }
        }

        // Addons
        for (EntrypointContainer<MeteorAddon> entrypoint : FabricLoader.getInstance().getEntrypointContainers("meteor", MeteorAddon.class)) {
            ModMetadata metadata = entrypoint.getProvider().getMetadata();
            MeteorAddon addon = entrypoint.getEntrypoint();

            addon.name = metadata.getName();
            addon.authors = new String[metadata.getAuthors().size()];
            if (metadata.containsCustomValue(NoxClient.MOD_ID + ":color")) {
                addon.color.parse(metadata.getCustomValue(NoxClient.MOD_ID + ":color").getAsString());
            }

            int i = 0;
            for (Person author : metadata.getAuthors()) {
                addon.authors[i++] = author.getName();
            }

            ADDONS.add(addon);
        }
    }
}
