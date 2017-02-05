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

package org.blockartistry.mod.DynSurround.client.hud;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.util.Color;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public final class LightLevelHUD {

	public static enum Mode {
		BLOCK, BLOCK_SKY;

		public static Mode cycle(final Mode mode) {
			int next = mode.ordinal() + 1;
			if (next >= values().length)
				next = 0;
			return values()[next];
		}
	}

	public static enum DisplayStyle {

		DEFAULT(0.03F) {
			@Override
			public void render(final double x, final double y, final double z, final float yaw, final float pitch,
					@Nonnull final LightCoord coord) {
				final FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
				GlStateManager.translate(x + 0.5F, y + 0.3F, z + 0.5F);
				GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
				GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
				GlStateManager.scale(-this.scale, -this.scale, this.scale);
				font.drawString(coord.text, -font.getStringWidth(coord.text) / 2, 0, coord.color);

			}
		},
		SURFACE(0.08F) {
			@Override
			public void render(final double x, final double y, final double z, final float yaw, final float pitch,
					@Nonnull final LightCoord coord) {
				final FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
				final int margin = -font.getStringWidth(coord.text) / 2;
				GlStateManager.translate(x + 0.45D, y + 0.0005D, z + 0.8D);
				GlStateManager.rotate(90F, 1F, 0F, 0F);
				GlStateManager.scale(-this.scale, -this.scale, this.scale);
				font.drawString(coord.text, margin, 0, coord.color);
			}
		};

		public abstract void render(final double x, final double y, final double z, final float yaw, final float pitch,
				@Nonnull final LightCoord coord);

		protected final float scale;

		private DisplayStyle(final float scale) {
			this.scale = scale;
		}

		public static DisplayStyle getStyle(final int v) {
			if (v >= values().length)
				return DEFAULT;
			return values()[v];
		}

	}

	private static class LightCoord {
		public final int x;
		public final int y;
		public final int z;
		public final String text;
		public final int color;

		public LightCoord(final int x, final int y, final int z, final int light, final int color) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.text = Integer.toString(light);
			this.color = color;
		}
	}

	public static boolean showHUD = false;
	public static Mode displayMode = Mode.BLOCK;

	private static final int SAFE = Color.MC_GREEN.rgbWithAlpha(0.75F);
	private static final int CAUTION = Color.MC_YELLOW.rgbWithAlpha(0.75F);
	private static final int HAZARD = Color.MC_RED.rgbWithAlpha(0.75F);

	// Allocation size of array. Seems large, until you fly and look
	// down at a roofed forest.
	private static int allocationSize = 2176;
	private static List<LightCoord> lightLevels = new ArrayList<LightCoord>();

	private static final Frustum frustum = new Frustum();

	protected static boolean inFrustum(final double x, final double y, final double z) {
		return frustum.isBoxInFrustum(x, y, z, x, y, z);
	}

	protected static void updateLightInfo() {
		final long tick = EnvironState.getTickCounter();
		if (tick == 0 || tick % 4 != 0)
			return;

		final EntityPlayer player = EnvironState.getPlayer();
		frustum.setPosition(player.posX, player.posY, player.posZ);

		final BlockPos origin = EnvironState.getPlayerPosition();
		lightLevels = new ArrayList<LightCoord>(allocationSize);

		final int skyLightSub = EnvironState.getWorld().calculateSkylightSubtracted(1.0F);
		final IChunkProvider provider = EnvironState.getWorld().getChunkProvider();
		final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
		final int rangeXZ = ModOptions.llBlockRange * 2 + 1;
		final int rangeY = ModOptions.llBlockRange + 1;
		final int xOffset = rangeXZ / 2;
		final int zOffset = rangeXZ / 2;
		final int yOffset = rangeY - 3;

		Chunk chunk = null;

		for (int dX = 0; dX < rangeXZ; dX++)
			for (int dZ = 0; dZ < rangeXZ; dZ++) {

				Material lastMaterial = null;

				for (int dY = 0; dY < rangeY; dY++) {

					final int trueX = origin.getX() + dX - xOffset;
					final int trueY = origin.getY() + dY - yOffset;
					final int trueZ = origin.getZ() + dZ - zOffset;

					if (trueY < 1 || !inFrustum(trueX, trueY, trueZ))
						continue;

					final int chunkX = trueX >> 4;
					final int chunkZ = trueZ >> 4;
					if (chunk == null || chunk.xPosition != chunkX || chunk.zPosition != chunkZ)
						chunk = provider.getLoadedChunk(chunkX, chunkZ);

					if (chunk == null)
						return;

					final Material currentMaterial = chunk.getBlockState(trueX, trueY, trueZ).getMaterial();
					if (!currentMaterial.isSolid() && !currentMaterial.isLiquid()) {

						if (lastMaterial == null) {
							lastMaterial = chunk.getBlockState(trueX, trueY - 1, trueZ).getMaterial();
						}

						if (lastMaterial.isSolid()) {
							mutable.setPos(trueX, trueY, trueZ);
							final int blockLight = chunk.getLightFor(EnumSkyBlock.BLOCK, mutable);
							final int skyLight = chunk.getLightFor(EnumSkyBlock.SKY, mutable) - skyLightSub;
							final int effective = Math.max(blockLight, skyLight);
							final int result = displayMode == Mode.BLOCK_SKY ? effective : blockLight;

							if (!ModOptions.llHideSafe || result <= ModOptions.llSpawnThreshold) {
								int color = SAFE;
								if (blockLight <= ModOptions.llSpawnThreshold)
									if (effective > ModOptions.llSpawnThreshold)
										color = CAUTION;
									else
										color = HAZARD;

								lightLevels.add(new LightCoord(trueX, trueY, trueZ, result, color));
							}
						}
					}

					lastMaterial = currentMaterial;
				}
			}

		allocationSize = Math.max(allocationSize, lightLevels.size());

	}

	@SubscribeEvent
	public static void doRender(@Nonnull final RenderWorldLastEvent event) {

		if (!showHUD)
			return;

		// Update state if needed
		updateLightInfo();

		// If no points...
		if (lightLevels.size() == 0)
			return;

		// Only render in first person
		final RenderManager manager = Minecraft.getMinecraft().getRenderManager();
		if (manager.options.thirdPersonView != 0)
			return;

		final DisplayStyle displayStyle = DisplayStyle.getStyle(ModOptions.llStyle);

		GlStateManager.pushMatrix();
		GlStateManager.pushAttrib();

		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableAlpha();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableDepth();
		GlStateManager.depthMask(true);

		final boolean thirdPerson = manager.options.thirdPersonView == 2;
		final float pitch = manager.playerViewX * (thirdPerson ? -1 : 1);
		final float yaw = -manager.playerViewY;

		for (final LightCoord coord : lightLevels) {

			final double x = coord.x - manager.viewerPosX;
			final double y = coord.y - manager.viewerPosY;
			final double z = coord.z - manager.viewerPosZ;

			GlStateManager.pushMatrix();
			GlStateManager.pushAttrib();

			displayStyle.render(x, y, z, yaw, pitch, coord);

			GlStateManager.popAttrib();
			GlStateManager.popMatrix();
		}

		GlStateManager.popAttrib();
		GlStateManager.popMatrix();
	}

}
