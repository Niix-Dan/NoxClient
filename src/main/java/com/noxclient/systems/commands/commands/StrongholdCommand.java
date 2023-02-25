package com.noxclient.systems.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.noxclient.systems.commands.Command;
import com.noxclient.utils.player.ChatUtils;
import com.noxclient.utils.player.FindItemResult;
import com.noxclient.utils.player.InvUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class StrongholdCommand extends Command {
    public StrongholdCommand() {
        super("stronghold", "Commands for stronghold triangulation.");
    }


    public double throw1_x, throw1_z, throw1_angle = 0;
    public double throw2_x, throw2_z, throw2_angle = 0;

    private double walk_dist = 22.5;


    public double opt1_x(double pos1_x, double pos1_angle) {
        return pos1_x + (walk_dist * (Math.cos(Math.toRadians((pos1_angle > 0 ? pos1_angle : (360 - Math.abs(pos1_angle))) + 90 - 90))));
    }
    public double opt1_z(double pos1_z, double pos1_angle) {
        return pos1_z + (walk_dist * (Math.sin(Math.toRadians((pos1_angle > 0 ? pos1_angle : (360 - Math.abs(pos1_angle))) + 90 - 90))));
    }

    public double opt2_x(double pos1_x, double pos1_angle) {
        return pos1_x + (walk_dist * (Math.cos(Math.toRadians((pos1_angle > 0 ? pos1_angle : (360 - Math.abs(pos1_angle))) + 90 + 90))));
    }
    public double opt2_z(double pos1_z, double pos1_angle) {
        return pos1_z + (walk_dist * (Math.sin(Math.toRadians((pos1_angle > 0 ? pos1_angle : (360 - Math.abs(pos1_angle))) + 90 + 90))));
    }

    public double getFinalX(double pos2_x, double pos2_angle, double angle_variation) {
        return pos2_x + ((walk_dist / Math.sin(Math.toRadians(angle_variation))) * (Math.cos(Math.toRadians((pos2_angle > 0 ? pos2_angle : (360 - Math.abs(pos2_angle))) + 90))));
    }

    public double getFinalZ(double pos2_z, double pos2_angle, double angle_variation) {
        return pos2_z + ((walk_dist / Math.sin(Math.toRadians(angle_variation))) * (Math.sin(Math.toRadians((pos2_angle > 0 ? pos2_angle : (360 - Math.abs(pos2_angle))) + 90))));
    }


    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("throw1").executes(ctx -> {

            FindItemResult eye = InvUtils.findInHotbar(Items.ENDER_EYE);
            if(eye.found()) {
                pos1_throw();
            } else {
                ChatUtils.info("§cNo eye found");
            }

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("throw2").executes(ctx -> {
            FindItemResult eye = InvUtils.findInHotbar(Items.ENDER_EYE);
            if(eye.found()) {
                if(!(throw1_angle == 0 && throw1_x == 0 && throw1_z == 0)) {
                    pos2_throw();
                } else {
                    ChatUtils.info("§cRun throw1 first");
                }
            } else {
                ChatUtils.info("§cNo eye found");
            }

            return SINGLE_SUCCESS;
        }));
    }

    private void pos1_throw() {
        FindItemResult eye = InvUtils.findInHotbar(Items.ENDER_EYE);
        InvUtils.swap(eye.slot(), false);

        Vec3d pos1 = mc.player.getPos();
        mc.player.setPos(mc.player.getBlockX() + 0.5, pos1.y, mc.player.getBlockZ() + 0.5);

        throw1_x = mc.player.getBlockX() + 0.5;
        throw1_z = mc.player.getBlockZ() + 0.5;

        Timer timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(eye.getHand(), 0));
            }
        }, 200);
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                Entity pearl = getClosestPearl();

                mc.player.lookAt(EntityAnchorArgumentType.EntityAnchor.FEET, pearl.getPos());
                throw1_angle = mc.player.getYaw();

                ChatUtils.info("§aCoordinates for throw2: ");

                ChatUtils.info(String.format("§aOption 1 §e-> §c%.0f.5 ~ %.0f.5", opt1_x(throw1_x, mc.player.getYaw()), opt1_z(throw1_z, mc.player.getYaw())));
                ChatUtils.info(String.format("§aOption 2 §e-> §c%.0f.5 ~ %.0f.5", opt2_x(throw1_x, mc.player.getYaw()), opt2_z(throw1_z, mc.player.getYaw())));

            }
        }, 1200);
    }


    private void pos2_throw() {
        FindItemResult eye = InvUtils.findInHotbar(Items.ENDER_EYE);
        InvUtils.swap(eye.slot(), false);

        Vec3d pos1 = mc.player.getPos();
        mc.player.setPos(mc.player.getBlockX() + 0.5, pos1.y, mc.player.getBlockZ() + 0.5);

        throw2_x = mc.player.getBlockX() + 0.5;
        throw2_z = mc.player.getBlockZ() + 0.5;

        Timer timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(eye.getHand(), 0));
            }
        }, 200);
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                Entity pearl = getClosestPearl();
                mc.player.lookAt(EntityAnchorArgumentType.EntityAnchor.FEET, pearl.getPos());
                throw2_angle = mc.player.getYaw();

                double final_x = getFinalX(throw2_x, mc.player.getYaw(), Math.abs(throw1_angle - throw2_angle));
                double final_z = getFinalZ(throw2_z, mc.player.getYaw(), Math.abs(throw1_angle - throw2_angle));

                //ChatUtils.info(String.format("§eCoordinate of stronghold: §c%.0f ~ %.0f", final_x, final_z));
                ChatUtils.info(String.format("§eCoordinate of stronghold:"));
                ChatUtils.info(String.format("§aOverworld: §b%.0f ~ §c%.0f", final_x, final_z));
                ChatUtils.info(String.format("§cNether: §b%.0f ~ §c%.0f", final_x / 8, final_z / 8));


                throw1_x = 0;
                throw1_z = 0;
                throw1_angle = 0;

                throw2_x = 0;
                throw2_z = 0;
                throw2_angle = 0;
            }
        }, 1200);
    }


    public static Entity getClosestPearl() {

        double lowestDistance = Integer.MAX_VALUE;
        Entity closest = null;

        for (Entity pearl : getAllPearls()) {
            double px = mc.player.getX();
            double py = mc.player.getY();
            double pz = mc.player.getZ();

            double _px = pearl.getX();
            double _py = pearl.getY();
            double _pz = pearl.getZ();

            double dx = px - _px;
            double dy = py - _py;
            double dz = pz - _pz;

            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (distance < lowestDistance) {
                lowestDistance = distance;
                closest = pearl;
            }
        }

        return closest;
    }

    public static ArrayList<Entity> getAllPearls() {
        try {
            ArrayList<Entity> pearls = new ArrayList<Entity>();
            for (Entity pearl : mc.world.getEntities()) {
                if(pearl.getType() == EntityType.EYE_OF_ENDER) pearls.add(pearl);
            }

            return pearls;
        } catch (NullPointerException ignored) {
            return new ArrayList<Entity>();
        }
    }
}
