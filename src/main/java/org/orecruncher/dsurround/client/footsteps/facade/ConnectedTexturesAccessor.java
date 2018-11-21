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

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.chisel.ctm.api.IFacade;

@SideOnly(Side.CLIENT)
final class ConnectedTexturesAccessor implements IFacadeAccessor {

	@Override
	public String getName() {
		return "ConnectedTexturesAccessor";
	}

	@Override
	public boolean instanceOf(Block block) {
		return isValid() && block instanceof IFacade;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	@Nonnull
	public IBlockState getBlockState(@Nonnull final EntityLivingBase entity, @Nonnull final IBlockState state,
			@Nonnull final IBlockAccess world, @Nonnull final Vec3d pos, @Nonnull final EnumFacing side) {
		if (state.getBlock() instanceof IFacade) {
			final IFacade facade = (IFacade) state.getBlock();
			final BlockPos blockPos = new BlockPos(pos);
			final IBlockState result = facade.getFacade(entity.getEntityWorld(), blockPos, side, blockPos);
			if (result != null)
				return result;
		}
		return state;
	}

}
