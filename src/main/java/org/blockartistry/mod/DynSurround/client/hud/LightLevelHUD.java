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
import org.blockartistry.mod.DynSurround.util.BlockStateProvider;
import org.blockartistry.mod.DynSurround.util.Color;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.WorldEntitySpawner;
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

		@Nonnull
		public static Mode getMode(final int v) {
			if (v >= values().length)
				return BLOCK;
			return values()[v];
		}
	}

	private static enum ColorSet {

		BRIGHT(Color.MC_GREEN, Color.MC_YELLOW, Color.MC_RED, Color.MC_DARKAQUA), DARK(Color.MC_DARKGREEN,
				Color.MC_GOLD, Color.MC_DARKRED, Color.MC_DARKBLUE);

		private static final float ALPHA = 0.75F;

		public final int safe;
		public final int caution;
		public final int hazard;
		public final int noSpawn;

		private ColorSet(@Nonnull final Color safe, @Nonnull final Color caution, @Nonnull final Color hazard,
				@Nonnull final Color noSpawn) {
			this.safe = safe.rgbWithAlpha(ALPHA);
			this.caution = caution.rgbWithAlpha(ALPHA);
			this.hazard = hazard.rgbWithAlpha(ALPHA);
			this.noSpawn = noSpawn.rgbWithAlpha(ALPHA);
		}

		public static ColorSet getStyle(final int v) {
			if (v >= values().length)
				return BRIGHT;
			return values()[v];
		}

	}

	private static enum DisplayStyle {

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
		},
		SURFACE_ROTATE(0.08F) {

			@Override
			public void render(final double x, final double y, final double z, final float yaw, final float pitch,
					@Nonnull final LightCoord coord) {
				final FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
				final int margin = -font.getStringWidth(coord.text) / 2;
				GlStateManager.translate(x + 0.5D, y, z + 0.5D);
				GlStateManager.rotate(surfaceRotationAngle, 0F, 1F, 0F);
				GlStateManager.translate(-0.05D, 0.0005D, 0.3D);
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

	private static final class LightCoord {
		public double x;
		public double y;
		public double z;
		public String text;
		public int color;
	}

	public static boolean showHUD = false;

	private static final String[] VALUES = new String[16];
	private static final float[] ROTATION = { 180, 0, 270, 90 };

	private static final BlockStateProvider blocks = new BlockStateProvider();
	private static final int ALLOCATION_SIZE = 2176;
	private static final List<LightCoord> lightLevels = new ArrayList<LightCoord>(ALLOCATION_SIZE);
	private static final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
	private static int nextCoord = 0;

	private static float surfaceRotationAngle = 0F;
	

	static {
		for (int i = 0; i < ALLOCATION_SIZE; i++)
			lightLevels.add(new LightCoord());

		for (int i = 0; i < VALUES.length; i++) {
			VALUES[i] = Integer.toString(i);
		}

	}

	private static LightCoord nextCoord() {
		if (nextCoord == lightLevels.size())
			lightLevels.add(new LightCoord());
		return lightLevels.get(nextCoord++);
	}

	private static final Frustum frustum = new Frustum();

	protected static boolean inFrustum(final double x, final double y, final double z) {
		return frustum.isBoxInFrustum(x, y, z, x, y, z);
	}

	protected static boolean renderLightLevel(@Nonnull final IBlockState state, @Nonnull final IBlockState below) {
		final Material stateMaterial = state.getMaterial();
		final Material belowMaterial = below.getMaterial();

		if (!stateMaterial.isSolid() && !stateMaterial.isLiquid() && belowMaterial.isSolid())
			return true;

		return false;
	}

	protected static boolean canMobSpawn(@Nonnull final BlockPos pos) {
		return WorldEntitySpawner.canCreatureTypeSpawnAtLocation(SpawnPlacementType.ON_GROUND, EnvironState.getWorld(),
				pos);
	}

	protected static float heightAdjustment(@Nonnull final IBlockState state, @Nonnull final IBlockState below,
			@Nonnull final BlockPos pos) {
		if (state.getBlock() == Blocks.AIR) {
			final AxisAlignedBB box = below.getCollisionBoundingBox(EnvironState.getWorld(), pos.down());
			return box == null ? 0 : (float) box.maxY - 1F;
		}

		final AxisAlignedBB box = state.getCollisionBoundingBox(EnvironState.getWorld(), pos);
		if (box == null)
			return 0F;
		final float adjust = (float) (box.maxY);
		return state.getBlock() == Blocks.SNOW_LAYER ? adjust + 0.125F : adjust;
	}

	protected static void updateLightInfo(@Nonnull final RenderManager manager, final double x, final double y,
			final double z) {
		final long tick = EnvironState.getTickCounter();
		if (tick == 0 || tick % 4 != 0)
			return;

		final boolean isThirdPerson = manager.options.thirdPersonView == 2;

		// Position frustum behind the player in order to reduce
		// clipping of nearby light level textures. Purpose of the
		// frustum is to reduce processing requirements and does
		// not have to be perfect.
		final EntityPlayer player = EnvironState.getPlayer();
		final Vec3d lookVec = player.getLookVec();
		final double fX, fY, fZ;
		if (isThirdPerson) {
			fX = x + lookVec.xCoord * 2D;
			fY = y + lookVec.yCoord * 2D;
			fZ = z + lookVec.zCoord * 2D;

		} else {
			fX = x - lookVec.xCoord * 2D;
			fY = y - lookVec.yCoord * 2D;
			fZ = z - lookVec.zCoord * 2D;
		}
		frustum.setPosition(fX, fY, fZ);
		nextCoord = 0;

		EnumFacing playerFacing = player.getHorizontalFacing();
		if (isThirdPerson)
			playerFacing = playerFacing.getOpposite();
		surfaceRotationAngle = ROTATION[playerFacing.getIndex() - 2];

		final ColorSet colors = ColorSet.getStyle(ModOptions.llColors);
		final Mode displayMode = Mode.getMode(ModOptions.llDisplayMode);
		final int skyLightSub = EnvironState.getWorld().calculateSkylightSubtracted(1.0F);
		final int rangeXZ = ModOptions.llBlockRange * 2 + 1;
		final int rangeY = ModOptions.llBlockRange + 1;
		final int originX = MathHelper.floor_double(x) - (rangeXZ / 2);
		final int originZ = MathHelper.floor_double(z) - (rangeXZ / 2);
		final int originY = MathHelper.floor_double(y) - (rangeY - 3);

		blocks.setWorld(EnvironState.getWorld());
		
		for (int dX = 0; dX < rangeXZ; dX++)
			for (int dZ = 0; dZ < rangeXZ; dZ++) {

				final int trueX = originX + dX;
				final int trueZ = originZ + dZ;
				
				if(!blocks.isAvailable(trueX, trueZ))
					return;
				
				IBlockState lastState = null;

				for (int dY = 0; dY < rangeY; dY++) {

					final int trueY = originY + dY;

					if (trueY < 1 || !inFrustum(trueX, trueY, trueZ))
						continue;

					final IBlockState state = blocks.getBlockState(trueX, trueY, trueZ);

					if (lastState == null)
						lastState = blocks.getBlockState(trueX, trueY - 1, trueZ);

					if (renderLightLevel(state, lastState)) {
						mutable.setPos(trueX, trueY, trueZ);

						final boolean mobSpawn = canMobSpawn(mutable);
						if (mobSpawn || !ModOptions.llHideSafe) {
							final int blockLight = blocks.getLightFor(EnumSkyBlock.BLOCK, mutable);
							final int skyLight = blocks.getLightFor(EnumSkyBlock.SKY, mutable) - skyLightSub;
							final int effective = Math.max(blockLight, skyLight);
							final int result = displayMode == Mode.BLOCK_SKY ? effective : blockLight;

							int color = colors.safe;
							if (!mobSpawn) {
								color = colors.noSpawn;
							} else if (blockLight <= ModOptions.llSpawnThreshold) {
								if (effective > ModOptions.llSpawnThreshold)
									color = colors.caution;
								else
									color = colors.hazard;
							}

							if (!(color == colors.safe && ModOptions.llHideSafe)) {
								final LightCoord coord = nextCoord();
								coord.x = trueX;
								coord.y = trueY + heightAdjustment(state, lastState, mutable);
								coord.z = trueZ;
								coord.text = VALUES[result];
								coord.color = color;
							}
						}
					}

					lastState = state;
				}
			}
	}

	@SubscribeEvent
	public static void doRender(@Nonnull final RenderWorldLastEvent event) {

		if (!showHUD)
			return;

		final RenderManager manager = Minecraft.getMinecraft().getRenderManager();
		final DisplayStyle displayStyle = DisplayStyle.getStyle(ModOptions.llStyle);

		updateLightInfo(manager, manager.viewerPosX, manager.viewerPosY, manager.viewerPosZ);

		if (nextCoord == 0)
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

		final boolean thirdPerson = manager.options.thirdPersonView == 2;
		final float pitch = manager.playerViewX * (thirdPerson ? -1 : 1);
		final float yaw = -manager.playerViewY;

		for (int i = 0; i < nextCoord; i++) {
			final LightCoord coord = lightLevels.get(i);
			final double x = coord.x - manager.viewerPosX;
			final double y = coord.y - manager.viewerPosY;
			final double z = coord.z - manager.viewerPosZ;
			GlStateManager.pushMatrix();
			displayStyle.render(x, y, z, yaw, pitch, coord);
			GlStateManager.popMatrix();
		}

		GlStateManager.disableAlpha();
		GlStateManager.disableBlend();
		GlStateManager.popAttrib();
		GlStateManager.popMatrix();
	}

}
