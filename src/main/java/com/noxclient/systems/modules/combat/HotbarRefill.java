package com.noxclient.systems.modules.combat;

import com.noxclient.events.world.TickEvent;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import com.noxclient.utils.player.FindItemResult;
import com.noxclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class HotbarRefill extends Module {
    public HotbarRefill() {
        super(Categories.Combat,  "hot-refill", "Automatically refills your hotbar.");
    }


    private List<ItemStack> hotbar = new ArrayList<ItemStack>();

    public void onActivate() {
        for(int i = 0 ; i < 9 ; i++) {
            hotbar.add(mc.player.getInventory().getStack(i));
        }
    }

    public void onDeactivate() {
        hotbar.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {

        for(int i = 0 ; i < 9 ; i++) {
            ItemStack item = hotbar.get(i);

            if((mc.player.getInventory().getStack(i).getCount() <= 0 || mc.player.getInventory().getStack(i).isEmpty()) && !item.isEmpty()) {
                FindItemResult nitem = InvUtils.find(item.getItem());
                InvUtils.move().from(nitem.slot()).toHotbar(i);
            }
        }
    }

}
