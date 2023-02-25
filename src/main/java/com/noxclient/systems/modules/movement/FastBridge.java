/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */


package com.noxclient.systems.modules.movement;


import meteordevelopment.orbit.EventHandler;
import com.noxclient.events.world.TickEvent;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;


public class FastBridge extends Module {
    public FastBridge() {
        super(Categories.Movement, "Fast bridge", "Automatically sneaks at block edge (idea by kokqi).");
    }

    boolean turn = true;
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.world.getBlockState(mc.player.getSteppingPos()).isAir()) {
            if (!mc.player.isOnGround()) return;
            turn = true;
            mc.options.sneakKey.setPressed(true);
        } else if (turn) {
            turn = false;
            mc.options.sneakKey.setPressed(false);
        }
    }
}
