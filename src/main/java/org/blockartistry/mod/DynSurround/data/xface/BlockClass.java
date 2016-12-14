/* This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
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

package org.blockartistry.mod.DynSurround.data.xface;

import java.util.HashMap;
import java.util.Map;

public enum BlockClass {

	NOT_EMITTER("NOT_EMITTER"),
	MESSYGROUND("MESSY_GROUND"),
	SWIM("_SWIM"),

	STRAW("straw"),
	BRUSHSTRAWTRANSITION("brush_straw_transition"),
	BRUSH("brush"),
	FIRE("fire"),
	RAILS("rails"),
	GRASS("grass"),
	ORGANICSOLID("organic_solid"),
	ORGANICDRY("organic_dry"),
	DIRT("dirt"),
	STONE("stone"),
	WOOD("wood"),
	BEDROCK("bedrock"),
	SAND("sand"),
	GRAVEL("gravel"),
	ORE("ore"),
	LEAVES("leaves"),
	MUD("mud"),
	GLASS("glass"),
	GLOWSTONE("glowstone"),
	COMPOSITE("composite"),
	STONEMACHINE("stonemachine"),
	SANDSTONE("sandstone"),
	WOODUTILITY("woodutility"),
	RUG("rug"),
	WOODSTICKY("wood_sticky"),
	HARDMETAL("hardmetal"),
	BRICKSTONE("brickstone"),
	MARBLE("marble"),
	EQUIPMENT("equipment"),
	OBSIDIAN("obsidian"),
	METALBAR("metalbar"),
	SQUEAKYWOOD("squeakywood"),
	BLUNTWOOD("bluntwood"),
	LADDER("ladder"),
	LADDERDEFAULT("ladder_default"),
	SNOW("snow"),
	ICE("ice"),
	ORGANIC("organic"),
	QUICKSAND("quicksand"),
	WATERFINE("waterfine"),
	STONEUTILITY("stoneutility"),
	METALCOMPRESSED("metalcompressed"),
	METALSUBPARTS("metalsubparts"),
	
	ANVIL("metalcompressed,hardmetal"),
	DAYLIGHTDETECTOR("stoneutility,glass"),
	PACKEDICE("stone,snow"),
	STANDINGBANNER("rug,straw,squeakywood"),
	WALLBANNER("rug,straw"),
	
	SAPLINGS("#sapling"),
	REED("#reed"),
	WHEAT("#wheat"),
	CROP("#crop"),
	FENCE("#fence");
	
	protected String name;
	private BlockClass(final String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	private static final Map<String,BlockClass> lookup = new HashMap<String,BlockClass>();
	public static BlockClass lookup(final String name) {
		if(lookup.size() == 0) {
			for(final BlockClass bc: BlockClass.values())
				lookup.put(bc.name, bc);
		}
		
		return lookup.get(name);
	}
}
