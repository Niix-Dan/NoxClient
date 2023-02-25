/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.mixin;

import com.noxclient.systems.modules.Modules;
import com.noxclient.systems.modules.misc.ShulkerDupe;
import com.noxclient.systems.modules.misc.InventoryTweaks;
import com.noxclient.utils.render.ContainerButtonWidget;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ShulkerBoxScreen.class)
public abstract class ShulkerBoxScreenMixin extends HandledScreen<ShulkerBoxScreenHandler> {
    public ShulkerBoxScreenMixin(ShulkerBoxScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();

        InventoryTweaks invTweaks = Modules.get().get(InventoryTweaks.class);

        if (invTweaks.isActive() && invTweaks.showButtons()) {
            addDrawableChild(new ContainerButtonWidget(
                x + backgroundWidth - 88,
                y + 3,
                40,
                12,
                Text.literal("Steal"),
                button -> invTweaks.steal(handler))
            );

            addDrawableChild(new ContainerButtonWidget(
                x + backgroundWidth - 46,
                y + 3,
                40,
                12,
                Text.literal("Dump"),
                button -> invTweaks.dump(handler))
            );
        }

        if (Modules.get().isActive(ShulkerDupe.class)) {
            addDrawableChild(new ContainerButtonWidget(
                x - 62,
                y + 3,
                60,
                12,
                Text.literal("DUPE ONE"),
                button -> Modules.get().get(ShulkerDupe.class).shoulddupe = ShulkerDupe.ShouldDupe.ONE
            ));
            addDrawableChild(new ContainerButtonWidget(
                x - 62,
                y + 20,
                60,
                12,
                Text.literal("DUPE ALL"),
                button -> Modules.get().get(ShulkerDupe.class).shoulddupe = ShulkerDupe.ShouldDupe.ALL
            ));
        }

        if (invTweaks.autoSteal()) invTweaks.steal(handler);
        if (invTweaks.autoDump()) invTweaks.dump(handler);
    }
}
