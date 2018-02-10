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

package org.blockartistry.DynSurround.client.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.ClientRegistry;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.client.handlers.scanners.AlwaysOnBlockEffectScanner;
import org.blockartistry.DynSurround.client.handlers.scanners.RandomBlockEffectScanner;
import org.blockartistry.DynSurround.registry.BiomeInfo;
import org.blockartistry.lib.BlockStateProvider;
import org.blockartistry.lib.WorldUtils;
import org.blockartistry.lib.math.MathStuff;

import gnu.trove.map.custom_hash.TObjectIntCustomHashMap;
import gnu.trove.strategy.IdentityHashingStrategy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class AreaSurveyHandler extends EffectHandlerBase {

	private static final int BIOME_SURVEY_RANGE = 20;
	private static final int INSIDE_SURVEY_RANGE = 3;

	private static final Cell[] cells;
	private static final float TOTAL_POINTS;

	// Used to throttle processing
	private static final int SURVEY_INTERVAL = 2;

	private static int biomeArea;
	private static final TObjectIntCustomHashMap<BiomeInfo> weights = new TObjectIntCustomHashMap<BiomeInfo>(
			IdentityHashingStrategy.INSTANCE);
	private static final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

	// "Finger print" of the last area survey.
	private static BiomeInfo surveyedBiome = null;
	private static int surveyedDimension = 0;
	private static BlockPos surveyedPosition = BlockPos.ORIGIN;

	private static final float INSIDE_THRESHOLD = 1.0F - 65.0F / 176.0F;
	private static float ceilingCoverageRatio = 0.0F;
	private static boolean reallyInside = false;

	protected final RandomBlockEffectScanner effects = new RandomBlockEffectScanner(
			ModOptions.general.specialEffectRange);
	protected final AlwaysOnBlockEffectScanner alwaysOn = new AlwaysOnBlockEffectScanner(
			ModOptions.general.specialEffectRange);

	static {

		final List<Cell> cellList = new ArrayList<Cell>();
		// Build our cell map
		for (int x = -INSIDE_SURVEY_RANGE; x <= INSIDE_SURVEY_RANGE; x++)
			for (int z = -INSIDE_SURVEY_RANGE; z <= INSIDE_SURVEY_RANGE; z++)
				cellList.add(new Cell(new Vec3i(x, 0, z), INSIDE_SURVEY_RANGE));

		// Sort so the highest score cells are first
		Collections.sort(cellList);
		cells = cellList.toArray(new Cell[0]);

		float totalPoints = 0.0F;
		for (final Cell c : cellList)
			totalPoints += c.potentialPoints();
		TOTAL_POINTS = totalPoints;
	}

	public static int getBiomeArea() {
		return biomeArea;
	}

	public static boolean isReallyInside() {
		return reallyInside;
	}

	public static TObjectIntCustomHashMap<BiomeInfo> getBiomes() {
		return weights;
	}

	private static void doCeilingCoverageRatio() {
		final BlockPos pos = EnvironState.getPlayerPosition();
		float score = 0.0F;
		for (int i = 0; i < cells.length; i++)
			score += cells[i].score(pos);

		ceilingCoverageRatio = 1.0F - (score / TOTAL_POINTS);
		reallyInside = ceilingCoverageRatio > INSIDE_THRESHOLD;
	}

	public AreaSurveyHandler() {
		super("Area Survey");
	}

	/*
	 * Perform a biome survey around the player at the specified range.
	 */
	private void doSurvey() {
		biomeArea = 0;
		weights.clear();

		if (EnvironState.getPlayerBiome().isFake()) {
			biomeArea = 1;
			weights.put(EnvironState.getPlayerBiome(), 1);
		} else {
			final World world = EnvironState.getWorld();
			final BlockStateProvider provider = WorldUtils.getDefaultBlockStateProvider().setWorld(world);

			for (int dX = -BIOME_SURVEY_RANGE; dX <= BIOME_SURVEY_RANGE; dX++)
				for (int dZ = -BIOME_SURVEY_RANGE; dZ <= BIOME_SURVEY_RANGE; dZ++) {
					biomeArea++;
					mutable.setPos(surveyedPosition.getX() + dX, surveyedPosition.getY(), surveyedPosition.getZ() + dZ);
					final BiomeInfo biome = ClientRegistry.BIOME.get(provider.getBiome(mutable));
					weights.adjustOrPutValue(biome, 1, 1);
				}
		}
	}

	@Override
	public void process(@Nonnull final EntityPlayer player) {

		this.effects.update();
		this.alwaysOn.update();

		final BlockPos position = EnvironState.getPlayerPosition();

		if (surveyedBiome != EnvironState.getPlayerBiome() || surveyedDimension != EnvironState.getDimensionId()
				|| surveyedPosition.compareTo(position) != 0) {
			surveyedBiome = EnvironState.getPlayerBiome();
			surveyedDimension = EnvironState.getDimensionId();
			surveyedPosition = position;
			doSurvey();
		}

		if (EnvironState.getTickCounter() % SURVEY_INTERVAL == 0)
			doCeilingCoverageRatio();
	}

	@Override
	public void onConnect() {
		weights.clear();
		MinecraftForge.EVENT_BUS.register(this.alwaysOn);
	}

	@Override
	public void onDisconnect() {
		MinecraftForge.EVENT_BUS.unregister(this.alwaysOn);
	}

	private static final class Cell implements Comparable<Cell> {

		private final Vec3i offset;
		private final float points;
		private final BlockPos.MutableBlockPos working;

		public Cell(@Nonnull final Vec3i offset, final int range) {
			this.offset = offset;
			final float xV = range - MathStuff.abs(offset.getX()) + 1;
			final float zV = range - MathStuff.abs(offset.getZ()) + 1;
			final float candidate = Math.min(xV, zV);
			this.points = candidate * candidate;
			this.working = new BlockPos.MutableBlockPos();
		}

		public float potentialPoints() {
			return this.points;
		}

		public float score(@Nonnull final BlockPos playerPos) {
			this.working.setPos(playerPos.getX() + this.offset.getX(), playerPos.getY() + this.offset.getY(),
					playerPos.getZ() + this.offset.getZ());
			final int y = WorldUtils.getTopSolidOrLiquidBlock(EnvironState.getWorld(), this.working).getY();
			return ((y - playerPos.getY()) < 3) ? this.points : 0.0F;
		}

		@Override
		public int compareTo(@Nonnull final Cell cell) {
			// Want big scores first in the list
			return -Float.compare(this.potentialPoints(), cell.potentialPoints());
		}

		@Override
		@Nonnull
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			builder.append(this.offset.toString());
			builder.append(" points: ").append(this.points);
			return builder.toString();
		}

	}

}
