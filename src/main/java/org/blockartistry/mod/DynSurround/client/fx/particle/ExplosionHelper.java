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

package org.blockartistry.mod.DynSurround.client.fx.particle;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public final class ExplosionHelper {

	private static final Block[] overWorldTypes = { Blocks.DIRT, Blocks.COBBLESTONE, Blocks.GRAVEL, Blocks.SAND };
	private static final ItemStack[] overWorldDebris = { new ItemStack(Items.APPLE), new ItemStack(Items.BED),
			new ItemStack(Items.BONE), new ItemStack(Items.BAKED_POTATO), new ItemStack(Items.COAL) };

	private static final Block[] netherTypes = { Blocks.NETHERRACK, Blocks.SOUL_SAND, Blocks.GRAVEL, Blocks.MAGMA };
	private static final ItemStack[] netherDebris = { new ItemStack(Items.NETHER_WART),
			new ItemStack(Items.GLOWSTONE_DUST), new ItemStack(Items.GOLD_NUGGET) };

	private static final Block[] endTypes = { Blocks.END_STONE, Blocks.OBSIDIAN };
	private static final ItemStack[] endDebris = { new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.ENDER_PEARL) };

	private ExplosionHelper() {

	}

	@SuppressWarnings("incomplete-switch")
	private static Block[] getAppropriateBlocks(@Nonnull final World world) {
		switch (world.provider.getDimensionType()) {
		case THE_END:
			return endTypes;
		case NETHER:
			return netherTypes;
		}
		return overWorldTypes;
	}

	@SuppressWarnings("incomplete-switch")
	private static ItemStack[] getAppropriateItemStacks(@Nonnull final World world) {
		switch (world.provider.getDimensionType()) {
		case THE_END:
			return endDebris;
		case NETHER:
			return netherDebris;
		}
		return overWorldDebris;
	}

	public static void doExplosion(@Nonnull final World world, final double x, final double y, final double z) {

		final Random rand = ThreadLocalRandom.current();
		final Block[] candidates = getAppropriateBlocks(world);
		final ItemStack[] stacks = getAppropriateItemStacks(world);

		for (int i = 0; i < 7; i++) {
			final float motionX = rand.nextFloat() * 10.0F - 5.0F;
			final float motionZ = rand.nextFloat() * 10.0F - 5.0F;
			final float motionY = rand.nextFloat() * 6.0F + 6.0F;

			final Block block = candidates[rand.nextInt(candidates.length)];

			final ParticleBlock p = new ParticleBlock(block, world, x, y, z, motionX, motionY, motionZ);
			p.setScale(0.05F + 0.10F * rand.nextFloat());
			p.setMaxAge(100);
			p.setPitchRate(18 + rand.nextFloat() * 18);
			p.setYawRate(18 + rand.nextFloat() * 18);
			p.setGravity(0.25F);
			ParticleHelper.addParticle(p);

			if (rand.nextInt(5) == 0) {
				final ItemStack stack = stacks[rand.nextInt(stacks.length)];
				final ParticleItemStack s = new ParticleItemStack(stack, world, x, y, z, motionX, motionY, motionZ);
				s.setScale(0.5F);
				s.setMaxAge(100);
				s.setPitchRate(18 + rand.nextFloat() * 18);
				s.setYawRate(18 + rand.nextFloat() * 18);
				s.setGravity(0.25F);
				ParticleHelper.addParticle(s);
			}
		}
	}

}
