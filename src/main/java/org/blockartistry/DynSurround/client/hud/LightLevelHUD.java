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

package org.blockartistry.DynSurround.client.hud;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.ClientChunkCache;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.event.ReloadEvent;
import org.blockartistry.lib.Color;
import org.blockartistry.lib.chunk.IBlockAccessEx;
import org.blockartistry.lib.collections.ObjectArray;
import org.blockartistry.lib.gfx.OpenGlState;
import org.blockartistry.lib.gfx.OpenGlUtil;
import org.blockartistry.lib.math.MathStuff;
import org.lwjgl.opengl.GL11;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class LightLevelHUD extends GuiOverlay {

	private static FontRenderer font;
	private static LightLevelTextureSheet sheet;

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

		//
		BRIGHT(Color.MC_GREEN, Color.MC_YELLOW, Color.MC_RED, Color.MC_DARKAQUA),
		//
		DARK(Color.MC_DARKGREEN, Color.MC_GOLD, Color.MC_DARKRED, Color.MC_DARKBLUE);

		public static final float ALPHA = 1F; // 0.75F;

		public final Color safe;
		public final Color caution;
		public final Color hazard;
		public final Color noSpawn;

		private ColorSet(@Nonnull final Color safe, @Nonnull final Color caution, @Nonnull final Color hazard,
				@Nonnull final Color noSpawn) {
			this.safe = safe;
			this.caution = caution;
			this.hazard = hazard;
			this.noSpawn = noSpawn;
		}

		public static ColorSet getStyle(final int v) {
			if (v >= values().length)
				return BRIGHT;
			return values()[v];
		}

	}

	private static final class LightCoord {
		public int x;
		public double y;
		public int z;
		public int lightLevel;
		public Color color;
	}

	public static boolean showHUD = false;

	private static final int ALLOCATION_SIZE = 2048;
	private static final ObjectArray<LightCoord> lightLevels = new ObjectArray<>(ALLOCATION_SIZE);
	private static final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
	private static int nextCoord = 0;

	static {
		for (int i = 0; i < ALLOCATION_SIZE; i++)
			lightLevels.add(new LightCoord());
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

		font = Minecraft.getMinecraft().fontRenderer;
		final boolean isThirdPerson = manager.options.thirdPersonView == 2;

		// Position frustum behind the player in order to reduce
		// clipping of nearby light level textures. Purpose of the
		// frustum is to reduce processing requirements and does
		// not have to be perfect.
		final EntityPlayer player = EnvironState.getPlayer();
		final Vec3d lookVec = player.getLookVec();
		final double fX, fY, fZ;
		if (isThirdPerson) {
			fX = x + lookVec.x * 2D;
			fY = y + lookVec.y * 2D;
			fZ = z + lookVec.z * 2D;

		} else {
			fX = x - lookVec.x * 2D;
			fY = y - lookVec.y * 2D;
			fZ = z - lookVec.z * 2D;
		}
		frustum.setPosition(fX, fY, fZ);
		nextCoord = 0;

		final ColorSet colors = ColorSet.getStyle(ModOptions.lightlevel.llColors);
		final Mode displayMode = Mode.getMode(ModOptions.lightlevel.llDisplayMode);
		final int skyLightSub = EnvironState.getWorld().calculateSkylightSubtracted(1.0F);
		final int rangeXZ = ModOptions.lightlevel.llBlockRange * 2 + 1;
		final int rangeY = ModOptions.lightlevel.llBlockRange + 1;
		final int originX = MathStuff.floor(x) - (rangeXZ / 2);
		final int originZ = MathStuff.floor(z) - (rangeXZ / 2);
		final int originY = MathStuff.floor(y) - (rangeY - 3);

		final IBlockAccessEx blocks = ClientChunkCache.INSTANCE;

		for (int dX = 0; dX < rangeXZ; dX++)
			for (int dZ = 0; dZ < rangeXZ; dZ++) {

				final int trueX = originX + dX;
				final int trueZ = originZ + dZ;

				if (!blocks.isAvailable(trueX, trueZ))
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
						if (mobSpawn || !ModOptions.lightlevel.llHideSafe) {
							final int blockLight = blocks.getLightFor(EnumSkyBlock.BLOCK, mutable);
							final int skyLight = blocks.getLightFor(EnumSkyBlock.SKY, mutable) - skyLightSub;
							final int effective = Math.max(blockLight, skyLight);
							final int result = displayMode == Mode.BLOCK_SKY ? effective : blockLight;

							Color color = colors.safe;
							if (!mobSpawn) {
								color = colors.noSpawn;
							} else if (blockLight <= ModOptions.lightlevel.llSpawnThreshold) {
								if (effective > ModOptions.lightlevel.llSpawnThreshold)
									color = colors.caution;
								else
									color = colors.hazard;
							}

							if (!(color == colors.safe && ModOptions.lightlevel.llHideSafe)) {
								final LightCoord coord = nextCoord();
								coord.x = trueX;
								coord.y = trueY + heightAdjustment(state, lastState, mutable);
								coord.z = trueZ;
								coord.lightLevel = result;
								coord.color = color;
							}
						}
					}

					lastState = state;
				}
			}
	}

	@Override
	public void doTick(final int tickRef) {
		if (!showHUD || tickRef == 0 || tickRef % 3 != 0)
			return;

		final RenderManager manager = Minecraft.getMinecraft().getRenderManager();
		updateLightInfo(manager, manager.viewerPosX, manager.viewerPosY, manager.viewerPosZ);
	}

	@SubscribeEvent
	public static void doRender(@Nonnull final RenderWorldLastEvent event) {
		if (!showHUD || nextCoord == 0)
			return;

		final EntityPlayer player = EnvironState.getPlayer();
		if (player == null)
			return;

		final RenderManager manager = Minecraft.getMinecraft().getRenderManager();

		final OpenGlState glState = OpenGlState.push();

		GlStateManager.enableTexture2D();
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableDepth();
		GlStateManager.depthFunc(GL11.GL_LEQUAL);
		GlStateManager.depthMask(true);

		if (useOldRenderMethod())
			drawStringRender(player, manager);
		else
			textureRender(player, manager);

		OpenGlState.pop(glState);
	}

	private static void drawStringRender(final EntityPlayer player, final RenderManager manager) {

		final boolean thirdPerson = manager.options.thirdPersonView == 2;
		EnumFacing playerFacing = player.getHorizontalFacing();
		if (thirdPerson)
			playerFacing = playerFacing.getOpposite();
		if (playerFacing == EnumFacing.SOUTH || playerFacing == EnumFacing.NORTH)
			playerFacing = playerFacing.getOpposite();
		final float rotationAngle = playerFacing.getOpposite().getHorizontalAngle();

		for (int i = 0; i < nextCoord; i++) {
			final LightCoord coord = lightLevels.get(i);
			final double x = coord.x - manager.viewerPosX;
			final double y = coord.y - manager.viewerPosY;
			final double z = coord.z - manager.viewerPosZ;

			final String text = String.valueOf(coord.lightLevel);
			final int margin = -(font.getStringWidth(text) + 1) / 2;
			final double scale = 0.08D;

			GlStateManager.pushMatrix();
			GlStateManager.translate(x + 0.5D, y, z + 0.5D);
			GlStateManager.rotate(rotationAngle, 0F, 1F, 0F);
			GlStateManager.translate(-0.05D, 0.0005D, 0.3D);
			GlStateManager.rotate(90F, 1F, 0F, 0F);
			GlStateManager.scale(-scale, -scale, scale);
			GlStateManager.translate(0.3F, 0.3F, 0F);
			font.drawString(text, margin, 0, Color.BLACK.rgbWithAlpha(0.99F), false);
			GlStateManager.translate(-0.3F, -0.3F, -0.001F);
			font.drawString(text, margin, 0, coord.color.rgbWithAlpha(0.99F), false);
			GlStateManager.popMatrix();
		}
	}

	private static void textureRender(final EntityPlayer player, final RenderManager manager) {

		final boolean isThirdPerson = manager.options.thirdPersonView == 2;
		EnumFacing playerFacing = player.getHorizontalFacing();
		if (isThirdPerson)
			playerFacing = playerFacing.getOpposite();

		final float rotationAngle = playerFacing.getOpposite().getHorizontalAngle();

		sheet.bindTexture();
		final BufferBuilder renderer = Tessellator.getInstance().getBuffer();

		for (int i = 0; i < nextCoord; i++) {

			final LightCoord coord = lightLevels.get(i);
			final double x = coord.x - manager.viewerPosX;
			final double y = coord.y - manager.viewerPosY;
			final double z = coord.z - manager.viewerPosZ;

			final Vec2f U = sheet.getMinMaxU(coord.lightLevel);
			final Vec2f V = sheet.getMinMaxV(coord.lightLevel);

			GlStateManager.pushMatrix();
			GlStateManager.translate(x + 0.5D, y, z + 0.5D);
			GlStateManager.rotate(-rotationAngle, 0F, 1F, 0F);
			GlStateManager.translate(-0.5D, 0.0005D, -0.5D);
			GlStateManager.rotate(90F, 1F, 0F, 0F);

			final float red = coord.color.red;
			final float green = coord.color.green;
			final float blue = coord.color.blue;
			final float alpha = ColorSet.ALPHA;

			renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			renderer.pos(0F, 1F, 0F).tex(U.x, V.x).color(red, green, blue, alpha).endVertex();
			renderer.pos(1F, 1F, 0F).tex(U.y, V.x).color(red, green, blue, alpha).endVertex();
			renderer.pos(1F, 0F, 0F).tex(U.y, V.y).color(red, green, blue, alpha).endVertex();
			renderer.pos(0F, 0F, 0F).tex(U.x, V.y).color(red, green, blue, alpha).endVertex();
			Tessellator.getInstance().draw();

			GlStateManager.popMatrix();
		}
	}

	private static boolean useOldRenderMethod() {
		return !OpenGlUtil.areFrameBuffersSafe();
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void resourceReloadEvent(final ReloadEvent.Resources event) {
		if (useOldRenderMethod()) {
			DSurround.log().info("Either OptiFine is installed or Framebuffers are disabled");
			DSurround.log().info("Using drawString method for light level HUD render");
			if (sheet != null) {
				sheet.release();
				sheet = null;
			}
		} else {
			DSurround.log().info("Using cached texture method for light level HUD render");
			if (sheet == null)
				sheet = new LightLevelTextureSheet();
			sheet.updateTexture();
		}
	}

}
