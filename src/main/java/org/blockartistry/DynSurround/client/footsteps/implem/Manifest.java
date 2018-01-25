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

package org.blockartistry.DynSurround.client.footsteps.implem;

import com.google.gson.annotations.SerializedName;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Manifest {
	
	public static class Metadata {
		@SerializedName("name")
		public String name = null;
		@SerializedName("author")
		public String author = null;
		@SerializedName("website")
		public String website = null;
	}
	
	@SerializedName("type")
	public String type = null;
	@SerializedName("engineVersion")
	public Integer engineVersion = 0;
	@SerializedName("metadata")
	public Metadata metadata = null;
	
	public String getType() {
		return this.type;
	}
	
	public int getEngineVersion() {
		return this.engineVersion.intValue();
	}
	
	public String getName() {
		return this.metadata != null && this.metadata.name != null ? this.metadata.name : "UNKNOWN";
	}
	
	public String getAuthor() {
		return this.metadata != null && this.metadata.author != null ? this.metadata.author : "UNKNOWN";
	}

	public String getWebsite() {
		return this.metadata != null && this.metadata.website != null ? this.metadata.website : "UNKNOWN";
	}
}
