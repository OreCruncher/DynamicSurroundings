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

package org.blockartistry.DynSurround.client.footsteps.system;

import javax.annotation.Nonnull;
import org.blockartistry.DynSurround.client.footsteps.implem.AcousticsManager;
import org.blockartistry.DynSurround.client.footsteps.interfaces.IAcoustic;
import org.blockartistry.lib.MCHelper;
import org.blockartistry.lib.collections.ObjectArray;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Association {

	private final IBlockState state;
	private final BlockPos pos;
	private final ObjectArray<IAcoustic> data = new ObjectArray<>(8);
	private final boolean isNotEmitter;

	public Association() {
		this(AcousticsManager.EMPTY);
	}

	public Association(@Nonnull final IAcoustic[] association) {
		this(null, null, association);
	}

	public Association(@Nonnull final IBlockState state, @Nonnull final BlockPos pos) {
		this(state, pos, AcousticsManager.EMPTY);
	}

	public Association(@Nonnull final IBlockState state, @Nonnull final BlockPos pos,
			@Nonnull final IAcoustic[] association) {
		this.state = state;
		this.pos = pos;
		this.data.addAll(association == null ? AcousticsManager.EMPTY : association);
		this.isNotEmitter = association == AcousticsManager.NOT_EMITTER;
	}

	@Nonnull
	public IAcoustic[] getData() {
		return this.data.toArray(new IAcoustic[0]);
	}

	@Nonnull
	public boolean getNoAssociation() {
		return this.data.size() == 0;
	}

	public boolean isLiquid() {
		return this.state != null && this.state.getMaterial().isLiquid();
	}

	public SoundType getSoundType() {
		return this.state != null ? MCHelper.getSoundType(this.state) : null;
	}

	public void add(@Nonnull final IAcoustic acoustics) {
		this.data.add(acoustics);
	}

	public void add(@Nonnull final IAcoustic[] acoustics) {
		this.data.addAll(acoustics);
	}

	@Nonnull
	public BlockPos getPos() {
		return this.pos;
	}

	public boolean isNotEmitter() {
		return this.isNotEmitter;
	}

}