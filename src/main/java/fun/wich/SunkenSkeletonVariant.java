package fun.wich;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public enum SunkenSkeletonVariant {
	PURPLE("purple", "textures/entity/skeleton/sunken_purple.png"),
	RED("red", "textures/entity/skeleton/sunken_red.png"),
	YELLOW("yellow", "textures/entity/skeleton/sunken_yellow.png"),
	BLUE("blue", "textures/entity/skeleton/sunken_blue.png"),
	PINK("pink", "textures/entity/skeleton/sunken_pink.png");
	public final String name;
	public final Identifier texture;
	SunkenSkeletonVariant(String name, String texture) {
		this.name = name;
		this.texture = Identifier.of(SunkenSkeletonMod.MOD_ID, texture);
	}
	public static SunkenSkeletonVariant get(SunkenSkeletonEntity entity) {
		SunkenSkeletonVariant[] variants = values();
		return variants[MathHelper.clamp(entity.getVariant(), 0, variants.length - 1)];
	}
}