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
package org.orecruncher.dsurround.client.footsteps.system.facade;

import javax.annotation.Nonnull;

import com.creativemd.littletiles.common.api.te.ILittleTileTE;
import com.creativemd.littletiles.common.blocks.BlockTile;

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

	@Override
	public String getName() {
		return "LittleTilesAccessor";
	}

	@Override
	public boolean instanceOf(@Nonnull final Block block) {
		return isValid() && block instanceof BlockTile;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public IBlockState getBlockState(@Nonnull final EntityLivingBase entity, @Nonnull final IBlockState state,
			@Nonnull final IBlockAccess world, @Nonnull final Vec3d pos, @Nonnull final EnumFacing side) {
		final BlockPos blockPos = new BlockPos(pos);
		final TileEntity te = world.getTileEntity(blockPos);
		if (te instanceof ILittleTileTE) {
			final ILittleTileTE ltte = (ILittleTileTE) te;
			final Vec3d anchor1 = pos.add(-0.25, 0, -0.25);
			final Vec3d anchor2 = pos.add(0.25, 0, 0.25);
			final AxisAlignedBB box = new AxisAlignedBB(anchor1, anchor2);
			final IBlockState result = ltte.getState(box, false);
			if (result != null)
				return result;
		}
		return state;
	}

}
