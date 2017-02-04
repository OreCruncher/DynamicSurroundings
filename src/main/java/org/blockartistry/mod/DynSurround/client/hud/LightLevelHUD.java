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

import java.util.Arrays;
import java.util.BitSet;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.util.Color;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public final class LightLevelHUD {

	public static enum Mode {
		NONE, BLOCK_SKY, BLOCK;

		public static Mode cycle(final Mode mode) {
			int next = mode.ordinal() + 1;
			if (next >= values().length)
				next = 0;
			return values()[next];
		}
	}

	public static Mode displayMode = Mode.NONE;

	private static final int MOB_SPAWN_LEVEL = 7;
	private static final float SCALE = 0.035F;

	private static final int DIM_XZ = 24 * 2 + 1;
	private static final int DIM_Y = 24 + 1;
	private static final int VOLUME = DIM_XZ * DIM_XZ * DIM_Y;

	private static final int X_OFFSET = DIM_XZ / 2;
	private static final int Z_OFFSET = DIM_XZ / 2;
	private static final int Y_OFFSET = DIM_Y - 3;

	private static final int NO_SPAWN = Color.MC_GREEN.rgbWithAlpha(0.75F);
	private static final int SPAWN = Color.MC_RED.rgbWithAlpha(0.75F);

	private static final byte[] lightLevels = new byte[VOLUME];
	private static final BitSet blockMobSpawn = new BitSet(VOLUME);
	private static BlockPos origin = BlockPos.ORIGIN;

	protected static int calculateIndex(final int x, final int y, final int z) {
		return y + DIM_Y * (z + DIM_XZ * x);
	}

	protected static void updateLightInfo() {
		final long tick = EnvironState.getTickCounter();
		if (tick == 0 || tick % 4 != 0)
			return;

		// Reset our data for the calculation pass
		Arrays.fill(lightLevels, (byte) -1);
		blockMobSpawn.clear();

		final World world = EnvironState.getWorld();
		origin = EnvironState.getPlayerPosition();
		final int skyLightSub = world.calculateSkylightSubtracted(1.0F);
		final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

		for (int dX = 0; dX < DIM_XZ; dX++)
			for (int dZ = 0; dZ < DIM_XZ; dZ++) {

				final int trueX = origin.getX() + dX - X_OFFSET;
				final int trueZ = origin.getZ() + dZ - Z_OFFSET;

				final Chunk chunk = world.getChunkFromChunkCoords(trueX >> 4, trueZ >> 4);
				if (!chunk.isLoaded())
					return;

				IBlockState lastState = null;
				BlockPos lastPos = null;
				for (int dY = 0; dY < DIM_Y; dY++) {

					final int trueY = origin.getY() + dY - Y_OFFSET;
					if (trueY < 1)
						continue;

					mutable.setPos(trueX, trueY, trueZ);
					IBlockState currentState = chunk.getBlockState(mutable);
					if (!currentState.getMaterial().isSolid() && !currentState.getMaterial().isLiquid()) {

						if (lastState == null) {
							lastPos = mutable.down();
							lastState = world.getBlockState(lastPos);
						}

						if (lastState.getMaterial().isSolid()) {
							final int index = calculateIndex(dX, dY, dZ);
							final int blockLight = chunk.getLightFor(EnumSkyBlock.BLOCK, mutable);
							final int skyLight = displayMode == Mode.BLOCK_SKY
									? chunk.getLightFor(EnumSkyBlock.SKY, mutable) - skyLightSub : 0;
							lightLevels[index] = (byte) Math.max(blockLight, skyLight);
						}
					}

					lastState = currentState;
					lastPos = mutable.toImmutable();
				}
			}

	}

	@SubscribeEvent
	public static void doRender(@Nonnull final RenderWorldLastEvent event) {

		if (displayMode == Mode.NONE)
			return;

		// Update state if needed
		updateLightInfo();
		if (origin == BlockPos.ORIGIN)
			return;

		// Render the data.
		final RenderManager manager = Minecraft.getMinecraft().getRenderManager();
		if (manager == null || manager.options == null)
			return;

		final FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
		if (font == null)
			return;

		GlStateManager.pushMatrix();
		GlStateManager.pushAttrib();

		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableAlpha();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableDepth();
		GlStateManager.depthMask(true);

		for (int dX = 0; dX < DIM_XZ; dX++)
			for (int dZ = 0; dZ < DIM_XZ; dZ++)
				for (int dY = 0; dY < DIM_Y; dY++) {

					final int index = calculateIndex(dX, dY, dZ);
					final byte light = lightLevels[index];
					if (light < 0)
						continue;

					GlStateManager.pushMatrix();
					GlStateManager.pushAttrib();

					final double x = origin.getX() - manager.viewerPosX + 0.5D + dX - X_OFFSET;
					final double y = origin.getY() - manager.viewerPosY + 0.3D + dY - Y_OFFSET;
					final double z = origin.getZ() - manager.viewerPosZ + 0.5D + dZ - Z_OFFSET;

					GlStateManager.translate(x, y, z);

					final boolean thirdPerson = manager.options.thirdPersonView == 2;
					final float pitch = manager.playerViewX * (thirdPerson ? -1 : 1);
					final float yaw = -manager.playerViewY;

					GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
					GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);

					GlStateManager.scale(-SCALE, -SCALE, SCALE);

					final int color = light <= MOB_SPAWN_LEVEL ? SPAWN : NO_SPAWN;

					final String text = Integer.toString(light);
					final int margin = -font.getStringWidth(text) / 2;

					font.drawString(text, margin, 0, color);

					GlStateManager.popAttrib();
					GlStateManager.popMatrix();
				}

		GlStateManager.popAttrib();
		GlStateManager.popMatrix();
	}

}
