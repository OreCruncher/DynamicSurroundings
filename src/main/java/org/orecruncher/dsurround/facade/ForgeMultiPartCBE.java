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

package org.orecruncher.dsurround.facade;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class ForgeMultiPartCBE implements IFacadeAccessor {

	protected Class<?> multipartBlock;
	protected Method rayTrace;
	protected Method getPart;
	protected Class<?> partTraceResult;
	protected Field partNumber;
	protected Class<?> microBlock;
	protected Field material;
	protected Class<?> materialManager;
	protected Method getMaterial;
	protected Class<?> blockMicroMaterial;
	protected Field blockState;

	public ForgeMultiPartCBE() {
		try {
			this.multipartBlock = Class.forName("codechicken.multipart.BlockMultipart");
			this.rayTrace = ReflectionHelper.findMethod(this.multipartBlock, "collisionRayTrace", null,
					IBlockState.class, World.class, BlockPos.class, Vec3d.class, Vec3d.class);
			this.getPart = ReflectionHelper.findMethod(this.multipartBlock, "getPart", null, IBlockAccess.class,
					BlockPos.class, int.class);
			this.partTraceResult = Class.forName("codechicken.multipart.PartRayTraceResult");
			this.partNumber = ReflectionHelper.findField(this.partTraceResult, "partIndex");
			this.microBlock = Class.forName("codechicken.microblock.Microblock");
			this.material = ReflectionHelper.findField(this.microBlock, "material");
			this.materialManager = Class.forName("codechicken.microblock.MicroMaterialRegistry");
			this.getMaterial = ReflectionHelper.findMethod(this.materialManager, "getMaterial", null, int.class);
			this.blockMicroMaterial = Class.forName("codechicken.microblock.BlockMicroMaterial");
			this.blockState = ReflectionHelper.findField(this.blockMicroMaterial, "state");
		} catch (@Nonnull final Throwable t) {
			this.multipartBlock = null;
			this.rayTrace = null;
		}
	}

	@Override
	public String getName() {
		return "ForgeMultiPartCBE";
	}

	@Override
	public boolean instanceOf(Block block) {
		return isValid() && this.multipartBlock.isInstance(block);
	}

	@Override
	public boolean isValid() {
		return this.multipartBlock != null;
	}

	@Override
	public IBlockState getBlockState(@Nonnull final EntityLivingBase entity, @Nonnull final IBlockState state,
			@Nonnull final IBlockAccess world, @Nonnull final BlockPos pos, @Nonnull final EnumFacing side) {
		try {
			final Vec3d vec = entity.getPositionVector();
			final Object result = this.rayTrace.invoke(state.getBlock(), state, entity.getEntityWorld(), pos, vec,
					vec.add(0, -1, 0));
			if (result != null) {
				final int partIdx = this.partNumber.getInt(result);
				final Object part = this.getPart.invoke(state.getBlock(), world, pos, partIdx);
				if (part != null) {
					final int materialId = this.material.getInt(part);
					final Object material = this.getMaterial.invoke(null, materialId);
					if (material != null) {
						final IBlockState facadeState = (IBlockState) this.blockState.get(material);
						if (facadeState != null)
							return facadeState;
					}
				}
			}
		} catch (@Nonnull final Throwable t) {

		}
		return state;
	}

}
