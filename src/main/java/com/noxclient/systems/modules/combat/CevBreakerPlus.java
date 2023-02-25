package com.noxclient.systems.modules.combat;

import com.noxclient.events.packets.PacketEvent;
import com.noxclient.events.world.TickEvent;
import com.noxclient.settings.*;
import com.noxclient.systems.friends.Friends;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import com.noxclient.systems.modules.Modules;
import com.noxclient.utils.BEntityUtils;
import com.noxclient.utils.BWorldUtils;
import com.noxclient.utils.player.DamageUtils;
import com.noxclient.utils.player.InvUtils;
import com.noxclient.utils.player.PlayerUtils;
import com.noxclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class CevBreakerPlus extends Module {

    public enum Mode {
        Normal,
        Packet,
        Instant
    }


    private final SettingGroup sgGeneral = settings.createGroup("General");
    private final SettingGroup sgBreaking = settings.createGroup("Breaking");
    private final SettingGroup sgPlacing = settings.createGroup("Placing");
    private final SettingGroup sgPause = settings.createGroup("Pause");
    private final SettingGroup sgRender = settings.createGroup("Render");


    // General
    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Whether to rotate or not.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> toggleModules = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-modules")
        .description("Turn off other modules when Cev Breaker is activated.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> toggleBack = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-back-on")
        .description("Turn the modules back on when Cev Breaker is deactivated.")
        .defaultValue(false)
        .visible(toggleModules::get)
        .build()
    );

    private final Setting<List<Module>> modules = sgGeneral.add(new ModuleListSetting.Builder()
        .name("modules")
        .description("Which modules to toggle.")
        /*.defaultValue(new ArrayList<>() {{
            add(Modules.get().get(Step.class));
            add(Modules.get().get(StepPlus.class));
            add(Modules.get().get(Speed.class));
            add(Modules.get().get(StrafePlus.class));
        }})*/
        .visible(toggleModules::get)
        .build()
    );


    // Breaking
    private final Setting<Mode> mode = sgBreaking.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("Which mode to use for breaking the obsidian.")
        .defaultValue(Mode.Packet)
        .build()
    );

    private final Setting<Boolean> smartDelay = sgBreaking.add(new BoolSetting.Builder()
        .name("smart-delay")
        .description("Waits until the target can get damaged again with breaking the block.")
        .defaultValue(true)
        .visible(() -> mode.get() == Mode.Instant)
        .build()
    );

    private final Setting<Integer> switchDelay = sgBreaking.add(new IntSetting.Builder()
        .name("switch-delay")
        .description("How many ticks to wait before hitting an entity after switching hotbar slots.")
        .defaultValue(1)
        .range(0,20)
        .sliderRange(0,20)
        .visible(() -> mode.get() == Mode.Packet)
        .build()
    );


    // Placing
    private final Setting<BWorldUtils.SwitchMode> switchMode = sgPlacing.add(new EnumSetting.Builder<BWorldUtils.SwitchMode>()
        .name("switch-mode")
        .description("How to switch to your target block.")
        .defaultValue(BWorldUtils.SwitchMode.Both)
        .build()
    );

    private final Setting<BWorldUtils.PlaceMode> placeMode = sgPlacing.add(new EnumSetting.Builder<BWorldUtils.PlaceMode>()
        .name("place-mode")
        .description("How to switch to your target block.")
        .defaultValue(BWorldUtils.PlaceMode.Both)
        .build()
    );

    private final Setting<Boolean> onlyAirPlace = sgPlacing.add(new BoolSetting.Builder()
        .name("only-air-place")
        .description("Forces you to only airplace to help with stricter rotations.")
        .defaultValue(false)
        .build()
    );

    private final Setting<BWorldUtils.AirPlaceDirection> airPlaceDirection = sgPlacing.add(new EnumSetting.Builder<BWorldUtils.AirPlaceDirection>()
        .name("place-direction")
        .description("Side to try to place at when you are trying to air place.")
        .defaultValue(BWorldUtils.AirPlaceDirection.Down)
        .build()
    );

    private final Setting<Integer> rotationPrio = sgPlacing.add(new IntSetting.Builder()
        .name("rotation-priority")
        .description("Rotation priority for Surround+.")
        .defaultValue(50)
        .sliderRange(0, 200)
        .visible(rotate::get)
        .build()
    );


    // Pause
    private final Setting<Double> pauseAtHealth = sgPause.add(new DoubleSetting.Builder()
        .name("pause-health")
        .description("Pauses when you go below a certain health.")
        .defaultValue(5)
        .min(0)
        .build()
    );

    private final Setting<Boolean> eatPause = sgPause.add(new BoolSetting.Builder()
        .name("pause-on-eat")
        .description("Pauses Crystal Aura when eating.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> drinkPause = sgPause.add(new BoolSetting.Builder()
        .name("pause-on-drink")
        .description("Pauses Crystal Aura when drinking.")
        .defaultValue(true)
        .build()
    );


    // Render
    private final Setting<Boolean> swing = sgRender.add(new BoolSetting.Builder()
        .name("render-swing")
        .description("Renders your swing client-side.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> info = sgRender.add(new BoolSetting.Builder()
        .name("chat-info")
        .description("Send info about Cev Breaker in the chat.")
        .defaultValue(true)
        .build()
    );


    public CevBreakerPlus() {
        super(Categories.Combat, "cev-breaker++", "Break crystals over a ppl's head to deal massive damage!");
    }


    private PlayerEntity closestTarget;
    private boolean startedYet;
    private int switchDelayLeft, timer, breakDelayLeft;
    private final List<PlayerEntity> blacklisted = new ArrayList<>();
    private final List<EndCrystalEntity> crystals = new ArrayList<>();
    public ArrayList<Module> toActivate;

    boolean pause = false;

    @EventHandler
    public void onActivate() {
        closestTarget = null;
        startedYet = false;
        switchDelayLeft = 0;
        timer = 0;
        blacklisted.clear();

        toActivate = new ArrayList<>();

        if (toggleModules.get() && !modules.get().isEmpty() && mc.world != null && mc.player != null) {
            for (Module module : modules.get()) {
                if (module.isActive()) {
                    module.toggle();
                    toActivate.add(module);
                }
            }
        }
    }

    @Override
    public void onDeactivate() {
        if (toggleBack.get() && !toActivate.isEmpty() && mc.world != null && mc.player != null) {
            for (Module module : toActivate) {
                if (!module.isActive()) {
                    module.toggle();
                }
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        switchDelayLeft--;
        breakDelayLeft--;
        timer--;
        int crystalSlot = InvUtils.findInHotbar(Items.END_CRYSTAL).slot();
        int obsidianSlot = InvUtils.findInHotbar(Items.OBSIDIAN).slot();
        int pickSlot = InvUtils.findInHotbar(Items.NETHERITE_PICKAXE).slot();
        pickSlot = pickSlot == -1 ? InvUtils.findInHotbar(Items.DIAMOND_PICKAXE).slot() : pickSlot;

        if((crystalSlot == -1 && !(mc.player.getOffHandStack().getItem() instanceof EndCrystalItem)) || obsidianSlot == -1 || pickSlot == -1) {
            if (info.get()) warning("No " + (crystalSlot == -1 && !(mc.player.getOffHandStack().getItem() instanceof EndCrystalItem) ? "crystals" : (obsidianSlot == -1 ? "obsidian" :  "pickaxe")) + " found, disabling...");
            toggle();
            return;
        }
        getEntities();
        if(closestTarget == null) {
            if (info.get()) error( "No target found, disabling...");
            toggle();
            return;
        }

        // Check pause settings
        if (PlayerUtils.shouldPause(false, eatPause.get(), drinkPause.get()) || PlayerUtils.getTotalHealth() <= pauseAtHealth.get()) {
            if (info.get() && !pause) warning("Pausing");
            pause = true;
            return;
        } else{pause = false;}

        BlockPos blockPos = closestTarget.getBlockPos().add(0, 2, 0);
        BlockState blockState = mc.world.getBlockState(blockPos);
        float[] rotation = PlayerUtils.calculateAngle(new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5));
        boolean crystalThere = false;
        for(EndCrystalEntity crystal : crystals) {      //Check for crystal in right pos
            if(crystal.getBlockPos().add(0, -1, 0).equals(blockPos)) {
                crystalThere = true;
                break;
            }
        }

        //Placing obby
        if(!blockState.isOf(Blocks.OBSIDIAN) && !crystalThere && (mc.player.getMainHandStack().getItem().equals(Items.OBSIDIAN) || switchDelayLeft <= 0)) {
            if(!BWorldUtils.place(blockPos, InvUtils.findInHotbar(Items.OBSIDIAN), rotate.get(), rotationPrio.get(), switchMode.get(), placeMode.get(), onlyAirPlace.get(), airPlaceDirection.get(), swing.get(), true, true)) {
                blacklisted.add(closestTarget);
                getEntities();
                if(closestTarget == null) {
                    if (info.get()) warning("Can't place obsidian above the target! Disabling...");
                    toggle();
                }
                return;
            }
        }

        //Placing crystal
        boolean offhand = mc.player.getOffHandStack().getItem() instanceof EndCrystalItem;
        boolean mainhand = mc.player.getMainHandStack().getItem() instanceof EndCrystalItem;
        if(!crystalThere && blockState.isOf(Blocks.OBSIDIAN)) {
            if(!(offhand || mainhand || switchDelayLeft <= 0)) return;
            double x = blockPos.up().getX();
            double y = blockPos.up().getY();
            double z = blockPos.up().getZ();

            if(!mc.world.getOtherEntities(null, new Box(x, y, z, x + 1D, y + 2D, z + 1D)).isEmpty()
                || !mc.world.getBlockState(blockPos.up()).isAir()) {
                blacklisted.add(closestTarget);
                getEntities();
                if(closestTarget == null) {
                    if (info.get()) warning("Can't place the crystal! Disabling...");
                    toggle();
                }
                return;
            }
            else {
                if(!offhand && !mainhand) mc.player.getInventory().selectedSlot = crystalSlot;
                Hand hand = offhand ? Hand.OFF_HAND : Hand.MAIN_HAND;
                BlockHitResult result = new BlockHitResult(mc.player.getPos(), blockPos.getY() < mc.player.getY() ? Direction.UP : Direction.DOWN, blockPos, false);
                if(swing.get()) mc.player.swingHand(Hand.MAIN_HAND);
                else mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                if(rotate.get()) {
                    Rotations.rotate(rotation[0], rotation[1], 25, () -> mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand, result, 0)));
                } else mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand, result, 0));
            }
        }

        //Breaking obby
        if(blockState.isAir() && mode.get() == Mode.Packet) startedYet = false;
        if((mc.player.getInventory().selectedSlot == pickSlot || switchDelayLeft <= 0) && crystalThere && blockState.isOf(Blocks.OBSIDIAN)) {
            Direction direction = BEntityUtils.rayTraceCheck(blockPos, true);
            if(mode.get() == Mode.Instant) {
                if(!startedYet) {
                    mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, direction));
                    startedYet = true;
                } else {
                    if(smartDelay.get() && closestTarget.hurtTime > 0) return;
                    mc.player.getInventory().selectedSlot = pickSlot;
                    mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));
                }
            }
            else if(mode.get() == Mode.Normal) {
                mc.player.getInventory().selectedSlot = pickSlot;
                mc.interactionManager.updateBlockBreakingProgress(blockPos, direction);
            }
            else if(mode.get() == Mode.Packet) {
                timer = startedYet ? timer : BEntityUtils.getBlockBreakingSpeed(blockState, blockPos, pickSlot);
                if(!startedYet) {
                    mine(blockPos, swing.get(), rotate.get());
                    startedYet = true;
                }
                else if(timer <= 0){
                    mc.player.getInventory().selectedSlot = pickSlot;
                }
            }
        }

        //Breaking the crystal
        BananaBomber BBomber = Modules.get().get(BananaBomber.class);
        if(BBomber.bestTarget == null || BBomber.bestTarget != closestTarget || BBomber.BminDamage.get() >= 6) {
            if (mode.get() == Mode.Packet && breakDelayLeft >= 0) return;
            for(EndCrystalEntity crystal : crystals) {
                if(DamageUtils.crystalDamage(closestTarget, crystal.getPos()) >= 6) {
                    float[] breakRotation = PlayerUtils.calculateAngle(BEntityUtils.crystalEdgePos(crystal));
                    if(swing.get()) mc.player.swingHand(Hand.MAIN_HAND);
                    else mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                    if(rotate.get()) Rotations.rotate(breakRotation[0], breakRotation[1], 30, () -> mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, false)));
                    else mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, false));
                    break;
                }
            }
        }
    }

    private void getEntities() {
        closestTarget = null;
        crystals.clear();
        for(Entity entity : mc.world.getEntities()) {
            if(entity.isInRange(mc.player, 6))
                if(entity.isAlive())
                    if(entity instanceof PlayerEntity) {
                        if(entity != mc.player)
                            if(Friends.get().shouldAttack((PlayerEntity) entity))
                                if(closestTarget == null || mc.player.distanceTo(entity) < mc.player.distanceTo(closestTarget))
                                    if(!blacklisted.contains(entity))
                                        closestTarget = (PlayerEntity) entity;
                    } else if(entity instanceof EndCrystalEntity)
                        crystals.add((EndCrystalEntity) entity);
        }
    }

    public void mine(BlockPos blockPos, boolean swing, boolean rotate) {
        if(rotate) {
            Rotations.rotate(Rotations.getYaw(blockPos), Rotations.getPitch(blockPos), () -> mine(blockPos, swing, false));
        } else {
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.UP));
            if(swing) mc.player.swingHand(Hand.MAIN_HAND);
            else mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));
        }
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (event.packet instanceof UpdateSelectedSlotC2SPacket) {
            switchDelayLeft = 1;
            breakDelayLeft = switchDelay.get();
        }
    }

    @Override
    public String getInfoString() {
        if (closestTarget != null) return closestTarget.getEntityName();
        return null;
    }
}
