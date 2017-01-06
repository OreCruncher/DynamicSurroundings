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

package org.blockartistry.mod.DynSurround.client.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.DSurround;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.event.DiagnosticEvent;
import org.blockartistry.mod.DynSurround.client.weather.WeatherProperties;
import org.blockartistry.mod.DynSurround.registry.BiomeInfo;
import org.blockartistry.mod.DynSurround.registry.BiomeRegistry;
import org.blockartistry.mod.DynSurround.registry.DimensionRegistry;
import org.blockartistry.mod.DynSurround.registry.Evaluator;
import org.blockartistry.mod.DynSurround.registry.RegistryManager;
import org.blockartistry.mod.DynSurround.registry.RegistryManager.RegistryType;
import org.blockartistry.mod.DynSurround.registry.SeasonRegistry;
import org.blockartistry.mod.DynSurround.registry.SeasonType;
import org.blockartistry.mod.DynSurround.registry.TemperatureRating;
import org.blockartistry.mod.DynSurround.util.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class EnvironStateHandler extends EffectHandlerBase {

	// Diagnostic strings to display in the debug HUD
	private static List<String> diagnostics = new ArrayList<String>();

	public static List<String> getDiagnostics() {
		return diagnostics;
	}

	public static class EnvironState {

		// State that is gathered from the various sources
		// to avoid requery. Used during the tick.
		private static String conditions = "";
		private static String biomeName = "";
		private static BiomeInfo playerBiome = null;
		private static SeasonType season = SeasonType.NONE;
		private static int dimensionId;
		private static String dimensionName;
		private static BlockPos playerPosition;
		private static EntityPlayer player;
		private static World world;
		private static boolean freezing;
		private static boolean fog;
		private static boolean humid;
		private static boolean dry;
		private static TemperatureRating playerTemperature = TemperatureRating.MILD;
		private static TemperatureRating biomeTemperature = TemperatureRating.MILD;
		private static boolean inside;

		private static int tickCounter;

		private static final String CONDITION_TOKEN_HURT = "hurt";
		private static final String CONDITION_TOKEN_HUNGRY = "hungry";
		private static final String CONDITION_TOKEN_BURNING = "burning";
		private static final String CONDITION_TOKEN_NOAIR = "noair";
		private static final String CONDITION_TOKEN_FLYING = "flying";
		private static final String CONDITION_TOKEN_SPRINTING = "sprinting";
		private static final String CONDITION_TOKEN_INLAVA = "inlava";
		private static final String CONDITION_TOKEN_INWATER = "inwater";
		private static final String CONDITION_TOKEN_INVISIBLE = "invisible";
		private static final String CONDITION_TOKEN_BLIND = "blind";
		private static final String CONDITION_TOKEN_MINECART = "ridingminecart";
		private static final String CONDITION_TOKEN_HORSE = "ridinghorse";
		private static final String CONDITION_TOKEN_BOAT = "ridingboat";
		private static final String CONDITION_TOKEN_PIG = "ridingpig";
		private static final String CONDITION_TOKEN_RIDING = "riding";
		private static final String CONDITION_TOKEN_FREEZING = "freezing";
		private static final String CONDITION_TOKEN_FOG = "fog";
		private static final String CONDITION_TOKEN_HUMID = "humid";
		private static final String CONDITION_TOKEN_DRY = "dry";
		private static final String CONDITION_TOKEN_INSIDE = "inside";
		private static final char CONDITION_SEPARATOR = '#';

		private static String getPlayerConditions(final EntityPlayer player) {
			final StringBuilder builder = new StringBuilder();

			builder.append(CONDITION_SEPARATOR).append(season.getValue());

			if (isPlayerHurt())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_HURT);
			if (isPlayerHungry())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_HUNGRY);
			if (isPlayerBurning())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_BURNING);
			if (isPlayerSuffocating())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_NOAIR);
			if (isPlayerFlying())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_FLYING);
			if (isPlayerSprinting())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_SPRINTING);
			if (isPlayerInLava())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_INLAVA);
			if (isPlayerInvisible())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_INVISIBLE);
			if (isPlayerBlind())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_BLIND);
			if (isPlayerInWater())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_INWATER);
			if (isFreezing())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_FREEZING);
			if (isFoggy())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_FOG);
			if (isHumid())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_HUMID);
			if (isDry())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_DRY);
			if (isPlayerInside())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_INSIDE);
			if (isPlayerRiding()) {
				builder.append(CONDITION_SEPARATOR);
				if (player.getRidingEntity() instanceof EntityMinecart)
					builder.append(CONDITION_TOKEN_MINECART);
				else if (player.getRidingEntity() instanceof EntityHorse)
					builder.append(CONDITION_TOKEN_HORSE);
				else if (player.getRidingEntity() instanceof EntityBoat)
					builder.append(CONDITION_TOKEN_BOAT);
				else if (player.getRidingEntity() instanceof EntityPig)
					builder.append(CONDITION_TOKEN_PIG);
				else
					builder.append(CONDITION_TOKEN_RIDING);
			}
			builder.append(CONDITION_SEPARATOR).append(playerTemperature.getValue());
			builder.append(CONDITION_SEPARATOR);
			return builder.toString();
		}

		private static BlockPos getPlayerPos() {
			return new BlockPos(player.posX, player.getEntityBoundingBox().minY, player.posZ);
		}

		private static void tick(final World world, final EntityPlayer player) {

			final DimensionRegistry dimensions = RegistryManager.get(RegistryType.DIMENSION);
			final SeasonRegistry seasons = RegistryManager.get(RegistryType.SEASON);

			EnvironState.player = player;
			EnvironState.world = player.worldObj;
			EnvironState.playerBiome = PlayerUtils.getPlayerBiome(player, false);
			EnvironState.biomeName = EnvironState.playerBiome.getBiomeName();
			EnvironState.season = seasons.getData(world).getSeasonType();
			EnvironState.dimensionId = world.provider.getDimension();
			EnvironState.dimensionName = world.provider.getDimensionType().getName();
			EnvironState.playerPosition = getPlayerPos();
			EnvironState.fog = FogEffectHandler.currentFogLevel() >= 0.01F;
			EnvironState.inside = AreaSurveyHandler.isReallyInside();

			final BiomeInfo trueBiome = PlayerUtils.getPlayerBiome(player, true);
			EnvironState.freezing = trueBiome.getFloatTemperature(EnvironState.playerPosition) < 0.15F;
			EnvironState.playerTemperature = seasons.getPlayerTemperature(world);
			EnvironState.biomeTemperature = seasons.getBiomeTemperature(world, getPlayerPosition());
			EnvironState.humid = trueBiome.isHighHumidity();
			EnvironState.dry = trueBiome.getRainfall() == 0;

			EnvironState.conditions = dimensions.getConditions(world) + getPlayerConditions(player);

			if (!Minecraft.getMinecraft().isGamePaused())
				EnvironState.tickCounter++;
		}

		public static String getConditions() {
			return conditions;
		}

		public static BiomeInfo getPlayerBiome() {
			return playerBiome;
		}

		public static String getBiomeName() {
			return biomeName;
		}

		public static SeasonType getSeason() {
			return season;
		}

		public static TemperatureRating getPlayerTemperature() {
			return playerTemperature;
		}

		public static TemperatureRating getBiomeTemperature() {
			return biomeTemperature;
		}

		public static int getDimensionId() {
			return dimensionId;
		}

		public static String getDimensionName() {
			return dimensionName;
		}

		public static EntityPlayer getPlayer() {
			return player;
		}

		public static World getWorld() {
			return world;
		}

		public static BlockPos getPlayerPosition() {
			return playerPosition;
		}

		public static boolean isPlayer(final Entity entity) {
			if (entity instanceof EntityPlayer) {
				final EntityPlayer ep = (EntityPlayer) entity;
				return ep.getUniqueID().equals(player.getUniqueID());
			}
			return false;
		}

		public static boolean isPlayer(final UUID id) {
			return player.getUniqueID().equals(id);
		}

		public static boolean isCreative() {
			return player.capabilities.isCreativeMode;
		}

		public static boolean isPlayerHurt() {
			return ModOptions.playerHurtThreshold != 0 && !isCreative()
					&& player.getHealth() <= ModOptions.playerHurtThreshold;
		}

		public static boolean isPlayerHungry() {
			return ModOptions.playerHungerThreshold != 0 && !isCreative()
					&& player.getFoodStats().getFoodLevel() <= ModOptions.playerHungerThreshold;
		}

		public static boolean isPlayerBurning() {
			return player.isBurning();
		}

		public static boolean isPlayerSuffocating() {
			return player.getAir() <= 0;
		}

		public static boolean isPlayerFlying() {
			return player.capabilities.isFlying;
		}

		public static boolean isPlayerSprinting() {
			return player.isSprinting();
		}

		public static boolean isPlayerInLava() {
			return player.isInLava();
		}

		public static boolean isPlayerInvisible() {
			return player.isInvisible();
		}

		public static boolean isPlayerBlind() {
			return player.isPotionActive(MobEffects.BLINDNESS);
		}

		public static boolean isPlayerInWater() {
			return player.isInWater();
		}

		public static boolean isPlayerRiding() {
			return player.isRiding();
		}

		public static boolean isPlayerOnGround() {
			return player.onGround;
		}

		public static boolean isPlayerMoving() {
			return player.distanceWalkedModified != player.prevDistanceWalkedModified;
		}

		public static boolean isPlayerInside() {
			return inside;
		}

		public static boolean isPlayerUnderground() {
			return playerBiome == BiomeRegistry.UNDERGROUND;
		}

		public static boolean isPlayerInSpace() {
			return playerBiome == BiomeRegistry.OUTERSPACE;
		}

		public static boolean isPlayerInClouds() {
			return playerBiome == BiomeRegistry.CLOUDS;
		}

		public static boolean isFreezing() {
			return freezing;
		}

		public static boolean isFoggy() {
			return fog;
		}

		public static boolean isHumid() {
			return humid;
		}

		public static boolean isDry() {
			return dry;
		}

		public static int getTickCounter() {
			return tickCounter;
		}

		public static double distanceToPlayer(final double x, final double y, final double z) {
			return player.getDistanceSq(x, y, z);
		}
	}
	
	@Override
	public String getHandlerName() {
		return "EnvironStateEffectHandler";
	}

	@Override
	public void process(final World world, final EntityPlayer player) {

		EnvironState.tick(world, player);

		// Gather diagnostics if needed
		if (Minecraft.getMinecraft().gameSettings.showDebugInfo && ModOptions.enableDebugLogging) {
			DSurround.getProfiler().startSection("GatherDebug");
			final DiagnosticEvent.Gather gather = new DiagnosticEvent.Gather(world, player);
			MinecraftForge.EVENT_BUS.post(gather);
			diagnostics = gather.output;
			DSurround.getProfiler().endSection();
		} else {
			diagnostics = null;
		}
	}
	
	/**
	 * Hook the entity join world event so we can get the player and world info
	 * ASAP.
	 */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onEntityJoin(@Nonnull final EntityJoinWorldEvent event) {
		if(event.getWorld().isRemote && event.getEntity() instanceof EntityPlayerSP) {
			EnvironState.tick(event.getWorld(), (EntityPlayer)event.getEntity());
		}
	}

	/**
	 * Hook the Forge text event to add on our diagnostics
	 */
	@SubscribeEvent
	public void onGatherText(@Nonnull final RenderGameOverlayEvent.Text event) {
		if (diagnostics != null && !diagnostics.isEmpty()) {
			event.getLeft().add("");
			event.getLeft().addAll(diagnostics);
		}
	}

	@Override
	public void onConnect() {
		diagnostics = null;
	}

	// Use the new scripting system to pull out data to display
	// for debug. Good for testing.
	private final static String[] scripts = { "'Dim: ' + player.dimension + '/' + player.dimensionName",
			"'Biome: ' + biome.name + '; Temp ' + biome.temperature + '/' + biome.temperatureValue + ' rainfall: ' + biome.rainfall",
			"'Weather: ' + IF(weather.isRaining,'rainfall: ' + weather.rainfall,'not raining') + IF(weather.isThundering,' thundering','') + ' Temp: ' + weather.temperature + '/' + weather.temperatureValue",
			"'Season: ' + season  + IF(isNight,' night',' day') + IF(player.isInside,' inside',' outside')",
			"'Player: Temp ' + player.temperature + '; health ' + player.health + '/' + player.maxHealth + '; food ' + player.food.level + '; saturation ' + player.food.saturation + IF(player.isHurt,' isHurt','') + IF(player.isHungry,' isHungry','') + ' pos: (' + player.X + ',' + player.Y + ',' + player.Z + ') light: ' + player.lightLevel", };

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void diagnostics(final DiagnosticEvent.Gather event) {
		for (final String s : scripts) {
			final String result = Evaluator.eval(s).toString();
			event.output.add(result);
		}

		event.output.add(WeatherProperties.diagnostic());
		event.output.add("Conditions: " + EnvironState.getConditions());

		final List<String> badScripts = Evaluator.getNaughtyList();
		for (final String s : badScripts) {
			event.output.add("BAD SCRIPT: " + s);
		}
	}

}
