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

package org.blockartistry.DynSurround.client.fx.particle;

import java.util.Random;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.lib.random.XorShiftRandom;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ExplosionHelper {

	private static class Assets {

		public Block[] blocks;
		public ItemStack[] stacks;
		public String[] mobs;

		public Assets setBlocks(final Block... b) {
			this.blocks = b;
			return this;
		}

		public Assets setStacks(final ItemStack... s) {
			this.stacks = s;
			return this;
		}

		public Assets setMobs(final String... m) {
			this.mobs = m;
			return this;
		}

		public Block getBlock(final Random rand) {
			return blocks != null && blocks.length > 0 ? blocks[rand.nextInt(blocks.length)] : null;
		}

		public ItemStack getStack(final Random rand) {
			return stacks != null && stacks.length > 0 ? stacks[rand.nextInt(stacks.length)] : null;
		}

		public String getMob(final Random rand) {
			return mobs != null && mobs.length > 0 ? mobs[rand.nextInt(mobs.length)] : null;
		}
	}

	private static final Assets OVERWORLD = new Assets()
			.setBlocks(Blocks.DIRT, Blocks.COBBLESTONE, Blocks.GRAVEL, Blocks.SAND, Blocks.SAPLING)
			.setStacks(new ItemStack(Items.FLINT), new ItemStack(Items.BRICK), new ItemStack(Items.BONE),
					new ItemStack(Items.STICK), new ItemStack(Items.COAL))
			.setMobs("pig", "sheep", "chicken", "cow", "villager", "wolf", "ocelot");

	private static final Assets NETHER = new Assets()
			.setBlocks(Blocks.NETHERRACK, Blocks.SOUL_SAND, Blocks.GRAVEL, Blocks.MAGMA)
			.setStacks(new ItemStack(Items.NETHER_WART), new ItemStack(Items.GLOWSTONE_DUST),
					new ItemStack(Items.GOLD_NUGGET), new ItemStack(Items.BLAZE_ROD))
			.setMobs("magma_cube", "zombie_pigman", "blaze");

	private static final Assets END = new Assets().setBlocks(Blocks.END_STONE, Blocks.OBSIDIAN)
			.setStacks(new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.ENDER_PEARL))
			.setMobs("enderman", "endermite");

	private ExplosionHelper() {

	}

	@SuppressWarnings("incomplete-switch")
	private static Assets getAssets(@Nonnull final World world) {
		switch (world.provider.getDimensionType()) {
		case NETHER:
			return ExplosionHelper.NETHER;
		case THE_END:
			return ExplosionHelper.END;
		}
		return ExplosionHelper.OVERWORLD;
	}

	private static ParticleAsset getParticle(@Nonnull final World world, final double x, final double y,
			final double z) {
		final Random rand = XorShiftRandom.current();
		final Assets assets = getAssets(world);

		final float motionX = rand.nextFloat() * 10.0F - 5.0F;
		final float motionZ = rand.nextFloat() * 10.0F - 5.0F;
		final float motionY = rand.nextFloat() * 6.0F + 6.0F;

		final int choice = rand.nextInt(20);
		if (choice < 3 && ModOptions.explosions.addMobParticles) {
			final String mob = assets.getMob(rand);
			if (StringUtils.isEmpty(mob))
				return null;
			final ParticleEntity pe = new ParticleEntity(mob, world, x, y, z, motionX, motionY, motionZ);
			pe.setScale(1.0F);
			pe.setMaxAge(75);
			pe.setPitchRate(18 + rand.nextFloat() * 18);
			pe.setYawRate(18 + rand.nextFloat() * 18);
			pe.setGravity(0.25F);
			return pe;
		} else if (choice < 8) {
			final ItemStack stack = assets.getStack(rand);
			if (stack == null)
				return null;
			final ParticleItemStack s = new ParticleItemStack(stack, world, x, y, z, motionX, motionY, motionZ);
			s.setScale(0.5F);
			s.setMaxAge(75);
			s.setPitchRate(18 + rand.nextFloat() * 18);
			s.setYawRate(18 + rand.nextFloat() * 18);
			s.setGravity(0.25F);
			return s;
		} else {
			final Block block = assets.getBlock(rand);
			if (block == null)
				return null;
			final ParticleBlock p = new ParticleBlock(block, world, x, y, z, motionX, motionY, motionZ);
			p.setScale(0.05F + 0.10F * rand.nextFloat());
			p.setMaxAge(75);
			p.setPitchRate(18 + rand.nextFloat() * 18);
			p.setYawRate(18 + rand.nextFloat() * 18);
			p.setGravity(0.25F);
			return p;
		}

	}

	public static void doExplosion(@Nonnull final World world, final double x, final double y, final double z) {

		for (int i = 0; i < 4; i++) {
			final ParticleAsset particle = getParticle(world, x, y, z);
			if (particle != null)
				ParticleHelper.addParticle(particle);
		}
	}

}
