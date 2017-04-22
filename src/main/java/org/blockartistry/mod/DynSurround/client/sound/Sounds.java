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

public final class Sounds {

	private Sounds() {

	}

	public static final SoundEffect JUMP = new SoundEffect("jump", SoundCategory.PLAYERS).setVariable(true);
	public static final SoundEffect CRAFTING = new SoundEffect("crafting", SoundCategory.PLAYERS);

	public static final SoundEffect SWORD_EQUIP = new SoundEffect("sword.equip", SoundCategory.PLAYERS);
	public static final SoundEffect SWORD_SWING = new SoundEffect("sword.swing", SoundCategory.PLAYERS);
	public static final SoundEffect AXE_EQUIP = new SoundEffect("blunt.equip", SoundCategory.PLAYERS);
	public static final SoundEffect AXE_SWING = new SoundEffect("blunt.swing", SoundCategory.PLAYERS);
	public static final SoundEffect BOW_EQUIP = new SoundEffect("bow.equip", SoundCategory.PLAYERS);
	public static final SoundEffect BOW_PULL = new SoundEffect("bow.pull", SoundCategory.PLAYERS);
	public static final SoundEffect TOOL_EQUIP = new SoundEffect("tool.equip", SoundCategory.PLAYERS);
	public static final SoundEffect TOOL_SWING = new SoundEffect("tool.swing", SoundCategory.PLAYERS);
	public static final SoundEffect UTILITY_EQUIP = new SoundEffect("utility.equip", SoundCategory.PLAYERS);

	public static final SoundEffect LIGHT_ARMOR_EQUIP = new SoundEffect("fs.armor.light_walk", SoundCategory.PLAYERS);
	public static final SoundEffect MEDIUM_ARMOR_EQUIP = new SoundEffect("fs.armor.medium_walk", SoundCategory.PLAYERS);
	public static final SoundEffect HEAVY_ARMOR_EQUIP = new SoundEffect("fs.armor.heavy_walk", SoundCategory.PLAYERS);
	public static final SoundEffect CRYSTAL_ARMOR_EQUIP = new SoundEffect("fs.armor.crystal_walk", SoundCategory.PLAYERS);
	
	public static final SoundEffect THUNDER = new SoundEffect("thunder", SoundCategory.WEATHER).setVolume(10000F);

	public static final SoundEffect WATER_DROP = new SoundEffect("waterdrops", SoundCategory.AMBIENT);
	public static final SoundEffect WATER_DRIP = new SoundEffect("waterdrips", SoundCategory.AMBIENT);
	public static final SoundEffect STEAM_HISS = new SoundEffect(new ResourceLocation("block.fire.extinguish"),
			SoundCategory.AMBIENT, 0.1F, 1.0F);

	public static final SoundEffect FIRE = new SoundEffect(new ResourceLocation("minecraft:block.fire.ambient"),
			SoundCategory.BLOCKS);

	public static final SoundEffect SPLASH = new SoundEffect("waterfall", SoundCategory.AMBIENT);

}
