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

package org.blockartistry.DynSurround.client.fx;

import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.DynSurround.api.effects.BlockEffectType;
import org.blockartistry.DynSurround.expression.ExpressionEngine;
import org.blockartistry.lib.BlockStateProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class BlockEffect implements ISpecialEffect {
	
	private int chance;
	protected String conditions = StringUtils.EMPTY;

	public BlockEffect() {
		this(100);
	}

	protected BlockEffect(final int chance) {
		this.chance = chance;
	}

	@Nonnull
	public abstract BlockEffectType getEffectType();

	public void setConditions(@Nullable final String conditions) {
		this.conditions = conditions == null ? StringUtils.EMPTY : conditions.intern();
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

	public boolean alwaysExecute() {
		return this.chance == 0;
	}

	/**
	 * Determines if the effect can trigger. Classes that override this method
	 * should make sure to call the parent last to avoid necessary CPU churn related
	 * to the script check.
	 */
	@Override
	public boolean canTrigger(@Nonnull final BlockStateProvider provider, @Nonnull final IBlockState state,
			@Nonnull final BlockPos pos, @Nonnull final Random random) {
		if (!alwaysExecute() && random.nextInt(getChance()) != 0)
			return false;

		return ExpressionEngine.instance().check(getConditions());
	}

	/**
	 * Override to provide the body of the effect that is to take place.
	 */
	@Override
	public abstract void doEffect(@Nonnull final BlockStateProvider provider, @Nonnull final IBlockState state,
			@Nonnull final BlockPos pos, @Nonnull final Random random);

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