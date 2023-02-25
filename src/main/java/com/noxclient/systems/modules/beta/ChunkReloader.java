package com.noxclient.systems.modules.beta;

import com.noxclient.events.world.TickEvent;
import com.noxclient.mixin.MinecraftClientAccessor;
import com.noxclient.settings.*;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import com.noxclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;

public class ChunkReloader extends Module {
    public ChunkReloader() {
        super(Categories.Dev, "chunk-reloader", "automatically reloads your chunks at certain frame rate");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> minFps = sgGeneral.add(new DoubleSetting.Builder()
        .name("min-fps")
        .description("The minimum fps for chunk reload.")
        .defaultValue(20)
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay in ticks between fps checks")
        .defaultValue(80)
        .build()
    );

    private final Setting<Boolean> log = sgGeneral.add(new BoolSetting.Builder()
        .name("debug")
        .description("Sent reloads on chat")
        .defaultValue(true)
        .build()
    );

    private int waiting = 0;

    @EventHandler
    public void onTick(TickEvent.Pre e) {
        if(waiting <= 0) {
            double fps = MinecraftClientAccessor.getFps();

            if(fps <= minFps.get()) {
                if(log.get()) {
                    ChatUtils.info(String.format("§cYour frame rate is below §e%.0f §6(%.0f)§c!", minFps.get(), fps));
                    ChatUtils.info("§6Reloading chunks...");
                }
                mc.worldRenderer.reload();
            }
            waiting = delay.get();
        }
        waiting--;
    }

}
