/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.modules.player;

import com.noxclient.events.world.TickEvent;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import com.noxclient.utils.player.FindItemResult;
import com.noxclient.utils.player.InvUtils;
import com.noxclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;

public class EXPThrower extends Module {
    public EXPThrower() {
        super(Categories.Player, "exp-thrower", "Automatically throws XP bottles from your hotbar.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        FindItemResult exp = InvUtils.findInHotbar(Items.EXPERIENCE_BOTTLE);
        if (!exp.found()) return;

        Rotations.rotate(mc.player.getYaw(), 90, () -> {
            if (exp.getHand() != null) {
                mc.interactionManager.interactItem(mc.player, exp.getHand());
            }
            else {
                InvUtils.swap(exp.slot(), true);
                mc.interactionManager.interactItem(mc.player, exp.getHand());
                InvUtils.swapBack();
            }
        });
    }
}
