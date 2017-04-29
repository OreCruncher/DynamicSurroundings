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

package org.blockartistry.DynSurround.client.fx.particle;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.DSurround;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class ParticleEntity extends ParticleAsset {

	protected final Entity prototype;
	protected final float normalScale;

	public ParticleEntity(@Nonnull String entity, @Nonnull final World world, final double x, final double y,
			final double z) {
		this(entity, world, x, y, z, 0, 0, 0);
	}

	public ParticleEntity(@Nonnull String entity, @Nonnull final World world, final double x, final double y,
			final double z, final double dX, final double dY, final double dZ) {
		this(new ResourceLocation(entity), world, x, y, z, dX, dY, dZ);
	}
	
	public ParticleEntity(@Nonnull ResourceLocation entity, @Nonnull final World world, final double x, final double y,
			final double z, final double dX, final double dY, final double dZ) {
		super(world, x, y, z, dX, dY, dZ);

		this.prototype = EntityList.createEntityByIDFromName(entity, world);
		if(this.prototype == null) {
			this.normalScale = 0;
			DSurround.log().warn("Entity missing? [%s]", entity.toString());
			return;
		}
		
		// From mob spawner block
		float f = 0.53125F;
		float f1 = Math.max(this.prototype.width, this.prototype.height);

		if ((double) f1 > 1.0D) {
			f /= f1;
		}

		this.normalScale = f;
		this.setScale(0.1F);
	}
	
	@Override
	public void setScale(final float scale) {
		super.setScale(scale * this.normalScale);
	}

	@Override
	protected void doModelTranslate() {
        GlStateManager.translate(0.0F, -0.2F, 0.0F);
	}
	
	@Override
	protected void handleRender(final float partialTicks) {
		
		if(this.prototype == null)
			return;

		// From mob spawner
		this.prototype.setLocationAndAngles(this.posX, this.posY, this.posZ, 0.0F, 0.0F);
		this.manager.doRenderEntity(this.prototype, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, false);

	}

}
