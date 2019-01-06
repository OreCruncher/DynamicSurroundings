/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) Abastro, OreCruncher
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

package org.orecruncher.dsurround.client.fx.particle;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.client.fx.ParticleCollections;
import org.orecruncher.dsurround.client.handlers.SoundEffectHandler;
import org.orecruncher.dsurround.client.sound.SoundEffect;
import org.orecruncher.dsurround.client.sound.Sounds;
import org.orecruncher.lib.WorldUtils;
import org.orecruncher.lib.chunk.ClientChunkCache;
import org.orecruncher.lib.chunk.IBlockAccessEx;
import org.orecruncher.lib.gfx.ParticleHelper;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleDrip;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleDripOverride extends ParticleDrip {

	private boolean firstTime = true;
	private final Material materialType;
	private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
	private int bobTimer = 40;

	protected ParticleDripOverride(final World worldIn, final double xCoordIn, final double yCoordIn,
			final double zCoordIn, final Material materialType) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, materialType);

		this.materialType = materialType;
	}

	private boolean doSteamHiss(@Nonnull final IBlockState state) {
		if (this.materialType == Material.LAVA)
			return state.getMaterial() == Material.WATER;
		return ParticleSteamCloud.isHotBlock(state);
	}

	@Override
	public void onUpdate() {

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if (this.materialType == Material.WATER) {
			this.particleRed = 0.2F;
			this.particleGreen = 0.3F;
			this.particleBlue = 1.0F;
		} else {
			this.particleRed = 1.0F;
			this.particleGreen = 16.0F / (40 - this.bobTimer + 16);
			this.particleBlue = 4.0F / (40 - this.bobTimer + 8);
		}

		this.motionY -= this.particleGravity;

		if (this.bobTimer-- > 0) {
			this.motionX *= 0.02D;
			this.motionY *= 0.02D;
			this.motionZ *= 0.02D;
			setParticleTextureIndex(113);
		} else {
			setParticleTextureIndex(112);
		}

		move(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.9800000190734863D;
		this.motionY *= 0.9800000190734863D;
		this.motionZ *= 0.9800000190734863D;

		if (this.particleMaxAge-- <= 0) {
			setExpired();
		}

		// Did we hit the ground?
		if (this.onGround) {
			this.pos.setPos(this.posX, this.posY, this.posZ);
			boolean doSteam = false;
			IBlockState state = ClientChunkCache.instance().getBlockState(this.pos);
			if (state.getBlock() == Blocks.LAVA) {
				doSteam = true;
			} else if (state.getMaterial().isLiquid()) {
				doSteam = ParticleCollections.addWaterRipple(this.world, this.posX, this.pos.getY() + 1,
						this.posZ) != null && this.materialType == Material.LAVA;
			} else {
				this.pos.move(EnumFacing.DOWN);
				state = ClientChunkCache.instance().getBlockState(this.pos);
				doSteam = ParticleSteamCloud.isHotBlock(state);
			}

			if (doSteam) {
				ParticleHelper.addParticle(
						new ParticleSteamCloud(this.world, this.posX, this.pos.getY() + 1, this.posZ, 0.01D));
			} else if (this.materialType != Material.LAVA) {
				this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX, this.posY, this.posZ, 0.0D, 0.0D,
						0.0D);
			}

			setExpired();
		}

		// Kill particles below 1. Drips on flat bedrock is a waste of ticks.
		if (this.posY < 1)
			setExpired();

		if (isAlive() && this.firstTime) {
			firstTime();
		}

	}

	protected void firstTime() {

		this.firstTime = false;
		final IBlockAccessEx provider = ClientChunkCache.instance();

		// If the particle is not positioned in an air block kill it
		// right away. No sense wasting time with it.
		this.pos.setPos(this.posX, this.posY, this.posZ);
		IBlockState state = provider.getBlockState(this.pos);
		if (!WorldUtils.isAirBlock(state)) {
			setExpired();
		} else {
			this.pos.setPos(this.posX, this.posY + 0.3D, this.posZ);
			final int y = this.pos.getY();

			state = provider.getBlockState(this.pos);
			if (!WorldUtils.isAirBlock(state) && !WorldUtils.isLeaves(state)) {
				// Find out where it is going to hit
				do {
					this.pos.move(EnumFacing.DOWN, 1);
					state = provider.getBlockState(this.pos);
				} while (this.pos.getY() > 0 && WorldUtils.isAirBlock(state));

				if (this.pos.getY() < 1)
					return;

				final int delay = 40 + (y - this.pos.getY()) * 2;
				this.pos.move(EnumFacing.UP, 1);

				final SoundEffect effect;

				// Lava into water/water into lava
				if (doSteamHiss(state)) {
					effect = Sounds.STEAM_HISS;
					// Hitting solid surface
				} else if (state.getMaterial().isSolid()) {
					effect = Sounds.WATER_DROP;
					// Water into water
				} else if (this.materialType == Material.WATER) {
					effect = Sounds.WATER_DRIP;
					// Lava into lava
				} else {
					effect = Sounds.WATER_DROP;
				}

				SoundEffectHandler.INSTANCE.playSoundAt(this.pos, effect, delay);
			}
		}
	}

	public static class LavaFactory implements IParticleFactory {
		@Override
		public Particle createParticle(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn,
				double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {
			return new ParticleDripOverride(worldIn, xCoordIn, yCoordIn, zCoordIn, Material.LAVA);
		}
	}

	public static class WaterFactory implements IParticleFactory {
		@Override
		public Particle createParticle(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn,
				double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {
			return new ParticleDripOverride(worldIn, xCoordIn, yCoordIn, zCoordIn, Material.WATER);
		}
	}

	public static void register() {
		Minecraft.getMinecraft().effectRenderer.registerParticle(EnumParticleTypes.DRIP_WATER.getParticleID(),
				new ParticleDripOverride.WaterFactory());
		Minecraft.getMinecraft().effectRenderer.registerParticle(EnumParticleTypes.DRIP_LAVA.getParticleID(),
				new ParticleDripOverride.LavaFactory());
	}

}