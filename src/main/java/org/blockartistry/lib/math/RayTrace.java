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
package org.blockartistry.lib.math;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.lib.WorldUtils;
import org.blockartistry.lib.chunk.IBlockAccessEx;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Helper class to raytrace from an entity in the direction they are looking. It
 * works similar to the getMouseOver() code, but tweaked to be able to plug in
 * any Entity.
 */
public final class RayTrace {

	private RayTrace() {

	}

	public static RayTraceResult trace(@Nonnull final EntityLivingBase entity) {

		final double range;
		if (entity instanceof EntityPlayer) {
			range = entity.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
		} else {
			// From EntityAIAttackMelee::getAttackReachSqr - approximate
			range = entity.width * 2F + 0.6F; // 0.6 == default entity width
		}
		final World world = entity.getEntityWorld();
		final Vec3d eyes = entity.getPositionEyes(1F);
		final Vec3d look = entity.getLook(1F); // 1.0F?
		final Vec3d rangedLook = eyes.addVector(look.x * range, look.y * range, look.z * range);

		RayTraceResult traceResult = rayTraceBlocks(world, eyes, rangedLook, false, false, true);

		Entity pointedEntity = null;
		boolean flag = false;
		double range1 = range;

		if (range > 3.0D) {
			flag = true;
		}

		if (traceResult != null) {
			range1 = traceResult.hitVec.distanceTo(eyes);
		}

		Vec3d hitLocation = null;
		final List<Entity> list = world.getEntitiesInAABBexcluding(entity,
				entity.getEntityBoundingBox().expand(look.x * range, look.y * range, look.z * range).grow(1.0D, 1.0D,
						1.0D),
				Predicates.and(EntitySelectors.NOT_SPECTATING,
						(Predicate<Entity>) (@Nullable final Entity e) -> e != null && e.canBeCollidedWith()));

		double d2 = range1;

		for (int j = 0; j < list.size(); ++j) {
			final Entity entity1 = list.get(j);
			final AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow(entity1.getCollisionBorderSize());
			final RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(eyes, rangedLook);

			if (axisalignedbb.contains(eyes)) {
				if (d2 >= 0.0D) {
					pointedEntity = entity1;
					hitLocation = raytraceresult == null ? eyes : raytraceresult.hitVec;
					d2 = 0.0D;
				}
			} else if (raytraceresult != null) {
				final double d3 = eyes.distanceTo(raytraceresult.hitVec);

				if (d3 < d2 || d2 == 0.0D) {
					if (entity1.getLowestRidingEntity() == entity.getLowestRidingEntity()
							&& !entity1.canRiderInteract()) {
						if (d2 == 0.0D) {
							pointedEntity = entity1;
							hitLocation = raytraceresult.hitVec;
						}
					} else {
						pointedEntity = entity1;
						hitLocation = raytraceresult.hitVec;
						d2 = d3;
					}
				}
			}
		}

		if (pointedEntity != null && flag && eyes.distanceTo(hitLocation) > 3.0D) {
			pointedEntity = null;
			traceResult = new RayTraceResult(RayTraceResult.Type.MISS, hitLocation, (EnumFacing) null,
					new BlockPos(hitLocation));
		}

		if (pointedEntity != null && (d2 < range1 || traceResult == null)) {
			traceResult = new RayTraceResult(pointedEntity, hitLocation);
		}

		return traceResult;
	}

	// From World.rayTraceBlocks()
	@Nullable
	public static RayTraceResult rayTraceBlocks(@Nonnull final World world, Vec3d vec31, Vec3d vec32,
			boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {

		final BlockPos.MutableBlockPos blockpos = new BlockPos.MutableBlockPos();
		final IBlockAccessEx provider = WorldUtils.getDefaultBlockStateProvider();

		if (!Double.isNaN(vec31.x) && !Double.isNaN(vec31.y) && !Double.isNaN(vec31.z)) {
			if (!Double.isNaN(vec32.x) && !Double.isNaN(vec32.y) && !Double.isNaN(vec32.z)) {
				final int i = MathStuff.floor(vec32.x);
				final int j = MathStuff.floor(vec32.y);
				final int k = MathStuff.floor(vec32.z);

				int l = MathStuff.floor(vec31.x);
				int i1 = MathStuff.floor(vec31.y);
				int j1 = MathStuff.floor(vec31.z);

				blockpos.setPos(l, i1, j1);
				IBlockState iblockstate = provider.getBlockState(blockpos);
				Block block = iblockstate.getBlock();

				if ((!ignoreBlockWithoutBoundingBox
						|| iblockstate.getCollisionBoundingBox(world, blockpos) != Block.NULL_AABB)
						&& block.canCollideCheck(iblockstate, stopOnLiquid)) {
					final RayTraceResult raytraceresult = iblockstate.collisionRayTrace(world, blockpos, vec31, vec32);

					if (raytraceresult != null) {
						return raytraceresult;
					}
				}

				RayTraceResult raytraceresult2 = null;
				int k1 = 200;

				while (k1-- >= 0) {
					if (Double.isNaN(vec31.x) || Double.isNaN(vec31.y) || Double.isNaN(vec31.z)) {
						return null;
					}

					if (l == i && i1 == j && j1 == k) {
						return returnLastUncollidableBlock ? raytraceresult2 : null;
					}

					boolean flag2 = true;
					boolean flag = true;
					boolean flag1 = true;
					double d0 = 999.0D;
					double d1 = 999.0D;
					double d2 = 999.0D;

					if (i > l) {
						d0 = l + 1.0D;
					} else if (i < l) {
						d0 = l + 0.0D;
					} else {
						flag2 = false;
					}

					if (j > i1) {
						d1 = i1 + 1.0D;
					} else if (j < i1) {
						d1 = i1 + 0.0D;
					} else {
						flag = false;
					}

					if (k > j1) {
						d2 = j1 + 1.0D;
					} else if (k < j1) {
						d2 = j1 + 0.0D;
					} else {
						flag1 = false;
					}

					double d3 = 999.0D;
					double d4 = 999.0D;
					double d5 = 999.0D;
					final double d6 = vec32.x - vec31.x;
					final double d7 = vec32.y - vec31.y;
					final double d8 = vec32.z - vec31.z;

					if (flag2) {
						d3 = (d0 - vec31.x) / d6;
					}

					if (flag) {
						d4 = (d1 - vec31.y) / d7;
					}

					if (flag1) {
						d5 = (d2 - vec31.z) / d8;
					}

					if (d3 == -0.0D) {
						d3 = -1.0E-4D;
					}

					if (d4 == -0.0D) {
						d4 = -1.0E-4D;
					}

					if (d5 == -0.0D) {
						d5 = -1.0E-4D;
					}

					EnumFacing enumfacing;

					if (d3 < d4 && d3 < d5) {
						enumfacing = i > l ? EnumFacing.WEST : EnumFacing.EAST;
						vec31 = new Vec3d(d0, vec31.y + d7 * d3, vec31.z + d8 * d3);
					} else if (d4 < d5) {
						enumfacing = j > i1 ? EnumFacing.DOWN : EnumFacing.UP;
						vec31 = new Vec3d(vec31.x + d6 * d4, d1, vec31.z + d8 * d4);
					} else {
						enumfacing = k > j1 ? EnumFacing.NORTH : EnumFacing.SOUTH;
						vec31 = new Vec3d(vec31.x + d6 * d5, vec31.y + d7 * d5, d2);
					}

					l = MathStuff.floor(vec31.x) - (enumfacing == EnumFacing.EAST ? 1 : 0);
					i1 = MathStuff.floor(vec31.y) - (enumfacing == EnumFacing.UP ? 1 : 0);
					j1 = MathStuff.floor(vec31.z) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);
					blockpos.setPos(l, i1, j1);
					iblockstate = provider.getBlockState(blockpos);
					block = iblockstate.getBlock();

					if (!ignoreBlockWithoutBoundingBox || iblockstate.getMaterial() == Material.PORTAL
							|| iblockstate.getCollisionBoundingBox(world, blockpos) != Block.NULL_AABB) {
						if (block.canCollideCheck(iblockstate, stopOnLiquid)) {
							final RayTraceResult raytraceresult1 = iblockstate.collisionRayTrace(world, blockpos, vec31,
									vec32);

							if (raytraceresult1 != null) {
								return raytraceresult1;
							}
						} else {
							raytraceresult2 = new RayTraceResult(RayTraceResult.Type.MISS, vec31, enumfacing, blockpos);
						}
					}
				}

				return returnLastUncollidableBlock ? raytraceresult2 : null;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

}
