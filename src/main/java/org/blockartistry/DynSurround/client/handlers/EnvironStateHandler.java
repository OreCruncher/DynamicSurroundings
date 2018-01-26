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

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.api.events.EnvironmentEvent;
import org.blockartistry.DynSurround.client.ClientRegistry;
import org.blockartistry.DynSurround.client.event.DiagnosticEvent;
import org.blockartistry.DynSurround.client.handlers.scanners.BattleScanner;
import org.blockartistry.DynSurround.client.weather.WeatherProperties;
import org.blockartistry.DynSurround.expression.ExpressionEngine;
import org.blockartistry.DynSurround.registry.ArmorClass;
import org.blockartistry.DynSurround.registry.BiomeInfo;
import org.blockartistry.DynSurround.registry.BiomeRegistry;
import org.blockartistry.DynSurround.registry.DimensionInfo;
import org.blockartistry.DynSurround.registry.DimensionRegistry;
import org.blockartistry.DynSurround.registry.SeasonRegistry;
import org.blockartistry.DynSurround.registry.SeasonType;
import org.blockartistry.DynSurround.registry.TemperatureRating;
import org.blockartistry.lib.MinecraftClock;
import org.blockartistry.lib.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EnvironStateHandler extends EffectHandlerBase {

	public static class EnvironState {

		// State that is gathered from the various sources
		// to avoid requery. Used during the tick.
		private static String biomeName;
		private static BiomeInfo playerBiome = null;
		private static BiomeInfo truePlayerBiome = null;
		private static SeasonType season;
		private static int dimensionId;
		private static String dimensionName;
		private static DimensionInfo dimInfo = DimensionInfo.NONE;
		private static BlockPos playerPosition;
		private static EntityPlayer player;
		private static World world;
		private static boolean freezing;
		private static boolean humid;
		private static boolean dry;
		private static TemperatureRating playerTemperature;
		private static TemperatureRating biomeTemperature;
		private static boolean inside;
		private static ArmorClass armorClass;
		private static ArmorClass footArmorClass;
		private static boolean inVillage;

		private static boolean isUnderground;
		private static boolean isInSpace;
		private static boolean isInClouds;

		private static int lightLevel;
		private static int tickCounter;

		private static MinecraftClock clock = new MinecraftClock();
		private static BattleScanner battle = new BattleScanner();

		private static BlockPos getPlayerPos() {
			return new BlockPos(player.posX, player.getEntityBoundingBox().minY, player.posZ);
		}

		private static void reset() {
			final BiomeInfo WTF = ClientRegistry.BIOME.WTF_INFO;
			biomeName = StringUtils.EMPTY;
			playerBiome = WTF;
			truePlayerBiome = WTF;
			season = SeasonType.NONE;
			dimensionId = 0;
			dimensionName = StringUtils.EMPTY;
			dimInfo = DimensionInfo.NONE;
			playerPosition = BlockPos.ORIGIN;
			player = null;
			world = null;
			freezing = false;
			humid = false;
			dry = false;
			playerTemperature = TemperatureRating.MILD;
			biomeTemperature = TemperatureRating.MILD;
			inside = false;
			armorClass = ArmorClass.NONE;
			footArmorClass = ArmorClass.NONE;
			inVillage = false;
			isUnderground = false;
			isInSpace = false;
			isInClouds = false;
			lightLevel = 0;
			tickCounter = 0;
			clock = new MinecraftClock();
			battle = new BattleScanner();
		}

		private static void tick(final World world, final EntityPlayer player) {

			final BiomeRegistry biomes = ClientRegistry.BIOME;
			final SeasonRegistry seasons = ClientRegistry.SEASON;
			final DimensionRegistry dimensions = ClientRegistry.DIMENSION;
			
			EnvironState.player = player;
			EnvironState.world = player.world;
			EnvironState.dimInfo = dimensions.getData(player.world);
			EnvironState.clock.update(EnvironState.world);
			EnvironState.playerBiome = PlayerUtils.getPlayerBiome(player, false);
			EnvironState.biomeName = EnvironState.playerBiome.getBiomeName();
			EnvironState.season = seasons.getSeasonType(world);
			EnvironState.dimensionId = world.provider.getDimension();
			EnvironState.dimensionName = world.provider.getDimensionType().getName();
			EnvironState.playerPosition = getPlayerPos();
			EnvironState.inside = AreaSurveyHandler.isReallyInside();

			EnvironState.truePlayerBiome = PlayerUtils.getPlayerBiome(player, true);
			EnvironState.freezing = EnvironState.truePlayerBiome
					.getFloatTemperature(EnvironState.playerPosition) < 0.15F;
			EnvironState.playerTemperature = seasons.getPlayerTemperature(world);
			EnvironState.biomeTemperature = seasons.getBiomeTemperature(world, getPlayerPosition());
			EnvironState.humid = EnvironState.truePlayerBiome.isHighHumidity();
			EnvironState.dry = EnvironState.truePlayerBiome.getRainfall() < 0.2F;

			EnvironState.armorClass = ArmorClass.effectiveArmorClass(player);
			EnvironState.footArmorClass = ArmorClass.footArmorClass(player);

			EnvironState.isUnderground = EnvironState.playerBiome == biomes.UNDERGROUND_INFO;
			EnvironState.isInSpace = EnvironState.playerBiome == biomes.OUTERSPACE_INFO;
			EnvironState.isInClouds = EnvironState.playerBiome == biomes.CLOUDS_INFO;

			final BlockPos pos = EnvironState.getPlayerPosition();
			final int blockLight = world.getLightFor(EnumSkyBlock.BLOCK, pos);
			final int skyLight = world.getLightFor(EnumSkyBlock.SKY, pos) - world.calculateSkylightSubtracted(1.0F);
			EnvironState.lightLevel = Math.max(blockLight, skyLight);

			// Trigger the battle scanner
			if (ModOptions.enableBattleMusic)
				EnvironState.battle.update();
			else
				EnvironState.battle.reset();

			if (!Minecraft.getMinecraft().isGamePaused())
				EnvironState.tickCounter++;
		}

		public static MinecraftClock getClock() {
			return clock;
		}

		public static BattleScanner getBattleScanner() {
			return battle;
		}
		
		public static DimensionInfo getDimensionInfo() {
			return dimInfo;
		}

		public static BiomeInfo getPlayerBiome() {
			return playerBiome;
		}

		public static BiomeInfo getTruePlayerBiome() {
			return truePlayerBiome;
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
				return player == null || ep.getUniqueID().equals(player.getUniqueID());
			}
			return false;
		}

		public static boolean isPlayer(final UUID id) {
			return player == null || player.getUniqueID().equals(id);
		}
		
		public static boolean isPlayer(final int id) {
			return player == null || player.getEntityId() == id;
		}

		public static boolean isCreative() {
			return player != null && player.capabilities.isCreativeMode;
		}

		public static boolean isPlayerHurt() {
			return player != null && ModOptions.playerHurtThreshold != 0 && !isCreative()
					&& player.getHealth() <= ModOptions.playerHurtThreshold;
		}

		public static boolean isPlayerHungry() {
			return player != null && ModOptions.playerHungerThreshold != 0 && !isCreative()
					&& player.getFoodStats().getFoodLevel() <= ModOptions.playerHungerThreshold;
		}

		public static boolean isPlayerBurning() {
			return player != null && player.isBurning();
		}

		public static boolean isPlayerSuffocating() {
			return player != null && player.getAir() <= 0;
		}

		public static boolean isPlayerFlying() {
			return player != null && player.capabilities.isFlying;
		}

		public static boolean isPlayerSprinting() {
			return player != null && player.isSprinting();
		}

		public static boolean isPlayerInLava() {
			return player != null && player.isInLava();
		}

		public static boolean isPlayerInvisible() {
			return player != null && player.isInvisible();
		}

		public static boolean isPlayerBlind() {
			return player != null && player.isPotionActive(MobEffects.BLINDNESS);
		}

		public static boolean isPlayerInWater() {
			return player != null && player.isInWater();
		}

		public static boolean isPlayerRiding() {
			return player != null && player.isRiding();
		}

		public static boolean isPlayerOnGround() {
			return player != null && player.onGround;
		}

		public static boolean isPlayerMoving() {
			return player != null && player.distanceWalkedModified != player.prevDistanceWalkedModified;
		}
		
		public static boolean isPlayerSneaking() {
			return player != null && player.isSneaking();
		}

		public static boolean isPlayerInside() {
			return inside;
		}

		public static boolean isPlayerUnderground() {
			return isUnderground;
		}

		public static boolean isPlayerInSpace() {
			return isInSpace;
		}

		public static boolean isPlayerInClouds() {
			return isInClouds;
		}

		public static boolean isFreezing() {
			return freezing;
		}

		public static boolean isHumid() {
			return humid;
		}

		public static boolean isDry() {
			return dry;
		}

		public static ArmorClass getPlayerArmorClass() {
			return armorClass;
		}

		public static ArmorClass getPlayerFootArmorClass() {
			return footArmorClass;
		}

		public static boolean inVillage() {
			return inVillage;
		}

		public static int getLightLevel() {
			return lightLevel;
		}

		public static int getTickCounter() {
			return tickCounter;
		}
		
		public static float getPartialTick() {
			return Minecraft.getMinecraft().getRenderPartialTicks();
		}

		public static double distanceToPlayer(final double x, final double y, final double z) {
			return player != null ? player.getDistanceSq(x, y, z) : 0D;
		}
	}

	public EnvironStateHandler() {
		super("EnvironStateEffectHandler");
	}
	
	@Override
	public void process(@Nonnull final EntityPlayer player) {
		EnvironState.tick(player.world, player);
		ExpressionEngine.instance().update();
	}

	/**
	 * Hook the entity join world event so we can get the player and world info
	 * ASAP.
	 */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onEntityJoin(@Nonnull final EntityJoinWorldEvent event) {
		if (event.getWorld().isRemote && event.getEntity() instanceof EntityPlayerSP) {
			EnvironState.tick(event.getWorld(), (EntityPlayer) event.getEntity());
		}
	}

	@SubscribeEvent
	public void onEnvironmentEvent(@Nonnull final EnvironmentEvent event) {
		EnvironState.inVillage = event.inVillage;
	}

	@Override
	public void onConnect() {
		EnvironState.reset();
		ExpressionEngine.instance();
	}

	@Override
	public void onDisconnect() {
		EnvironState.reset();
	}

	// Use the new scripting system to pull out data to display
	// for debug. Good for testing.
	private final static String[] scripts = { "'Dim: ' + player.dimension + '/' + player.dimensionName",
			"'Biome: ' + biome.name + '; Temp ' + biome.temperature + '/' + biome.temperatureValue + ' rainfall: ' + biome.rainfall",
			"'Weather: ' + IF(weather.isRaining,'rainfall: ' + weather.rainfall,'not raining') + IF(weather.isThundering,' thundering','') + ' Temp: ' + weather.temperature + '/' + weather.temperatureValue",
			"'Season: ' + season  + IF(isNight,' night',' day') + IF(player.isInside,' inside',' outside')",
			"'Player: Temp ' + player.temperature + '; health ' + player.health + '/' + player.maxHealth + '; food ' + player.food.level + '; saturation ' + player.food.saturation + IF(player.isHurt,' isHurt','') + IF(player.isHungry,' isHungry','') + ' pos: (' + player.X + ',' + player.Y + ',' + player.Z + ') light: ' + player.lightLevel",
			"'Village: ' + player.inVillage" };

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void diagnostics(final DiagnosticEvent.Gather event) {
		for (final String s : scripts) {
			final String result = ExpressionEngine.instance().eval(s).toString();
			event.output.add(result);
		}

		event.output.add(WeatherProperties.diagnostic());
		event.output.add("Aurora: " + (AuroraEffectHandler.getCurrentAurora() == null ? "NONE"
				: AuroraEffectHandler.getCurrentAurora().toString()));

		final List<String> badScripts = ExpressionEngine.instance().getNaughtyList();
		for (final String s : badScripts) {
			event.output.add("BAD SCRIPT: " + s);
		}
	}

}
