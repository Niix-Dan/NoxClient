/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.modules.render;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalGetToBlock;
import com.noxclient.gui.GuiTheme;
import com.noxclient.gui.renderer.GuiRenderer;
import com.noxclient.gui.tabs.screens.EditSystemScreen;
import com.noxclient.gui.widgets.WLabel;
import com.noxclient.gui.widgets.WWidget;
import com.noxclient.gui.widgets.containers.WTable;
import com.noxclient.gui.widgets.pressable.WButton;
import com.noxclient.gui.widgets.pressable.WCheckbox;
import com.noxclient.gui.widgets.pressable.WMinus;
import com.noxclient.settings.*;
import com.noxclient.systems.waypoints.Waypoint;
import com.noxclient.systems.waypoints.Waypoints;
import com.noxclient.events.game.OpenScreenEvent;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import com.noxclient.utils.Utils;
import com.noxclient.utils.player.PlayerUtils;
import com.noxclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ListIterator;

import static com.noxclient.utils.player.ChatUtils.formatCoords;

public class WaypointsModule extends Module {
    private static final Color GRAY = new Color(200, 200, 200);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgDeathPosition = settings.createGroup("Death Position");

    public final Setting<Integer> textRenderDistance = sgGeneral.add(new IntSetting.Builder()
        .name("text-render-distance")
        .description("Maximum distance from the center of the screen at which text will be rendered.")
        .defaultValue(100)
        .min(0)
        .sliderMax(200)
        .build()
    );

    private final Setting<Integer> maxDeathPositions = sgDeathPosition.add(new IntSetting.Builder()
        .name("max-death-positions")
        .description("The amount of death positions to save, 0 to disable")
        .defaultValue(0)
        .min(0)
        .sliderMax(20)
        .onChanged(this::cleanDeathWPs)
        .build()
    );

    private final Setting<Boolean> dpChat = sgDeathPosition.add(new BoolSetting.Builder()
        .name("chat")
        .description("Send a chat message with your position once you die")
        .defaultValue(false)
        .build()
    );

    public WaypointsModule() {
        super(Categories.Render, "waypoints", "Allows you to create waypoints.");
    }

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (!(event.screen instanceof DeathScreen)) return;

        if (!event.isCancelled()) addDeath(mc.player.getPos());
    }

    public void addDeath(Vec3d deathPos) {
        String time = dateFormat.format(new Date());
        if (dpChat.get()) {
            MutableText text = Text.literal("Died at ");
            text.append(formatCoords(deathPos));
            text.append(String.format(" on %s.", time));
            info(text);
        }

        // Create waypoint
        if (maxDeathPositions.get() > 0) {
            Waypoint waypoint = new Waypoint.Builder()
                .name("Death " + time)
                .icon("skull")
                .pos(new BlockPos(deathPos).up(2))
                .dimension(PlayerUtils.getDimension())
                .build();

            Waypoints.get().add(waypoint);
        }

        cleanDeathWPs(maxDeathPositions.get());
    }

    private void cleanDeathWPs(int max) {
        int oldWpC = 0;

        ListIterator<Waypoint> wps = Waypoints.get().iteratorReverse();
        while (wps.hasPrevious()) {
            Waypoint wp = wps.previous();
            if (wp.name.get().startsWith("Death ") && "skull".equals(wp.icon.get())) {
                oldWpC++;
                if (oldWpC > max)
                    Waypoints.get().remove(wp);
            }
        }
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        if (!Utils.canUpdate()) return theme.label("You need to be in a world.");

        WTable table = theme.table();
        initTable(theme, table);
        return table;
    }

    private void initTable(GuiTheme theme, WTable table) {
        table.clear();

        for (Waypoint waypoint : Waypoints.get()) {
            boolean validDim = Waypoints.checkDimension(waypoint);

            table.add(new WIcon(waypoint));

            WLabel name = table.add(theme.label(waypoint.name.get())).expandCellX().widget();
            if (!validDim) name.color = GRAY;

            WCheckbox visible = table.add(theme.checkbox(waypoint.visible.get())).widget();
            visible.action = () -> {
                waypoint.visible.set(visible.checked);
                Waypoints.get().save();
            };

            WButton edit = table.add(theme.button(GuiRenderer.EDIT)).widget();
            edit.action = () -> mc.setScreen(new EditWaypointScreen(theme, waypoint, null));

            // Goto
            if (validDim) {
                WButton gotoB = table.add(theme.button("Goto")).widget();
                gotoB.action = () -> {
                    IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
                    if (baritone.getPathingBehavior().isPathing()) baritone.getPathingBehavior().cancelEverything();
                    baritone.getCustomGoalProcess().setGoalAndPath(new GoalGetToBlock(waypoint.getPos()));
                };
            }

            WMinus remove = table.add(theme.minus()).widget();
            remove.action = () -> {
                Waypoints.get().remove(waypoint);
                initTable(theme, table);
            };

            table.row();
        }

        table.add(theme.horizontalSeparator()).expandX();
        table.row();

        WButton create = table.add(theme.button("Create")).expandX().widget();
        create.action = () -> mc.setScreen(new EditWaypointScreen(theme, null, () -> initTable(theme, table)));
    }

    private class EditWaypointScreen extends EditSystemScreen<Waypoint> {
        public EditWaypointScreen(GuiTheme theme, Waypoint value, Runnable reload) {
            super(theme, value, reload);
        }

        @Override
        public Waypoint create() {
            return new Waypoint.Builder()
                .pos(mc.player.getBlockPos().up(2))
                .dimension(PlayerUtils.getDimension())
                .build();
        }

        @Override
        public boolean save() {
            return !isNew || Waypoints.get().add(value);
        }

        @Override
        public Settings getSettings() {
            return value.settings;
        }
    }

    private static class WIcon extends WWidget {
        private final Waypoint waypoint;

        public WIcon(Waypoint waypoint) {
            this.waypoint = waypoint;
        }

        @Override
        protected void onCalculateSize() {
            double s = theme.scale(32);

            width = s;
            height = s;
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            renderer.post(() -> waypoint.renderIcon(x, y, 1, width));
        }
    }
}
