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
package org.orecruncher.dsurround.data.xface;

import com.google.gson.annotations.SerializedName;

public class VariatorConfig {

	@SerializedName("immobileDuration")
	public int immobileDuration = 200;
	@SerializedName("eventOnJump")
	public boolean eventOnJump = true;
	@SerializedName("landHardDistanceMin")
	public float landHardDistanceMin = 0.9F;
	@SerializedName("speedToJumpAsMultifoot")
	public float speedToJumpAsMultifoot = 0.005F;
	@SerializedName("speedToRun")
	public float speedToRun = 0.22F; // 0.022F; slow

	@SerializedName("stride")
	public float stride = 0.75F; // 0.95F; slow
	@SerializedName("strideStair")
	public float strideStair = this.stride * 0.65F;
	@SerializedName("strideLadder")
	public float strideLadder = 0.5F;
	@SerializedName("quadrupedMultiplier")
	public float quadrupedMultiplier = 1.25F; // 0.925; slow

	@SerializedName("playWander")
	public boolean playWander = true;
	@SerializedName("quadruped")
	public boolean quadruped = false;
	@SerializedName("playJump")
	public boolean playJump = false;
	@SerializedName("distanceToCenter")
	public float distanceToCenter = 0.2F;
	@SerializedName("hasFootprint")
	public boolean hasFootprint = true;
	@SerializedName("footprintStyle")
	public int footprintStyle = 6;
	@SerializedName("footprintScale")
	public float footprintScale = 1.0F;
	@SerializedName("volumeScale")
	public float volumeScale = 1.0F;
}
