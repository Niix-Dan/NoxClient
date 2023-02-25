package com.noxclient.systems.modules.movement;

import com.noxclient.events.world.TickEvent;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class Moon extends Module {
    public Moon() {
        super(Categories.Movement, "moon", "Makes you go weee.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre e) {
        mc.player.addVelocity(0, 0.0568000030517578, 0);
    }
}
