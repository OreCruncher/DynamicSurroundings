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

import org.blockartistry.mod.DynSurround.client.fx.WaterSplashJetEffect;
import org.blockartistry.mod.DynSurround.client.sound.PositionedEmitter;
import org.blockartistry.mod.DynSurround.client.sound.SoundEffect;
import org.blockartistry.mod.DynSurround.util.WorldUtils;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleWaterSplash extends ParticleJet {

	private static final SoundEffect SPLASH = new SoundEffect("waterfall", SoundCategory.AMBIENT);
	private static final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

	private PositionedEmitter emitter;

	public ParticleWaterSplash(final int strength, final World world, final double x, final double y, final double z) {
		super(strength, world, x, y, z);
	}

	@Override
	public boolean shouldDie() {
		return !WaterSplashJetEffect.isValidSpawnBlock(this.world, this.getPos());
	}

	@Override
	public void renderParticle(VertexBuffer buffer, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

		super.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);

	}

	@Override
	public int getFXLayer() {
		return 0;
	}

	private boolean setupSound() {
		return this.emitter == null && this.rand.nextInt(3) == 0;
	}

	// Entity.resetHeight()
	@Override
	protected void spawnJetParticle() {

		if (setupSound()) {
			pos.setPos(this.posX, this.posY, this.posZ);
			this.emitter = new PositionedEmitter(SPLASH, pos);
			final float volume = this.jetStrength / 10.0F;
			final float pitch = 1.0F - 0.7F * (volume / 3.0F)
					+ (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F;
			this.emitter.setVolume(volume);
			this.emitter.setPitch(pitch);
		}
		
		if(this.emitter != null)
			this.emitter.update();

		final int splashCount = this.getParticleLimit() - getCurrentParticleCount();

		for (int j = 0; (float) j < splashCount; ++j) {
			final double xOffset = (this.rand.nextDouble() * 2.0F - 1.0F);
			final double zOffset = (this.rand.nextDouble() * 2.0F - 1.0F);
			if (WorldUtils.isSolidBlock(this.world, pos.setPos(this.posX + xOffset, this.posY, this.posZ + zOffset)))
				continue;

			final double motionX = xOffset * (this.jetStrength / 40.0D);
			final double motionY = 0.1D + this.rand.nextDouble() * this.jetStrength / 20.0D;
			final double motionZ = zOffset * (this.jetStrength / 40.D);
			final IParticleMote particle = new MoteWaterSpray(this.world, this.posX + xOffset, (double) (this.posY),
					this.posZ + zOffset, motionX, motionY, motionZ);
			addParticle(particle);
		}
	}

}
