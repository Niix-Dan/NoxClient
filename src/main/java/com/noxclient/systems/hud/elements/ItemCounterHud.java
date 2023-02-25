package com.noxclient.systems.hud.elements;

import com.noxclient.settings.*;
import com.noxclient.systems.hud.*;
import com.noxclient.utils.Utils;
import com.noxclient.utils.misc.Names;
import com.noxclient.utils.misc.Pool;
import com.noxclient.utils.player.InvUtils;
import net.minecraft.item.*;

import java.util.*;

public class ItemCounterHud extends HudElement {
    public static final HudElementInfo<ItemCounterHud> INFO = new HudElementInfo<>(Hud.GROUP, "item-counter", "Count different items in text.", ItemCounterHud::new);
    private final SettingGroup sgGeneral = settings.getDefaultGroup();


    // General
    private final Setting<SortMode> sortMode = sgGeneral.add(new EnumSetting.Builder<SortMode>()
        .name("sort-mode")
        .description("How to sort the items list.")
        .defaultValue(SortMode.Smallest)
        .build()
    );

    private final Setting<Alignment> alignment = sgGeneral.add(new EnumSetting.Builder<Alignment>()
        .name("alignment")
        .description("Horizontal alignment.")
        .defaultValue(Alignment.Auto)
        .build()
    );

    private final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
        .name("shadow")
        .description("Renders shadow behind text.")
        .defaultValue(true)
        .build()
    );

    private final Setting<List<Item>> countedItems = sgGeneral.add(new ItemListSetting.Builder()
        .name("items")
        .description("Which items to display in the counter list.")
        .defaultValue(new ArrayList<>(0))
        .build()
    );

    public ItemCounterHud() {
        super(INFO);
    }


    private final Pool<CountedItem> itemPool = new Pool<>(CountedItem::new);
    private final List<CountedItem> items = new ArrayList<>();


    @Override
    public void tick(HudRenderer renderer) {
        if (!Utils.canUpdate()) return;

        updateList(renderer);

        double width = 0;
        double height = 0;

        if (items.isEmpty()) {
            width = Math.max(width, renderer.textWidth("Item Counter"));
            height += renderer.textHeight(shadow.get());
        } else {
            double i = 0;
            for (CountedItem item : items) {
                width = Math.max(width, item.totalWidth);
                height += renderer.textHeight(shadow.get());
                if (i > 0) height += 2;
                i++;
            }
        }

        box.setSize(width, height);
    }

    @Override
    public void render(HudRenderer renderer) {
        if (!Utils.canUpdate()) return;

        updateList(renderer);

        if (items.isEmpty()) {
            renderer.text("Item Counter", x, this.y, TextHud.getSectionColor(0), shadow.get());
        } else {
            double y = this.y;
            double i = 0;

            for (CountedItem item : items) {
                double lineWidth = renderer.textWidth(item.name + item.count);

                double x = this.x + alignX(lineWidth, alignment.get());
                x = renderer.text(item.name, x, y, TextHud.getSectionColor(0), shadow.get());
                renderer.text(item.count, x, y, TextHud.getSectionColor(1), shadow.get());

                y += renderer.textHeight(shadow.get());
                if (i > 0) y += 2;
                i++;
            }
        }
    }

    private void updateList(HudRenderer renderer) {
        for (CountedItem item : items) itemPool.free(item);
        items.clear();

        for (Item item : countedItems.get()) items.add(itemPool.get().set(renderer, item));

        if (sortMode.get() == SortMode.Smallest) {
            items.sort(Comparator.comparing(item -> item.totalWidth));
        } else items.sort(Comparator.comparing(item -> (0-item.totalWidth)));
    }

    private static class CountedItem {
        public Item item;
        public String name;
        public String count;
        public double totalWidth;

        public CountedItem set(HudRenderer renderer, Item i) {
            item = i;
            name = getName(i) + ": ";
            count = String.valueOf(InvUtils.find(i).count());
            totalWidth = renderer.textWidth(name + count);

            return this;
        }
    }

    public static String getName(Item item) {
        if (item instanceof BedItem) return "Beds";
        if (item instanceof ExperienceBottleItem) return "XP Bottles";
        if (item instanceof EndCrystalItem) return "Crystals";
        if (item instanceof EnchantedGoldenAppleItem) return "Gapples";
        if (item instanceof EnderPearlItem) return "Pearls";

        if (item == Items.TOTEM_OF_UNDYING) return "Totems";
        if (item == Items.ENDER_CHEST) return "Echests";

        return Names.get(item);
    }

    public enum SortMode {
        Biggest,
        Smallest
    }
}
