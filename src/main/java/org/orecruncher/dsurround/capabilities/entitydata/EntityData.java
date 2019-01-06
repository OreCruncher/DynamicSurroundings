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

package org.orecruncher.dsurround.capabilities.entitydata;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.network.Network;
import org.orecruncher.dsurround.network.PacketEntityData;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;

public final class EntityData implements IEntityDataSettable {

	public static final int NO_ENTITY = -1;

	private final Entity entity;
	private boolean isAttacking;
	private boolean isFleeing;
	private boolean isDirty;

	public EntityData() {
		this.entity = null;
	}

	public EntityData(@Nonnull final Entity entity) {
		this.entity = entity;
	}

	@Override
	public int getEntityId() {
		return this.entity != null ? this.entity.getEntityId() : NO_ENTITY;
	}

	@Override
	public boolean isAttacking() {
		return this.isAttacking;
	}

	@Override
	public void setAttacking(final boolean flag) {
		this.isDirty = (this.isAttacking != flag) | this.isDirty;
		this.isAttacking = flag;
	}

	@Override
	public boolean isFleeing() {
		return this.isFleeing;
	}

	@Override
	public void setFleeing(final boolean flag) {
		this.isDirty = (this.isFleeing != flag) | this.isDirty;
		this.isFleeing = flag;
	}

	@Override
	public boolean isDirty() {
		return this.isDirty;
	}

	@Override
	public void clearDirty() {
		this.isDirty = false;
	}

	@Override
	public void sync() {
		if (this.entity != null && !this.entity.world.isRemote) {
			Network.sendToEntityViewers(this.entity, new PacketEntityData(this));
			clearDirty();
		}
	}

	@Override
	@Nonnull
	public NBTTagCompound serializeNBT() {
		final NBTTagCompound nbt = new NBTTagCompound();
		nbt.setBoolean(NBT.ATTACKING, isAttacking());
		nbt.setBoolean(NBT.FLEEING, isFleeing());
		return nbt;
	}

	@Override
	public void deserializeNBT(@Nonnull final NBTTagCompound nbt) {
		setAttacking(nbt.getBoolean(NBT.ATTACKING));
		setFleeing(nbt.getBoolean(NBT.FLEEING));
	}

	private static class NBT {
		public static final String ATTACKING = "a";
		public static final String FLEEING = "f";
	}

}
