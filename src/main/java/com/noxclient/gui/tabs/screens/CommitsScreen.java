/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.tabs.screens;

import com.noxclient.gui.widgets.containers.WHorizontalList;
import com.noxclient.gui.widgets.containers.WTable;
import com.noxclient.addons.GithubRepo;
import com.noxclient.addons.MeteorAddon;
import com.noxclient.gui.GuiTheme;
import com.noxclient.gui.WindowScreen;
import com.noxclient.utils.network.Http;
import com.noxclient.utils.network.MeteorExecutor;
import net.minecraft.util.Util;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class CommitsScreen extends WindowScreen {
    private final MeteorAddon addon;
    private Commit[] commits;

    public CommitsScreen(GuiTheme theme, MeteorAddon addon) {
        super(theme, "Commits for " + addon.name);

        this.addon = addon;

        locked = true;
        lockedAllowClose = true;

        MeteorExecutor.execute(() -> {
            GithubRepo repo = addon.getRepo();
            Response res = Http.get(String.format("https://api.github.com/repos/%s/compare/%s...%s", repo.getOwnerName(), addon.getCommit(), repo.branch())).sendJson(Response.class);

            if (res != null) {
                commits = res.commits;
                taskAfterRender = this::populateWidgets;
            }
            else locked = false;
        });
    }

    @Override
    public void initWidgets() {
        // Only initialize widgets after data arrives
    }

    private void populateWidgets() {
        // Top
        WHorizontalList l = add(theme.horizontalList()).expandX().widget();

        String text = "There are %d new commits";
        if (commits.length == 1) text = "There is %d new commit";
        l.add(theme.label(String.format(text, commits.length))).expandX();

        String website = addon.getWebsite();
        if (website != null) l.add(theme.button("Website")).widget().action = () -> Util.getOperatingSystem().open(website);

        l.add(theme.button("GitHub")).widget().action = () -> {
            GithubRepo repo = addon.getRepo();
            Util.getOperatingSystem().open(String.format("https://github.com/%s/tree/%s", repo.getOwnerName(), repo.branch()));
        };

        // Commits
        if (commits.length > 0) {
            add(theme.horizontalSeparator()).padVertical(theme.scale(8)).expandX();

            WTable t = add(theme.table()).expandX().widget();
            t.horizontalSpacing = 0;

            for (Commit commit : commits) {
                String date = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(commit.commit.committer.date));
                t.add(theme.label(date)).top().right().widget().color = theme.textSecondaryColor();

                t.add(theme.label(getMessage(commit))).widget().action = () -> Util.getOperatingSystem().open(String.format("https://github.com/%s/commit/%s", addon.getRepo().getOwnerName(), commit.sha));
                t.row();
            }
        }

        locked = false;
    }

    private static String getMessage(Commit commit) {
        StringBuilder sb = new StringBuilder(" - ");
        String message = commit.commit.message;

        for (int i = 0; i < message.length(); i++) {
            if (i >= 80) {
                sb.append("...");
                break;
            }

            char c = message.charAt(i);

            if (c == '\n') {
                sb.append("...");
                break;
            }

            sb.append(c);
        }

        return sb.toString();
    }

    private static class Response {
        public Commit[] commits;
    }

    private static class Commit {
        public String sha;
        public CommitInner commit;
    }

    private static class CommitInner {
        public Committer committer;
        public String message;
    }

    private static class Committer {
        public String date;
    }
}
