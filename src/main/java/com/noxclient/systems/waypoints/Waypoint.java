/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.systems.waypoints;

import com.noxclient.NoxClient;
import com.noxclient.renderer.GL;
import com.noxclient.renderer.Renderer2D;
import com.noxclient.settings.*;
import com.noxclient.utils.misc.ISerializable;
import com.noxclient.utils.player.PlayerUtils;
import com.noxclient.utils.render.color.SettingColor;
import com.noxclient.utils.world.Dimension;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.Objects;

public class Waypoint implements ISerializable<Waypoint> {
    public final Settings settings = new Settings();

    private final SettingGroup sgVisual = settings.createGroup("Visual");
    private final SettingGroup sgPosition = settings.createGroup("Position");

    public Setting<String> name = sgVisual.add(new StringSetting.Builder()
        .name("name")
        .description("The name of the waypoint.")
        .defaultValue("Home")
        .build()
    );

    public Setting<String> icon = sgVisual.add(new ProvidedStringSetting.Builder()
        .name("icon")
        .description("The icon of the waypoint.")
        .defaultValue("Square")
        .supplier(() -> Waypoints.BUILTIN_ICONS)
        .onChanged(v -> validateIcon())
        .build()
    );

    public Setting<SettingColor> color = sgVisual.add(new ColorSetting.Builder()
        .name("color")
        .description("The color of the waypoint.")
        .defaultValue(NoxClient.ADDON.color.toSetting())
        .build()
    );

    public Setting<Boolean> visible = sgVisual.add(new BoolSetting.Builder()
        .name("visible")
        .description("Whether to show the waypoint.")
        .defaultValue(true)
        .build()
    );

    public Setting<Integer> maxVisible = sgVisual.add(new IntSetting.Builder()
        .name("max-visible-distance")
        .description("How far away to render the waypoint.")
        .defaultValue(5000)
        .build()
    );

    public Setting<Double> scale = sgVisual.add(new DoubleSetting.Builder()
        .name("scale")
        .description("The scale of the waypoint.")
        .defaultValue(1)
        .build()
    );

    public Setting<BlockPos> pos = sgPosition.add(new BlockPosSetting.Builder()
        .name("location")
        .description("The location of the waypoint.")
        .defaultValue(BlockPos.ORIGIN)
        .build()
    );

    public Setting<Dimension> dimension = sgPosition.add(new EnumSetting.Builder<Dimension>()
        .name("dimension")
        .description("Which dimension the waypoint is in.")
        .defaultValue(Dimension.Overworld)
        .build()
    );

    public Setting<Boolean> opposite = sgPosition.add(new BoolSetting.Builder()
        .name("opposite-dimension")
        .description("Whether to show the waypoint in the opposite dimension.")
        .defaultValue(true)
        .visible(() -> dimension.get() != Dimension.End)
        .build()
    );

    private Waypoint() {}
    public Waypoint(NbtElement tag) {
        fromTag((NbtCompound) tag);
    }

    public void renderIcon(double x, double y, double a, double size) {
        AbstractTexture texture = Waypoints.get().icons.get(icon.get());
        if (texture == null) return;

        int preA = color.get().a;
        color.get().a *= a;

        GL.bindTexture(texture.getGlId());
        Renderer2D.TEXTURE.begin();
        Renderer2D.TEXTURE.texQuad(x, y, size, size, color.get());
        Renderer2D.TEXTURE.render(null);

        color.get().a = preA;
    }

    public BlockPos getPos() {
        Dimension dim = dimension.get();
        BlockPos pos = this.pos.get();

        Dimension currentDim = PlayerUtils.getDimension();
        if (dim == currentDim || dim.equals(Dimension.End)) return this.pos.get();

        return switch (dim) {
            case Overworld -> new BlockPos(pos.getX() / 8, pos.getY(), pos.getZ() / 8);
            case Nether -> new BlockPos(pos.getX() * 8, pos.getY(), pos.getZ() * 8);
            default -> null;
        };
    }

    private void validateIcon() {
        Map<String, AbstractTexture> icons = Waypoints.get().icons;

        AbstractTexture texture = icons.get(icon.get());
        if (texture == null && !icons.isEmpty()) {
            icon.set(icons.keySet().iterator().next());
        }
    }

    public static class Builder {
        private String name = "", icon = "";
        private BlockPos pos = BlockPos.ORIGIN;
        private Dimension dimension = Dimension.Overworld;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public Builder pos(BlockPos pos) {
            this.pos = pos;
            return this;
        }

        public Builder dimension(Dimension dimension) {
            this.dimension = dimension;
            return this;
        }

        public Waypoint build() {
            Waypoint waypoint = new Waypoint();

            if (!name.equals(waypoint.name.getDefaultValue())) waypoint.name.set(name);
            if (!icon.equals(waypoint.icon.getDefaultValue())) waypoint.icon.set(icon);
            if (!pos.equals(waypoint.pos.getDefaultValue())) waypoint.pos.set(pos);
            if (!dimension.equals(waypoint.dimension.getDefaultValue())) waypoint.dimension.set(dimension);

            return waypoint;
        }
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.put("settings", settings.toTag());

        return tag;
    }

    @Override
    public Waypoint fromTag(NbtCompound tag) {
        if (tag.contains("settings")) {
            settings.fromTag(tag.getCompound("settings"));
        }

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Waypoint waypoint = (Waypoint) o;
        return Objects.equals(waypoint.name.get(), this.name.get());
    }
}
