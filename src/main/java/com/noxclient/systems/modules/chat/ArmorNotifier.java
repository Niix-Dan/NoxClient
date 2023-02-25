package com.noxclient.systems.modules.chat;

import com.noxclient.events.world.TickEvent;
import com.noxclient.settings.DoubleSetting;
import com.noxclient.settings.Setting;
import com.noxclient.settings.SettingGroup;
import com.noxclient.settings.StringSetting;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import com.noxclient.utils.player.ArmorUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ArmorNotifier extends Module {
    private boolean alertedHelmet;
    private boolean alertedChestplate;
    private boolean alertedLeggings;
    private boolean alertedBoots;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTexts = settings.createGroup("Alerts");

    // General

    private final Setting<Double> threshold = sgGeneral.add(new DoubleSetting.Builder()
        .name("durability")
        .description("How low an armor piece needs to be to alert you.")
        .defaultValue(15)
        .range(1, 100)
        .sliderRange(1, 100)
        .build()
    );

    private final Setting<String> helmetAlert = sgTexts.add(new StringSetting.Builder()
        .name("helmet")
        .description("Durability alert for your helmet")
        .defaultValue("Your (highlight)helmet(default) has low durability!")
        .build()
    );

    private final Setting<String> chestAlert = sgTexts.add(new StringSetting.Builder()
        .name("chestplate")
        .description("Durability alert for your chestplate")
        .defaultValue("Your (highlight)chestplate(default) has low durability!")
        .build()
    );

    private final Setting<String> legsAlert = sgTexts.add(new StringSetting.Builder()
        .name("leggings")
        .description("Durability alert for your leggings")
        .defaultValue("Your (highlight)leggings(default) has low durability!")
        .build()
    );

    private final Setting<String> bootsAlert = sgTexts.add(new StringSetting.Builder()
        .name("boots")
        .description("Durability alert for your boots")
        .defaultValue("Your (highlight)boots(default) has low durability!")
        .build()
    );

    // TODO: Notify modes & other players

    public ArmorNotifier() {
        super(Categories.Chat, "armor-notifier", "Notifies you when your armor is low.");
    }

    @Override
    public void onActivate() {
        alertedHelmet = false;
        alertedChestplate = false;
        alertedLeggings = false;
        alertedBoots = false;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        Iterable<ItemStack> armorPieces = mc.player.getArmorItems();
        for (ItemStack armorPiece : armorPieces){
            if (ArmorUtils.checkThreshold(armorPiece, threshold.get())) {
                if (ArmorUtils.isHelmet(armorPiece) && !alertedHelmet) {
                    warning(helmetAlert.get());
                    alertedHelmet = true;
                }

                if (ArmorUtils.isChestplate(armorPiece) && !alertedChestplate) {
                    warning(chestAlert.get());
                    alertedChestplate = true;
                }

                if (ArmorUtils.areLeggings(armorPiece) && !alertedLeggings) {
                    warning(legsAlert.get());
                    alertedLeggings = true;
                }

                if (ArmorUtils.areBoots(armorPiece) && !alertedBoots) {
                    warning(bootsAlert.get());
                    alertedBoots = true;
                }
            }

            if (!ArmorUtils.checkThreshold(armorPiece, threshold.get())) {
                if (ArmorUtils.isHelmet(armorPiece) && alertedHelmet) alertedHelmet = false;
                if (ArmorUtils.isChestplate(armorPiece) && alertedChestplate) alertedChestplate = false;
                if (ArmorUtils.areLeggings(armorPiece) && alertedLeggings) alertedLeggings = false;
                if (ArmorUtils.areBoots(armorPiece) && alertedBoots) alertedBoots = false;
            }
        }
    }
}
