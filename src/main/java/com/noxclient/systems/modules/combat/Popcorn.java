package com.noxclient.systems.modules.combat;

import com.noxclient.events.entity.EntityAddedEvent;
import com.noxclient.events.packets.PacketEvent;
import com.noxclient.events.render.Render3DEvent;
import com.noxclient.events.world.TickEvent;
import com.noxclient.renderer.ShapeMode;
import com.noxclient.settings.*;
import com.noxclient.systems.friends.Friends;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import com.noxclient.utils.entity.EntityUtils;
import com.noxclient.utils.player.ChatUtils;
import com.noxclient.utils.player.DamageUtils;
import com.noxclient.utils.popcorn.Timer;
import com.noxclient.utils.popcorn.UtilAC;
import com.noxclient.utils.popcorn.manaH;
import com.noxclient.utils.render.color.Color;
import com.noxclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Popcorn extends Module {
    public Popcorn() {super(Categories.Combat, "popcorn", "Do you want popcorn for your fight?");}
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPlace = settings.createGroup("Place");
    private final SettingGroup sgExplode = settings.createGroup("Explode");
    private final SettingGroup sgRange = settings.createGroup("Range");
    private final SettingGroup sgDamage = settings.createGroup("Damage");
    private final SettingGroup sgRotate = settings.createGroup("Rotate");
    private final SettingGroup sgExtrapolation = settings.createGroup("Extrapolation");
    private final SettingGroup sgRaytrace = settings.createGroup("Raytrace");
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgDebug = settings.createGroup("Debug");

    //  General Page
    private final Setting<Boolean> place = sgGeneral.add(new BoolSetting.Builder()
        .name("place")
        .description("Places crystals.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> explode = sgGeneral.add(new BoolSetting.Builder()
        .name("Explode")
        .description("Explodes crystals.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> pauseEat = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause Eat")
        .description("Pauses while eating.")
        .defaultValue(true)
        .build()
    );

    //  Place Page
    private final Setting<Boolean> oldVerPlacements = sgPlace.add(new BoolSetting.Builder()
        .name("1.12 Placements")
        .description("Uses 1.12 crystal mechanics.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> instantPlace = sgPlace.add(new BoolSetting.Builder()
        .name("Instant Non-Blocked")
        .description("Ignores delay after crystal hitbox has disappeared.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> instant = sgPlace.add(new BoolSetting.Builder()
        .name("Instant Place")
        .description("Places after exploding a crystal.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> placeSpeed = sgPlace.add(new DoubleSetting.Builder()
        .name("Place Speed")
        .description("How many times should the module place per second.")
        .defaultValue(10)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> slowDamage = sgPlace.add(new DoubleSetting.Builder()
        .name("Slow Damage")
        .description("Switches to slow speed when the target would take low damage.")
        .defaultValue(3)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> slowSpeed = sgPlace.add(new DoubleSetting.Builder()
        .name("Slow Speed")
        .description("How many times should the module place per second when damage is under slow damage.")
        .defaultValue(2)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<SwingMode> placeSwing = sgPlace.add(new EnumSetting.Builder<SwingMode>()
        .name("Place Swing")
        .description(".")
        .defaultValue(SwingMode.Full)
        .build()
    );
    private final Setting<Boolean> clearSend = sgPlace.add(new BoolSetting.Builder()
        .name("Clear Send")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> clearReceive = sgPlace.add(new BoolSetting.Builder()
        .name("Clear Receive")
        .description(".")
        .defaultValue(false)
        .build()
    );

    //  Explode Page
    private final Setting<Boolean> instantExp = sgExplode.add(new BoolSetting.Builder()
        .name("Instant Explode")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> expSpeed = sgExplode.add(new DoubleSetting.Builder()
        .name("Explode Speed")
        .description(".")
        .defaultValue(2)
        .range(0.01, 20)
        .sliderRange(0.01, 20)
        .build()
    );
    private final Setting<Boolean> setDead = sgExplode.add(new BoolSetting.Builder()
        .name("Set Dead")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> setDeadDelay = sgExplode.add(new DoubleSetting.Builder()
        .name("Set Dead Delay")
        .description(".")
        .defaultValue(0.05)
        .range(0, 1)
        .sliderRange(0, 1)
        .visible(setDead::get)
        .build()
    );
    private final Setting<SwingMode> explodeSwing = sgExplode.add(new EnumSetting.Builder<SwingMode>()
        .name("Explode Swing")
        .description(".")
        .defaultValue(SwingMode.Full)
        .build()
    );

    private final Setting<Double> placeRange = sgRange.add(new DoubleSetting.Builder()
        .name("Place Range")
        .description("Range for placing.")
        .defaultValue(5.2)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<RangeMode> placeRangeMode = sgRange.add(new EnumSetting.Builder<RangeMode>()
        .name("Place Range Mode")
        .description("Where to calculate ranges from.")
        .defaultValue(RangeMode.Automatic)
        .build()
    );
    private final Setting<Double> blockWidth = sgRange.add(new DoubleSetting.Builder()
        .name("Block Width")
        .description(".")
        .defaultValue(2)
        .range(0, 3)
        .sliderRange(0, 3)
        .visible(() -> placeRangeMode.get().equals(RangeMode.CustomBox))
        .build()
    );
    private final Setting<Double> blockHeight = sgRange.add(new DoubleSetting.Builder()
        .name("Block Height")
        .description(".")
        .defaultValue(2)
        .range(0, 3)
        .sliderRange(0, 3)
        .visible(() -> placeRangeMode.get().equals(RangeMode.CustomBox))
        .build()
    );
    private final Setting<Double> placeHeight = sgRange.add(new DoubleSetting.Builder()
        .name("Place Height")
        .description(".")
        .defaultValue(-0.5)
        .sliderRange(-1, 2)
        .visible(() -> placeRangeMode.get().equals(RangeMode.Height))
        .build()
    );
    private final Setting<Double> expRange = sgRange.add(new DoubleSetting.Builder()
        .name("Explode Range")
        .description(".")
        .defaultValue(4.8)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<RangeMode> expRangeMode = sgRange.add(new EnumSetting.Builder<RangeMode>()
        .name("Explode Range Mode")
        .description(".")
        .defaultValue(RangeMode.NCP)
        .build()
    );
    private final Setting<Double> crystalWidth = sgRange.add(new DoubleSetting.Builder()
        .name("Crystal Width")
        .description(".")
        .defaultValue(2)
        .range(0, 3)
        .sliderRange(0, 3)
        .visible(() -> expRangeMode.get().equals(RangeMode.CustomBox))
        .build()
    );
    private final Setting<Double> crystalHeight = sgRange.add(new DoubleSetting.Builder()
        .name("Crystal Height")
        .description(".")
        .defaultValue(2)
        .range(0, 3)
        .sliderRange(0, 3)
        .visible(() -> expRangeMode.get().equals(RangeMode.CustomBox))
        .build()
    );
    private final Setting<Double> expHeight = sgRange.add(new DoubleSetting.Builder()
        .name("Explode Height")
        .description(".")
        .defaultValue(1)
        .sliderRange(-1, 2)
        .visible(() -> expRangeMode.get().equals(RangeMode.Height))
        .build()
    );

    private final Setting<DmgCheckMode> dmgCheckMode = sgDamage.add(new EnumSetting.Builder<DmgCheckMode>()
        .name("Dmg Check Mode")
        .description(".")
        .defaultValue(DmgCheckMode.Normal)
        .build()
    );
    private final Setting<Double> minPlace = sgDamage.add(new DoubleSetting.Builder()
        .name("Min Place")
        .description(".")
        .defaultValue(4)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> maxSelfPlace = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Self Place")
        .description(".")
        .defaultValue(8)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> maxSelfPlaceRatio = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Self Place Ratio")
        .description(".")
        .defaultValue(0.3)
        .range(0, 5)
        .sliderRange(0, 5)
        .build()
    );
    private final Setting<Double> maxFriendPlace = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Friend Place")
        .description(".")
        .defaultValue(8)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> maxFriendPlaceRatio = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Friend Place Ratio")
        .description(".")
        .defaultValue(0.5)
        .range(0, 5)
        .sliderRange(0, 5)
        .build()
    );
    private final Setting<Double> minExplode = sgDamage.add(new DoubleSetting.Builder()
        .name("Min Explode")
        .description(".")
        .defaultValue(2.5)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> maxSelfExp = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Self Explode")
        .description(".")
        .defaultValue(9)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> maxSelfExpRatio = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Self Explode Ratio")
        .description(".")
        .defaultValue(0.4)
        .range(0, 5)
        .sliderRange(0, 5)
        .build()
    );
    private final Setting<Double> maxFriendExp = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Friend Explode")
        .description(".")
        .defaultValue(12)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> maxFriendExpRatio = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Friend Explode Ratio")
        .description(".")
        .defaultValue(0.5)
        .range(0, 5)
        .sliderRange(0, 5)
        .build()
    );
    private final Setting<Double> forcePop = sgDamage.add(new DoubleSetting.Builder()
        .name("Force Pop")
        .description(".")
        .defaultValue(2)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> antiFriendPop = sgDamage.add(new DoubleSetting.Builder()
        .name("Anti Friend Pop")
        .description(".")
        .defaultValue(2)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> antiSelfPop = sgDamage.add(new DoubleSetting.Builder()
        .name("Anti Self Pop")
        .description(".")
        .defaultValue(2)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );

    private final Setting<Integer> extrapolation = sgExtrapolation.add(new IntSetting.Builder()
        .name("Extrapolation")
        .description(".")
        .defaultValue(0)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    private final Setting<Integer> extSmoothness = sgExtrapolation.add(new IntSetting.Builder()
        .name("Extrapolation Smoothening")
        .description(".")
        .defaultValue(5)
        .range(1, 20)
        .sliderRange(1, 20)
        .build()
    );

    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("Render")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<RenderMode> renderMode = sgRender.add(new EnumSetting.Builder<RenderMode>()
        .name("Render Mode")
        .description(".")
        .defaultValue(RenderMode.Corn)
        .build()
    );
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description(".")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<Double> renderTime = sgRender.add(new DoubleSetting.Builder()
        .name("Render Time")
        .description("How long the box should remain in full alpha.")
        .defaultValue(0.3)
        .min(0)
        .sliderRange(0, 10)
        .visible(() -> renderMode.get().equals(RenderMode.Earthhack) || renderMode.get().equals(RenderMode.Future))
        .build()
    );
    private final Setting<Double> fadeTime = sgRender.add(new DoubleSetting.Builder()
        .name("Fade Time")
        .description("How long the fading should take.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .visible(() -> renderMode.get().equals(RenderMode.Earthhack) || renderMode.get().equals(RenderMode.Future))
        .build()
    );
    private final Setting<Double> animationSpeed = sgRender.add(new DoubleSetting.Builder()
        .name("Animation Speed")
        .description(".")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .visible(() -> renderMode.get().equals(RenderMode.Corn))
        .build()
    );
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description("U blind?")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    private final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description("U blind?")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );

    private final Setting<Boolean> debugRange = sgDebug.add(new BoolSetting.Builder()
        .name("Debug Range")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> debugRangeX = sgDebug.add(new DoubleSetting.Builder()
        .name("Range Pos X")
        .description(".")
        .defaultValue(0)
        .sliderRange(-1000, 1000)
        .visible(debugRange::get)
        .build()
    );
    private final Setting<Double> debugRangeY = sgDebug.add(new DoubleSetting.Builder()
        .name("Range Pos Y")
        .description(".")
        .defaultValue(0)
        .sliderRange(-1000, 1000)
        .visible(debugRange::get)
        .build()
    );
    private final Setting<Double> debugRangeZ = sgDebug.add(new DoubleSetting.Builder()
        .name("Range Pos Z")
        .description(".")
        .defaultValue(0)
        .sliderRange(-1000, 1000)
        .visible(debugRange::get)
        .build()
    );
    private final Setting<Double> debugRangeHeight1 = sgDebug.add(new DoubleSetting.Builder()
        .name("Debug Range Height 1")
        .description(".")
        .defaultValue(0)
        .sliderRange(-2, 2)
        .visible(debugRange::get)
        .build()
    );
    private final Setting<Double> debugRangeHeight2 = sgDebug.add(new DoubleSetting.Builder()
        .name("Debug Range Height 2")
        .description(".")
        .defaultValue(0)
        .sliderRange(-2, 2)
        .visible(debugRange::get)
        .build()
    );
    private final Setting<Double> debugRangeHeight3 = sgDebug.add(new DoubleSetting.Builder()
        .name("Debug Range Height 3")
        .description(".")
        .defaultValue(0)
        .sliderRange(-2, 2)
        .visible(debugRange::get)
        .build()
    );
    private final Setting<Double> debugRangeHeight4 = sgDebug.add(new DoubleSetting.Builder()
        .name("Debug Range Height 4")
        .description(".")
        .defaultValue(0)
        .sliderRange(-2, 2)
        .visible(debugRange::get)
        .build()
    );
    private final Setting<Double> debugRangeHeight5 = sgDebug.add(new DoubleSetting.Builder()
        .name("Debug Range Height 5")
        .description(".")
        .defaultValue(0)
        .sliderRange(-2, 2)
        .visible(debugRange::get)
        .build()
    );

    double highestEnemy = 0;
    double enemyHP = 0;
    double highestFriend = 0;
    double friendHP = 0;
    double self = 0;
    double placeTimer = 0;
    double delayTimer = 0;
    BlockPos placePos = null;
    Timer attacked = new Timer(false);
    Map<String, Box> extPos = new HashMap<>();
    List<PlayerEntity> enemies = new ArrayList<>();
    Map<String, List<Vec3d>> motions = new HashMap<>();
    List<Box> blocked = new ArrayList<>();
    Map<Vec3d, double[][]> dmgCache = new HashMap<>();
    BlockPos lastPos = null;
    Map<BlockPos, Double[]> earthMap = new HashMap<>();

    Vec3d renderPos = null;
    double renderProgress = 0;


    public enum DmgCheckMode {
        Normal,
        Safe
    }
    public enum SwingMode {
        Disabled,
        Packet,
        Full
    }
    public enum RangeMode {
        Automatic,
        Height,
        NCP,
        Vanilla,
        CustomBox
    }
    public enum RenderMode {
        Corn,
        Future,
        Earthhack
    }

    @Override
    public void onActivate() {
        super.onActivate();
        earthMap.clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onTickPre(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null) {
            for (PlayerEntity pl : mc.world.getPlayers()) {
                String key = pl.getName().getString();
                if (motions.containsKey(key)) {
                    List<Vec3d> vec = motions.get(key);
                    if (vec != null) {
                        if (vec.size() >= extSmoothness.get()) {
                            for (int i = vec.size() - extSmoothness.get(); i <= 0; i++) {
                                vec.remove(0);
                            }
                        }
                        vec.add(motionCalc(pl));
                    } else {
                        List<Vec3d> list = new ArrayList<>();
                        list.add(motionCalc(pl));
                        motions.put(key, list);
                    }
                } else {
                    List<Vec3d> list = new ArrayList<>();
                    list.add(motionCalc(pl));
                    motions.put(key, list);
                }
            }
            extPos = getExtPos();
            if (debugRange.get()) {
                ChatUtils.sendMsg(Text.of(debugRangeHeight(1) + "  " + dist(debugRangePos(1)) + "\n" +
                    debugRangeHeight(2) + "  " + dist(debugRangePos(2)) + "\n" +
                    debugRangeHeight(3) + "  " + dist(debugRangePos(3)) + "\n" +
                    debugRangeHeight(4) + "  " + dist(debugRangePos(4)) + "\n" +
                    debugRangeHeight(5) + "  " + dist(debugRangePos(5))));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onRender3D(Render3DEvent event) {
        double d = event.frameTime;
        attacked.update(d);
        placeTimer = Math.max(placeTimer - d * getSpeed(), 0);
        update();

        if (render.get()) {
            switch (renderMode.get()) {
                case Corn -> {
                    if (placePos != null && !pausedCheck()) {
                        renderProgress = Math.min(1, renderProgress + event.frameTime);
                        renderPos = smoothMove(renderPos, new Vec3d(placePos.getX(), placePos.getY(), placePos.getZ()), event.frameTime * animationSpeed.get());
                    } else {
                        renderProgress = Math.max(0, renderProgress - event.frameTime);
                    }
                    if (renderPos != null) {
                        Box box = new Box(renderPos.getX() + 0.5 - renderProgress / 2, renderPos.getY() - 0.5 - renderProgress / 2, renderPos.getZ() + 0.5 - renderProgress / 2,
                            renderPos.getX() + 0.5 + renderProgress / 2, renderPos.getY() - 0.5 + renderProgress / 2, renderPos.getZ() + 0.5 + renderProgress / 2);
                        event.renderer.box(box, new Color(color.get().r, color.get().g, color.get().b, Math.round(color.get().a / 5f)), color.get(), shapeMode.get(), 0);
                    }
                }
                case Future -> {
                    if (placePos != null && !pausedCheck()) {
                        renderPos = new Vec3d(placePos.getX(), placePos.getY(), placePos.getZ());
                        renderProgress = fadeTime.get() + renderTime.get();
                    } else {
                        renderProgress = Math.max(0, renderProgress - event.frameTime);
                    }
                    if (renderProgress > 0 && renderPos != null) {
                        event.renderer.box(new Box(renderPos.getX(), renderPos.getY() - 1, renderPos.getZ(),
                                renderPos.getX() + 1, renderPos.getY(), renderPos.getZ() + 1),
                            new Color(color.get().r, color.get().g, color.get().b, (int) Math.round(color.get().a * Math.min(1, renderProgress / fadeTime.get()))),
                            new Color(lineColor.get().r, lineColor.get().g, lineColor.get().b, (int) Math.round(lineColor.get().a * Math.min(1, renderProgress / fadeTime.get()))), shapeMode.get(), 0);
                    }
                }
                case Earthhack -> {
                    List<BlockPos> toRemove = new ArrayList<>();
                    for (Map.Entry<BlockPos, Double[]> entry : earthMap.entrySet()) {
                        BlockPos pos = entry.getKey();
                        Double[] alpha = entry.getValue();
                        if (alpha[0] <= d) {
                            toRemove.add(pos);
                        } else {
                            event.renderer.box(UtilAC.getBox(pos),
                                new Color(color.get().r, color.get().g, color.get().b, (int) Math.round(color.get().a * Math.min(1, alpha[0] / alpha[1]))),
                                new Color(lineColor.get().r, lineColor.get().g, lineColor.get().b, (int) Math.round(lineColor.get().a * Math.min(1, alpha[0] / alpha[1]))), shapeMode.get(), 0);
                            entry.setValue(new Double[]{alpha[0] - d, alpha[1]});
                        }
                    }
                    toRemove.forEach(earthMap::remove);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onEntity(EntityAddedEvent event) {
        if (instantExp.get() && event.entity instanceof EndCrystalEntity) {
            Vec3d vec = event.entity.getPos();
            if (canExplode(vec)) {
                explode(event.entity.getId(), null, vec);
                if (instant.get() && placePos != null && placePos.equals(event.entity.getBlockPos())) {
                    Hand hand = getHand(Items.END_CRYSTAL);
                    if (hand != null && !pausedCheck()) {
                        placeCrystal(placePos.down(), hand);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onReceive(PacketEvent.Receive event) {
        if (event.packet instanceof ExplosionS2CPacket) {
            if (clearReceive.get()) {blocked.clear();}
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        if (mc.player != null && mc.world != null) {

        }
    }

    // Other stuff

    void update() {
        dmgCache.clear();
        placePos = place.get() ? getPlacePos((int) Math.ceil(placeRange.get())) : null;
        Entity expEntity = null;
        double[] value = null;
        Hand handToUse = getHand(Items.END_CRYSTAL);
        if (!pausedCheck() && handToUse != null && explode.get()) {
            for (Entity en : mc.world.getEntities()) {
                if (en.getType().equals(EntityType.END_CRYSTAL)) {
                    if (canExplode(en.getPos())) {
                        double[] dmg = getDmg(en.getPos())[0];
                        if ((expEntity == null || value == null) ||
                            (dmgCheckMode.get().equals(DmgCheckMode.Normal) && dmg[0] > value[0]) ||
                            (dmgCheckMode.get().equals(DmgCheckMode.Safe) && dmg[2] / dmg[0] < value[2] / dmg[0])) {
                            expEntity = en;
                            value = dmg;
                        }
                    }
                }
            }
        }
        if (expEntity != null) {
            if (!isAttacked(expEntity.getId())) {
                explode(expEntity.getId(), expEntity, expEntity.getPos());
            }
        }
        if (placePos != null) {
            if (handToUse != null) {
                if (!placePos.equals(lastPos)) {
                    lastPos = placePos;
                    placeTimer = 0;
                }
                if (!pausedCheck()) {
                    if (placeTimer <= 0 || (instantPlace.get() && !shouldSlow() && !isBlocked(placePos))) {
                        placeTimer = 1;
                        placeCrystal(placePos.down(), handToUse);
                    }
                }
            }
        } else {
            lastPos = null;
        }
    }

    void placeCrystal(BlockPos pos, Hand handToUse) {
        if (handToUse != null && pos != null && mc.player != null) {
            if (renderMode.get().equals(RenderMode.Earthhack)) {
                if (!earthMap.containsKey(pos)) {
                    earthMap.put(pos, new Double[]{fadeTime.get() + renderTime.get(), fadeTime.get()});
                } else {
                    earthMap.replace(pos, new Double[]{fadeTime.get() + renderTime.get(), fadeTime.get()});
                }
            }

            blocked.add(new Box(pos.getX() - 0.5, pos.getY() + 1, pos.getZ() - 0.5, pos.getX() + 1.5, pos.getY() + 2, pos.getZ() + 1.5));
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(handToUse,
                new BlockHitResult(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Direction.UP, pos, false), 0));
            if (!placeSwing.get().equals(SwingMode.Disabled)) {swing(handToUse, placeSwing.get());}
        }
    }

    boolean isBlocked(BlockPos pos) {
        Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
        for (Box bb : blocked) {
            if (bb.intersects(box)) {
                return true;
            }
        }
        return false;
    }

    boolean isAttacked(int id) {
        return attacked.contains(id);
    }

    void explode(int id, Entity en, Vec3d vec) {
        if (en != null) {
            attackEntity(en, true, true, true);
        } else {
            attackID(id, vec, true, true, true);
        }
    }

    void attackID(int id, Vec3d pos, boolean checkSD, boolean swing, boolean confirm) {
        Hand handToUse = getHand(Items.END_CRYSTAL);
        if (handToUse != null && !pausedCheck()) {
            EndCrystalEntity en = new EndCrystalEntity(mc.world, pos.x, pos.y, pos.z);
            en.setId(id);
            attackEntity(en, checkSD, swing, confirm);
        }
    }

    void attackEntity(Entity en, boolean checkSD, boolean swing, boolean confirm) {
        if (mc.player != null) {
            Hand handToUse = getHand(Items.END_CRYSTAL);
            if (handToUse != null) {
                attacked.add(en.getId(), 1 / expSpeed.get());
                delayTimer = 0;
                mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(en, mc.player.isSneaking()));
                if (swing && !explodeSwing.get().equals(SwingMode.Disabled)) {swing(handToUse, explodeSwing.get());}
                if (confirm && clearSend.get()) {blocked.clear();}
                if (setDead.get() && checkSD) {
                    manaH.DELAY.add(() -> setEntityDead(en), setDeadDelay.get());
                }
            }
        }
    }

    boolean canExplode(Vec3d vec) {
        if (!inExplodeRange(expRangePos(vec))) {
            return false;
        }
        double[][] result = getDmg(vec);
        if (!explodeDamageCheck(result[0], result[1])) {
            return false;
        }
        return true;
    }

    Hand getHand(Item item) {
        if (manaH.HOLDING.isHolding(item)) {
            return Hand.MAIN_HAND;
        }
        if (mc.player.getOffHandStack().getItem().equals(item)) {
            return Hand.OFF_HAND;
        }
        return null;
    }

    void swing(Hand hand, SwingMode mode) {
        if (mode == SwingMode.Packet) {
            mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(hand));
        }
        if (mode == SwingMode.Full) {
            mc.player.swingHand(hand);
        }
    }

    boolean pausedCheck() {
        if (mc.player != null) {
            return pauseEat.get() && (mc.player.isUsingItem() && mc.player.isHolding(Items.ENCHANTED_GOLDEN_APPLE));
        }
        return true;
    }

    void setEntityDead(Entity en) {
        en.remove(Entity.RemovalReason.KILLED);
    }

    BlockPos getPlacePos(int r) {
        BlockPos bestPos = null;
        double[] highest = null;
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = mc.player.getBlockPos().add(x, y, z);

                    if (!air(pos) || !(!oldVerPlacements.get() || air(pos.up())) || !inPlaceRange(placeRangePos(pos)) ||
                        !crystalBlock(pos.down()) || !inExplodeRange(expRangePos(new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5)))) {continue;}

                    double[][] result = getDmg(new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5));

                    if (!placeDamageCheck(result[0], result[1], highest)) {continue;}

                    Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
                    if (EntityUtils.intersectsWithEntity(box, entity -> !(entity.getType().equals(EntityType.END_CRYSTAL) && canExplode(entity.getPos())))) {continue;}
                    bestPos = pos;
                    highest = result[0];
                }
            }
        }
        return bestPos;
    }

    boolean placeDamageCheck(double[] dmg, double[] health, double[] highest) {

        if (highest != null) {
            if (dmgCheckMode.get().equals(DmgCheckMode.Normal) && dmg[0] < highest[0]) {return false;}
            if (dmgCheckMode.get().equals(DmgCheckMode.Safe) && dmg[2] / dmg[0] > highest[2] / highest[0]) {return false;}
        }

        double playerHP = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        if (playerHP >= 0 && dmg[2] * antiSelfPop.get() >= playerHP) {return false;}
        if (health[1] >= 0 && dmg[1] * antiFriendPop.get() >= health[1]) {return false;}
        if (health[0] >= 0 && dmg[0] * forcePop.get() >= health[0]) {return true;}

        //  Min Damage
        if (dmg[0] < minPlace.get()) {return false;}

        //  Max Damage
        if (dmg[1] > maxFriendPlace.get()) {return false;}
        if (dmg[1] / dmg[0] > maxFriendPlaceRatio.get()) {return false;}
        if (dmg[2] > maxSelfPlace.get()) {return false;}
        if (dmg[2] / dmg[0] > maxSelfPlaceRatio.get()) {return false;}
        return true;
    }

    boolean explodeDamageCheck(double[] dmg, double[] health) {

        double playerHP = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        if (playerHP >= 0 && dmg[2] * forcePop.get() >= playerHP) {return false;}
        if (health[1] >= 0 && dmg[1] * antiFriendPop.get() >= health[1]) {return false;}
        if (health[0] >= 0 && dmg[0] * forcePop.get() >= health[0]) {return true;}

        if (dmg[0] < minExplode.get()) {return false;}

        if (dmg[1] > maxFriendExp.get()) {return false;}
        if (dmg[1] / dmg[0] > maxFriendExpRatio.get()) {return false;}
        if (dmg[2] > maxSelfExp.get()) {return false;}
        if (dmg[2] / dmg[0] > maxSelfExpRatio.get()) {return false;}
        return true;
    }

    double[][] getDmg(Vec3d vec) {
        if (dmgCache.containsKey(vec)) {
            return dmgCache.get(vec);
        }
        highestEnemy = -1;
        highestFriend = -1;
        self = -1;
        enemyHP = -1;
        friendHP = -1;
        List<AbstractClientPlayerEntity> players = mc.world.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            AbstractClientPlayerEntity player = players.get(i);
            if (player.getHealth() <= 0 || player.isSpectator() || player == mc.player) {
                continue;
            }
            Box ogBB = null;
            if (extrapolation.get() != 0) {
                ogBB = player.getBoundingBox();
                player.setBoundingBox(extPos.get(player.getName().getString()));
            }
            double dmg = DamageUtils.crystalDamage(player, vec);
            if (extrapolation.get() != 0) {
                player.setBoundingBox(ogBB);
            }
            double hp = player.getHealth() + player.getAbsorptionAmount();

            //  friend
            if (Friends.get().isFriend(player)) {
                if (dmg > highestFriend) {
                    highestFriend = dmg;
                    friendHP = hp;
                }
            }
            //  enemy
            else if (dmg > highestEnemy) {
                highestEnemy = dmg;
                enemyHP = hp;
            }
        }
        self = DamageUtils.crystalDamage(mc.player, vec);
        double[][] result = new double[][]{new double[] {highestEnemy, highestFriend, self}, new double[]{enemyHP, friendHP}};
        dmgCache.put(vec, result);
        return result;
    }

    boolean air(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR);
    }
    boolean crystalBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock().equals(Blocks.OBSIDIAN) ||
            mc.world.getBlockState(pos).getBlock().equals(Blocks.BEDROCK);
    }
    boolean inPlaceRange(Vec3d vec) {
        return dist(mc.player.getEyePos().add(-vec.x, -vec.y, -vec.z)) <= placeRange.get();
    }
    boolean inExplodeRange(Vec3d vec) {
        return dist(mc.player.getEyePos().add(-vec.x, -vec.y, -vec.z)) <= expRange.get();
    }
    double dist(Vec3d distances) {
        return Math.sqrt(distances.x * distances.x + distances.y * distances.y + distances.z * distances.z);
    }
    Vec3d expRangePos(Vec3d vec) {
        switch (expRangeMode.get()) {
            case Automatic -> {
                return vec.add(0, 1, 0);
            }
            case Height -> {
                return vec.add(0, expHeight.get(), 0);
            }
            case NCP -> {
                return new Vec3d(vec.x, Math.min(Math.max(mc.player.getEyeY(), vec.y), vec.y + 2), vec.z);
            }
            case CustomBox -> {
                return UtilAC.getClosest(mc.player.getEyePos(), vec, crystalWidth.get(),crystalHeight.get());
            }
        }

        return UtilAC.getClosest(mc.player.getEyePos(), vec, 2, 2);

    }
    Vec3d placeRangePos(BlockPos pos) {
        switch (placeRangeMode.get()) {
            case Automatic -> {
                return new Vec3d(pos.getX() + 0.5, pos.getY() - 0.5, pos.getZ() + 0.5);
            }
            case Height -> {
                return new Vec3d(pos.getX() + 0.5, pos.getY() + placeHeight.get(), pos.getZ() + 0.5);
            }
            case NCP -> {
                return new Vec3d(pos.getX() + 0.5, Math.min(Math.max(mc.player.getEyeY(), pos.getY() - 1), pos.getY()), pos.getZ() + 0.5);
            }
            case CustomBox -> {
                return UtilAC.getClosest(mc.player.getEyePos(), new Vec3d(pos.getX() + 0.5, pos.getY() - 1, pos.getZ() + 0.5), blockWidth.get(), blockHeight.get());
            }
        }

        return UtilAC.getClosest(mc.player.getEyePos(), new Vec3d(pos.getX() + 0.5, pos.getY() - 1, pos.getZ() + 0.5), 1, 1);
    }
    Vec3d debugRangePos(int id) {
        return mc.player.getEyePos().add(-debugRangeX.get() - 0.5, -debugRangeY.get() - debugRangeHeight(id), -debugRangeZ.get() - 0.5);
    }
    double debugRangeHeight(int id) {return id == 1 ? debugRangeHeight1.get() : id == 2 ? debugRangeHeight2.get() : id == 3 ? debugRangeHeight3.get() : id == 4 ? debugRangeHeight4.get() : debugRangeHeight5.get();}
    double getSpeed() {
        return shouldSlow() ? slowSpeed.get() : placeSpeed.get();
    }
    boolean shouldSlow() {return placePos != null && getDmg(new Vec3d(placePos.getX() + 0.5, placePos.getY(), placePos.getZ() + 0.5))[0][0] <= slowDamage.get();}
    Vec3d smoothMove(Vec3d current, Vec3d target, double delta) {
        if (current == null) {return target;}
        double absX = Math.abs(current.x - target.x);
        double absY = Math.abs(current.y - target.y);
        double absZ = Math.abs(current.z - target.z);

        double x = absX * delta;
        double y = absY * delta;
        double z = absZ * delta;
        return new Vec3d(current.x > target.x ? Math.max(target.x, current.x - x) : Math.min(target.x, current.x + x),
            current.y > target.y ? Math.max(target.y, current.y - y) : Math.min(target.y, current.y + y),
            current.z > target.z ? Math.max(target.z, current.z - z) : Math.min(target.z, current.z + z));
    }

    Vec3d motionCalc(PlayerEntity en) {
        return new Vec3d(en.getX() - en.prevX, en.getY() - en.prevY, en.getZ() - en.prevZ);
    }

    Map<String, Box> getExtPos() {
        Map<String, Box> map = new HashMap<>();
        enemies = new ArrayList<>();
        if (!mc.world.getPlayers().isEmpty()) {
            for (int p = 0; p < mc.world.getPlayers().size(); p++) {
                PlayerEntity en = mc.world.getPlayers().get(p);
                if (en != mc.player && !Friends.get().isFriend(en)) {
                    if (!motions.isEmpty()) {
                        Vec3d motion = average(motions.get(en.getName().getString()));
                        double x = motion.x;
                        double y = motion.y;
                        double z = motion.z;
                        Box box = en.getBoundingBox();
                        if (!inside(en, box)) {
                            for (int i = 0; i < extrapolation.get(); i++) {
                                //x
                                if (!inside(en, box.offset(x, 0, 0))) {
                                    box = box.offset(x, 0, z);
                                }

                                //z
                                if (!inside(en, box.offset(0, 0, z))) {
                                    box = box.offset(0, 0, z);
                                }

                                //y
                                if (inside(en, box.offset(0, -0.04, 0))) {
                                    y = -0.08;
                                } else {
                                    if (!inside(en, box.offset(0, y, 0))) {
                                        box = box.offset(0, y, 0);
                                        y -= 0.08;
                                    }
                                }
                            }
                        }
                        map.put(en.getName().getString(), box);
                    }
                }
            }
        }
        return map;
    }

    Vec3d average(List<Vec3d> vec) {
        Vec3d total = new Vec3d(0, 0, 0);
        if (vec != null) {
            if (!vec.isEmpty()) {
                for (Vec3d vec3d : vec) {
                    total = total.add(vec3d);
                }
                return new Vec3d(total.x / vec.size(), total.y / vec.size(), total.z / vec.size());
            }
        }
        return total;
    }

    boolean inside(PlayerEntity en, Box bb) {
        return mc.world.getBlockCollisions(en, bb).iterator().hasNext();
    }
}
