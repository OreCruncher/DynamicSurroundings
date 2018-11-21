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

package org.orecruncher.dsurround.client.footsteps.system;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.registry.acoustics.AcousticRegistry;
import org.orecruncher.dsurround.registry.acoustics.IAcoustic;
import org.orecruncher.lib.MCHelper;
import org.orecruncher.lib.collections.ObjectArray;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Association {

	private final IBlockState state;
	private final FootStrikeLocation location;
	private final ObjectArray<IAcoustic> data = new ObjectArray<>(8);
	private final boolean isNotEmitter;

	public Association() {
		this(AcousticRegistry.EMPTY);
	}

	public Association(@Nonnull final IAcoustic[] association) {
		this(null, null, association);
	}
	
	public Association(@Nonnull final EntityLivingBase entity, @Nonnull final IAcoustic[] association) {
		final Vec3d vec = entity.getPositionVector();
		this.state = null;
		this.location = new FootStrikeLocation(entity, vec.x, vec.y + 1, vec.z);;
		this.data.addAll(association == null ? AcousticRegistry.EMPTY : association);
		this.isNotEmitter = association == AcousticRegistry.NOT_EMITTER;
	}

	public Association(@Nonnull final IBlockState state, @Nonnull final FootStrikeLocation pos) {
		this(state, pos, AcousticRegistry.EMPTY);
	}

	public Association(@Nonnull final IBlockState state, @Nonnull final FootStrikeLocation pos,
			@Nonnull final IAcoustic[] association) {
		this.state = state;
		this.location = pos;
		this.data.addAll(association == null ? AcousticRegistry.EMPTY : association);
		this.isNotEmitter = association == AcousticRegistry.NOT_EMITTER;
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

	public void add(@Nonnull final Collection<? extends IAcoustic> collection) {
		this.data.addAll(collection);
	}

	@Nonnull
	public FootStrikeLocation getStrikeLocation() {
		return this.location;
	}
	
	public boolean hasStrikeLocation() {
		return this.location != null;
	}
	
	@Nullable
	public BlockPos getStepPos() {
		return this.location != null ? this.location.getStepPos() : null;
	}

	public boolean isNotEmitter() {
		return this.isNotEmitter;
	}

}