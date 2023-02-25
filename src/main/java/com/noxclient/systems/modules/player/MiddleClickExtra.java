/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.modules.player;

//Created by squidoodly 06/07/2020

import com.noxclient.settings.BoolSetting;
import com.noxclient.settings.EnumSetting;
import com.noxclient.settings.Setting;
import com.noxclient.settings.SettingGroup;
import com.noxclient.events.entity.player.FinishUsingItemEvent;
import com.noxclient.events.entity.player.StoppedUsingItemEvent;
import com.noxclient.events.meteor.MouseButtonEvent;
import com.noxclient.events.world.TickEvent;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import com.noxclient.utils.misc.input.KeyAction;
import com.noxclient.utils.player.FindItemResult;
import com.noxclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE;

public class MiddleClickExtra extends Module {
    private enum Type {
        Immediate,
        LongerSingleClick,
        Longer
    }

    public enum Mode {
        Pearl(Items.ENDER_PEARL, Type.Immediate),
        Rocket(Items.FIREWORK_ROCKET, Type.Immediate),

        Rod(Items.FISHING_ROD, Type.LongerSingleClick),

        Bow(Items.BOW, Type.Longer),
        Gap(Items.GOLDEN_APPLE, Type.Longer),
        EGap(Items.ENCHANTED_GOLDEN_APPLE, Type.Longer),
        Chorus(Items.CHORUS_FRUIT, Type.Longer),
        XP(Items.EXPERIENCE_BOTTLE, Type.Immediate);

        private final Item item;
        private final Type type;

        Mode(Item item, Type type) {
            this.item = item;
            this.type = type;
        }
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
            .name("mode")
            .description("Which item to use when you middle click.")
            .defaultValue(Mode.Pearl)
            .build()
    );

    private final Setting<Boolean> notify = sgGeneral.add(new BoolSetting.Builder()
        .name("notify")
        .description("Notifies you when you do not have the specified item in your hotbar.")
        .defaultValue(true)
        .build()
    );

    private boolean isUsing;

    public MiddleClickExtra() {
        super(Categories.Player, "middle-click-extra", "Lets you use items when you middle click.");
    }

    @Override
    public void onDeactivate() {
        stopIfUsing();
    }

    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action != KeyAction.Press || event.button != GLFW_MOUSE_BUTTON_MIDDLE) return;

        FindItemResult result = InvUtils.findInHotbar(mode.get().item);

        if (!result.found()) {
            if (notify.get()) warning("Unable to find specified item.");
            return;
        }

        InvUtils.swap(result.slot(), true);

        switch (mode.get().type) {
            case Immediate -> {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                InvUtils.swapBack();
            }
            case LongerSingleClick -> mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            case Longer -> {
                mc.options.useKey.setPressed(true);
                isUsing = true;
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (isUsing) {
            boolean pressed = true;

            if (mc.player.getMainHandStack().getItem() instanceof BowItem) {
                pressed = BowItem.getPullProgress(mc.player.getItemUseTime()) < 1;
            }

            mc.options.useKey.setPressed(pressed);
        }
    }

    @EventHandler
    private void onFinishUsingItem(FinishUsingItemEvent event) {
        stopIfUsing();
    }

    @EventHandler
    private void onStoppedUsingItem(StoppedUsingItemEvent event) {
        stopIfUsing();
    }

    private void stopIfUsing() {
        if (isUsing) {
            mc.options.useKey.setPressed(false);
            InvUtils.swapBack();
            isUsing = false;
        }
    }
}
