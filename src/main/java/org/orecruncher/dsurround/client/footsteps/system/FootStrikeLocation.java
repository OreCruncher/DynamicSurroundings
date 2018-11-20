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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.client.footsteps.system.facade.FacadeHelper;
import org.orecruncher.dsurround.registry.RegistryManager;
import org.orecruncher.lib.WorldUtils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class FootStrikeLocation {

	private final EntityLivingBase entity;
	private final Vec3d strike;
	private final BlockPos stepPos;

	public FootStrikeLocation(@Nonnull final EntityLivingBase entity, final double x, final double y, final double z) {
		this(entity, new Vec3d(x, y, z));
	}

	public FootStrikeLocation(@Nonnull final EntityLivingBase entity, @Nonnull final Vec3d loc) {
		this.entity = entity;
		this.strike = loc;
		this.stepPos = new BlockPos(loc);
	}

	protected FootStrikeLocation(@Nonnull final EntityLivingBase entity, @Nonnull final Vec3d loc,
			@Nonnull final BlockPos pos) {
		this.entity = entity;
		this.strike = loc;
		this.stepPos = pos;
	}

	public FootStrikeLocation rebase(@Nonnull final BlockPos pos) {
		if (!this.stepPos.equals(pos)) {
			return new FootStrikeLocation(this.entity, this.strike, pos);
		}
		return this;
	}

	@Nonnull
	public EntityLivingBase getEntity() {
		return this.entity;
	}

	@Nonnull
	public BlockPos getStepPos() {
		return this.stepPos;
	}

	@Nonnull
	public Vec3d getStrikePosition() {
		return this.strike;
	}

	public Vec3d north() {
		return offset(EnumFacing.NORTH, 1);
	}
	
	public Vec3d south() {
		return offset(EnumFacing.SOUTH, 1);
	}
	
	public Vec3d east() {
		return offset(EnumFacing.EAST, 1);
	}
	
	public Vec3d west() {
		return offset(EnumFacing.WEST, 1);
	}
	
	public Vec3d up() {
		return offset(EnumFacing.UP, 1);
	}
	
	public Vec3d down() {
		return offset(EnumFacing.DOWN, 1);
	}
	
	/**
	 * Offsets this strike position n blocks in the given direction
	 */
	@Nonnull
	public Vec3d offset(@Nonnull final EnumFacing facing, final float n) {
		return n == 0 ? this.strike
				: new Vec3d(this.strike.x + facing.getXOffset() * n, this.strike.y + facing.getYOffset() * n,
						this.strike.z + facing.getZOffset() * n);
	}

	/**
	 * Determines the actual footprint location based on the BlockPos provided. The
	 * print is to ride on top of the bounding box. If the block does not have a
	 * print a null is returned.
	 *
	 * @param entity The Entity generating the print
	 * @param pos    The block position where the footprint is to be placed on top
	 * @param xx     Calculated foot position for X
	 * @param zz     Calculated foot position for Z
	 * @return Vector containing footprint coordinates or null if no footprint is to
	 *         be generated
	 */
	@Nullable
	protected Vec3d footprintPosition() {
		final World world = this.entity.getEntityWorld();
		final IBlockState state = WorldUtils.getBlockState(world, this.stepPos);
		if (hasFootstepImprint(world, state, this.strike)) {
			final double entityY = this.entity.getEntityBoundingBox().minY;
			final double blockY = getBoundingBoxY(entityY, world, state, this.stepPos);
			return new Vec3d(this.strike.x, Math.max(entityY, blockY), this.strike.z);

		}
		return null;
	}
	
	protected double getBoundingBoxY(final double baseY, @Nonnull final IBlockAccess world, @Nonnull final IBlockState state, @Nonnull final BlockPos pos) {
		final AxisAlignedBB bounding = state.getBoundingBox(world, pos);
		final AxisAlignedBB collision = state.getCollisionBoundingBox(world, pos);
		if (bounding.maxY == collision.maxY)
			return baseY;
		return Math.max(baseY, pos.getY() + Math.max(bounding.maxY, collision.maxY));
	}

	protected boolean hasFootstepImprint(@Nonnull final World world, @Nullable final IBlockState state,
			@Nonnull final Vec3d pos) {
		final IBlockState footstepState = FacadeHelper.resolveState(this.entity, state, world, pos, EnumFacing.UP);
		return RegistryManager.FOOTSTEPS.hasFootprint(footstepState);
	}
}
