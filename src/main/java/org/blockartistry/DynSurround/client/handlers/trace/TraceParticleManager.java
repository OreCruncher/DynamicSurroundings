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
package org.blockartistry.DynSurround.client.handlers.trace;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.lib.ThreadGuard;
import org.blockartistry.lib.ThreadGuard.Action;

import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TraceParticleManager extends ParticleManager {

	protected final ParticleManager manager;
	protected TObjectIntHashMap<Class<?>> counts = new TObjectIntHashMap<>();

	protected final ThreadGuard guard = new ThreadGuard(DSurround.log(), Side.CLIENT, "ParticleManager")
			.setAction(ModOptions.features.developmentMode ? Action.EXCEPTION
					: ModOptions.logging.enableDebugLogging ? Action.LOG : Action.NONE);

	public TraceParticleManager(@Nonnull final ParticleManager manager) {
		super(null, null);

		this.manager = manager;
	}

	private void checkForClientThread(final String method) {
		this.guard.check(method);
	}

	public TObjectIntHashMap<Class<?>> getSnaptshot() {
		final TObjectIntHashMap<Class<?>> result = this.counts;
		this.counts = new TObjectIntHashMap<>();
		return result;
	}

	public void emitParticleAtEntity(Entity entityIn, EnumParticleTypes particleTypes) {
		checkForClientThread("emitParticleAtEntity");
		this.manager.emitParticleAtEntity(entityIn, particleTypes);
	}

	public Particle spawnEffectParticle(int particleId, double xCoord, double yCoord, double zCoord, double xSpeed,
			double ySpeed, double zSpeed, int... parameters) {
		checkForClientThread("spawnEffectParticle");
		return this.manager.spawnEffectParticle(particleId, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
	}

	public void addEffect(Particle effect) {
		checkForClientThread("addEffect");
		if (effect != null)
			this.counts.adjustOrPutValue(effect.getClass(), 1, 1);
		this.manager.addEffect(effect);
	}

	public void updateEffects() {
		checkForClientThread("updateEffects");
		this.manager.updateEffects();
	}

	public void renderParticles(Entity entityIn, float partialTicks) {
		checkForClientThread("renderParticles");
		this.manager.renderParticles(entityIn, partialTicks);
	}

	public void renderLitParticles(Entity entityIn, float partialTick) {
		checkForClientThread("renderLitParticles");
		this.manager.renderLitParticles(entityIn, partialTick);
	}

	public void clearEffects(@Nullable World worldIn) {
		checkForClientThread("clearEffects");
		this.manager.clearEffects(worldIn);
	}

	public void addBlockDestroyEffects(BlockPos pos, IBlockState state) {
		checkForClientThread("addBlockDestroyEffects");
		this.manager.addBlockDestroyEffects(pos, state);
	}

	public void addBlockHitEffects(BlockPos pos, EnumFacing side) {
		checkForClientThread("addBlockHitEffects");
		this.manager.addBlockHitEffects(pos, side);
	}

	public void addBlockHitEffects(BlockPos pos, net.minecraft.util.math.RayTraceResult target) {
		checkForClientThread("addBlockHitEffects");
		this.manager.addBlockHitEffects(pos, target);
	}

	public String getStatistics() {
		checkForClientThread("getStatistics");
		return this.manager.getStatistics();
	}
}
