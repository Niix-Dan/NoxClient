package com.noxclient.systems.modules.movement;

import com.noxclient.NoxClient;
import com.noxclient.events.entity.player.PlayerMoveEvent;
import com.noxclient.settings.*;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import com.noxclient.systems.modules.Modules;
import com.noxclient.systems.modules.world.Timer;
import com.noxclient.utils.BEntityUtils;
import com.noxclient.utils.TimerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;

public class ReverseStepTimer extends Module {
    public enum Mode {
        Timer,
        Packet,
        Both
    }


    private final SettingGroup sgGeneral = settings.getDefaultGroup();


    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("Mode to use to bypass reverse step.")
        .defaultValue(Mode.Packet)
        .build()
    );

    private final Setting<Double> timer = sgGeneral.add(new DoubleSetting.Builder()
        .name("timer")
        .description("How fast to speed up timer for timer mode.")
        .min(0)
        .defaultValue(10)
        .visible(() -> mode.get() != Mode.Packet)
        .build()
    );

    private final Setting<Double> height = sgGeneral.add(new DoubleSetting.Builder()
        .name("height")
        .description("The maximum y height you are allowed to fall.")
        .min(0)
        .defaultValue(5)
        .build()
    );

    private final Setting<Boolean> webs = sgGeneral.add(new BoolSetting.Builder()
        .name("webs")
        .description("Will pull you even if there are webs below you.")
        .defaultValue(false)
        .build()
    );


    public ReverseStepTimer() {
        super(Categories.Movement, "reverse-step+", "Tries to bypass strict anticheats for reverse step.");
    }


    private int fallTicks;
    private final TimerUtils strictTimer = new TimerUtils();


    @EventHandler
    private void onMove(PlayerMoveEvent event) {
        if (mc.player.isSubmergedInWater() || mc.player.isInLava() || mc.player.isFallFlying() || mc.player.isClimbing() || mc.player.getVehicle() != null) return;

        if (BEntityUtils.isWebbed(mc.player) && !webs.get()) return;

        if (mc.options.jumpKey.isPressed() || mc.options.sneakKey.isPressed()) return;

        if (mc.player.isOnGround() && mc.world.isAir(BEntityUtils.playerPos(mc.player).down())) fallTicks = 0;
        else fallTicks++;

        if (mc.player.fallDistance > 0 && (fallTicks > 0 && fallTicks < 10)) {
            double fallingBlock = mc.world.getBottomY();
            for (double y = mc.player.getY(); y > mc.world.getBottomY(); y -= 0.001) {
                if (mc.world.getBlockState(new BlockPos(mc.player.getX(), y, mc.player.getZ())).getBlock().getDefaultState().getCollisionShape(mc.world, new BlockPos(0, 0,0 )) == null) continue;

                fallingBlock = y;
                break;
            }

            if (fallingBlock >= mc.player.getY()) return;
            double fallHeight = mc.player.getY() - fallingBlock;
            if (fallHeight > height.get()) return;

            if (mode.get() != Mode.Timer) {
                // if (strictTimer.passedSec(1)) {
                if (mc.player.networkHandler != null) {
                    if (fallHeight > 0.5) {
                        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() - 0.07840000152, mc.player.getZ(), false));
                        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() - 0.23363200604, mc.player.getZ(), false));
                        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() - 0.46415937495, mc.player.getZ(), false));
                        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() - 0.76847620241, mc.player.getZ(), false));

                        if (fallHeight >= 1.5) {
                            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() - 1.14510670065, mc.player.getZ(), false));
                            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() - 1.59260459764, mc.player.getZ(), false));

                            if (fallHeight >= 2.5) {
                                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() - 2.10955254674, mc.player.getZ(), false));
                                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() - 2.69456154825, mc.player.getZ(), false));

                                if (fallHeight >= 3.5) {
                                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() - 3.34627038241, mc.player.getZ(), false));
                                }
                            }
                        }
                    }
                }

                mc.player.setPosition(mc.player.getX(), fallingBlock + 0.1, mc.player.getZ());
                mc.player.setVelocity(0, 0, 0);
                strictTimer.reset();
                //  }
            }

            if (mode.get() != Mode.Packet) Modules.get().get(Timer.class).setOverride(timer.get());
        }

        if (mode.get() != Mode.Packet) Modules.get().get(Timer.class).setOverride(Timer.OFF);
    }
}
