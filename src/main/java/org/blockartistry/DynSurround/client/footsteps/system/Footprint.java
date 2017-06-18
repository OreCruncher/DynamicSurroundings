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

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.facade.FacadeHelper;
import org.blockartistry.lib.WorldUtils;
import org.blockartistry.lib.collections.IdentityHashSet;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Footprint {

	private static final Set<Material> FOOTPRINTABLE = new IdentityHashSet<Material>();
	static {
		FOOTPRINTABLE.add(Material.CLAY);
		FOOTPRINTABLE.add(Material.GRASS);
		FOOTPRINTABLE.add(Material.GROUND);
		FOOTPRINTABLE.add(Material.ICE);
		FOOTPRINTABLE.add(Material.SAND);
		FOOTPRINTABLE.add(Material.CRAFTED_SNOW);
		FOOTPRINTABLE.add(Material.SNOW);
	}

	private Vec3d stepLoc;
	private boolean isRightFoot;
	private float rotation;

	public static boolean hasFootstepImprint(@Nullable final IBlockState state, @Nonnull final BlockPos pos) {
		if (state != null) {
			final IBlockState footstepState = FacadeHelper.resolveState(state, EnvironState.getWorld(), pos,
					EnumFacing.UP);
			return FOOTPRINTABLE.contains(footstepState.getMaterial());
		}
		return false;
	}
	
	public static boolean hasFootstepImprint(@Nonnull final Vec3d pos) {
		final BlockPos blockPos = new BlockPos(pos);
		final IBlockState state = WorldUtils.getBlockState(EnvironState.getWorld(), blockPos);
		if (state != null) {
			return hasFootstepImprint(state, blockPos);
		}
		return false;
	}
	
	public static Footprint produce(@Nonnull final Vec3d stepLoc, final float rotation, final boolean rightFoot) {
		final Footprint print = new Footprint();
		print.stepLoc = stepLoc;
		print.rotation = rotation;
		print.isRightFoot = rightFoot;
		return print;
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

}
