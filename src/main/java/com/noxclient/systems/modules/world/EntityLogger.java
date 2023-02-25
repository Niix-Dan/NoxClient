package com.noxclient.systems.modules.world;

import com.noxclient.events.entity.EntityAddedEvent;
import com.noxclient.settings.*;
import com.noxclient.systems.friends.Friends;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;
import com.noxclient.utils.player.ChatUtils;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class EntityLogger extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Object2BooleanMap<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Select specific entities.")
        .build()
    );

    private final Setting<Boolean> playerNames = this.sgGeneral.add(new BoolSetting.Builder()
        .name("player-names")
        .description("Shows the player's name.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> friends = this.sgGeneral.add(new BoolSetting.Builder()
        .name("friends")
        .description("Logs friends.")
        .defaultValue(true)
        .build()
    );


    public EntityLogger() {
        super(Categories.World, "entity-logger", "Sends a client-side chat alert if a specified entity appears in render distance.");
    }

    @EventHandler
    private void onEntityAdded(EntityAddedEvent e) {
        if(e.entity.equals(mc.player)) return;

        if(entities.get().containsKey(e.entity.getType())) {
            String name;
            if(e.entity instanceof PlayerEntity && (!friends.get() && Friends.get().isFriend((PlayerEntity) e.entity)))
                return;

            if(e.entity instanceof PlayerEntity && playerNames.get()) {
                name = "§c" + e.entity.getEntityName() + " §5(Player)§f";
            } else {
                name = "§c" + e.entity.getType().getName().getString() + "§f";
            }

            String coords = String.format("§a%d %d %d", e.entity.getBlockX(), e.entity.getBlockY(), e.entity.getBlockZ());
            ChatUtils.info(name+" has spawned at "+coords);
        }
    }
}
