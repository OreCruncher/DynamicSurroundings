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

package org.blockartistry.mod.DynSurround.client.footsteps.system;

import java.util.List;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.client.footsteps.implem.AcousticsManager;
import org.blockartistry.mod.DynSurround.client.footsteps.interfaces.IAcoustic;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Association {

	private IBlockState state;
	private BlockPos pos;

	private List<IAcoustic> data = null;

	private boolean noAssociation = false;
	private boolean isPrimative = false;

	public Association() {
	}

	public Association(@Nonnull final IBlockState state, @Nonnull final BlockPos pos) {
		this.state = state;
		this.pos = pos;
	}

	@Nonnull
	public List<IAcoustic> getData() {
		return this.data;
	}

	@Nonnull
	public Association setAssociation(@Nonnull final List<IAcoustic> association) {
		this.data = association;
		this.noAssociation = false;
		return this;
	}

	@Nonnull
	public Association setNoAssociation() {
		this.noAssociation = true;
		return this;
	}

	@Nonnull
	public boolean getNoAssociation() {
		return this.noAssociation;
	}

	@Nonnull
	public Association setPrimitive(@Nonnull final List<IAcoustic> primative) {
		this.data = primative;
		this.isPrimative = true;
		return this;
	}

	public boolean isPrimative() {
		return this.isPrimative;
	}

	public Block getBlock() {
		return this.state.getBlock();
	}

	@Nonnull
	public IBlockState getState() {
		return this.state;
	}

	@Nonnull
	public BlockPos getPos() {
		return this.pos;
	}

	public boolean isNotEmitter() {
		return this.data == AcousticsManager.NOT_EMITTER;
	}
}