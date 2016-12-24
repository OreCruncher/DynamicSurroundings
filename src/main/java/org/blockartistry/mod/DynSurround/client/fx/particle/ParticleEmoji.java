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

package org.blockartistry.mod.DynSurround.client.fx.particle;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.entity.EmojiType;
import org.blockartistry.mod.DynSurround.entity.EntityEmojiCapability;
import org.blockartistry.mod.DynSurround.entity.IEntityEmoji;
import org.blockartistry.mod.DynSurround.util.MathStuff;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class ParticleEmoji extends Particle {

	// Number of seconds for the particle to complete an
	// orbit around a mob.
	private static final float ORBITAL_PERIOD = 2.0F;

	// Number of degrees the particle will move in a
	// single tick.
	private static final float ORBITAL_TICK = 360.0F / (ORBITAL_PERIOD * 20.0F);
	
	// Number of ticks to keep the icon around until
	// the particle is dismissed.
	private static final int HOLD_TICK_COUNT = 30;

	private final Entity subject;
	private final IEntityEmoji emoji;
	private ResourceLocation activeTexture;

	private int holdTicks;
	private float period;
	private float radius;

	public ParticleEmoji(@Nonnull final Entity entity) {
		super(entity.getEntityWorld(), entity.posX, entity.posY, entity.posZ);

		final double newY = entity.posY + entity.height - (entity.isSneaking() ? 0.25D : 0);
		this.setPosition(entity.posX, newY, entity.posZ);
		this.prevPosX = entity.posX;
		this.prevPosY = newY;
		this.prevPosZ = entity.posZ;

		this.canCollide = false;
		this.motionX = 0.0D;
		this.motionY = 0.0D;
		this.motionZ = 0.0D;
		this.subject = entity;

		this.particleAlpha = 0.99F;
		this.radius = (entity.width / 2.0F) + 0.25F;
		this.period = this.rand.nextFloat() * 360.0F;
		
		this.emoji = entity.getCapability(EntityEmojiCapability.CAPABILIITY, null);
		this.activeTexture = this.emoji.getEmojiType().getResource();
	}

	public boolean shouldExpire() {
		if (!this.isAlive())
			return true;
		
		if(this.subject == null || !this.subject.isEntityAlive())
			return true;

		if (this.subject.isInvisibleToPlayer(EnvironState.getPlayer()))
			return true;

		return this.emoji == null;
	}

	@Override
	public void onUpdate() {
		if (shouldExpire()) {
			this.setExpired();
		} else {
			
			this.prevPosX = this.posX;
			this.prevPosY = this.posY;
			this.prevPosZ = this.posZ;

			final double newY = this.subject.posY + this.subject.height - (this.subject.isSneaking() ? 0.25D : 0);
			this.setPosition(this.subject.posX, newY, this.subject.posZ);

			// Calculate the current period values
			this.period = MathStuff.wrapDegrees(this.period + ORBITAL_TICK);

			// If there is no emoji we want to keep the current one
			// around for a period of time to give a better feel.
			if(this.emoji.getEmojiType() == EmojiType.NONE) {
				this.holdTicks++;
				if(this.holdTicks >= HOLD_TICK_COUNT) {
					this.setExpired();
				}
				return;
			}
			
			// Reset the hold counter and set the texture
			this.holdTicks = 0;
			this.activeTexture = this.emoji.getEmojiType().getResource();

		}
	}

	// Good resource:
	// https://github.com/TheGreyGhost/MinecraftByExample/blob/master/src/main/java/minecraftbyexample/mbe50_particle/FlameParticle.java
	@Override
	public void renderParticle(final VertexBuffer buffer, final Entity entityIn, final float partialTicks,
			final float edgeLRdirectionX, final float edgeUDdirectionY, final float edgeLRdirectionZ,
			final float edgeUDdirectionX, final float edgeUDdirectionZ) {

		if (this.activeTexture == null)
			return;

		Minecraft.getMinecraft().getTextureManager().bindTexture(this.activeTexture);

		final double minU = 0;
		final double maxU = 1;
		final double minV = 0;
		final double maxV = 1;

		final double scale = 0.1F * this.particleScale; // vanilla scaling
														// factor
		final double scaleLR = scale;
		final double scaleUD = scale;

		float x = ((float) (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - interpPosX));
		float y = ((float) (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - interpPosY));
		float z = ((float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - interpPosZ));

		// Calculate the location of the drawn particle based
		// on the current period.
		final float degrees = MathStuff.wrapDegrees(this.period + ORBITAL_TICK * partialTicks);
		final float cosine = MathStuff.cos(MathStuff.toRadians(degrees));
		final float sine = MathStuff.sin(MathStuff.toRadians(degrees));
		x = cosine * this.radius + x;
		z = sine * this.radius + z;
		y = cosine * 0.25F + y;

		final int combinedBrightness = this.getBrightnessForRender(partialTicks);
		final int slX16 = combinedBrightness >> 16 & 65535;
		final int blX16 = combinedBrightness & 65535;
		
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
		buffer.pos(x - edgeLRdirectionX * scaleLR - edgeUDdirectionX * scaleUD, y - edgeUDdirectionY * scaleUD,
				z - edgeLRdirectionZ * scaleLR - edgeUDdirectionZ * scaleUD).tex(maxU, maxV)
				.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha)
				.lightmap(slX16, blX16).endVertex();
		buffer.pos(x - edgeLRdirectionX * scaleLR + edgeUDdirectionX * scaleUD, y + edgeUDdirectionY * scaleUD,
				z - edgeLRdirectionZ * scaleLR + edgeUDdirectionZ * scaleUD).tex(maxU, minV)
				.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha)
				.lightmap(slX16, blX16).endVertex();
		buffer.pos(x + edgeLRdirectionX * scaleLR + edgeUDdirectionX * scaleUD, y + edgeUDdirectionY * scaleUD,
				z + edgeLRdirectionZ * scaleLR + edgeUDdirectionZ * scaleUD).tex(minU, minV)
				.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha)
				.lightmap(slX16, blX16).endVertex();
		buffer.pos(x + edgeLRdirectionX * scaleLR - edgeUDdirectionX * scaleUD, y - edgeUDdirectionY * scaleUD,
				z + edgeLRdirectionZ * scaleLR - edgeUDdirectionZ * scaleUD).tex(minU, maxV)
				.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha)
				.lightmap(slX16, blX16).endVertex();
		Tessellator.getInstance().draw();
	}

	@Override
	public int getBrightnessForRender(float partialTick) {
		//final int FULL_BRIGHTNESS_VALUE = 0xf000f0;
		//return FULL_BRIGHTNESS_VALUE;

		return this.subject.getBrightnessForRender(partialTick);
		
		// if you want the brightness to be the local illumination (from block
		// light and sky light) you can just use
		// Entity.getBrightnessForRender() base method, which contains:
		// BlockPos blockpos = new BlockPos(this.posX, this.posY, this.posZ);
		// return this.worldObj.isBlockLoaded(blockpos) ?
		// this.worldObj.getCombinedLight(blockpos, 0) : 0;
	}

	@Override
	public boolean isTransparent() {
		return false;
	}

	@Override
	public int getFXLayer() {
		return 3;
	}

}
