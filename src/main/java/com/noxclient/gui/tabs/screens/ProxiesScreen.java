/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.tabs.screens;

import com.noxclient.gui.renderer.GuiRenderer;
import com.noxclient.gui.widgets.WLabel;
import com.noxclient.gui.widgets.containers.WHorizontalList;
import com.noxclient.gui.widgets.containers.WTable;
import com.noxclient.gui.widgets.pressable.WButton;
import com.noxclient.gui.widgets.pressable.WCheckbox;
import com.noxclient.gui.widgets.pressable.WMinus;
import com.noxclient.NoxClient;
import com.noxclient.gui.GuiTheme;
import com.noxclient.gui.WindowScreen;
import com.noxclient.settings.Settings;
import com.noxclient.systems.proxies.Proxies;
import com.noxclient.systems.proxies.Proxy;
import com.noxclient.utils.misc.NbtUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ProxiesScreen extends WindowScreen {
    private final List<WCheckbox> checkboxes = new ArrayList<>();

    public ProxiesScreen(GuiTheme theme) {
        super(theme, "Proxies");
    }

    @Override
    public void initWidgets() {
        WTable table = add(theme.table()).expandX().minWidth(400).widget();
        initTable(table);

        add(theme.horizontalSeparator()).expandX();

        WHorizontalList l = add(theme.horizontalList()).expandX().widget();

        // New
        WButton newBtn = l.add(theme.button("New")).expandX().widget();
        newBtn.action = () -> NoxClient.mc.setScreen(new EditProxyScreen(theme, null, this::reload));

        // Import
        PointerBuffer filters = BufferUtils.createPointerBuffer(1);

        ByteBuffer txtFilter = MemoryUtil.memASCII("*.txt");

        filters.put(txtFilter);
        filters.rewind();

        WButton importBtn = l.add(theme.button("Import")).expandX().widget();
        importBtn.action = () -> {
            String selectedFile = TinyFileDialogs.tinyfd_openFileDialog("Import Proxies", null, filters, null, false);
            if (selectedFile != null) {
                File file = new File(selectedFile);
                NoxClient.mc.setScreen(new ProxiesImportScreen(theme, file));
            }
        };
    }

    private void initTable(WTable table) {
        table.clear();
        if (Proxies.get().isEmpty()) return;

        for (Proxy proxy : Proxies.get()) {
            WCheckbox enabled = table.add(theme.checkbox(proxy.enabled.get())).widget();
            checkboxes.add(enabled);
            enabled.action = () -> {
                boolean checked = enabled.checked;
                Proxies.get().setEnabled(proxy, checked);

                for (WCheckbox checkbox : checkboxes) checkbox.checked = false;
                enabled.checked = checked;
            };

            WLabel name = table.add(theme.label(proxy.name.get())).widget();
            name.color = theme.textColor();

            WLabel type = table.add(theme.label("(" + proxy.type.get() + ")")).widget();
            type.color = theme.textSecondaryColor();

            WHorizontalList ipList = table.add(theme.horizontalList()).expandCellX().widget();
            ipList.spacing = 0;

            ipList.add(theme.label(proxy.address.get()));
            ipList.add(theme.label(":")).widget().color = theme.textSecondaryColor();
            ipList.add(theme.label(Integer.toString(proxy.port.get())));

            WButton edit = table.add(theme.button(GuiRenderer.EDIT)).widget();
            edit.action = () -> NoxClient.mc.setScreen(new EditProxyScreen(theme, proxy, this::reload));

            WMinus remove = table.add(theme.minus()).widget();
            remove.action = () -> {
                Proxies.get().remove(proxy);
                reload();
            };

            table.row();
        }
    }

    @Override
    public boolean toClipboard() {
        return NbtUtils.toClipboard(Proxies.get());
    }

    @Override
    public boolean fromClipboard() {
        return NbtUtils.fromClipboard(Proxies.get());
    }

    protected static class EditProxyScreen extends EditSystemScreen<Proxy> {
        public EditProxyScreen(GuiTheme theme, Proxy value, Runnable reload) {
            super(theme, value, reload);
        }

        @Override
        public Proxy create() {
            return new Proxy.Builder().build();
        }

        @Override
        public boolean save() {
            return value.resolveAddress() && (!isNew || Proxies.get().add(value));
        }

        @Override
        public Settings getSettings() {
            return value.settings;
        }
    }
}
