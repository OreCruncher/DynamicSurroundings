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

package org.blockartistry.mod.DynSurround.client.fx.particle;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.client.handlers.SoundEffectHandler;
import org.blockartistry.mod.DynSurround.client.sound.SoundEffect;
import org.blockartistry.mod.DynSurround.client.sound.Sounds;
import org.blockartistry.mod.DynSurround.util.WorldUtils;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleDrip;
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

	protected ParticleDripOverride(final World worldIn, final double xCoordIn, final double yCoordIn,
			final double zCoordIn, final Material materialType) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, materialType);

		this.materialType = materialType;
	}

	private boolean doSteamHiss(@Nonnull final IBlockState state) {
		final Material blockMaterial = state.getMaterial();
		if (this.materialType == Material.LAVA && blockMaterial == Material.WATER)
			return true;
		return this.materialType == Material.WATER && blockMaterial == Material.LAVA;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (isAlive()) {
			if (this.posY < 1) {
				setExpired();
			} else if (this.firstTime) {

				this.firstTime = false;

				// If the particle is not positioned in an air block kill it right
				// away.  No sense wasting time with it.
				final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
				pos.setPos(this.posX, this.posY, this.posZ);
				if (!WorldUtils.isAirBlock(this.worldObj, pos)) {
					setExpired();
				} else {
					pos.setPos(this.posX, this.posY + 0.3D, this.posZ);
					final int y = pos.getY();

					IBlockState state = this.worldObj.getBlockState(pos);
					if (!WorldUtils.isAirBlock(state) && !WorldUtils.isLeaves(state)) {
						// Find out where it is going to hit
						do {
							pos.move(EnumFacing.DOWN, 1);
							state = this.worldObj.getBlockState(pos);
						} while (pos.getY() > 0 && WorldUtils.isAirBlock(state));

						if (pos.getY() < 1)
							return;

						final int delay = 40 + (y - pos.getY()) * 2;
						pos.move(EnumFacing.UP, 1);

						final SoundEffect effect;

						// Hitting solid surface
						if (state.getMaterial().isSolid()) {
							effect = Sounds.WATER_DROP;
							// Lava into water/water into lava
						} else if (doSteamHiss(state)) {
							effect = Sounds.STEAM_HISS;
							// Water into water
						} else if (this.materialType == Material.WATER) {
							effect = Sounds.WATER_DRIP;
							// Lava into lava
						} else {
							effect = Sounds.WATER_DROP;
						}

						SoundEffectHandler.INSTANCE.playSoundAt(pos, effect, delay);
					}
				}
			}
		}
	}

	public static class LavaFactory implements IParticleFactory {
		public Particle createParticle(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn,
				double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {
			return new ParticleDripOverride(worldIn, xCoordIn, yCoordIn, zCoordIn, Material.LAVA);
		}
	}

	public static class WaterFactory implements IParticleFactory {
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