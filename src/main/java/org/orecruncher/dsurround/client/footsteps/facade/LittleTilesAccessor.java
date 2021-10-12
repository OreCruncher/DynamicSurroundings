/* This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
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
package org.orecruncher.dsurround.client.footsteps.facade;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.client.footsteps.Generator;
import org.orecruncher.lib.ReflectedField;

import com.creativemd.littletiles.common.api.te.ILittleTileTE;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
final class LittleTilesAccessor implements IFacadeAccessor {

	private static final float RANGE = 0.25F / 2F;
	
	private static final Class<?> BLOCK_CLASS;
	
	static {
		
		Class<?> theClass = ReflectedField.resolveClass("com.creativemd.littletiles.common.blocks.BlockTile");
		if (theClass == null) {
			theClass = ReflectedField.resolveClass("com.creativemd.littletiles.common.block.BlockTile");
		}
		
		BLOCK_CLASS = theClass;
	}

	@Nonnull
	@Override
	public String getName() {
		return "LittleTilesAccessor";
	}

	@Override
	public boolean instanceOf(@Nonnull final Block block) {
		return isValid() && BLOCK_CLASS.isInstance(block);
	}

	@Override
	public boolean isValid() {
		return BLOCK_CLASS != null;
	}

	@Override
	public IBlockState getBlockState(@Nonnull final EntityLivingBase entity, @Nonnull final IBlockState state,
			@Nonnull final IBlockAccess world, @Nonnull final Vec3d pos, @Nullable final EnumFacing side) {
		final BlockPos blockPos = new BlockPos(pos);
		final TileEntity te = world.getTileEntity(blockPos);
		if (te instanceof ILittleTileTE) {
			final ILittleTileTE ltte = (ILittleTileTE) te;
			final Vec3d anchor1 = pos.addVector(-RANGE, Generator.PROBE_DEPTH, -RANGE);
			final Vec3d anchor2 = pos.addVector(RANGE, Generator.PROBE_DEPTH + 0.125D, RANGE);
			final AxisAlignedBB box = new AxisAlignedBB(anchor1, anchor2);
			final IBlockState result = ltte.getState(box, false);
			if (result != null)
				return result;
		}
		return state;
	}

}
