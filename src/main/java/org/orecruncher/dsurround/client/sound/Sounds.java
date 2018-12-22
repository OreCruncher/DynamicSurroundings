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

package org.orecruncher.dsurround.client.sound;

import org.orecruncher.dsurround.ModInfo;
import org.orecruncher.dsurround.registry.RegistryManager;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class Sounds {

	private Sounds() {

	}

	public final static SoundEffect CRAFTING;
	public final static SoundEffect SWORD_EQUIP;
	public final static SoundEffect SWORD_SWING;
	public final static SoundEffect AXE_EQUIP;
	public final static SoundEffect AXE_SWING;
	public final static SoundEffect BOW_EQUIP;
	public final static SoundEffect BOW_PULL;
	public final static SoundEffect TOOL_EQUIP;
	public final static SoundEffect TOOL_SWING;
	public final static SoundEffect UTILITY_EQUIP;
	public final static SoundEffect FOOD_EQUIP;
	public final static SoundEffect SHIELD_EQUIP;
	public final static SoundEffect SHIELD_USE;

	public final static SoundEffect LEATHER_ARMOR_EQUIP;
	public final static SoundEffect CHAIN_ARMOR_EQUIP;
	public final static SoundEffect PLATE_ARMOR_EQUIP;
	public final static SoundEffect CRYSTAL_ARMOR_EQUIP;

	public final static SoundEffect BOOK_EQUIP;
	public final static SoundEffect POTION_EQUIP;

	public final static SoundEffect THUNDER;
	public final static SoundEffect RAINFALL;

	public final static SoundEffect WATER_DROP;
	public final static SoundEffect WATER_DRIP;
	public final static SoundEffect STEAM_HISS;

	public final static SoundEffect FIRE;

	// Waterfalls
	public final static SoundEffect WATERFALL0;
	public final static SoundEffect WATERFALL1;
	public final static SoundEffect WATERFALL2;
	public final static SoundEffect WATERFALL3;
	public final static SoundEffect WATERFALL4;
	public final static SoundEffect WATERFALL5;

	// Weather stuff
	public final static SoundEvent RAIN;
	public final static SoundEvent DUST;

	// Minecraft Cave Stuff
	public final static SoundEffect AMBIENT_CAVE;

	static {
		// Weather
		RAIN = RegistryManager.SOUND.getSound(new ResourceLocation(ModInfo.RESOURCE_ID, "rain"));
		DUST = RegistryManager.SOUND.getSound(new ResourceLocation(ModInfo.RESOURCE_ID, "dust"));

		// SoundEffects
		CRAFTING = new SoundEffect.Builder("crafting", SoundCategory.PLAYERS).build();

		SWORD_EQUIP = new SoundEffect.Builder("sword.equip", SoundCategory.PLAYERS).setVolume(0.5F).build();
		SWORD_SWING = new SoundEffect.Builder("sword.swing", SoundCategory.PLAYERS).build();
		AXE_EQUIP = new SoundEffect.Builder("blunt.equip", SoundCategory.PLAYERS).setVolume(0.35F).build();
		AXE_SWING = new SoundEffect.Builder("blunt.swing", SoundCategory.PLAYERS).build();
		BOW_EQUIP = new SoundEffect.Builder("bow.equip", SoundCategory.PLAYERS).setVolume(0.30F).build();
		BOW_PULL = new SoundEffect.Builder("bow.pull", SoundCategory.PLAYERS).build();
		TOOL_EQUIP = new SoundEffect.Builder("tool.equip", SoundCategory.PLAYERS).setVolume(0.30F).build();
		TOOL_SWING = new SoundEffect.Builder("tool.swing", SoundCategory.PLAYERS).build();
		UTILITY_EQUIP = new SoundEffect.Builder("utility.equip", SoundCategory.PLAYERS).setVolume(0.35F).build();
		FOOD_EQUIP = new SoundEffect.Builder("food.equip", SoundCategory.PLAYERS).setVolume(0.35F).build();
		SHIELD_EQUIP = new SoundEffect.Builder("shield.equip", SoundCategory.PLAYERS).setVolume(0.30F).build();
		SHIELD_USE = new SoundEffect.Builder("shield.use", SoundCategory.PLAYERS).setVolume(0.30F).build();

		LEATHER_ARMOR_EQUIP = new SoundEffect.Builder("armor.light_walk", SoundCategory.PLAYERS).build();
		CHAIN_ARMOR_EQUIP = new SoundEffect.Builder("armor.medium_walk", SoundCategory.PLAYERS).build();
		PLATE_ARMOR_EQUIP = new SoundEffect.Builder("armor.heavy_walk", SoundCategory.PLAYERS).build();
		CRYSTAL_ARMOR_EQUIP = new SoundEffect.Builder("armor.crystal_walk", SoundCategory.PLAYERS).build();

		BOOK_EQUIP = new SoundEffect.Builder("pageflip", SoundCategory.PLAYERS).build();
		POTION_EQUIP = new SoundEffect.Builder("potion.equip", SoundCategory.PLAYERS).build();

		THUNDER = new SoundEffect.Builder("thunder", SoundCategory.WEATHER).setVolume(10000F).build();
		RAINFALL = new SoundEffect.Builder("rain", SoundCategory.WEATHER).build();

		WATER_DROP = new SoundEffect.Builder("waterdrops", SoundCategory.AMBIENT).build();
		WATER_DRIP = new SoundEffect.Builder("waterdrips", SoundCategory.AMBIENT).build();
		STEAM_HISS = new SoundEffect.Builder(new ResourceLocation("block.fire.extinguish"), SoundCategory.AMBIENT)
				.setVolume(0.1F).setPitch(1.0F).build();

		FIRE = new SoundEffect.Builder(new ResourceLocation("minecraft:block.fire.ambient"), SoundCategory.BLOCKS)
				.build();

		// Waterfalls
		WATERFALL0 = new SoundEffect.Builder("waterfall.0", SoundCategory.AMBIENT).build();
		WATERFALL1 = new SoundEffect.Builder("waterfall.1", SoundCategory.AMBIENT).build();
		WATERFALL2 = new SoundEffect.Builder("waterfall.2", SoundCategory.AMBIENT).build();
		WATERFALL3 = new SoundEffect.Builder("waterfall.3", SoundCategory.AMBIENT).build();
		WATERFALL4 = new SoundEffect.Builder("waterfall.4", SoundCategory.AMBIENT).build();
		WATERFALL5 = new SoundEffect.Builder("waterfall.5", SoundCategory.AMBIENT).build();

		AMBIENT_CAVE = new SoundEffect.Builder(new ResourceLocation("minecraft:ambient.cave"), SoundCategory.AMBIENT)
				.build();

	}

}
