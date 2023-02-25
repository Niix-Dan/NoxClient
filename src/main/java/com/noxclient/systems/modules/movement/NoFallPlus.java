/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.modules.movement;


import com.noxclient.mixin.PlayerMoveC2SPacketAccessor;
import com.noxclient.mixininterface.IVec3d;
import com.noxclient.settings.BoolSetting;
import com.noxclient.settings.EnumSetting;
import com.noxclient.settings.Setting;
import com.noxclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import com.noxclient.events.packets.PacketEvent;
import com.noxclient.systems.modules.Categories;
import com.noxclient.systems.modules.Module;

public class NoFallPlus extends Module {
	public NoFallPlus() {
		super(Categories.Movement, "NoFall-plus", "Prevent you from fall damage.");
	}
	private final SettingGroup sgGeneral = settings.getDefaultGroup();

	private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
		.name("mode")
		.description("NoFall mode.")
		.defaultValue(Mode.Matrix3)
		.build()
	);
	private final Setting<Boolean> newMode = sgGeneral.add(new BoolSetting.Builder()
		.name("new-mode")
		.description("Better anticheat support.")
		.defaultValue(true)
		.onChanged((e) -> {
			if (e) {
				lastY = 3;
			}
			else {
				lastY = 4;
			}
		})
		.build()
	);

	@Override
	public void onActivate() {
		if (newMode.get()) {
			lastY = 3;
		}
		else {
			lastY = 4;
		}
	}

	public enum Mode
	{
		Matrix3,
		Matrix4,
	}

	@EventHandler
	private void onSendPacket(PacketEvent.Send event) {
		work(event.packet);
	}

	@EventHandler
	private void onSendPacketSent(PacketEvent.Sent event) {
		work(event.packet);
	}

	private boolean checkY(PlayerMoveC2SPacket packet) {
		if (packet.changesPosition()) {
			if (mc.player != null && (int)mc.player.fallDistance % 4 == 0 && mc.player.fallDistance >= 4 && lastY == 4) {
				if (newMode.get()) {
					lastY = 3;
				}
				if (mode.get() == Mode.Matrix4) {
					IVec3d vec3d = (IVec3d)mc.player.getVelocity();
					vec3d.setY(0);
				}
				return true;
			}
			else if (mc.player != null && (int)mc.player.fallDistance % 3 == 0 && mc.player.fallDistance >= 3 && lastY == 3) {
				if (newMode.get()) {
					lastY = 4;
				}
				if (mode.get() == Mode.Matrix4) {
					IVec3d vec3d = (IVec3d)mc.player.getVelocity();
					vec3d.setY(0);
				}
				return true;
			}
		}
		return false;
	}

	private double lastY = 4;

	private void work(Packet<?> packet) {
		if (packet instanceof PlayerMoveC2SPacket move) {
			if (checkY(move)) {
				((PlayerMoveC2SPacketAccessor) move).setOnGround(true);
			}
		}
	}
}
