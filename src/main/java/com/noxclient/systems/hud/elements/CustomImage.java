

/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.hud.elements;


import com.noxclient.NoxClient;
import com.noxclient.settings.*;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import com.noxclient.systems.hud.Hud;
import com.noxclient.systems.hud.HudElement;
import com.noxclient.systems.hud.HudElementInfo;
import com.noxclient.systems.hud.HudRenderer;
import com.noxclient.utils.network.Http;

// credit to meteorplus for this!
// big yes
import static com.noxclient.utils.render.color.Color.WHITE;

public class CustomImage extends HudElement {

	public static final HudElementInfo<CustomImage> INFO = new HudElementInfo<>(Hud.GROUP, "CustomImage", "Nox on top", CustomImage::new);

	private final SettingGroup sgGeneral = settings.getDefaultGroup();

	private final Setting<String> link = sgGeneral.add(new StringSetting.Builder()
		.name("Link")
		.description("Image link.")
		.defaultValue("https://i.imgur.com/FRIifmr.png")
		.onChanged(this::loadImage)
		.build()
	);

	private final Setting<Double> imgWidth = sgGeneral.add(new DoubleSetting.Builder()
		.name("width")
		.description("The scale of the image.")
		.defaultValue(100)
		.min(10)
		.onChanged((size) -> calculateSize())
		.sliderRange(70, 1000)
		.build()
	);

	private static final Identifier TEXID = new Identifier("plus", "logo2");

	private final Setting<Double> imgHeight = sgGeneral.add(new DoubleSetting.Builder()
		.name("height")
		.description("The scale of the image.")
		.defaultValue(100)
		.min(10)
		.onChanged((size) -> calculateSize())
		.sliderRange(70, 1000)
		.build()
	);

	private final Setting<Boolean> onInventory = sgGeneral.add(new BoolSetting.Builder()
		.name("Only-inventory")
		.description("Work in inventory.")
		.defaultValue(false)
		.build()
	);

	private final Setting<Boolean> noChat = sgGeneral.add(new BoolSetting.Builder()
		.name("No-chat")
		.description("Not work in chat.")
		.defaultValue(false)
		.visible(onInventory::get)
		.build()
	);

	private final Setting<Boolean> Invert = sgGeneral.add(new BoolSetting.Builder()
		.name("Invert")
		.description("Inverts the image.")
		.defaultValue(false)
		.build()
	);

	public CustomImage() {
		super(INFO);
		calculateSize();
	}

	public void calculateSize() {
		box.setSize(imgWidth.get(), imgHeight.get());
	}

	@Override
	public void render(HudRenderer renderer) {
		if (empty) {
			loadImage(link.get());
			return;
		}
		if ((onInventory.get() && NoxClient.mc != null && NoxClient.mc.currentScreen != null) || isInEditor()) {
			if (noChat.get() && !isInEditor() && NoxClient.mc.currentScreen instanceof ChatScreen) return;
			if (!Invert.get()){
				renderer.texture(TEXID, box.getRenderX(), box.getRenderY(), imgWidth.get(), imgHeight.get(), WHITE);
			} else {
				renderer.texture(TEXID, box.getRenderX()+imgWidth.get(), box.getRenderY(), -(imgWidth.get()), imgHeight.get(), WHITE);
			}
		}
		else if (!onInventory.get()) {
			if (!Invert.get()){
				renderer.texture(TEXID, box.getRenderX(), box.getRenderY(), imgWidth.get(), imgHeight.get(), WHITE);
			} else {
				renderer.texture(TEXID, box.getRenderX()+imgWidth.get(), box.getRenderY(), -(imgWidth.get()), imgHeight.get(), WHITE);
			}
		}
	}

	private boolean locked = false;
	private boolean empty = true;
	private void loadImage(String url) {
		if (locked) {
			return;
		}
		new Thread(() -> {
			try {
				locked = true;
				var img = NativeImage.read(Http.get(url).sendInputStream());
				NoxClient.mc.getTextureManager().registerTexture(TEXID, new NativeImageBackedTexture(img));
				empty = false;
			} catch (Exception ignored) {
				empty = true;
			} finally {
				locked = false;
			}
		}).start();
	}

}
