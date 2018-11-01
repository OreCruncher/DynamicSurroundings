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

package org.orecruncher.dsurround.network;

import javax.annotation.Nonnull;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

/**
 * Custom TargetPoint that contains entity ID as well as serialization
 * capability.
 */
public class Locus extends TargetPoint {

	public final int entityId;

	public Locus(@Nonnull final Entity entity, final double range) {
		this(entity, entity.posX, entity.posY, entity.posZ, range);
	}

	public Locus(@Nonnull final Entity entity, @Nonnull final Vec3d pos, final double range) {
		this(entity, pos.x, pos.y, pos.z, range);
	}

	public Locus(@Nonnull final Entity entity, final double x, final double y, final double z, final double range) {
		super(entity.world.provider.getDimension(), x, y, z, range);
		this.entityId = entity.getEntityId();
	}

	public Locus(@Nonnull final ByteBuf buf) {
		super(buf.readInt(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
		this.entityId = buf.readInt();
	}

	public Locus(@Nonnull final Locus locus, final int range) {
		super(locus.dimension, locus.x, locus.y, locus.z, range);
		this.entityId = locus.entityId;
	}

	public boolean isAssociatedEntity(@Nonnull final Entity entity) {
		return this.entityId != -1 && entity != null && this.entityId == entity.getEntityId();
	}

	@Nonnull
	public Vec3d getCoords() {
		return new Vec3d(this.x, this.y, this.z);
	}

	public void toBytes(@Nonnull final ByteBuf buf) {
		buf.writeInt(this.dimension);
		buf.writeFloat((float) this.x);
		buf.writeFloat((float) this.y);
		buf.writeFloat((float) this.z);
		buf.writeFloat((float) this.range);
		buf.writeInt(this.entityId);
	}

}
