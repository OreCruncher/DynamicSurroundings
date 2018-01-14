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

package org.blockartistry.DynSurround.client.weather;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.client.handlers.AuroraEffectHandler;
import org.blockartistry.DynSurround.registry.DimensionRegistry;
import org.blockartistry.DynSurround.registry.RegistryManager;
import org.blockartistry.DynSurround.registry.RegistryManager.RegistryType;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = DSurround.MOD_ID)
public final class AuroraRenderer extends IRenderHandler {

	private static boolean shouldHook(@Nonnull final World world) {
		final IRenderHandler handler = world.provider.getSkyRenderer();
		if (handler instanceof AuroraRenderer)
			return false;

		final DimensionRegistry registry = RegistryManager.<DimensionRegistry>get(RegistryType.DIMENSION);
		return registry.hasAuroras(world);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void doRender(@Nonnull final RenderWorldLastEvent event) {
		// Make sure that the sky renderer is an aurora renderer
		if (shouldHook(Minecraft.getMinecraft().world)) {
			final AuroraRenderer hook = new AuroraRenderer(Minecraft.getMinecraft().world.provider.getSkyRenderer());
			Minecraft.getMinecraft().world.provider.setSkyRenderer(hook);
		}
	}

	protected final IRenderHandler handler;
	protected final AuroraRenderHandler auroraRender;
	
	public AuroraRenderer(@Nullable final IRenderHandler handler) {
		this.handler = handler;
		
		if(OpenGlHelper.areShadersSupported())
			this.auroraRender = new AuroraRenderHandlerShader();
		else
			this.auroraRender = new AuroraRenderHandler();
	}

	@Override
	public void render(final float partialTicks, @Nonnull final WorldClient world, @Nonnull final Minecraft mc) {
		if (this.handler == null) {
			// There isn't another handler. This means we have to call back
			// into the Minecraft code to render the normal sky. This is tricky
			// because we need to unhook ourselves and rehook after rendering.
			world.provider.setSkyRenderer(null);
			try {
				mc.renderGlobal.renderSky(partialTicks, 2);
			} catch (final Throwable t) {
				;
			}
			world.provider.setSkyRenderer(this);
		} else {
			// Call the existing handler
			this.handler.render(partialTicks, world, mc);
		}

		// Render our aurora if it is present
		final Aurora aurora = AuroraEffectHandler.getCurrentAurora();
		if (aurora != null) {
			this.auroraRender.renderAurora(partialTicks, aurora);
		}
	}

}
