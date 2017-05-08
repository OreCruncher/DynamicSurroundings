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
import java.util.UUID;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.api.events.EnvironmentEvent;
import org.blockartistry.DynSurround.client.event.DiagnosticEvent;
import org.blockartistry.DynSurround.client.event.ServerDataEvent;
import org.blockartistry.DynSurround.client.handlers.scanners.BattleScanner;
import org.blockartistry.DynSurround.client.weather.WeatherProperties;
import org.blockartistry.DynSurround.registry.ArmorClass;
import org.blockartistry.DynSurround.registry.BiomeInfo;
import org.blockartistry.DynSurround.registry.BiomeRegistry;
import org.blockartistry.DynSurround.registry.Evaluator;
import org.blockartistry.DynSurround.registry.RegistryManager;
import org.blockartistry.DynSurround.registry.SeasonRegistry;
import org.blockartistry.DynSurround.registry.SeasonType;
import org.blockartistry.DynSurround.registry.TemperatureRating;
import org.blockartistry.DynSurround.registry.RegistryManager.RegistryType;
import org.blockartistry.lib.MinecraftClock;
import org.blockartistry.lib.PlayerUtils;

import gnu.trove.procedure.TIntDoubleProcedure;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class EnvironStateHandler extends EffectHandlerBase {

	// Diagnostic strings to display in the debug HUD
	private List<String> diagnostics = new ArrayList<String>();

	// TPS status strings to display
	private List<String> serverDataReport = new ArrayList<String>();

	public static class EnvironState {

		// State that is gathered from the various sources
		// to avoid requery. Used during the tick.
		private static String biomeName = "";
		private static BiomeInfo playerBiome = null;
		private static BiomeInfo truePlayerBiome = null;
		private static SeasonType season = SeasonType.NONE;
		private static int dimensionId;
		private static String dimensionName;
		private static BlockPos playerPosition;
		private static EntityPlayer player;
		private static World world;
		private static boolean freezing;
		private static boolean humid;
		private static boolean dry;
		private static TemperatureRating playerTemperature = TemperatureRating.MILD;
		private static TemperatureRating biomeTemperature = TemperatureRating.MILD;
		private static boolean inside;
		private static ArmorClass armorClass = ArmorClass.NONE;
		private static ArmorClass footArmorClass = ArmorClass.NONE;
		private static boolean inVillage;
		
		private static boolean isUnderground;
		private static boolean isInSpace;
		private static boolean isInClouds;

		private static int tickCounter;
		
		private static final MinecraftClock clock = new MinecraftClock();
		private static final BattleScanner battle = new BattleScanner();

		private static BlockPos getPlayerPos() {
			return new BlockPos(player.posX, player.getEntityBoundingBox().minY, player.posZ);
		}

		private static void tick(final World world, final EntityPlayer player) {

			final BiomeRegistry biomes = RegistryManager.get(RegistryType.BIOME);
			final SeasonRegistry seasons = RegistryManager.get(RegistryType.SEASON);

			EnvironState.player = player;
			EnvironState.world = player.worldObj;
			EnvironState.clock.update(EnvironState.world);
			EnvironState.playerBiome = PlayerUtils.getPlayerBiome(player, false);
			EnvironState.biomeName = EnvironState.playerBiome.getBiomeName();
			EnvironState.season = seasons.getSeasonType(world);
			EnvironState.dimensionId = world.provider.getDimension();
			EnvironState.dimensionName = world.provider.getDimensionType().getName();
			EnvironState.playerPosition = getPlayerPos();
			EnvironState.inside = AreaSurveyHandler.isReallyInside();

			EnvironState.truePlayerBiome = PlayerUtils.getPlayerBiome(player, true);
			EnvironState.freezing = EnvironState.truePlayerBiome.getFloatTemperature(EnvironState.playerPosition) < 0.15F;
			EnvironState.playerTemperature = seasons.getPlayerTemperature(world);
			EnvironState.biomeTemperature = seasons.getBiomeTemperature(world, getPlayerPosition());
			EnvironState.humid = EnvironState.truePlayerBiome.isHighHumidity();
			EnvironState.dry = EnvironState.truePlayerBiome.getRainfall() < 0.2F;

			EnvironState.armorClass = ArmorClass.effectiveArmorClass(player);
			EnvironState.footArmorClass = ArmorClass.footArmorClass(player);
			
			EnvironState.isUnderground = EnvironState.playerBiome == biomes.UNDERGROUND_INFO;
			EnvironState.isInSpace = EnvironState.playerBiome == biomes.OUTERSPACE_INFO;
			EnvironState.isInClouds = EnvironState.playerBiome == biomes.CLOUDS_INFO;

			// Trigger the battle scanner
			EnvironState.battle.update();
			
			if (!Minecraft.getMinecraft().isGamePaused())
				EnvironState.tickCounter++;
		}

		public static MinecraftClock getClock() {
			return clock;
		}
		
		public static BattleScanner getBattleScanner() {
			return battle;
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
	public void pre(@Nonnull final World world, @Nonnull final EntityPlayer player) {
		EnvironState.tick(world, player);
	}

	@Override
	public void process(@Nonnull final World world, @Nonnull final EntityPlayer player) {

		// Gather diagnostics if needed
		if (Minecraft.getMinecraft().gameSettings.showDebugInfo && ModOptions.enableDebugLogging) {
			DSurround.getProfiler().startSection("GatherDebug");
			final DiagnosticEvent.Gather gather = new DiagnosticEvent.Gather(world, player);
			MinecraftForge.EVENT_BUS.post(gather);
			this.diagnostics = gather.output;
			DSurround.getProfiler().endSection();
		} else {
			this.diagnostics = null;
		}
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

	/**
	 * Hook the Forge text event to add on our diagnostics
	 */
	@SubscribeEvent
	public void onGatherText(@Nonnull final RenderGameOverlayEvent.Text event) {
		if (this.diagnostics != null && !this.diagnostics.isEmpty()) {
			event.getLeft().add("");
			event.getLeft().addAll(this.diagnostics);
		}

		if (Minecraft.getMinecraft().gameSettings.showDebugInfo && this.serverDataReport != null) {
			event.getRight().add(" ");
			event.getRight().addAll(this.serverDataReport);
		}
	}

	@Override
	public void onConnect() {
		this.diagnostics = null;
		this.serverDataReport = null;
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
			final String result = Evaluator.eval(s).toString();
			event.output.add(result);
		}

		event.output.add(WeatherProperties.diagnostic());
		event.output.add("Aurora: " + (AuroraEffectHandler.getCurrentAurora() == null ? "NONE"
				: AuroraEffectHandler.getCurrentAurora().toString()));

		final List<String> badScripts = Evaluator.getNaughtyList();
		for (final String s : badScripts) {
			event.output.add("BAD SCRIPT: " + s);
		}
	}

	@Nonnull
	private static TextFormatting getTpsFormatPrefix(final int tps) {
		if (tps <= 10)
			return TextFormatting.RED;
		if (tps <= 15)
			return TextFormatting.YELLOW;
		return TextFormatting.GREEN;
	}

	@SubscribeEvent
	public void serverDataEvent(final ServerDataEvent event) {
		final ArrayList<String> data = new ArrayList<String>();

		final int diff = event.total - event.free;

		data.add(TextFormatting.GOLD + "Server Information");
		data.add(String.format("Mem: %d%% %03d/%3dMB", diff * 100 / event.max, diff, event.max));
		data.add(String.format("Allocated: %d%% %3dMB", event.total * 100 / event.max, event.total));
		final int tps = (int) Math.min(1000.0D / event.meanTickTime, 20.0D);
		data.add(String.format("Ticktime Overall:%s %5.3fms (%d TPS)", getTpsFormatPrefix(tps), event.meanTickTime,
				tps));
		event.dimTps.forEachEntry(new TIntDoubleProcedure() {
			@Override
			public boolean execute(int a, double b) {
				final String dimName = DimensionManager.getProviderType(a).getName();
				final int tps = (int) Math.min(1000.0D / b, 20.0D);
				data.add(String.format("%s (%d):%s %7.3fms (%d TPS)", dimName, a, getTpsFormatPrefix(tps), b, tps));
				return true;
			}

		});

		Collections.sort(data.subList(4, data.size()));
		this.serverDataReport = data;
	}

}
