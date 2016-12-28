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

package org.blockartistry.mod.DynSurround.client.fx;

import java.util.Random;

import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleBubbleJet;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleDustJet;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleFireJet;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleFountainJet;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleJet;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleSteamJet;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleWaterSplash;
import org.blockartistry.mod.DynSurround.client.handlers.ParticleSystemHandler;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class JetEffect extends BlockEffect {

	private static final int MAX_STRENGTH = 10;

	protected final BlockPos.MutableBlockPos pos1 = new BlockPos.MutableBlockPos();

	protected int countBlocks(final World world, final BlockPos pos, final IBlockState state, final int dir) {
		int count = 0;
		int idx = pos.getY();
		while (count < MAX_STRENGTH) {
			if (world.getBlockState(pos1.setPos(pos.getX(), idx, pos.getZ())).getBlock() != state.getBlock())
				return count;
			count++;
			idx += dir;
		}
		return count;
	}

	// Takes into account partial blocks because of flow
	private static double jetSpawnHeight(final IBlockState state, final BlockPos pos) {
		final int meta = state.getBlock().getMetaFromState(state);
		return 1.1D - BlockLiquid.getLiquidHeightPercent(meta) + pos.getY();
	}

	public JetEffect(final int chance) {
		super(chance);
	}

	protected void addEffect(final ParticleJet fx) {
		ParticleSystemHandler.INSTANCE.addSystem(fx);
	}

	@Override
	public boolean trigger(final IBlockState state, final World world, final BlockPos pos, final Random random) {
		return ParticleSystemHandler.INSTANCE.okToSpawn(pos) && super.trigger(state, world, pos, random);
	}

	public static class Fire extends JetEffect {
		public Fire(final int chance) {
			super(chance);
		}

		@Override
		public boolean trigger(final IBlockState state, final World world, final BlockPos pos, final Random random) {
			return super.trigger(state, world, pos, random) && world.isAirBlock(pos.up());
		}

		public void doEffect(final IBlockState state, final World world, final BlockPos pos, final Random random) {
			final int lavaBlocks = countBlocks(world, pos, state, -1);
			final double spawnHeight = jetSpawnHeight(state, pos);
			final ParticleJet effect = new ParticleFireJet(lavaBlocks, world, pos.getX() + 0.5D, spawnHeight,
					pos.getZ() + 0.5D);
			addEffect(effect);
		}
	}

	public static class Bubble extends JetEffect {
		public Bubble(final int chance) {
			super(chance);
		}

		@Override
		public boolean trigger(final IBlockState state, final World world, final BlockPos pos, final Random random) {
			return super.trigger(state, world, pos, random) && world.getBlockState(pos.down()).getMaterial().isSolid();
		}

		public void doEffect(final IBlockState state, final World world, final BlockPos pos, final Random random) {
			final int waterBlocks = countBlocks(world, pos, state, 1);
			final ParticleJet effect = new ParticleBubbleJet(waterBlocks, world, pos.getX() + 0.5D, pos.getY() + 0.1D,
					pos.getZ() + 0.5D);
			addEffect(effect);
		}
	}

	public static class Steam extends JetEffect {

		public Steam(final int chance) {
			super(chance);
		}

		protected int lavaCount(final World world, final BlockPos pos) {
			int blockCount = 0;
			for (int i = -1; i <= 1; i++)
				for (int j = -1; j <= 1; j++)
					for (int k = -1; k <= 1; k++) {
						if (world.getBlockState(pos.add(i, j, k)).getBlock() == Blocks.LAVA)
							blockCount++;
					}
			return blockCount;
		}

		@Override
		public boolean trigger(final IBlockState state, final World world, final BlockPos pos, final Random random) {
			if (!super.trigger(state, world, pos, random) || !world.isAirBlock(pos.up()))
				return false;

			return lavaCount(world, pos) != 0;
		}

		public void doEffect(final IBlockState state, final World world, final BlockPos pos, final Random random) {
			final int strength = lavaCount(world, pos);
			final double spawnHeight = jetSpawnHeight(state, pos);
			final ParticleJet effect = new ParticleSteamJet(strength, world, pos.getX() + 0.5D, spawnHeight,
					pos.getZ() + 0.5D);
			addEffect(effect);
		}
	}

	public static class Dust extends JetEffect {

		public Dust(final int chance) {
			super(chance);
		}

		@Override
		public boolean trigger(final IBlockState state, final World world, final BlockPos pos, final Random random) {
			return super.trigger(state, world, pos, random) && world.isAirBlock(pos.down());
		}

		public void doEffect(final IBlockState state, final World world, final BlockPos pos, final Random random) {
			final ParticleJet effect = new ParticleDustJet(2, world, pos.getX() + 0.5D, pos.getY() - 0.2D,
					pos.getZ() + 0.5D, state);
			addEffect(effect);
		}
	}

	public static class Fountain extends JetEffect {
		public Fountain(final int chance) {
			super(chance);
		}

		@Override
		public boolean trigger(final IBlockState state, final World world, final BlockPos pos, final Random random) {
			return super.trigger(state, world, pos, random) && world.isAirBlock(pos.up());
		}

		public void doEffect(final IBlockState state, final World world, final BlockPos pos, final Random random) {
			final ParticleJet effect = new ParticleFountainJet(5, world, pos.getX() + 0.5D, pos.getY() + 1.1D,
					pos.getZ() + 0.5D, state);
			addEffect(effect);
		}

	}

	public static class WaterSplash extends JetEffect {
		public WaterSplash(final int chance) {
			super(chance);
		}

		private static boolean isLiquidBlock(final Block block) {
			return BlockLiquid.class.isAssignableFrom(block.getClass());
		}

		private static boolean partialLiquidOrAir(final World world, final BlockPos pos) {
			final IBlockState state = world.getBlockState(pos);
			if (state.getMaterial() == Material.AIR)
				return true;

			if (!isLiquidBlock(state.getBlock()))
				return false;

			// getLiquidHeightPercent() returns the percentage of *air* in the block,
			// not how much water is in it.
			final float height = BlockLiquid.getLiquidHeightPercent(state.getBlock().getMetaFromState(state));
			return height >= 0.12F;
		}

		private static boolean isUnboundedLiquid(final World world, final BlockPos pos) {
			return partialLiquidOrAir(world, pos.north()) || partialLiquidOrAir(world, pos.south())
					|| partialLiquidOrAir(world, pos.east()) || partialLiquidOrAir(world, pos.west());
		}

		private int liquidBlockCount(final World world, final BlockPos pos) {
			BlockPos workBlock = pos;

			int count;
			for (count = 0; count < MAX_STRENGTH; count++) {
				final IBlockState state = world.getBlockState(workBlock);
				if (!isLiquidBlock(state.getBlock()) || !isUnboundedLiquid(world, workBlock))
					break;
				workBlock = workBlock.up();
			}

			return count;
		}

		public static boolean isValidSpawnBlock(final World world, final BlockPos pos) {
			if (world.getBlockState(pos).getMaterial() != Material.WATER)
				return false;
			final boolean unbounded = isUnboundedLiquid(world, pos);
			return (unbounded && world.getBlockState(pos.down()).getMaterial().isSolid())
					|| (!unbounded && world.getBlockState(pos.up()).getBlock() instanceof BlockDynamicLiquid);
		}

		@Override
		public boolean trigger(final IBlockState state, final World world, final BlockPos pos, final Random random) {
			if (!(state.getBlock() instanceof BlockLiquid) || !super.trigger(state, world, pos, random))
				return false;

			return isValidSpawnBlock(world, pos);
		}

		public void doEffect(final IBlockState state, final World world, final BlockPos pos, final Random random) {
			final boolean isUnbounded = isUnboundedLiquid(world, pos);
			final int strength = liquidBlockCount(world, pos.up()) + (isUnbounded ? 1 : 0);

			// Not sure how this can happen. Appears to happen
			// when water blocks are receding.
			if (strength <= 0)
				return;

			final float height = BlockLiquid.getLiquidHeightPercent(state.getBlock().getMetaFromState(state));
			final double y;
			if (isUnbounded && height < 0.8)
				y = pos.getY() + height + 0.1D;
			else if (!isUnbounded)
				y = pos.getY() + 1.1D;
			else
				y = pos.getY() + 0.1D;

			final ParticleJet effect = new ParticleWaterSplash(strength, world, pos.getX() + 0.5D, y,
					pos.getZ() + 0.5D);
			addEffect(effect);
		}
	}
}
