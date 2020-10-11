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

package org.orecruncher.dsurround.client.handlers;

import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.capabilities.CapabilityDimensionInfo;
import org.orecruncher.dsurround.capabilities.CapabilitySeasonInfo;
import org.orecruncher.dsurround.capabilities.dimension.DimensionInfo;
import org.orecruncher.dsurround.capabilities.dimension.IDimensionInfo;
import org.orecruncher.dsurround.capabilities.season.ISeasonInfo;
import org.orecruncher.dsurround.capabilities.season.TemperatureRating;
import org.orecruncher.dsurround.client.handlers.scanners.BattleScanner;
import org.orecruncher.dsurround.client.handlers.scanners.CeilingCoverage;
import org.orecruncher.dsurround.client.weather.Weather;
import org.orecruncher.dsurround.event.DiagnosticEvent;
import org.orecruncher.dsurround.expression.ExpressionEngine;
import org.orecruncher.dsurround.registry.RegistryManager;
import org.orecruncher.dsurround.registry.biome.BiomeInfo;
import org.orecruncher.dsurround.registry.biome.BiomeRegistry;
import org.orecruncher.dsurround.registry.item.ItemClass;
import org.orecruncher.lib.DiurnalUtils;
import org.orecruncher.lib.DiurnalUtils.DayCycle;
import org.orecruncher.lib.MinecraftClock;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EnvironStateHandler extends EffectHandlerBase {

	protected final static class EnvironStateData {
		// State that is gathered from the various sources
		// to avoid requery. Used during the tick.
		public BiomeInfo playerBiome = RegistryManager.BIOME.WTF_INFO;
		public BiomeInfo truePlayerBiome = RegistryManager.BIOME.WTF_INFO;
		public int dimensionId;
		public String dimensionName = StringUtils.EMPTY;
		public IDimensionInfo dimInfo = DimensionInfo.NONE;
		public BlockPos playerPosition = BlockPos.ORIGIN;
		public TemperatureRating playerTemperature = TemperatureRating.MILD;
		public TemperatureRating biomeTemperature = TemperatureRating.MILD;
		public ItemStack armorStack = ItemStack.EMPTY;
		public ItemStack footArmorStack = ItemStack.EMPTY;
		
		public boolean inside;
		public boolean inVillage;
		public boolean isUnderground;
		public boolean isInSpace;
		public boolean isInClouds;
		public int lightLevel;
		public int tickCounter = 1;

		public DayCycle dayCycle = DayCycle.NO_SKY;

		public MinecraftClock clock = new MinecraftClock();
		public BattleScanner battle = new BattleScanner();
	}
	
	public final static class EnvironState {
		
		private static EnvironStateData data = new EnvironStateData();

		public static MinecraftClock getClock() {
			return data.clock;
		}

		public static BattleScanner getBattleScanner() {
			return data.battle;
		}

		public static IDimensionInfo getDimensionInfo() {
			return data.dimInfo;
		}

		public static BiomeInfo getPlayerBiome() {
			return data.playerBiome;
		}

		public static BiomeInfo getTruePlayerBiome() {
			return data.truePlayerBiome;
		}

		public static String getBiomeName() {
			return getPlayerBiome().getBiomeName();
		}

		public static TemperatureRating getPlayerTemperature() {
			return data.playerTemperature;
		}

		public static TemperatureRating getBiomeTemperature() {
			return data.biomeTemperature;
		}

		public static int getDimensionId() {
			return data.dimensionId;
		}

		public static String getDimensionName() {
			return data.dimensionName;
		}

		public static EntityPlayer getPlayer() {
			return Minecraft.getMinecraft().player;
		}

		public static World getWorld() {
			return Minecraft.getMinecraft().world;
		}

		public static BlockPos getPlayerPosition() {
			return data.playerPosition;
		}

		public static boolean isPlayer(final Entity entity) {
			if (entity == null)
				return false;
			EntityPlayer player = getPlayer();
			if (player == null)
				return false;
			return entity.getPersistentID().equals(player.getPersistentID());
		}

		public static boolean isPlayerHurt() {
			if (ModOptions.player.playerHurtThreshold == 0)
				return false;
			final EntityPlayer player = getPlayer();
			return !player.isCreative() && player.getHealth() <= (ModOptions.player.playerHurtThreshold * player.getMaxHealth());
		}

		public static boolean isPlayerHungry() {
			if (ModOptions.player.playerHungerThreshold == 0)
				return false;
			final EntityPlayer player = getPlayer();
			return !player.isCreative()	&& player.getFoodStats().getFoodLevel() <= ModOptions.player.playerHungerThreshold;
		}

		public static boolean isPlayerBurning() {
			return getPlayer().isBurning();
		}

		public static boolean isPlayerSuffocating() {
			final EntityPlayer player = getPlayer();
			return !player.isCreative() && player.getAir() <= 0;
		}

		public static boolean isPlayerFlying() {
			return getPlayer().capabilities.isFlying;
		}

		public static boolean isPlayerSprinting() {
			return getPlayer().isSprinting();
		}

		public static boolean isPlayerInLava() {
			return getPlayer().isInLava();
		}

		public static boolean isPlayerInvisible() {
			return getPlayer().isInvisible();
		}

		public static boolean isPlayerBlind() {
			return getPlayer().isPotionActive(MobEffects.BLINDNESS);
		}

		public static boolean isPlayerInWater() {
			return getPlayer().isInWater();
		}

		public static boolean isPlayerMoving() {
			final EntityPlayer player = getPlayer();
			return player.distanceWalkedModified != player.prevDistanceWalkedModified;
		}

		public static boolean isPlayerInside() {
			return data.inside;
		}

		public static boolean isPlayerUnderground() {
			return data.isUnderground;
		}

		public static boolean isPlayerInSpace() {
			return data.isInSpace;
		}

		public static boolean isPlayerInClouds() {
			return data.isInClouds;
		}

		public static ItemStack getPlayerItemStack() {
			return data.armorStack;
		}

		public static ItemStack getPlayerFootArmorStack() {
			return data.footArmorStack;
		}

		public static boolean inVillage() {
			return data.inVillage;
		}

		public static void setInVillage(final boolean f) {
			data.inVillage = f;
		}

		public static int getLightLevel() {
			return data.lightLevel;
		}

		public static int getTickCounter() {
			return data.tickCounter;
		}

		public static DayCycle getDayCycle() {
			return data.dayCycle;
		}

		public static float getPartialTick() {
			return Minecraft.getMinecraft().getRenderPartialTicks();
		}
		
		protected static void setData(final EnvironStateData d) {
			data = d;
		}
		
		protected static EnvironStateData getData() {
			return data;
		}

	}

	protected final CeilingCoverage ceiling = new CeilingCoverage();

	public EnvironStateHandler() {
		super("State Handler");
	}

	@Override
	public void process(@Nonnull final EntityPlayer player) {

		// Gather fast references
		final EnvironStateData data = EnvironState.getData();
		final World world = player.getEntityWorld();
		final BiomeRegistry biomes = RegistryManager.BIOME;
		final ISeasonInfo season = CapabilitySeasonInfo.getCapability(world);

		// Advance our tick counter if the game isn't paused
		if (!Minecraft.getMinecraft().isGamePaused())
			data.tickCounter++;

		// Update scanners and things that feed into other operations
		Weather.update();
		this.ceiling.update();
		data.clock.update(world);
		data.battle.update(player);
		
		// Gather frequently accessed info
		data.dimInfo = CapabilityDimensionInfo.getCapability(world);
		data.playerBiome = biomes.getPlayerBiome(player, false);
		data.dimensionId = world.provider.getDimension();
		data.dimensionName = world.provider.getDimensionType().getName();
		data.playerPosition = new BlockPos(player.posX, player.getEntityBoundingBox().minY, player.posZ);
		data.inside = this.ceiling.isReallyInside();

		data.truePlayerBiome = biomes.getPlayerBiome(player, true);

		// Seen in the wild where the capability was not attached for some reason
		if (season != null) {
			data.playerTemperature = season.getPlayerTemperature();
			data.biomeTemperature = season.getBiomeTemperature(data.playerPosition);
		} else {
			data.playerTemperature = TemperatureRating.MILD;
			data.biomeTemperature = TemperatureRating.MILD;
		}

		data.armorStack = ItemClass.effectiveArmorStack(player);
		data.footArmorStack = ItemClass.footArmorStack(player);

		data.isUnderground = data.playerBiome == biomes.UNDERGROUND_INFO;
		data.isInSpace = data.playerBiome == biomes.OUTERSPACE_INFO;
		data.isInClouds = data.playerBiome == biomes.CLOUDS_INFO;

		final int blockLight = world.getLightFor(EnumSkyBlock.BLOCK, data.playerPosition);
		final int skyLight = world.getLightFor(EnumSkyBlock.SKY, data.playerPosition) - world.calculateSkylightSubtracted(1.0F);
		data.lightLevel = Math.max(blockLight, skyLight);

		data.dayCycle = DiurnalUtils.getCycle(world);

		// Resets cached script variables so they are updated
		ExpressionEngine.instance().reset();
	}
	
	private void reset() {
		EnvironState.setData(new EnvironStateData());
	}

	@Override
	public void onConnect() {
		this.reset();
		
		// Ensures we have an expression instance up
		ExpressionEngine.instance();
	}

	@Override
	public void onDisconnect() {
		this.reset();
	}

	// Use the new scripting system to pull out data to display
	// for debug. Good for testing.
	private final static String[] scripts = {
		//@formatter:off
		"'Dim: ' + dim.id + '/' + dim.name",
		"'Biome: ' + biome.name + ' (' + biome.id + '); Temp ' + biome.temperature + '/' + biome.temperatureValue + ' rainfall: ' + biome.rainfall + ' traits: ' + biome.traits",
		"'Weather: ' + IF(weather.isRaining,'rainfall: ' + weather.rainfall,'not raining') + IF(weather.isThundering,' thundering','') + ' Temp: ' + weather.temperature + '/' + weather.temperatureValue + ' ice: ' + IF(weather.canWaterFreeze, 'true', 'false') + ' ' + IF(weather.temperatureValue < 0.2, '(breath)', '')",
		"'Season: ' + season.season + IF(diurnal.isNight,' night',' day') + IF(player.isInside,' inside',' outside') + ' celestialAngle: ' + diurnal.celestialAngle",
		"'Player: Temp ' + player.temperature + '; health ' + player.health + '/' + player.maxHealth + '; food ' + player.food.level + '; saturation ' + player.food.saturation + IF(player.isHurt,' isHurt','') + IF(player.isHungry,' isHungry','') + ' pos: (' + player.X + ',' + player.Y + ',' + player.Z + ') light: ' + player.lightLevel",
		"'Village: ' + player.inVillage"
		//@formatter:on
	};

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void diagnostics(final DiagnosticEvent.Gather event) {

		event.output.add(TextFormatting.GREEN + "Minecraft Date: " + EnvironState.getClock().toString());

		for (final String s : scripts) {
			final String result = ExpressionEngine.instance().eval(s).toString();
			event.output.add(TextFormatting.YELLOW + result);
		}

		final List<String> badScripts = ExpressionEngine.instance().getNaughtyList();
		for (final String s : badScripts) {
			event.output.add("BAD SCRIPT: " + s);
		}

		event.output.add(TextFormatting.RED + EnvironState.getBattleScanner().toString());
	}

}
