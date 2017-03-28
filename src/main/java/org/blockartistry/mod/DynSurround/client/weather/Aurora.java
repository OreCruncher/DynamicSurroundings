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

package org.blockartistry.mod.DynSurround.client.weather;

import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.data.AuroraData;
import org.blockartistry.mod.DynSurround.data.AuroraPreset;
import org.blockartistry.mod.DynSurround.data.ColorPair;
import org.blockartistry.mod.DynSurround.util.Color;
import org.blockartistry.mod.DynSurround.util.MathStuff;

import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public final class Aurora {

	private static final boolean MULTIPLES = ModOptions.auroraMultipleBands;

	private static final float ANGLE1 = MathStuff.PI_F / 16.0F;
	private static final float ANGLE2 = MathStuff.toRadians(90.0F / 7.0F);
	private static final float AURORA_SPEED = 0.75F;
	private static final float AURORA_AMPLITUDE = 18.0F;
	private static final float AURORA_WAVELENGTH = 8.0F;
	private static final int ALPHA_INCREMENT_MOD = 8;

	public float posX;
	public float posZ;

	private Node[][] bands;
	private long seed;
	private float cycle = 0.0F;
	private int fadeTimer = 0;
	private int alphaLimit = 128;
	private int fadeLimit = 128 * ALPHA_INCREMENT_MOD;
	private boolean isAlive = true;
	private int length;
	private float nodeLength;
	private float nodeWidth;
	private int bandOffset;

	// Base color of the aurora
	private final Color baseColor;
	// Fade color of the aurora
	private final Color fadeColor;
	// Alpha setting of the aurora for fade
	private int alpha = 1;

	public Aurora(@Nonnull final AuroraData data) {
		this(data.posX, data.posZ, data.seed, data.colorSet, data.preset);
	}

	public Aurora(final float x, final float z, final long seed, final int colorConfig, final int presetConfig) {
		this.seed = seed;
		this.posX = x;
		this.posZ = z;

		final ColorPair pair = ColorPair.get(colorConfig);
		this.baseColor = pair.baseColor;
		this.fadeColor = pair.fadeColor;

		preset(presetConfig);
		generateBands();

		// Initialize at least once for a non-animated aurora
		translate(0);
	}

	@Nonnull
	public Node[][] getNodeList() {
		return this.bands;
	}

	private void preset(final int preset) {
		final AuroraPreset p = AuroraPreset.get(preset);
		this.length = p.length;
		this.nodeLength = p.nodeLength;
		this.nodeWidth = p.nodeWidth;
		this.bandOffset = p.bandOffset;
		this.alphaLimit = p.alphaLimit;
		this.fadeLimit = this.alphaLimit * ALPHA_INCREMENT_MOD;
	}

	@Nonnull
	public Color getBaseColor() {
		return this.baseColor;
	}

	@Nonnull
	public Color getFadeColor() {
		return this.fadeColor;
	}

	public int getAlpha() {
		return this.alpha;
	}

	public float getAlphaf() {
		return (float) this.alpha / 255.0F;
	}

	public boolean isAlive() {
		return this.isAlive;
	}

	public void die() {
		if (this.isAlive) {
			this.isAlive = false;
			this.fadeTimer = 0;
		}
	}

	public boolean isComplete() {
		return !this.isAlive() && this.fadeTimer >= this.fadeLimit;
	}

	public void update() {
		if (this.fadeTimer < this.fadeLimit) {

			if ((this.fadeTimer % ALPHA_INCREMENT_MOD) == 0 && this.alpha >= 0)
				this.alpha += this.isAlive ? 1 : -1;

			if (this.alpha <= 0)
				this.fadeTimer = this.fadeLimit;
			else
				this.fadeTimer++;
		}

		if ((this.cycle += AURORA_SPEED) >= 360.0F)
			this.cycle -= 360.0F;
	}

	private void generateBands() {
		this.bands = new Node[MULTIPLES ? 3 : 1][];
		final Node[] band0 = this.bands[0] = populate();
		final Node[] band1, band2;
		if (this.bands.length > 1) {
			band1 = this.bands[1] = populateFromTemplate(this.bands[0], this.bandOffset);
			band2 = this.bands[2] = populateFromTemplate(this.bands[0], -this.bandOffset);
		} else {
			band1 = band2 = null;
		}

		final float factor = MathStuff.PI_F / (this.length / 4);
		final int lowerBound = this.length / 8 + 1;
		final int upperBound = this.length * 7 / 8 - 1;

		int count = 0;
		for (int i = 0; i < this.length; i++) {
			// Scale the widths at the head and tail of the
			// aurora band. This makes them taper.
			final float width;
			if (i < lowerBound) {
				width = MathStuff.sin(factor * count++) * this.nodeWidth;
			} else if (i > upperBound) {
				width = MathStuff.sin(factor * count--) * this.nodeWidth;
			} else {
				width = this.nodeWidth;
			}

			band0[i].setWidth(width);
			if (band1 != null) {
				band1[i].setWidth(width);
				band2[i].setWidth(width);
			}
		}
	}

	@Nonnull
	private static Node[] populateFromTemplate(@Nonnull final Node[] template, final int offset) {
		final Node[] tet = new Node[template.length];
		for (int i = 0; i < template.length; i++)
			tet[i] = new Node(template[i], offset);
		return tet;
	}

	@Nonnull
	private Node[] populate() {
		final Node[] nodeList = new Node[this.length];
		final Random nodeRand = new Random(this.seed);
		final int bound = this.length / 2 - 1;

		float angleTotal = 0.0F;
		for (int i = this.length / 8 / 2 - 1; i >= 0; i--) {
			float angle = (nodeRand.nextFloat() - 0.5F) * 8.0F;
			angleTotal += angle;
			if (MathStuff.abs(angleTotal) > 180.0F) {
				angle = -angle;
				angleTotal += angle;
			}

			for (int k = 7; k >= 0; k--) {
				final int idx = i * 8 + k;
				if (idx == bound) {
					nodeList[idx] = new Node(0.0F, 7.0F + nodeRand.nextFloat(), 0.0F, angle);
				} else {
					float y;
					if (i == 0)
						y = MathStuff.sin(ANGLE1 * k) * 7.0F + nodeRand.nextFloat() / 2.0F;
					else
						y = 10.0F + nodeRand.nextFloat() * 5.0F;

					final Node node = nodeList[idx + 1];
					final float subAngle = node.angle + angle;
					final float subAngleRads = MathStuff.toRadians(subAngle);
					final float z = node.posZ - (MathStuff.sin(subAngleRads) * this.nodeLength);
					final float x = node.posX - (MathStuff.cos(subAngleRads) * this.nodeLength);

					nodeList[idx] = new Node(x, y, z, subAngle);
				}
			}
		}

		angleTotal = 0.0F;
		for (int j = this.length / 8 / 2; j < this.length / 8; j++) {
			float angle = (nodeRand.nextFloat() - 0.5F) * 8.0F;
			angleTotal += angle;
			if (MathStuff.abs(angleTotal) > 180.0F) {
				angle = -angle;
				angleTotal += angle;
			}
			for (int h = 0; h < 8; h++) {
				float y;
				if (j == this.length / 8 - 1)
					y = MathStuff.cos(ANGLE2 * h) * 7.0F + nodeRand.nextFloat() / 2.0F;
				else
					y = 10.0F + nodeRand.nextFloat() * 5.0F;

				final Node node = nodeList[j * 8 + h - 1];
				final float subAngle = node.angle + angle;
				final float subAngleRads = MathStuff.toRadians(subAngle);
				final float z = node.posZ + (MathStuff.sin(subAngleRads) * this.nodeLength);
				final float x = node.posX + (MathStuff.cos(subAngleRads) * this.nodeLength);

				nodeList[j * 8 + h] = new Node(x, y, z, subAngle);
			}
		}

		return nodeList;
	}

	/*
	 * Calculates the next "frame" of the aurora if it is being animated.
	 */
	public void translate(final float partialTick) {
		final Node[] nodeList = this.bands[0];
		Node[] second = null;
		Node[] third = null;
		if (this.bands.length > 1) {
			second = this.bands[1];
			third = this.bands[2];
		}

		final float c = this.cycle + AURORA_SPEED * partialTick;
		for (int i = 0; i < nodeList.length; i++) {
			// Travelling sine wave: https://en.wikipedia.org/wiki/Wavelength
			final float f = MathStuff.cos(MathStuff.toRadians(AURORA_WAVELENGTH * i + c));
			final float dZ = f * AURORA_AMPLITUDE;
			final float dY = f * 3.0F;
			Node node = nodeList[i];
			node.setDeltaZ(dZ);
			node.setDeltaY(dY);
			if (second != null) {
				node = second[i];
				node.setDeltaZ(dZ);
				node.setDeltaY(dY);

				node = third[i];
				node.setDeltaZ(dZ);
				node.setDeltaY(dY);
			}
		}
		findAngles(nodeList);
		if (second != null) {
			findAngles(second);
			findAngles(third);
		}
	}

	private static void findAngles(@Nonnull final Node[] nodeList) {
		nodeList[0].findAngles(null);
		for (int i = 1; i < nodeList.length - 1; i++)
			nodeList[i].findAngles(nodeList[i + 1]);
		nodeList[nodeList.length - 1].findAngles(null);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("X:").append(this.posX).append(", Z:").append(this.posZ);
		builder.append(", base").append(this.baseColor.toString());
		builder.append(", fade").append(this.fadeColor.toString());
		builder.append(", alpha:").append(this.alpha);
		if (!this.isAlive)
			builder.append(", FADING");
		return builder.toString();
	}

}
