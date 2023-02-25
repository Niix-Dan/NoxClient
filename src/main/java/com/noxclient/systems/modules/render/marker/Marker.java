/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.modules.render.marker;

import com.noxclient.gui.GuiTheme;
import com.noxclient.gui.renderer.GuiRenderer;
import com.noxclient.gui.widgets.WLabel;
import com.noxclient.gui.widgets.WWidget;
import com.noxclient.gui.widgets.containers.WHorizontalList;
import com.noxclient.gui.widgets.containers.WVerticalList;
import com.noxclient.gui.widgets.input.WDropdown;
import com.noxclient.gui.widgets.pressable.WButton;
import com.noxclient.gui.widgets.pressable.WCheckbox;
import com.noxclient.gui.widgets.pressable.WMinus;
import com.noxclient.events.render.Render3DEvent;
import com.noxclient.events.world.TickEvent;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;

public class Marker extends Module {
    private final MarkerFactory factory = new MarkerFactory();
    private final ArrayList<BaseMarker> markers = new ArrayList<>();

    public Marker() {
        super(Categories.Render, "marker", "Renders shapes. Useful for large scale projects");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        for (BaseMarker marker : markers) {
            if (marker.isVisible()) marker.tick();
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        for (BaseMarker marker : markers) {
            if (marker.isVisible()) marker.render(event);
        }
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = super.toTag();

        NbtList list = new NbtList();
        for (BaseMarker marker : markers) {
            NbtCompound mTag = new NbtCompound();
            mTag.putString("type", marker.getTypeName());
            mTag.put("marker", marker.toTag());

            list.add(mTag);
        }

        tag.put("markers", list);
        return tag;
    }

    @Override
    public Module fromTag(NbtCompound tag) {
        super.fromTag(tag);

        markers.clear();
        NbtList list = tag.getList("markers", 10);

        for (NbtElement tagII : list) {
            NbtCompound tagI = (NbtCompound) tagII;

            String type = tagI.getString("type");
            BaseMarker marker = factory.createMarker(type);

            if (marker != null) {
                NbtCompound markerTag = (NbtCompound) tagI.get("marker");
                if (markerTag != null) marker.fromTag(markerTag);

                markers.add(marker);
            }
        }

        return this;
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WVerticalList list = theme.verticalList();
        fillList(theme, list);
        return list;
    }

    protected void fillList(GuiTheme theme, WVerticalList list) {
        // Marker List
        for (BaseMarker marker : markers) {
            WHorizontalList hList = list.add(theme.horizontalList()).expandX().widget();

            // Name
            WLabel label = hList.add(theme.label(marker.name.get())).widget();
            label.tooltip = marker.description.get();

            // Dimension
            hList.add(theme.label(" - " + marker.getDimension().toString())).expandX().widget().color = theme.textSecondaryColor();

            // Toggle
            WCheckbox checkbox = hList.add(theme.checkbox(marker.isActive())).widget();
            checkbox.action = () -> {
                if (marker.isActive() != checkbox.checked) marker.toggle();
            };

            // Edit
            WButton edit = hList.add(theme.button(GuiRenderer.EDIT)).widget();
            edit.action = () -> mc.setScreen(marker.getScreen(theme));

            // Remove
            WMinus remove = hList.add(theme.minus()).widget();
            remove.action = () -> {
                markers.remove(marker);
                marker.settings.unregisterColorSettings();

                list.clear();
                fillList(theme, list);
            };
        }

        // Bottom
        WHorizontalList bottom = list.add(theme.horizontalList()).expandX().widget();

        WDropdown<String> newMarker = bottom.add(theme.dropdown(factory.getNames(), factory.getNames()[0])).widget();
        WButton add = bottom.add(theme.button("Add")).expandX().widget();
        add.action = () -> {
            String name = newMarker.get();
            markers.add(factory.createMarker(name));

            list.clear();
            fillList(theme, list);
        };

    }
}
