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

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.mod.DynSurround.client.footsteps.implem.AcousticsManager;
import org.blockartistry.mod.DynSurround.client.footsteps.interfaces.IAcoustic;
import org.blockartistry.mod.DynSurround.util.MCHelper;
import org.blockartistry.mod.DynSurround.util.MyUtils;

import gnu.trove.set.hash.THashSet;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Association {
	
	private static final Set<Material> FOOTPRINTABLE = new THashSet<Material>();
	static {
		FOOTPRINTABLE.add(Material.CLAY);
		FOOTPRINTABLE.add(Material.GRASS);
		FOOTPRINTABLE.add(Material.GROUND);
		FOOTPRINTABLE.add(Material.ICE);
		FOOTPRINTABLE.add(Material.SAND);
		FOOTPRINTABLE.add(Material.CRAFTED_SNOW);
		FOOTPRINTABLE.add(Material.SNOW);
	}

	private final IBlockState state;
	private final BlockPos pos;
	private IAcoustic[] data;
	
	private Vec3d stepLoc;
	private boolean isRightFoot;
	private float rotation;

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
		this.data = association == null ? AcousticsManager.EMPTY : association;
	}

	@Nonnull
	public IAcoustic[] getData() {
		return this.data;
	}

	@Nonnull
	public boolean getNoAssociation() {
		return this.data.length == 0;
	}

	public boolean isLiquid() {
		return this.state != null && this.state.getMaterial().isLiquid();
	}
	
	public boolean hasFootstepImprint() {
		return this.state != null && FOOTPRINTABLE.contains(this.state.getMaterial());
	}
	
	@Nullable
	public Vec3d getStepLocation() {
		return this.stepLoc;
	}
	
	public boolean isRightFoot() {
		return this.isRightFoot;
	}
	
	public float getRotation() {
		return this.rotation;
	}

	public SoundType getSoundType() {
		return this.state != null ? MCHelper.getSoundType(this.state) : null;
	}

	public void add(@Nonnull final IAcoustic acoustics) {
		this.data = MyUtils.append(this.data, acoustics);
	}
	
	public void setStepLocation(@Nonnull final Vec3d stepLoc, final float rotation, final boolean rightFoot) {
		this.stepLoc = stepLoc;
		this.rotation = rotation;
		this.isRightFoot = rightFoot;
	}

	@Nonnull
	public BlockPos getPos() {
		return this.pos;
	}

	public boolean isNotEmitter() {
		return this.data == AcousticsManager.NOT_EMITTER;
	}
}