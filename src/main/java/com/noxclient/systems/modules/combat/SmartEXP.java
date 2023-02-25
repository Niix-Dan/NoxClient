/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */
package com.noxclient.systems.modules.combat;

import com.noxclient.settings.*;
import com.noxclient.events.world.TickEvent;

import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import com.noxclient.utils.player.*;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class SmartEXP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPause = settings.createGroup("Pause Conditions");

    private final Setting<com.noxclient.systems.modules.combat.AutoEXP.Mode> mode = sgGeneral.add(new EnumSetting.Builder<com.noxclient.systems.modules.combat.AutoEXP.Mode>()
        .name("mode")
        .description("Which items to repair.")
        .defaultValue(com.noxclient.systems.modules.combat.AutoEXP.Mode.Both)
        .build()
    );

    private final Setting<Boolean> syncslot = sgGeneral.add(new BoolSetting.Builder()
        .name("Sync Slot")
        .description("Syncronize the slot to your main hand.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> slot = sgGeneral.add(new IntSetting.Builder()
        .name("exp-slot")
        .description("The slot to put the exp bottle into.")
        .invisible(syncslot::get)
        .defaultValue(6)
        .range(1, 9)
        .sliderRange(1, 9)
        .build()
    );

    private final Setting<Integer> dls = sgGeneral.add(new IntSetting.Builder()
        .name("action-delay")
        .description("The delay between xp use actions in ticks.")
        .defaultValue(2)
        .build()
    );

    private final Setting<Integer> minThreshold = sgGeneral.add(new IntSetting.Builder()
        .name("min-threshold")
        .description("The minimum durability percentage that an item needs to fall to, to be repaired.")
        .defaultValue(60)
        .range(1, 95)
        .sliderRange(1, 95)
        .build()
    );

    private final Setting<Integer> maxThreshold = sgGeneral.add(new IntSetting.Builder()
        .name("max-threshold")
        .description("The maximum durability percentage to repair items to.")
        .defaultValue(80)
        .range(1, 100)
        .sliderRange(1, 100)
        .build()
    );


    // Misc

    private final Setting<Boolean> pauseOnEat = sgPause.add(new BoolSetting.Builder()
        .name("pause-on-eat")
        .description("Pauses while eating.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> pauseOnDrink = sgPause.add(new BoolSetting.Builder()
        .name("pause-on-drink")
        .description("Pauses while drinking potions.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> pauseOnMine = sgPause.add(new BoolSetting.Builder()
        .name("pause-on-mine")
        .description("Pauses while mining blocks.")
        .defaultValue(false)
        .build()
    );

    private int repairingI;
    private int delay;

    public SmartEXP() {
        super(Categories.Combat, "smart-exp", "Automatically repairs your armor and tools in pvp.");
    }

    @Override
    public void onActivate() {
        repairingI = -1;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if(delay >= 0) {
            delay--;
            return;
        }
        delay = dls.get();
        if (PlayerUtils.shouldPause(pauseOnMine.get(), pauseOnEat.get(), pauseOnDrink.get())) return;

        if (repairingI == -1) {
            if (mode.get() != com.noxclient.systems.modules.combat.AutoEXP.Mode.Hands) {
                for (int i = 0; i < mc.player.getInventory().armor.size(); i++) {
                    if (needsRepair(mc.player.getInventory().armor.get(i), minThreshold.get())) {
                        repairingI = SlotUtils.ARMOR_START + i;
                        break;
                    }
                }
            }

            if (mode.get() != com.noxclient.systems.modules.combat.AutoEXP.Mode.Armor && repairingI == -1) {
                for (Hand hand : Hand.values()) {
                    if (needsRepair(mc.player.getStackInHand(hand), minThreshold.get())) {
                        repairingI = hand == Hand.MAIN_HAND ? mc.player.getInventory().selectedSlot : SlotUtils.OFFHAND;
                        break;
                    }
                }
            }
        }

        if (repairingI != -1) {
            if (!needsRepair(mc.player.getInventory().getStack(repairingI), maxThreshold.get() >= 100 ? 99 : maxThreshold.get())) {
                repairingI = -1;
                return;
            }

            FindItemResult exp = InvUtils.find(Items.EXPERIENCE_BOTTLE);

            if (exp.found()) {
                if (!exp.isHotbar() && !exp.isOffhand()) {
                    if(syncslot.get()) InvUtils.move().from(exp.slot()).toHotbar(mc.player.getInventory().selectedSlot);
                    else InvUtils.move().from(exp.slot()).toHotbar(slot.get() - 1);
                }

                Rotations.rotate(mc.player.getYaw(), 90, () -> {
                    if(syncslot.get()) InvUtils.swap(mc.player.getInventory().selectedSlot, true);
                    else InvUtils.swap(slot.get() - 1, true);

                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);

                    InvUtils.swapBack();

                    if(syncslot.get()) InvUtils.move().fromHotbar(mc.player.getInventory().selectedSlot).to(exp.slot());
                    else InvUtils.move().fromHotbar(slot.get() - 1).to(exp.slot());
                });
            }
        }
    }

    private boolean needsRepair(ItemStack itemStack, double threshold) {
        if (itemStack.isEmpty() || EnchantmentHelper.getLevel(Enchantments.MENDING, itemStack) < 1) return false;
        return (itemStack.getMaxDamage() - itemStack.getDamage()) / (double) itemStack.getMaxDamage() * 100 <= threshold;
    }

    public enum Mode {
        Armor,
        Hands,
        Both
    }
}

