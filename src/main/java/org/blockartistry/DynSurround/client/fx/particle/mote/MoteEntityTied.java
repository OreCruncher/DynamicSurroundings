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

package org.blockartistry.DynSurround.client.fx.particle.mote;

import java.lang.ref.WeakReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/*
 * Entity tied particles have their positions located at the entities position
 * plus height.
 */

@SideOnly(Side.CLIENT)
public abstract class MoteEntityTied extends MoteMotionBase {

	private final WeakReference<Entity> subject;

	public MoteEntityTied(@Nonnull final Entity entity) {
		super(entity.getEntityWorld(), 0, 0, 0, 0, 0, 0);

		final double newY = entity.posY + entity.height - (entity.isSneaking() ? 0.25D : 0);
		this.prevX = this.posX = entity.posX;
		this.prevY = this.posY = newY;
		this.prevZ = this.posZ = entity.posZ;
		this.position.setPos(this.posX, this.posY, this.posZ);

		this.motionX = 0.0D;
		this.motionY = 0.0D;
		this.motionZ = 0.0D;
		this.gravity = 0.0D;
		this.subject = new WeakReference<>(entity);
	}

	@Nullable
	public Entity getEntity() {
		return this.subject.get();
	}

	protected boolean shouldExpire() {
		if (!isAlive() || this.subject.isEnqueued())
			return true;

		final Entity entity = getEntity();
		return !entity.isEntityAlive() || entity.isInvisibleToPlayer(EnvironState.getPlayer());
	}

	// Adjust the Y of the particle based on what is needed
	protected double heightAdjust() {
		return (this.subject.get().isSneaking() ? -0.25D : 0);
	}
	
	@Override
	public void update() {
		if (shouldExpire()) {
			this.isAlive = false;
			return;
		}

		this.prevX = this.posX;
		this.prevY = this.posY;
		this.prevZ = this.posZ;

		final Entity entity = this.subject.get();
		this.posX = entity.posX;
		this.posY = entity.posY + entity.height + heightAdjust();
		this.posZ = entity.posZ;
		this.position.setPos(this.posX, this.posY, this.posZ);
	}
}
