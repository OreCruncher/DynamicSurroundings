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

package org.orecruncher.dsurround.client.fx.particle.mote;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.dsurround.entity.CapabilityEmojiData;
import org.orecruncher.dsurround.entity.EmojiType;
import org.orecruncher.dsurround.entity.IEmojiData;
import org.orecruncher.lib.math.MathStuff;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MoteEmoji extends MoteEntityTied {

	// Number of seconds for the particle to complete an
	// orbit around a mob.
	private static final float ORBITAL_PERIOD = 2.0F;

	// Number of degrees the particle will move in a
	// single tick.
	private static final float ORBITAL_TICK = 360.0F / (ORBITAL_PERIOD * 20.0F);

	private final IEmojiData emoji;

	private boolean shouldRender;
	private float period;
	private final float radius;

	protected float scale;
	protected float texU1, texU2;
	protected float texV1, texV2;

	public MoteEmoji(@Nonnull final Entity entity) {
		super(entity);

		this.scale = 1.0F;
		this.alpha = 0.99F;
		this.radius = (entity.width / 2.0F) + 0.25F;
		this.period = RANDOM.nextFloat() * 360.0F;

		// This particle doesn't age like normal. It hangs
		// around until it is no longer needed.
		this.maxAge = Integer.MAX_VALUE;

		this.scale = 0.125F;

		this.emoji = entity.getCapability(CapabilityEmojiData.EMOJI, null);
	}

	protected boolean shouldRender() {
		if (!this.isAlive())
			return false;

		final EntityPlayer player = EnvironState.getPlayer();
		final double distSq = ModOptions.speechbubbles.speechBubbleRange * ModOptions.speechbubbles.speechBubbleRange;
		return player.getDistanceSq(this.posX, this.posY, this.posZ) <= distSq;
	}

	@Override
	public void update() {
		
		super.update();
		
		if(!this.isAlive())
			return;

		// Calculate the current period values
		this.period = MathStuff.wrapDegrees(this.period + ORBITAL_TICK);

		// If there is no emoji we want to keep the current one
		// around for a period of time to give a better feel.
		if (this.emoji.getEmojiType() == EmojiType.NONE) {
			return;
		}

		// Setup the texels
		final int textureIdx = this.emoji.getEmojiType().ordinal() - 1;
		final int texX = textureIdx % 4;
		final int texY = textureIdx / 4;
		this.texU1 = texX * 0.25F;
		this.texU2 = this.texU1 + 0.25F;
		this.texV1 = texY * 0.25F;
		this.texV2 = this.texV1 + 0.25F;

		this.shouldRender = shouldRender();

	}

	@Override
	public void renderParticle(final BufferBuilder buffer, final Entity entityIn, final float partialTicks,
			final float edgeLRdirectionX, final float edgeUDdirectionY, final float edgeLRdirectionZ,
			final float edgeUDdirectionX, final float edgeUDdirectionZ) {

		if (!this.shouldRender)
			return;

		float x = renderX(partialTicks);
		float y = renderY(partialTicks);
		float z = renderZ(partialTicks);

		// Calculate the location of the drawn particle based
		// on the current period.
		final float degrees = MathStuff.wrapDegrees(this.period + ORBITAL_TICK * partialTicks);
		final float cosine = MathStuff.cos(MathStuff.toRadians(degrees));
		final float sine = MathStuff.sin(MathStuff.toRadians(degrees));
		x = cosine * this.radius + x;
		z = sine * this.radius + z;
		y = cosine * 0.25F + y;

		drawVertex(buffer, x - edgeLRdirectionX * this.scale - edgeUDdirectionX * this.scale,
				y - edgeUDdirectionY * this.scale, z - edgeLRdirectionZ * this.scale - edgeUDdirectionZ * this.scale,
				this.texU2, this.texV2);
		drawVertex(buffer, x - edgeLRdirectionX * this.scale + edgeUDdirectionX * this.scale,
				y + edgeUDdirectionY * this.scale, z - edgeLRdirectionZ * this.scale + edgeUDdirectionZ * this.scale,
				this.texU2, this.texV1);
		drawVertex(buffer, x + edgeLRdirectionX * this.scale + edgeUDdirectionX * this.scale,
				y + edgeUDdirectionY * this.scale, z + edgeLRdirectionZ * this.scale + edgeUDdirectionZ * this.scale,
				this.texU1, this.texV1);
		drawVertex(buffer, x + edgeLRdirectionX * this.scale - edgeUDdirectionX * this.scale,
				y - edgeUDdirectionY * this.scale, z + edgeLRdirectionZ * this.scale - edgeUDdirectionZ * this.scale,
				this.texU1, this.texV2);

	}

}
