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

package org.blockartistry.DynSurround.client.aurora;

import java.util.Random;

import javax.annotation.Nonnull;

import org.blockartistry.lib.math.MathStuff;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AuroraBand {

	protected static final float ANGLE1 = MathStuff.PI_F / 16.0F;
	protected static final float ANGLE2 = MathStuff.toRadians(90.0F / 7.0F);
	protected static final float AURORA_SPEED = 0.75F;
	protected static final float AURORA_WAVELENGTH = 8.0F;
	public static final float AURORA_AMPLITUDE = 18.0F;

	protected final Random random;

	protected Node[] nodes;
	protected float cycle = 0.0F;
	protected int alphaLimit = 128;
	protected int length;
	protected float nodeLength;
	protected float nodeWidth;

	public AuroraBand(final Random random, final AuroraGeometry geo, final boolean noTaper, final boolean fixedHeight) {
		this.random = random;
		this.preset(geo);
		this.generateBands(noTaper, fixedHeight);
		this.translate(0);
	}

	public AuroraBand(final Random random, final AuroraGeometry geo) {
		this(random, geo, false, false);
	}

	protected AuroraBand(final Node[] nodes, final AuroraBand band) {
		this.random = band.random;
		this.nodes = nodes;
		this.cycle = band.cycle;
		this.length = band.length;
		this.nodeLength = band.nodeLength;
		this.nodeWidth = band.nodeWidth;
		this.alphaLimit = band.alphaLimit;
		this.translate(0);
	}

	public int getAlphaLimit() {
		return this.alphaLimit;
	}

	@Nonnull
	public Node[] getNodeList() {
		return this.nodes;
	}

	public float getNodeWidth() {
		return this.nodeWidth;
	}

	public void update() {
		if ((this.cycle += AURORA_SPEED) >= 360.0F)
			this.cycle -= 360.0F;
	}

	public AuroraBand copy(final int offset) {
		final Node[] newNodes = new Node[this.nodes.length];
		for (int i = 0; i < this.nodes.length; i++)
			newNodes[i] = new Node(this.nodes[i], offset);
		return new AuroraBand(newNodes, this);
	}

	/*
	 * Calculates the next "frame" of the aurora if it is being animated.
	 */
	public void translate(final float partialTick) {
		final float c = this.cycle + AURORA_SPEED * partialTick;
		for (int i = 0; i < this.nodes.length; i++) {
			// Travelling sine wave: https://en.wikipedia.org/wiki/Wavelength
			final float f = MathStuff.cos(MathStuff.toRadians(AURORA_WAVELENGTH * i + c));
			final float dZ = f * AURORA_AMPLITUDE;
			final float dY = f * 3.0F;
			final Node node = this.nodes[i];
			node.setDeltaZ(dZ);
			node.setDeltaY(dY);
		}
		findAngles(this.nodes);
	}

	protected void preset(final AuroraGeometry geo) {
		this.length = geo.length;
		this.nodeLength = geo.nodeLength;
		this.nodeWidth = geo.nodeWidth;
		this.alphaLimit = geo.alphaLimit;
	}

	protected void generateBands(final boolean noTaper, final boolean fixedHeight) {
		this.nodes = populate(noTaper, fixedHeight);
		final float factor = MathStuff.PI_F / (this.length / 4);
		final int lowerBound = this.length / 8 + 1;
		final int upperBound = this.length * 7 / 8 - 1;

		int count = 0;
		for (int i = 0; i < this.length; i++) {
			// Scale the widths at the head and tail of the
			// aurora band. This makes them taper.
			float width;
			if (noTaper) {
				width = this.nodeWidth;
			} else if (i < lowerBound) {
				width = MathStuff.sin(factor * count++) * this.nodeWidth;
			} else if (i > upperBound) {
				width = MathStuff.sin(factor * count--) * this.nodeWidth;
			} else {
				width = this.nodeWidth;
			}

			this.nodes[i].setWidth(width);
		}
	}

	@Nonnull
	protected Node[] populate(final boolean noTaper, final boolean fixedHeight) {
		final Node[] nodeList = new Node[this.length];
		final int bound = this.length / 2 - 1;

		float angleTotal = 0.0F;
		for (int i = this.length / 8 / 2 - 1; i >= 0; i--) {
			float angle = (this.random.nextFloat() - 0.5F) * 8.0F;
			angleTotal += angle;
			if (MathStuff.abs(angleTotal) > 180.0F) {
				angle = -angle;
				angleTotal += angle;
			}

			for (int k = 7; k >= 0; k--) {
				final int idx = i * 8 + k;
				if (idx == bound) {
					final float amplitude = fixedHeight ? AURORA_AMPLITUDE : (7.0F + this.random.nextFloat());
					nodeList[idx] = new Node(0.0F, amplitude, 0.0F, angle);
				} else {
					float y;
					if (fixedHeight)
						y = AURORA_AMPLITUDE;
					else if (i == 0)
						y = MathStuff.sin(ANGLE1 * k) * 7.0F + this.random.nextFloat() / 2.0F;
					else
						y = 10.0F + this.random.nextFloat() * 5.0F;

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
			float angle = (this.random.nextFloat() - 0.5F) * 8.0F;
			angleTotal += angle;
			if (MathStuff.abs(angleTotal) > 180.0F) {
				angle = -angle;
				angleTotal += angle;
			}
			for (int h = 0; h < 8; h++) {
				float y;
				if (fixedHeight) {
					y = AURORA_AMPLITUDE;
				} else if (j == this.length / 8 - 1)
					y = MathStuff.cos(ANGLE2 * h) * 7.0F + this.random.nextFloat() / 2.0F;
				else
					y = 10.0F + this.random.nextFloat() * 5.0F;

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

	protected static void findAngles(@Nonnull final Node[] nodeList) {
		nodeList[0].findAngles(null);
		for (int i = 1; i < nodeList.length - 1; i++)
			nodeList[i].findAngles(nodeList[i + 1]);
		nodeList[nodeList.length - 1].findAngles(null);
	}
}
