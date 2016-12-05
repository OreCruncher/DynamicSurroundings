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

package org.blockartistry.mod.DynSurround.client.footsteps.game.system;

import net.minecraft.block.Block;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Association {
	
	private Block block;
	private int meta;
	
	public int x;
	public int y;
	public int z;
	
	private String data = null;
	
	private boolean noAssociation = false;
	private boolean isPrimative = false;
	
	public Association() {
	}
	
	public Association(final Block block, final int meta, final int xx, final int yy, final int zz) {
		this.block = block;
		this.meta = meta;
		this.x = xx;
		this.y = yy;
		this.z = zz;
	}
	
	public String getData() {
		return this.data;
	}
	
	public Association setAssociation(final String association) {
		this.data = association;
		this.noAssociation = false;
		return this;
	}
	
	public Association setNoAssociation() {
		this.noAssociation = true;
		return this;
	}
	
	public boolean getNoAssociation() {
		return this.noAssociation;
	}
	
	public Association setPrimitive(final String primative) {
		this.data = primative;
		this.isPrimative = true;
		return this;
	}
	
	public boolean isPrimative() {
		return this.isPrimative;
	}
	
	public Block getBlock() {
		return this.block;
	}
	
	public int getMeta() {
		return this.meta;
	}
	
	public boolean isNotEmitter() {
		return this.data != null && this.data.equals("NOT_EMITTER");
	}
}