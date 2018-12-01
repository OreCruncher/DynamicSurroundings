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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.lib.compat.ModEnvironment;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class FacadeHelper {

	private static final Map<Block, IFacadeAccessor> crackers = new Reference2ObjectOpenHashMap<>();

	private static void addAccessor(@Nonnull final List<IFacadeAccessor> accessors,
			@Nonnull final IFacadeAccessor accessor) {
		if (accessor.isValid()) {
			ModBase.log().info("Facade Accessor: %s", accessor.getName());
			accessors.add(accessor);
		}
	}

	static {

		final List<IFacadeAccessor> accessors = new ArrayList<>();

		// Run down the list of supported accessors. The instance will
		// tell us if it is valid or not. Order is important - want to
		// use a mod specific interface before general ones.
		if (ModEnvironment.LittleTiles.isLoaded())
			addAccessor(accessors, new LittleTilesAccessor());
		if (ModEnvironment.ForgeMultipartCBE.isLoaded())
			addAccessor(accessors, new ForgeMultiPartCBE());
		if (ModEnvironment.ConnectedTextures.isLoaded())
			addAccessor(accessors, new ConnectedTexturesAccessor());

		addAccessor(accessors, new EnderIOFacadeAccessor());
		addAccessor(accessors, new CoFHCoreCoverAccessor());

		// Last hail mary - is this even supported anymore?
		addAccessor(accessors, new ChiselFacadeAccessor());

		// Iterate through the block list filling out our cracker list.
		if (accessors.size() > 0) {
			final Iterator<Block> itr = Block.REGISTRY.iterator();
			while (itr.hasNext()) {
				final Block b = itr.next();
				for (int i = 0; i < accessors.size(); i++) {
					final IFacadeAccessor accessor = accessors.get(i);
					if (accessor.instanceOf(b)) {
						crackers.put(b, accessor);
						break;
					}
				}
			}
		}

	}

	protected FacadeHelper() {

	}

	@Nonnull
	public static IBlockState resolveState(@Nonnull final EntityLivingBase entity, @Nonnull final IBlockState state,
			@Nonnull final IBlockAccess world, @Nonnull final Vec3d pos, @Nullable final EnumFacing side) {
		if (crackers.size() > 0 && state != Blocks.AIR.getDefaultState()) {
			final IFacadeAccessor accessor = crackers.get(state.getBlock());
			if (accessor != null) {
				final IBlockState newState = accessor.getBlockState(entity, state, world, pos, side);
				if (newState != null)
					return newState;
			}
		}
		return state;
	}

}
