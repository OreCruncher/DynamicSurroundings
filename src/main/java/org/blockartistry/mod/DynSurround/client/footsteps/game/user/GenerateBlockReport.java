/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
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

package org.blockartistry.mod.DynSurround.client.footsteps.game.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.blockartistry.mod.DynSurround.compat.MCHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.SoundType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GenerateBlockReport {
	private List<String> justNames;
	private List<String> results;

	public GenerateBlockReport() {
		this.justNames = new ArrayList<String>();
		this.results = new ArrayList<String>();

		for (Object o : Block.REGISTRY) {
			Block block = (Block) o;
			String name = MCHelper.nameOf(block);

			// stepSound.stepSoundName
			SoundType soundType = MCHelper.getSoundType(block);
			String soundName;
			if (soundType == null) {
				soundName = "NO_STEP";
			} else if (soundType.getStepSound() == null) {
				soundName = "NO_SOUND";
			} else {
				soundName = soundType.getStepSound().getSoundName().toString();
			}

			if (block instanceof BlockLiquid) {
				soundName += "," + "EXTENDS_LIQUID";
			}
			if (block instanceof BlockBush) {
				soundName += "," + "EXTENDS_BUSH";
			}
			if (block instanceof BlockDoublePlant) {
				soundName += "," + "EXTENDS_DOUBLE_PLANT";
			}
			if (block instanceof BlockCrops) {
				soundName += "," + "EXTENDS_CROPS";
			}
			if (block instanceof BlockContainer) {
				soundName += "," + "EXTENDS_CONTAINER";
			}
			if (block instanceof BlockLeaves) {
				soundName += "," + "EXTENDS_LEAVES";
			}
			if (block instanceof BlockRailBase) {
				soundName += "," + "EXTENDS_RAIL";
			}
			if (block instanceof BlockSlab) {
				soundName += "," + "EXTENDS_SLAB";
			}
			if (block instanceof BlockStairs) {
				soundName += "," + "EXTENDS_STAIRS";
			}
			if (block instanceof BlockBreakable) {
				soundName += "," + "EXTENDS_BREAKABLE";
			}
			if (block instanceof BlockFalling) {
				soundName += "," + "EXTENDS_PHYSICALLY_FALLING";
			}
			if (block instanceof BlockPane) {
				soundName += "," + "EXTENDS_PANE";
			}
			if (block instanceof BlockRotatedPillar) {
				soundName += "," + "EXTENDS_PILLAR";
			}
			if (block instanceof BlockTorch) {
				soundName += "," + "EXTENDS_TORCH";
			}
			/*
			 * if (!block.func_149662_c()) { soundName += "," + "FUNC_POPPABLE";
			 * }
			 */
			if (!MCHelper.isOpaqueCube(block)) {
				soundName += "," + "HITBOX";
			}

			this.justNames.add(name);
			this.results.add(name + " = " + soundName);
		}

		Collections.sort(this.justNames);
		Collections.sort(this.results);
	}

	public List<String> getResults() {
		return this.results;
	}

	public List<String> getBlockNames() {
		return this.justNames;
	}

}
