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

import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.util.Color;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
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
		NONE, BLOCK, BLOCK_SKY;

		public static Mode cycle(final Mode mode) {
			int next = mode.ordinal() + 1;
			if (next >= values().length)
				next = 0;
			return values()[next];
		}
	}

	private static class LightCoord {
		public final int x;
		public final int y;
		public final int z;
		public final int lightLevel;
		public String text;

		public LightCoord(final int x, final int y, final int z, final int light) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.lightLevel = light;
			this.text = Integer.toString(light);
		}
	}

	public static Mode displayMode = Mode.NONE;

	private static final int MOB_SPAWN_LEVEL = 7;
	private static final float SCALE = 0.035F;

	private static final int DIM_XZ = 24 * 2 + 1;
	private static final int DIM_Y = 24 + 1;

	private static final int X_OFFSET = DIM_XZ / 2;
	private static final int Z_OFFSET = DIM_XZ / 2;
	private static final int Y_OFFSET = DIM_Y - 3;

	private static final int NO_SPAWN = Color.MC_GREEN.rgbWithAlpha(0.75F);
	private static final int SPAWN = Color.MC_RED.rgbWithAlpha(0.75F);

	private static List<LightCoord> lightLevels = new ArrayList<LightCoord>();

	protected static void updateLightInfo() {
		final long tick = EnvironState.getTickCounter();
		if (tick == 0 || tick % 4 != 0)
			return;

		final BlockPos origin = EnvironState.getPlayerPosition();
		lightLevels = new ArrayList<LightCoord>();

		final int skyLightSub = EnvironState.getWorld().calculateSkylightSubtracted(1.0F);
		final IChunkProvider provider = EnvironState.getWorld().getChunkProvider();
		final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

		Chunk chunk = null;

		for (int dX = 0; dX < DIM_XZ; dX++)
			for (int dZ = 0; dZ < DIM_XZ; dZ++) {

				final int trueZ = origin.getZ() + dZ - Z_OFFSET;
				final int trueX = origin.getX() + dX - X_OFFSET;

				final int chunkX = trueX >> 4;
				final int chunkZ = trueZ >> 4;

				if (chunk == null || chunk.xPosition != chunkX || chunk.zPosition != chunkZ)
					chunk = provider.getLoadedChunk(chunkX, chunkZ);

				if (chunk == null)
					return;

				Material lastMaterial = null;

				for (int dY = 0; dY < DIM_Y; dY++) {

					final int trueY = origin.getY() + dY - Y_OFFSET;
					if (trueY < 1)
						continue;

					final Material currentMaterial = chunk.getBlockState(trueX, trueY, trueZ).getMaterial();
					if (!currentMaterial.isSolid() && !currentMaterial.isLiquid()) {

						if (lastMaterial == null) {
							lastMaterial = chunk.getBlockState(trueX, trueY - 1, trueZ).getMaterial();
						}

						if (lastMaterial.isSolid()) {
							mutable.setPos(trueX, trueY, trueZ);
							final int blockLight = chunk.getLightFor(EnumSkyBlock.BLOCK, mutable);
							final int skyLight = displayMode == Mode.BLOCK_SKY
									? chunk.getLightFor(EnumSkyBlock.SKY, mutable) - skyLightSub : 0;
							lightLevels.add(new LightCoord(trueX, trueY, trueZ, Math.max(blockLight, skyLight)));
						}
					}

					lastMaterial = currentMaterial;
				}
			}

	}

	@SubscribeEvent
	public static void doRender(@Nonnull final RenderWorldLastEvent event) {

		if (displayMode == Mode.NONE)
			return;

		// Update state if needed
		updateLightInfo();

		if (lightLevels.size() == 0)
			return;

		// Render the data.
		final RenderManager manager = Minecraft.getMinecraft().getRenderManager();
		final FontRenderer font = Minecraft.getMinecraft().fontRendererObj;

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
			final double x = coord.x - manager.viewerPosX + 0.5D;
			final double y = coord.y - manager.viewerPosY + 0.3D;
			final double z = coord.z - manager.viewerPosZ + 0.5D;
			final int color = coord.lightLevel <= MOB_SPAWN_LEVEL ? SPAWN : NO_SPAWN;
			final int margin = -font.getStringWidth(coord.text) / 2;

			GlStateManager.pushMatrix();
			GlStateManager.pushAttrib();
			GlStateManager.translate(x, y, z);
			GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
			GlStateManager.scale(-SCALE, -SCALE, SCALE);

			font.drawString(coord.text, margin, 0, color);

			GlStateManager.popAttrib();
			GlStateManager.popMatrix();
		}

		GlStateManager.popAttrib();
		GlStateManager.popMatrix();
	}

}
