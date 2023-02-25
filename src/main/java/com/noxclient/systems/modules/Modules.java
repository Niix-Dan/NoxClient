/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.modules;

import com.google.common.collect.Ordering;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.noxclient.NoxClient;
import com.noxclient.settings.Setting;
import com.noxclient.settings.SettingGroup;
import com.noxclient.systems.modules.beta.*;
import com.noxclient.systems.modules.chat.*;
import com.noxclient.systems.modules.combat.*;
import com.noxclient.systems.modules.misc.*;
import com.noxclient.systems.modules.misc.swarm.Swarm;
import com.noxclient.systems.modules.movement.*;
import com.noxclient.systems.modules.movement.elytrafly.ElytraFly;
import com.noxclient.systems.modules.movement.speed.Speed;
import com.noxclient.systems.modules.player.*;
import com.noxclient.systems.modules.render.*;
import com.noxclient.systems.modules.render.marker.Marker;
import com.noxclient.systems.modules.render.search.Search;
import com.noxclient.systems.modules.world.*;
import com.noxclient.systems.modules.world.Timer;
import com.noxclient.events.game.GameJoinedEvent;
import com.noxclient.events.game.GameLeftEvent;
import com.noxclient.events.game.OpenScreenEvent;
import com.noxclient.events.meteor.ActiveModulesChangedEvent;
import com.noxclient.events.meteor.KeyEvent;
import com.noxclient.events.meteor.ModuleBindChangedEvent;
import com.noxclient.events.meteor.MouseButtonEvent;
import com.noxclient.systems.System;
import com.noxclient.systems.Systems;
import com.noxclient.utils.Utils;
import com.noxclient.utils.misc.Keybind;
import com.noxclient.utils.misc.MeteorIdentifier;
import com.noxclient.utils.misc.ValueComparableMap;
import com.noxclient.utils.misc.input.Input;
import com.noxclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryEntryList;
import net.minecraft.util.registry.RegistryKey;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class Modules extends System<Modules> {
    public static final ModuleRegistry REGISTRY = new ModuleRegistry();

    private static final List<Category> CATEGORIES = new ArrayList<>();

    private final List<Module> modules = new ArrayList<>();
    private final Map<Class<? extends Module>, Module> moduleInstances = new HashMap<>();
    private final Map<Category, List<Module>> groups = new HashMap<>();

    private final List<Module> active = new ArrayList<>();
    private Module moduleToBind;

    public Modules() {
        super("modules");
    }

    public static Modules get() {
        return Systems.get(Modules.class);
    }

    @Override
    public void init() {

        initCombat();
        initPlayer();
        initMovement();
        initRender();
        initWorld();
        initMisc();
        initChat();
        initDev();
    }

    public void initDev() {
        //add(new MinecartTntAura());
        //add(new OpenAnarchyAutoDupe());

        add(new ChunkReloader());
        add(new BedrockWalk());
    }

    public void initChat() {
        add(new AutoEZ());
        add(new AutoReply());
        add(new ChatNotify());

        add(new ArmorNotifier());
        add(new AutoLogin());
        add(new BetterChat());
        add(new ChatBot());
        add(new GroupChat());
        add(new Spam());

        add(new Announcer());
        add(new AutoTpa());
        add(new ChatEmotes());
        add(new JoinCommand());

    }

    private void initCombat() {
        add(new BananaBomber());
        add(new BurrowBreaker());
        add(new CevBreakerPlus());

        add(new HotbarRefill());
        add(new AutoPot());
        add(new KnockbackPlus());
        add(new SmartEXP());
        add(new CevBreaker());
        add(new TNTAura());
        add(new AutoShield());
        add(new Popcorn());

        add(new AimAssist());
        add(new AnchorAura());
//        add(new AntiAnchor());
        add(new AntiAnvil());
        add(new AntiBed());
        add(new ArrowDodge());
        add(new AutoAnvil());
        add(new AutoArmor());
        add(new AutoCity());
        add(new AutoEXP());
        add(new AutoTotem());
        add(new AutoTrap());
        add(new AutoWeapon());
        add(new AutoWeb());
        add(new BedAura());
        add(new BowAimbot());
        add(new BowSpam());
        add(new Burrow());
        add(new Criticals());
        add(new CrystalAura());
        add(new Hitboxes());
        add(new HoleFiller());
        add(new KillAura());
        add(new Offhand());
        add(new Quiver());
        add(new SelfAnvil());
        add(new SelfTrap());
        add(new SelfWeb());
        add(new Surround());
    }

    private void initPlayer() {
        add(new IgnoreBorder());
        add(new ChorusExploit());
        add(new MultiTask());
        add(new PingSpoofer());
        add(new NoDesync());

        add(new AntiHunger());
        add(new AutoEat());
        add(new AutoFish());
        add(new AutoGap());
        add(new AutoMend());
        add(new AutoReplenish());
        add(new AutoTool());
        add(new ChestSwap());
        add(new EXPThrower());
        add(new FakePlayer());
        add(new FastUse());
        add(new GhostHand());
        add(new InstaMine());
        add(new LiquidInteract());
        add(new MiddleClickExtra());
        add(new NoBreakDelay());
        add(new NoInteract());
        add(new NoMiningTrace());
        add(new NoRotate());
        add(new OffhandCrash());
        add(new PacketMine());
        add(new Portals());
        add(new PotionSaver());
        add(new PotionSpoof());
        add(new Reach());
        add(new Rotation());
        add(new SpeedMine());
    }

    private void initMovement() {
        add(new Glide());
        add(new Moon());
        add(new Phase());
        add(new AutoMLG());
        add(new Boost());
        add(new ReverseStepTimer());
        add(new StepPlus());

        add(new NcpEFly());
        add(new Fly());
        add(new RubberbandFly());
        add(new PacketFly());
        add(new TPFly());

        add(new AirJump());
        add(new Anchor());
        add(new AntiAFK());
        add(new AntiLevitation());
        add(new AntiVoid());
        add(new NoFallPlus());
        add(new AutoJump());
        add(new AutoWalk());
        add(new Blink());
        add(new BoatFly());
        add(new ClickTP());
        add(new ElytraBoost());
        add(new ElytraFly());
        add(new EntityControl());
        add(new EntitySpeed());
        add(new FastClimb());
        add(new Flight());
        add(new GUIMove());
        add(new HighJump());
        add(new Jesus());
        add(new LongJump());
        add(new NoFall());
        add(new FastBridge());
        add(new EntityFly());
        add(new NoSlow());
        add(new Parkour());
        add(new ReverseStep());
        add(new SafeWalk());
        add(new Scaffold());
        add(new Slippy());
        add(new Sneak());
        add(new Speed());
        add(new Spider());
        add(new Sprint());
        add(new Step());
        add(new TridentBoost());
        add(new Velocity());
    }

    private void initRender() {
        add(new SoundLocator());
        add(new NewChunksPlus());

        add(new BetterTooltips());
        add(new BlockSelection());
        add(new BossStack());
        add(new NewChunks());
        add(new Breadcrumbs());
        add(new BreakIndicators());
        add(new RenderTweaks());
        add(new CameraTweaks());
        add(new Chams());
        add(new CityESP());
        add(new PenisESP());
        add(new EntityOwner());
        add(new ESP());
        add(new Freecam());
        add(new FreeLook());
        add(new Fullbright());
        add(new HandView());
        add(new HoleESP());
        add(new ItemPhysics());
        add(new ItemHighlight());
        add(new LightOverlay());
        add(new LogoutSpots());
        add(new Marker());
        add(new Nametags());
        add(new NoRender());
        add(new Search());
        add(new StorageESP());
        add(new TimeChanger());
        add(new Tracers());
        add(new Trail());
        add(new SkeletonESP());
        add(new Trajectories());
        add(new UnfocusedCPU());
        add(new VoidESP());
        add(new WallHack());
        add(new PenisESP());
        add(new WaypointsModule());
        add(new Xray());
        add(new Zoom());
        add(new Blur());
        add(new PopChams());
        add(new TunnelESP());
    }

    private void initWorld() {
        add(new InstaMinePlus());

        add(new AutoLavaCaster());
        add(new AdvPlacer());
        add(new AntiAdventureMode());

        add(new AirPlace());
        add(new Ambience());
        add(new AntiCactus());
        add(new AutoBreed());
        add(new AutoBrewer());
        add(new AutoMount());
        add(new AutoNametag());
        add(new AutoShearer());
        add(new AutoSign());
        add(new AutoSmelter());
        add(new BuildHeight());
        add(new EChestFarmer());
        add(new EndermanLook());
        add(new Flamethrower());
        add(new InfinityMiner());
        add(new LiquidFiller());
        add(new MountBypass());
        add(new Nuker());
        add(new ChestAura());
        add(new AntiVanish());
        add(new AutoCraft());
        add(new StashFinder());
        add(new SpawnProofer());
        add(new Timer());
        add(new VeinMiner());
        add(new HighwayBuilder());
    }

    private void initMisc() {
        add(new ShulkerDupe());
        add(new AutoGrind());
        add(new PacketDuplicator());
        add(new ShulkersSception());
        add(new WitherAura());
        add(new OneClickEat());
        add(new VillagerRoller());

        add(new ShulkerDupePlus());
        add(new EntityLogger());

        add(new Swarm());
        add(new AntiPacketKick());
        add(new AutoClicker());
        add(new AutoLog());
        add(new AutoReconnect());
        add(new AutoRespawn());
        add(new BetterBeacons());
        add(new BetterTab());
        add(new BookBot());
        add(new DiscordRPC());
        add(new MessageAura());
        add(new MiddleClickFriend());
        add(new NameProtect());
        add(new Notebot());
        add(new Notifier());
        add(new PacketCanceller());
        add(new SoundBlocker());
        add(new ServerSpoof());
        add(new InventoryTweaks());
    }

    @Override
    public void load(File folder) {
        for (Module module : modules) {
            for (SettingGroup group : module.settings) {
                for (Setting<?> setting : group) setting.reset();
            }
        }

        super.load(folder);
    }

    public void sortModules() {
        for (List<Module> modules : groups.values()) {
            modules.sort(Comparator.comparing(o -> o.title));
        }
        modules.sort(Comparator.comparing(o -> o.title));
    }

    public static void registerCategory(Category category) {
        if (!Categories.REGISTERING) throw new RuntimeException("Modules.registerCategory - Cannot register category outside of onRegisterCategories callback.");

        CATEGORIES.add(category);
    }

    public static Iterable<Category> loopCategories() {
        return CATEGORIES;
    }

    public static Category getCategoryByHash(int hash) {
        for (Category category : CATEGORIES) {
            if (category.hashCode() == hash) return category;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T get(Class<T> klass) {
        return (T) moduleInstances.get(klass);
    }

    public Module get(String name) {
        for (Module module : moduleInstances.values()) {
            if (module.name.equalsIgnoreCase(name)) return module;
        }

        return null;
    }

    public boolean isActive(Class<? extends Module> klass) {
        Module module = get(klass);
        return module != null && module.isActive();
    }

    public List<Module> getGroup(Category category) {
        return groups.computeIfAbsent(category, category1 -> new ArrayList<>());
    }

    public Collection<Module> getAll() {
        return moduleInstances.values();
    }

    public List<Module> getList() {
        return modules;
    }

    public int getCount() {
        return moduleInstances.values().size();
    }

    public List<Module> getActive() {
        synchronized (active) {
            return active;
        }
    }

    public Set<Module> searchTitles(String text) {
        Map<Module, Integer> modules = new ValueComparableMap<>(Ordering.natural().reverse());

        for (Module module : this.moduleInstances.values()) {
            int words = Utils.search(module.title, text);
            if (words > 0) modules.put(module, modules.getOrDefault(module, 0) + words);
        }

        return modules.keySet();
    }

    public Set<Module> searchSettingTitles(String text) {
        Map<Module, Integer> modules = new ValueComparableMap<>(Ordering.natural().reverse());

        for (Module module : this.moduleInstances.values()) {
            for (SettingGroup sg : module.settings) {
                for (Setting<?> setting : sg) {
                    int words = Utils.search(setting.title, text);
                    if (words > 0) modules.put(module, modules.getOrDefault(module, 0) + words);
                }
            }
        }

        return modules.keySet();
    }
    void addActive(Module module) {
        synchronized (active) {
            if (!active.contains(module)) {
                active.add(module);
                NoxClient.EVENT_BUS.post(ActiveModulesChangedEvent.get());
            }
        }
    }

    void removeActive(Module module) {
        synchronized (active) {
            if (active.remove(module)) {
                NoxClient.EVENT_BUS.post(ActiveModulesChangedEvent.get());
            }
        }
    }

    // Binding

    public void setModuleToBind(Module moduleToBind) {
        this.moduleToBind = moduleToBind;
    }

    public boolean isBinding() {
        return moduleToBind != null;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onKeyBinding(KeyEvent event) {
        if (event.action == KeyAction.Press && onBinding(true, event.key)) event.cancel();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onButtonBinding(MouseButtonEvent event) {
        if (event.action == KeyAction.Press && onBinding(false, event.button)) event.cancel();
    }

    private boolean onBinding(boolean isKey, int value) {
        if (!isBinding()) return false;

        if (moduleToBind.keybind.canBindTo(isKey, value)) {
            moduleToBind.keybind.set(isKey, value);
            moduleToBind.info("Bound to (highlight)%s(default).", moduleToBind.keybind);
        }
        else if (value == GLFW.GLFW_KEY_ESCAPE) {
            moduleToBind.keybind.set(Keybind.none());
            moduleToBind.info("Removed bind.");
        }
        else return false;

        NoxClient.EVENT_BUS.post(ModuleBindChangedEvent.get(moduleToBind));
        moduleToBind = null;

        return true;
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onKey(KeyEvent event) {
        if (event.action == KeyAction.Repeat) return;
        onAction(true, event.key, event.action == KeyAction.Press);
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action == KeyAction.Repeat) return;
        onAction(false, event.button, event.action == KeyAction.Press);
    }

    private void onAction(boolean isKey, int value, boolean isPress) {
        if (NoxClient.mc.currentScreen == null && !Input.isKeyPressed(GLFW.GLFW_KEY_F3)) {
            for (Module module : moduleInstances.values()) {
                if (module.keybind.matches(isKey, value) && (isPress || module.toggleOnBindRelease)) {
                    module.toggle();
                    module.sendToggledMsg();
                }
            }
        }
    }

    // End of binding

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onOpenScreen(OpenScreenEvent event) {
        if (!Utils.canUpdate()) return;

        for (Module module : moduleInstances.values()) {
            if (module.toggleOnBindRelease && module.isActive()) {
                module.toggle();
                module.sendToggledMsg();
            }
        }
    }

    @EventHandler
    private void onGameJoined(GameJoinedEvent event) {
        synchronized (active) {
            for (Module module : modules) {
                if (module.isActive() && !module.runInMainMenu) {
                    NoxClient.EVENT_BUS.subscribe(module);
                    module.onActivate();
                }
            }
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        synchronized (active) {
            for (Module module : modules) {
                if (module.isActive() && !module.runInMainMenu) {
                    NoxClient.EVENT_BUS.unsubscribe(module);
                    module.onDeactivate();
                }
            }
        }
    }

    public void disableAll() {
        synchronized (active) {
            for (Module module : modules) {
                if (module.isActive()) module.toggle();
            }
        }
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        NbtList modulesTag = new NbtList();
        for (Module module : getAll()) {
            NbtCompound moduleTag = module.toTag();
            if (moduleTag != null) modulesTag.add(moduleTag);
        }
        tag.put("modules", modulesTag);

        return tag;
    }

    @Override
    public Modules fromTag(NbtCompound tag) {
        disableAll();

        NbtList modulesTag = tag.getList("modules", 10);
        for (NbtElement moduleTagI : modulesTag) {
            NbtCompound moduleTag = (NbtCompound) moduleTagI;
            Module module = get(moduleTag.getString("name"));
            if (module != null) module.fromTag(moduleTag);
        }

        return this;
    }

    // INIT MODULES

    public void add(Module module) {
        //if(!Config.get().isValid && module.isActive()) module.toggle(false);
        //if(!Config.get().isValid) return;


        // Check if the module's category is registered
        if (!CATEGORIES.contains(module.category)) {
            throw new RuntimeException("Modules.addModule - Module's category was not registered.");
        }

        // Remove the previous module with the same name
        AtomicReference<Module> removedModule = new AtomicReference<>();
        if (moduleInstances.values().removeIf(module1 -> {
            if (module1.name.equals(module.name)) {
                removedModule.set(module1);
                module1.settings.unregisterColorSettings();

                return true;
            }

            return false;
        })) {
            getGroup(removedModule.get().category).remove(removedModule.get());
        }

        // Add the module
        moduleInstances.put(module.getClass(), module);
        modules.add(module);
        getGroup(module.category).add(module);

        // Register color settings for the module
        module.settings.registerColorSettings(module);
    }



    public static class ModuleRegistry extends Registry<Module> {
        public ModuleRegistry() {
            super(RegistryKey.ofRegistry(new MeteorIdentifier("modules")), Lifecycle.stable());
        }

        @Override
        public int size() {
            return Modules.get().getAll().size();
        }

        @Override
        public Identifier getId(Module entry) {
            return null;
        }

        @Override
        public Optional<RegistryKey<Module>> getKey(Module entry) {
            return Optional.empty();
        }

        @Override
        public int getRawId(Module entry) {
            return 0;
        }

        @Override
        public Module get(RegistryKey<Module> key) {
            return null;
        }

        @Override
        public Module get(Identifier id) {
            return null;
        }

        @Override
        public Lifecycle getEntryLifecycle(Module object) {
            return null;
        }

        @Override
        public Lifecycle getLifecycle() {
            return null;
        }

        @Override
        public Set<Identifier> getIds() {
            return null;
        }
        @Override
        public boolean containsId(Identifier id) {
            return false;
        }

        @Nullable
        @Override
        public Module get(int index) {
            return null;
        }

        @Override
        public Iterator<Module> iterator() {
            return new ModuleIterator();
        }

        @Override
        public boolean contains(RegistryKey<Module> key) {
            return false;
        }

        @Override
        public Set<Map.Entry<RegistryKey<Module>, Module>> getEntrySet() {
            return null;
        }

        @Override
        public Set<RegistryKey<Module>> getKeys() {
            return null;
        }

        @Override
        public Optional<RegistryEntry<Module>> getRandom(Random random) {
            return Optional.empty();
        }

        @Override
        public Registry<Module> freeze() {
            return null;
        }

        @Override
        public RegistryEntry<Module> getOrCreateEntry(RegistryKey<Module> key) {
            return null;
        }

        @Override
        public DataResult<RegistryEntry<Module>> getOrCreateEntryDataResult(RegistryKey<Module> key) {
            return null;
        }

        @Override
        public RegistryEntry.Reference<Module> createEntry(Module value) {
            return null;
        }

        @Override
        public Optional<RegistryEntry<Module>> getEntry(int rawId) {
            return Optional.empty();
        }

        @Override
        public Optional<RegistryEntry<Module>> getEntry(RegistryKey<Module> key) {
            return Optional.empty();
        }

        @Override
        public Stream<RegistryEntry.Reference<Module>> streamEntries() {
            return null;
        }

        @Override
        public Optional<RegistryEntryList.Named<Module>> getEntryList(TagKey<Module> tag) {
            return Optional.empty();
        }

        @Override
        public RegistryEntryList.Named<Module> getOrCreateEntryList(TagKey<Module> tag) {
            return null;
        }

        @Override
        public Stream<Pair<TagKey<Module>, RegistryEntryList.Named<Module>>> streamTagsAndEntries() {
            return null;
        }

        @Override
        public Stream<TagKey<Module>> streamTags() {
            return null;
        }

        @Override
        public boolean containsTag(TagKey<Module> tag) {
            return false;
        }

        @Override
        public void clearTags() {

        }

        @Override
        public void populateTags(Map<TagKey<Module>, List<RegistryEntry<Module>>> tagEntries) {

        }

        private static class ModuleIterator implements Iterator<Module> {
            private final Iterator<Module> iterator = Modules.get().getAll().iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Module next() {
                return iterator.next();
            }
        }
    }
}
