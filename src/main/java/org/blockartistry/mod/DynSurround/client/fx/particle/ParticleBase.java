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

import org.blockartistry.lib.random.XorShiftRandom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class ParticleBase extends Particle {
	
	protected final RenderManager manager = Minecraft.getMinecraft().getRenderManager();
	protected final FontRenderer font = Minecraft.getMinecraft().fontRendererObj;

	protected ParticleBase(@Nonnull final World worldIn, final double posXIn, final double posYIn,
			final double posZIn) {
		super(worldIn, posXIn, posYIn, posZIn);
		
		this.rand = XorShiftRandom.current();
	}

	public ParticleBase(@Nonnull final World worldIn, final double xCoordIn, final double yCoordIn,
			final double zCoordIn, final double xSpeedIn, final double ySpeedIn, final double zSpeedIn) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);

		this.rand = XorShiftRandom.current();
	}
	
	protected double interpX() {
		return this.manager.viewerPosX;
	}
	
	protected double interpY() {
		return this.manager.viewerPosY;
	}
	
	protected double interpZ() {
		return this.manager.viewerPosZ;
	}
	
	protected void bindTexture(@Nonnull final ResourceLocation resource) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(resource);
	}

	protected boolean isThirdPersonView() {
		return this.manager.options.thirdPersonView == 2;
	}
}
