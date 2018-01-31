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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.entity.Entity;
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
public class RayTrace {

	protected final Entity entity;

	public RayTrace(@Nonnull final Entity entity) {
		this.entity = entity;
	}

	public RayTraceResult trace(float range) {
		return this.trace(range, 1F);
	}

	public RayTraceResult trace(final float range, final float partialTicks) {

		final Entity entity = this.entity;
		final World world = this.entity.getEntityWorld();

		final Vec3d eyes = entity.getPositionEyes(partialTicks);
		final Vec3d look = entity.getLook(partialTicks); // 1.0F?
		final Vec3d rangedLook = eyes.addVector(look.x * range, look.y * range, look.z * range);

		RayTraceResult traceResult = entity.rayTrace(range, partialTicks);

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
		List<Entity> list = world
				.getEntitiesInAABBexcluding(
						entity, entity.getEntityBoundingBox().expand(look.x * range, look.y * range, look.z * range)
								.grow(1.0D, 1.0D, 1.0D),
						Predicates.and(EntitySelectors.NOT_SPECTATING, new Predicate<Entity>() {
							public boolean apply(@Nullable final Entity e) {
								return e != null && e.canBeCollidedWith();
							}
						}));

		double d2 = range1;

		for (int j = 0; j < list.size(); ++j) {
			final Entity entity1 = list.get(j);
			final AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox()
					.grow((double) entity1.getCollisionBorderSize());
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

}
