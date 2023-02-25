/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.gui.widgets;

import com.noxclient.gui.WindowScreen;
import com.noxclient.gui.renderer.GuiRenderer;
import com.noxclient.gui.tabs.WindowTabScreen;
import com.noxclient.gui.utils.AlignmentY;
import com.noxclient.gui.utils.Cell;
import com.noxclient.gui.utils.WindowConfig;
import com.noxclient.gui.widgets.containers.WHorizontalList;
import com.noxclient.gui.widgets.pressable.WPressable;
import com.noxclient.gui.tabs.Tab;
import com.noxclient.gui.tabs.TabScreen;
import com.noxclient.gui.tabs.Tabs;
import com.noxclient.utils.render.color.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import com.noxclient.NoxClient;

import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;

public abstract class WTopBar extends WHorizontalList {
    protected abstract Color getButtonColor(boolean pressed, boolean hovered);

    protected abstract Color getNameColor();

    public WTopBar() {
        spacing = 0;
    }
    @Override
    public void init() {
        for (Tab tab : Tabs.get()) {
            add(new WTopBarButton(tab));
        }
    }

    protected class WTopBarButton extends WPressable {
        private final Tab tab;

        public WTopBarButton(Tab tab) {
            this.tab = tab;
        }


        @Override
        protected void onCalculateSize() {
            double pad = pad();

            if(MinecraftClient.getInstance().getWindow().isFullscreen()) {
                y = (MinecraftClient.getInstance().getWindow().getHeight()) - (pad + theme.textHeight());
            } else {
                y = (MinecraftClient.getInstance().getWindow().getHeight()) - (pad + theme.textHeight() + pad);
            }

            width = pad + theme.textWidth(tab.name) + pad;
            height = pad + theme.textHeight() + pad;
        }

        @Override
        protected void onPressed(int button) {
            double pad = pad();

            if(MinecraftClient.getInstance().getWindow().isFullscreen()) {
                y = (MinecraftClient.getInstance().getWindow().getHeight()) - (pad + theme.textHeight());
            } else {
                y = (MinecraftClient.getInstance().getWindow().getHeight()) - (pad + theme.textHeight() + pad);
            }

            Screen screen = NoxClient.mc.currentScreen;

            if (!(screen instanceof TabScreen) || ((TabScreen) screen).tab != tab) {
                double mouseX = NoxClient.mc.mouse.getX();
                double mouseY = NoxClient.mc.mouse.getY();

                tab.openScreen(theme);
                glfwSetCursorPos(NoxClient.mc.getWindow().getHandle(), mouseX, mouseY);
            }
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            double pad = pad();
            Color color = getButtonColor(pressed || (NoxClient.mc.currentScreen instanceof TabScreen && ((TabScreen) NoxClient.mc.currentScreen).tab == tab), mouseOver);


            if(MinecraftClient.getInstance().getWindow().isFullscreen()) {
                y = (MinecraftClient.getInstance().getWindow().getHeight()) - (pad + theme.textHeight());
            } else {
                y = (MinecraftClient.getInstance().getWindow().getHeight()) - (pad + theme.textHeight() + pad);
            }

            renderer.quad(x, y, width, height, color);
            renderer.text(tab.name, x + pad, y + pad, getNameColor(), false);
        }
    }
}
