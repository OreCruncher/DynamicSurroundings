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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.mod.DynSurround.api.effects.BlockEffectType;
import org.blockartistry.mod.DynSurround.api.events.BlockEffectEvent;
import org.blockartistry.mod.DynSurround.registry.Evaluator;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class BlockEffect {

	private int chance;
	protected String conditions = ".*";

	public BlockEffect() {
		this(100);
	}

	protected BlockEffect(final int chance) {
		this.chance = chance;
	}

	@Nonnull
	public abstract BlockEffectType getEffectType();

	public void setConditions(@Nullable final String conditions) {
		this.conditions = conditions == null ? ".*" : conditions;
	}

	@Nonnull
	public String getConditions() {
		return this.conditions;
	}

	public void setChance(final int chance) {
		this.chance = chance;
	}

	public int getChance() {
		return this.chance;
	}

	public boolean trigger(@Nonnull final IBlockState state, @Nonnull final World world, @Nonnull final BlockPos pos,
			@Nonnull final Random random) {
		if (getChance() > 0 && random.nextInt(getChance()) != 0)
			return false;

		if (Evaluator.check(getConditions())) {
			final BlockEffectEvent event = new BlockEffectEvent(world, getEffectType(), pos);
			return !MinecraftForge.EVENT_BUS.post(event);
		}

		return false;
	}

	public abstract void doEffect(@Nonnull final IBlockState state, @Nonnull final World world,
			@Nonnull final BlockPos pos, @Nonnull final Random random);

	public void process(@Nonnull final IBlockState state, @Nonnull final World world, @Nonnull final BlockPos pos,
			@Nonnull final Random random) {
		if (trigger(state, world, pos, random))
			doEffect(state, world, pos, random);
	}

	@Override
	@Nonnull
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("type: ").append(getEffectType().getName());
		builder.append(" conditions: [").append(getConditions()).append(']');
		builder.append("; chance:").append(getChance());
		builder.append(' ').append(this.getClass().getSimpleName());
		return builder.toString();
	}
}