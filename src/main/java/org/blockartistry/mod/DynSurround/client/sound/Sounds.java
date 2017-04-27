/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher, Abastro
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.blockartistry.mod.DynSurround.client.sound;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class Sounds {

	private Sounds() {

	}

	public static final SoundEffect JUMP = new SoundEffect.Builder("jump", SoundCategory.PLAYERS).setVariablePitch(true)
			.build();
	public static final SoundEffect CRAFTING = new SoundEffect.Builder("crafting", SoundCategory.PLAYERS).build();

	public static final SoundEffect SWORD_EQUIP = new SoundEffect.Builder("sword.equip", SoundCategory.PLAYERS)
			.setVolume(0.5F).build();
	public static final SoundEffect SWORD_SWING = new SoundEffect.Builder("sword.swing", SoundCategory.PLAYERS).build();
	public static final SoundEffect AXE_EQUIP = new SoundEffect.Builder("blunt.equip", SoundCategory.PLAYERS)
			.setVolume(0.35F).build();
	public static final SoundEffect AXE_SWING = new SoundEffect.Builder("blunt.swing", SoundCategory.PLAYERS).build();
	public static final SoundEffect BOW_EQUIP = new SoundEffect.Builder("bow.equip", SoundCategory.PLAYERS)
			.setVolume(0.30F).build();
	public static final SoundEffect BOW_PULL = new SoundEffect.Builder("bow.pull", SoundCategory.PLAYERS).build();
	public static final SoundEffect TOOL_EQUIP = new SoundEffect.Builder("tool.equip", SoundCategory.PLAYERS)
			.setVolume(0.30F).build();
	public static final SoundEffect TOOL_SWING = new SoundEffect.Builder("tool.swing", SoundCategory.PLAYERS).build();
	public static final SoundEffect UTILITY_EQUIP = new SoundEffect.Builder("utility.equip", SoundCategory.PLAYERS)
			.setVolume(0.35F).build();

	public static final SoundEffect SHIELD_EQUIP = TOOL_EQUIP;

	public static final SoundEffect LIGHT_ARMOR_EQUIP = new SoundEffect.Builder("fs.armor.light_walk",
			SoundCategory.PLAYERS).build();
	public static final SoundEffect MEDIUM_ARMOR_EQUIP = new SoundEffect.Builder("fs.armor.medium_walk",
			SoundCategory.PLAYERS).build();
	public static final SoundEffect HEAVY_ARMOR_EQUIP = new SoundEffect.Builder("fs.armor.heavy_walk",
			SoundCategory.PLAYERS).build();
	public static final SoundEffect CRYSTAL_ARMOR_EQUIP = new SoundEffect.Builder("fs.armor.crystal_walk",
			SoundCategory.PLAYERS).build();

	public static final SoundEffect THUNDER = new SoundEffect.Builder("thunder", SoundCategory.WEATHER)
			.setVolume(10000F).build();
	public static final SoundEffect RAINFALL = new SoundEffect.Builder("rain", SoundCategory.WEATHER).build();

	public static final SoundEffect WATER_DROP = new SoundEffect.Builder("waterdrops", SoundCategory.AMBIENT).build();
	public static final SoundEffect WATER_DRIP = new SoundEffect.Builder("waterdrips", SoundCategory.AMBIENT).build();
	public static final SoundEffect STEAM_HISS = new SoundEffect.Builder(new ResourceLocation("block.fire.extinguish"),
			SoundCategory.AMBIENT).setVolume(0.1F).setPitch(1.0F).build();

	public static final SoundEffect FIRE = new SoundEffect.Builder(new ResourceLocation("minecraft:block.fire.ambient"),
			SoundCategory.BLOCKS).build();

	public static final SoundEffect WATERFALL = new SoundEffect.Builder("waterfall", SoundCategory.AMBIENT).build();

}
