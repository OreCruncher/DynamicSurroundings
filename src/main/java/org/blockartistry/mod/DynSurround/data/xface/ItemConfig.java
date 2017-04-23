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

package org.blockartistry.mod.DynSurround.data.xface;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;

public final class ItemConfig {
	
	@SerializedName("swordSound")
	public List<String> swordSound = ImmutableList.of();
	@SerializedName("axeSound")
	public List<String> axeSound = ImmutableList.of();
	@SerializedName("bowSound")
	public List<String> bowSound = ImmutableList.of();
	@SerializedName("toolSound")
	public List<String> toolSound = ImmutableList.of();
	@SerializedName("shieldSound")
	public List<String> shieldSound = ImmutableList.of();
	@SerializedName("crystalArmor")
	public List<String> crystalArmor = ImmutableList.of();
	@SerializedName("heavyArmor")
	public List<String> heavyArmor = ImmutableList.of();
	@SerializedName("mediumArmor")
	public List<String> mediumArmor = ImmutableList.of();
	@SerializedName("lightArmor")
	public List<String> lightArmor = ImmutableList.of();

}
