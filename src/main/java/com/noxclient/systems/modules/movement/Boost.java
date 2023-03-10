package com.noxclient.systems.modules.movement;

import com.noxclient.events.world.TickEvent;
import com.noxclient.settings.*;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;

public class Boost extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> strength = sgGeneral.add(new DoubleSetting.Builder()
        .name("strength")
        .description("Strength to yeet you with.")
        .defaultValue(0.5)
        .min(0.00001)
        .sliderMax(10)
        .build()
    );
    private final Setting<Boolean> autoBoost = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-boost")
        .description("Automatically boosts you.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("delay between each boost")
        .visible(autoBoost::get)
        .defaultValue(20)
        .range(0, 120)
        .sliderRange(0, 120)
        .build()
    );

    public Boost() {
        super(Categories.Movement, "boost", "Works like a dash move.");
    }

    private int _delay = 0;

    @Override
    public void onActivate() {
        boostPlayer();
        _delay = delay.get();
        if(!autoBoost.get()) this.toggle();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if(autoBoost.get()) {
            if(_delay <= 1) {
                boostPlayer();
                _delay = delay.get();
            } else {
                _delay--;
            }
        }
    }

    public void boostPlayer() {
        Vec3d v = mc.player.getRotationVecClient().multiply(strength.get());
        mc.player.addVelocity(v.getX(), v.getY(), v.getZ());
    }
}
