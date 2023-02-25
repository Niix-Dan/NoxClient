package com.noxclient.systems.modules.movement;

import com.noxclient.events.world.TickEvent;
import com.noxclient.settings.DoubleSetting;
import com.noxclient.settings.EnumSetting;
import com.noxclient.settings.Setting;
import com.noxclient.settings.SettingGroup;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;

import java.util.ArrayDeque;
import java.util.Deque;

public class StepPlus extends Module {
    public enum Mode {
        Packet,
        Vanilla,
        Spider,
        Jump
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();


    // General
    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("Mode to use for Step+.")
        .defaultValue(Mode.Packet)
        .build()
    );

    private final Setting<Double> height = sgGeneral.add(new DoubleSetting.Builder()
        .name("height")
        .description("Step height.")
        .defaultValue(1)
        .range(0.1,20)
        .sliderRange(0.1,20)
        .visible(() -> mode.get() == Mode.Vanilla)
        .build()
    );

    private final Setting<Double> cooldown = sgGeneral.add(new DoubleSetting.Builder()
        .name("cooldown")
        .description("Cooldown in seconds to prevent rubberbanding.")
        .defaultValue(2)
        .range(0,20)
        .sliderRange(0,20)
        .build()
    );


    public StepPlus() {
        super(Categories.Movement, "step+", "Allows you to walk up full blocks.");
    }


    private boolean flag;
    private int lastStep = 0;
    private final Deque<Double> queue = new ArrayDeque<>();


    @Override
    public void onActivate() {
        assert mc.player != null;
        mc.player.stepHeight = 0.5F;
    }

    @Override
    public void onDeactivate() {
        assert mc.player != null;
        mc.player.stepHeight = 0.5F;
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        mc.player.stepHeight = mode.get() == Mode.Vanilla ? height.get().floatValue() : 0.5f;

        if (!mc.player.horizontalCollision) {
            queue.clear();
        }

        if (!(mc.player.age < lastStep || mc.player.age >= lastStep + cooldown.get() * 20)) {
            return;
        }

        if (!mc.world.getBlockState(mc.player.getBlockPos().add(0, mc.player.getHeight() + 1, 0)).getMaterial().isReplaceable()
            || mc.player.input.jumping
            || !(mc.player.input.pressingForward || mc.player.input.pressingBack || mc.player.input.pressingLeft || mc.player.input.pressingRight)) {
            return;
        }

        if (!queue.isEmpty()) {
            mc.player.updatePosition(mc.player.getX(), queue.poll(), mc.player.getZ());
            return;
        }

        if (mode.get() == Mode.Packet && mc.player.horizontalCollision && mc.player.isOnGround()) {
            if (!isTouchingWall(mc.player.getBoundingBox().offset(0, 1, 0)) || !isTouchingWall(mc.player.getBoundingBox().offset(0, 1.5, 0))) {
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.42, mc.player.getZ(), false));
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.75, mc.player.getZ(), false));

                if (isTouchingWall(mc.player.getBoundingBox().offset(0, 1, 0)) && !isTouchingWall(mc.player.getBoundingBox().offset(0, 1.5, 0))) {
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1, mc.player.getZ(), false));
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.15, mc.player.getZ(), false));
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.24, mc.player.getZ(), false));
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.15, mc.player.getZ(), true));
                    mc.player.updatePosition(mc.player.getX(), mc.player.getY() + 1.0, mc.player.getZ());
                } else {
                    mc.player.updatePosition(mc.player.getX(), mc.player.getY() + 1, mc.player.getZ());
                }

                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
                lastStep = mc.player.age;
            }
        } else if (mode.get() == Mode.Spider) {
            if (!mc.player.horizontalCollision && flag) {
                mc.player.setVelocity(mc.player.getVelocity().x, -0.1, mc.player.getVelocity().z);
                lastStep = mc.player.age;
                flag = false;
            } else if (mc.player.horizontalCollision) {
                mc.player.setVelocity(mc.player.getVelocity().x, Math.min((mc.player.getY() + 1) - Math.floor(mc.player.getY()), 0.42), mc.player.getVelocity().z);
                flag = true;
            }
        } else if (mode.get() == Mode.Jump) {
            if (mc.player.horizontalCollision && mc.player.isOnGround()) {
                mc.player.jump();
                flag = true;
            }

            if (flag && !mc.player.horizontalCollision /*pos + 1.065 < mc.player.getY()*/) {
                mc.player.setVelocity(mc.player.getVelocity().x, -0.1, mc.player.getVelocity().z);
                lastStep = mc.player.age;
                flag = false;
            }
        }
    }

    private boolean isTouchingWall(Box box) {
        // Check in 2 calls instead of just box.expand(0.01, 0, 0.01) to prevent it getting stuck in corners
        return !mc.world.isSpaceEmpty(box.expand(0.01, 0, 0))
            || !mc.world.isSpaceEmpty(box.expand(0, 0, 0.01));
    }
}
