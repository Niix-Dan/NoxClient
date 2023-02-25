package com.noxclient.systems.modules.misc;

import com.noxclient.events.render.Render3DEvent;
import com.noxclient.events.world.PlaySoundEvent;
import com.noxclient.events.world.TickEvent;
import com.noxclient.renderer.ShapeMode;
import com.noxclient.settings.*;
import com.noxclient.systems.modules.Categories;
import com.noxclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.noxclient.systems.modules.Module;

public class SoundLocator extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final Setting<List<SoundEvent>> sounds = sgGeneral.add(new SoundEventListSetting.Builder()
        .name("sounds")
        .description("Sounds to find.")
        .defaultValue(new ArrayList<>(0))
        .build()
    );

    private final Setting<Boolean> whitelist = sgGeneral.add(new BoolSetting.Builder()
        .name("whitelist")
        .description("Set sounds list to whitelist")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> timeS = sgGeneral.add(new IntSetting.Builder()
        .name("time")
        .description("Song render time")
        .defaultValue(60)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The side color of the target sound rendering.")
        .defaultValue(new SettingColor(255, 0, 0, 70))
        .build()
    );
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The line color of the target sound rendering.")
        .defaultValue(new SettingColor(255, 0, 0))
        .build()
    );

    public SoundLocator() {
        super(Categories.Render, "sound-locator", "Render boxes on sounds locations");
    }

    private List<Vec3d> renderPos = new ArrayList<Vec3d>();
    private List<Integer> delay = new ArrayList<Integer>();

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        Iterator<Integer> iterator = delay.iterator();

        while (iterator.hasNext()) {
            int time = iterator.next();
            if (time <= 0) {
                iterator.remove();
                renderPos.remove(0);
            } else {
                delay.set(delay.indexOf(time), time - 1);
            }
        }
    }

    @EventHandler
    private void onPlaySound(PlaySoundEvent event) {
        if(whitelist.get()) {
            for (SoundEvent sound : sounds.get()) {
                if (sound.getId().equals(event.sound.getId())) {
                    printSound(event.sound);
                    break;
                }
            }
        } else {
            printSound(event.sound);
        }
    }

    private void printSound(SoundInstance sound) {
        WeightedSoundSet soundSet = mc.getSoundManager().get(sound.getId());

        Vec3d pos = new Vec3d(sound.getX() - 0.5, sound.getY() - 0.5, sound.getZ() - 0.5);
        if(!renderPos.contains(pos)) {
            renderPos.add(pos);
            delay.add(timeS.get());
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        renderPos.forEach(pos -> {
            event.renderer.box(Box.from(pos), sideColor.get(), lineColor.get(), shapeMode.get(), 0);
        });
    }
}
