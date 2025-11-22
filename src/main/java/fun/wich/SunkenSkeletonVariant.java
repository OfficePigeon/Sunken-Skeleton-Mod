package fun.wich;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.function.Supplier;

public enum SunkenSkeletonVariant {
	PURPLE("purple", "textures/entity/skeleton/sunken_purple.png", () -> Items.BUBBLE_CORAL, () -> Items.BUBBLE_CORAL_FAN),
	RED("red", "textures/entity/skeleton/sunken_red.png", () -> Items.FIRE_CORAL, () -> Items.FIRE_CORAL_FAN),
	YELLOW("yellow", "textures/entity/skeleton/sunken_yellow.png", () -> Items.HORN_CORAL, () -> Items.HORN_CORAL_FAN),
	BLUE("blue", "textures/entity/skeleton/sunken_blue.png", () -> Items.TUBE_CORAL, () -> Items.TUBE_CORAL_FAN),
	PINK("pink", "textures/entity/skeleton/sunken_pink.png", () -> Items.BRAIN_CORAL, () -> Items.BRAIN_CORAL_FAN);

	public final String name;
	public final Identifier texture;
	public final Supplier<Item> coral;
	public final Supplier<Item> coral_fan;
	SunkenSkeletonVariant(String name, String texture, Supplier<Item> coral, Supplier<Item> coral_fan) {
		this.name = name;
		this.texture = Identifier.of(SunkenSkeletonMod.MOD_ID, texture);
		this.coral = coral;
		this.coral_fan = coral_fan;
	}
	public static SunkenSkeletonVariant get(SunkenSkeletonEntity entity) {
		SunkenSkeletonVariant[] variants = values();
		return variants[MathHelper.clamp(entity.getVariant(), 0, variants.length - 1)];
	}
}