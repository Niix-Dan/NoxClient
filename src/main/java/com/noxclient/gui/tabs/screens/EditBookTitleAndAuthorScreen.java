/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.tabs.screens;

import com.noxclient.gui.widgets.containers.WTable;
import com.noxclient.gui.widgets.input.WTextBox;
import com.noxclient.NoxClient;
import com.noxclient.gui.GuiTheme;
import com.noxclient.gui.WindowScreen;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EditBookTitleAndAuthorScreen extends WindowScreen {
    private final ItemStack itemStack;
    private final Hand hand;

    public EditBookTitleAndAuthorScreen(GuiTheme theme, ItemStack itemStack, Hand hand) {
        super(theme, "Edit title & author");
        this.itemStack = itemStack;
        this.hand = hand;
    }

    @Override
    public void initWidgets() {
        WTable t = add(theme.table()).expandX().widget();

        t.add(theme.label("Title"));
        WTextBox title = t.add(theme.textBox(itemStack.getOrCreateNbt().getString("title"))).minWidth(220).expandX().widget();
        t.row();

        t.add(theme.label("Author"));
        WTextBox author = t.add(theme.textBox(itemStack.getNbt().getString("author"))).minWidth(220).expandX().widget();
        t.row();

        t.add(theme.button("Done")).expandX().widget().action = () -> {
            itemStack.getNbt().putString("author", author.get());
            itemStack.getNbt().putString("title", title.get());

            BookScreen.Contents contents = new BookScreen.WrittenBookContents(itemStack);
            List<String> pages = new ArrayList<>(contents.getPageCount());
            for (int i = 0; i < contents.getPageCount(); i++) pages.add(contents.getPage(i).getString());

            NoxClient.mc.getNetworkHandler().sendPacket(new BookUpdateC2SPacket(hand == Hand.MAIN_HAND ? NoxClient.mc.player.getInventory().selectedSlot : 40, pages, Optional.of(title.get())));

            close();
        };
    }
}
