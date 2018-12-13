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

package org.orecruncher.dsurround.lib;

import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSetMultimap;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.capabilities.Capability;

public class ProxyWorld extends World {

	private final @Nonnull World wrapped;

	public ProxyWorld(@Nonnull World wrapped) {
		super(wrapped.getSaveHandler(), wrapped.getWorldInfo(), wrapped.provider, wrapped.profiler, false);
		this.wrapped = wrapped;
	}

	@Override
	public boolean spawnEntity(@Nonnull Entity entityIn) {
		return false;
	}

	// from here on: just relays to the wrapped world

	@Override
	protected @Nonnull IChunkProvider createChunkProvider() {
		return new IChunkProvider() {
			@Override
			public Chunk getLoadedChunk(int x, int z) {
				return new EmptyChunk(ProxyWorld.this, x, z);
			}

			@Override
			public @Nonnull Chunk provideChunk(int x, int z) {
				return new EmptyChunk(ProxyWorld.this, x, z);
			}

			@Override
			public boolean tick() {
				return false;
			}

			@Override
			public @Nonnull String makeString() {
				return "";
			}

			@Override
			public boolean isChunkGeneratedAt(int x, int z) {
				return false;
			}
		};
	}

	@Override
	protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
		return true;
	}

	@Override
	public @Nonnull World init() {
		return this.wrapped.init();
	}

	@Override
	public @Nonnull Biome getBiomeForCoordsBody(@Nonnull BlockPos pos) {
		return this.wrapped.getBiomeForCoordsBody(pos);
	}

	@Override
	public @Nonnull BiomeProvider getBiomeProvider() {
		return this.wrapped.getBiomeProvider();
	}

	@Override
	public void initialize(@Nonnull WorldSettings settings) {
		this.wrapped.initialize(settings);
	}

	@Override
	@Nullable
	public MinecraftServer getMinecraftServer() {
		return this.wrapped.getMinecraftServer();
	}

	@Override
	public void setInitialSpawnLocation() {
		this.wrapped.setInitialSpawnLocation();
	}

	@Override
	public @Nonnull IBlockState getGroundAboveSeaLevel(@Nonnull BlockPos pos) {
		return this.wrapped.getGroundAboveSeaLevel(pos);
	}

	@Override
	public boolean isAirBlock(@Nonnull BlockPos pos) {
		return this.wrapped.isAirBlock(pos);
	}

	@Override
	public boolean isBlockLoaded(@Nonnull BlockPos pos) {
		return this.wrapped.isBlockLoaded(pos);
	}

	@Override
	public boolean isBlockLoaded(@Nonnull BlockPos pos, boolean allowEmpty) {
		return this.wrapped.isBlockLoaded(pos, allowEmpty);
	}

	@Override
	public boolean isAreaLoaded(@Nonnull BlockPos center, int radius) {
		return this.wrapped.isAreaLoaded(center, radius);
	}

	@Override
	public boolean isAreaLoaded(@Nonnull BlockPos center, int radius, boolean allowEmpty) {
		return this.wrapped.isAreaLoaded(center, radius, allowEmpty);
	}

	@Override
	public boolean isAreaLoaded(@Nonnull BlockPos from, @Nonnull BlockPos to) {
		return this.wrapped.isAreaLoaded(from, to);
	}

	@Override
	public boolean isAreaLoaded(@Nonnull BlockPos from, @Nonnull BlockPos to, boolean allowEmpty) {
		return this.wrapped.isAreaLoaded(from, to, allowEmpty);
	}

	@Override
	public boolean isAreaLoaded(@Nonnull StructureBoundingBox box) {
		return this.wrapped.isAreaLoaded(box);
	}

	@Override
	public boolean isAreaLoaded(@Nonnull StructureBoundingBox box, boolean allowEmpty) {
		return this.wrapped.isAreaLoaded(box, allowEmpty);
	}

	@Override
	public @Nonnull Chunk getChunk(@Nonnull BlockPos pos) {
		return this.wrapped.getChunk(pos);
	}

	@Override
	public @Nonnull Chunk getChunk(int chunkX, int chunkZ) {
		return this.wrapped.getChunk(chunkX, chunkZ);
	}

	@Override
	public boolean setBlockState(@Nonnull BlockPos pos, @Nonnull IBlockState newState, int flags) {
		return this.wrapped.setBlockState(pos, newState, flags);
	}

	@Override
	public void markAndNotifyBlock(@Nonnull BlockPos pos, @Nullable Chunk chunk, @Nonnull IBlockState iblockstate,
			@Nonnull IBlockState newState, int flags) {
		this.wrapped.markAndNotifyBlock(pos, chunk, iblockstate, newState, flags);
	}

	@Override
	public boolean setBlockToAir(@Nonnull BlockPos pos) {
		return this.wrapped.setBlockToAir(pos);
	}

	@Override
	public boolean destroyBlock(@Nonnull BlockPos pos, boolean dropBlock) {
		return this.wrapped.destroyBlock(pos, dropBlock);
	}

	@Override
	public boolean setBlockState(@Nonnull BlockPos pos, @Nonnull IBlockState state) {
		return this.wrapped.setBlockState(pos, state);
	}

	@Override
	public void notifyBlockUpdate(@Nonnull BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newState,
			int flags) {
		this.wrapped.notifyBlockUpdate(pos, oldState, newState, flags);
	}

	@Override
	public void markBlocksDirtyVertical(int x1, int z1, int x2, int z2) {
		this.wrapped.markBlocksDirtyVertical(x1, z1, x2, z2);
	}

	@Override
	public void markBlockRangeForRenderUpdate(@Nonnull BlockPos rangeMin, @Nonnull BlockPos rangeMax) {
		this.wrapped.markBlockRangeForRenderUpdate(rangeMin, rangeMax);
	}

	@Override
	public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
		this.wrapped.markBlockRangeForRenderUpdate(x1, y1, z1, x2, y2, z2);
	}

	@Override
	public void notifyNeighborsOfStateExcept(@Nonnull BlockPos pos, @Nonnull Block blockType,
			@Nonnull EnumFacing skipSide) {
		this.wrapped.notifyNeighborsOfStateExcept(pos, blockType, skipSide);
	}

	@Override
	public boolean isBlockTickPending(@Nonnull BlockPos pos, @Nonnull Block blockType) {
		return this.wrapped.isBlockTickPending(pos, blockType);
	}

	@Override
	public boolean canSeeSky(@Nonnull BlockPos pos) {
		return this.wrapped.canSeeSky(pos);
	}

	@Override
	public boolean canBlockSeeSky(@Nonnull BlockPos pos) {
		return this.wrapped.canBlockSeeSky(pos);
	}

	@Override
	public int getLight(@Nonnull BlockPos pos) {
		return this.wrapped.getLight(pos);
	}

	@Override
	public int getLightFromNeighbors(@Nonnull BlockPos pos) {
		return this.wrapped.getLightFromNeighbors(pos);
	}

	@Override
	public int getLight(@Nonnull BlockPos pos, boolean checkNeighbors) {
		return this.wrapped.getLight(pos, checkNeighbors);
	}

	@Override
	public @Nonnull BlockPos getHeight(@Nonnull BlockPos pos) {
		return this.wrapped.getHeight(pos);
	}

	@SuppressWarnings("deprecation")
	@Override
	public int getChunksLowestHorizon(int x, int z) {
		return this.wrapped.getChunksLowestHorizon(x, z);
	}

	@Override
	public int getLightFromNeighborsFor(@Nonnull EnumSkyBlock type, @Nonnull BlockPos pos) {
		return this.wrapped.getLightFromNeighborsFor(type, pos);
	}

	@Override
	public int getLightFor(@Nonnull EnumSkyBlock type, @Nonnull BlockPos pos) {
		return this.wrapped.getLightFor(type, pos);
	}

	@Override
	public void setLightFor(@Nonnull EnumSkyBlock type, @Nonnull BlockPos pos, int lightValue) {
		this.wrapped.setLightFor(type, pos, lightValue);
	}

	@Override
	public void notifyLightSet(@Nonnull BlockPos pos) {
		this.wrapped.notifyLightSet(pos);
	}

	@Override
	public int getCombinedLight(@Nonnull BlockPos pos, int lightValue) {
		return this.wrapped.getCombinedLight(pos, lightValue);
	}

	@Override
	public float getLightBrightness(@Nonnull BlockPos pos) {
		return this.wrapped.getLightBrightness(pos);
	}

	@Override
	public @Nonnull IBlockState getBlockState(@Nonnull BlockPos pos) {
		return this.wrapped.getBlockState(pos);
	}

	@Override
	public boolean isDaytime() {
		return this.wrapped.isDaytime();
	}

	@Override
	@Nullable
	public RayTraceResult rayTraceBlocks(@Nonnull Vec3d start, @Nonnull Vec3d end) {
		return this.wrapped.rayTraceBlocks(start, end);
	}

	@Override
	@Nullable
	public RayTraceResult rayTraceBlocks(@Nonnull Vec3d start, @Nonnull Vec3d end, boolean stopOnLiquid) {
		return this.wrapped.rayTraceBlocks(start, end, stopOnLiquid);
	}

	@Override
	@Nullable
	public RayTraceResult rayTraceBlocks(@Nonnull Vec3d vec31, @Nonnull Vec3d vec32, boolean stopOnLiquid,
			boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
		return this.wrapped.rayTraceBlocks(vec31, vec32, stopOnLiquid, ignoreBlockWithoutBoundingBox,
				returnLastUncollidableBlock);
	}

	@Override
	public void playSound(@Nullable EntityPlayer player1, @Nonnull BlockPos pos, @Nonnull SoundEvent soundIn,
			@Nonnull SoundCategory category, float volume, float pitch) {
		this.wrapped.playSound(player1, pos, soundIn, category, volume, pitch);
	}

	@Override
	public void playSound(@Nullable EntityPlayer player1, double x, double y, double z, @Nonnull SoundEvent soundIn,
			@Nonnull SoundCategory category, float volume, float pitch) {
		this.wrapped.playSound(player1, x, y, z, soundIn, category, volume, pitch);
	}

	@Override
	public void playSound(double x, double y, double z, @Nonnull SoundEvent soundIn, @Nonnull SoundCategory category,
			float volume, float pitch, boolean distanceDelay) {
		this.wrapped.playSound(x, y, z, soundIn, category, volume, pitch, distanceDelay);
	}

	@Override
	public void playRecord(@Nonnull BlockPos blockPositionIn, @Nullable SoundEvent soundEventIn) {
		this.wrapped.playRecord(blockPositionIn, soundEventIn);
	}

	@Override
	public void spawnParticle(@Nonnull EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord,
			double xSpeed, double ySpeed, double zSpeed, @Nonnull int... parameters) {
		this.wrapped.spawnParticle(particleType, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
	}

	@Override
	public void spawnParticle(@Nonnull EnumParticleTypes particleType, boolean ignoreRange, double xCoord,
			double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, @Nonnull int... parameters) {
		this.wrapped.spawnParticle(particleType, ignoreRange, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed,
				parameters);
	}

	@Override
	public boolean addWeatherEffect(@Nonnull Entity entityIn) {
		return this.wrapped.addWeatherEffect(entityIn);
	}

	@Override
	public void onEntityAdded(@Nonnull Entity entityIn) {
		this.wrapped.onEntityAdded(entityIn);
	}

	@Override
	public void onEntityRemoved(@Nonnull Entity entityIn) {
		this.wrapped.onEntityRemoved(entityIn);
	}

	@Override
	public void removeEntity(@Nonnull Entity entityIn) {
		this.wrapped.removeEntity(entityIn);
	}

	@Override
	public void removeEntityDangerously(@Nonnull Entity entityIn) {
		this.wrapped.removeEntityDangerously(entityIn);
	}

	@Override
	public void addEventListener(@Nonnull IWorldEventListener listener) {
		this.wrapped.addEventListener(listener);
	}

	@Override
	public @Nonnull List<AxisAlignedBB> getCollisionBoxes(@Nullable Entity entityIn, @Nonnull AxisAlignedBB aabb) {
		return this.wrapped.getCollisionBoxes(entityIn, aabb);
	}

	@Override
	public void removeEventListener(@Nonnull IWorldEventListener listener) {
		this.wrapped.removeEventListener(listener);
	}

	@Override
	public boolean collidesWithAnyBlock(@Nonnull AxisAlignedBB bbox) {
		return this.wrapped.collidesWithAnyBlock(bbox);
	}

	@Override
	public int calculateSkylightSubtracted(float p_72967_1_) {
		return this.wrapped.calculateSkylightSubtracted(p_72967_1_);
	}

	@Override
	public float getSunBrightnessFactor(float p_72967_1_) {
		return this.wrapped.getSunBrightnessFactor(p_72967_1_);
	}

	@Override
	public float getSunBrightness(float p_72971_1_) {
		return this.wrapped.getSunBrightness(p_72971_1_);
	}

	@Override
	public float getSunBrightnessBody(float p_72971_1_) {
		return this.wrapped.getSunBrightnessBody(p_72971_1_);
	}

	@Override
	public @Nonnull Vec3d getSkyColor(@Nonnull Entity entityIn, float partialTicks) {
		return this.wrapped.getSkyColor(entityIn, partialTicks);
	}

	@Override
	public @Nonnull Vec3d getSkyColorBody(@Nonnull Entity entityIn, float partialTicks) {
		return this.wrapped.getSkyColorBody(entityIn, partialTicks);
	}

	@Override
	public float getCelestialAngle(float partialTicks) {
		return this.wrapped.getCelestialAngle(partialTicks);
	}

	@Override
	public int getMoonPhase() {
		return this.wrapped.getMoonPhase();
	}

	@Override
	public float getCurrentMoonPhaseFactor() {
		return this.wrapped.getCurrentMoonPhaseFactor();
	}

	@Override
	public float getCurrentMoonPhaseFactorBody() {
		return this.wrapped.getCurrentMoonPhaseFactorBody();
	}

	@Override
	public float getCelestialAngleRadians(float partialTicks) {
		return this.wrapped.getCelestialAngleRadians(partialTicks);
	}

	@Override
	public @Nonnull Vec3d getCloudColour(float partialTicks) {
		return this.wrapped.getCloudColour(partialTicks);
	}

	@Override
	public @Nonnull Vec3d getCloudColorBody(float partialTicks) {
		return this.wrapped.getCloudColorBody(partialTicks);
	}

	@Override
	public @Nonnull Vec3d getFogColor(float partialTicks) {
		return this.wrapped.getFogColor(partialTicks);
	}

	@Override
	public @Nonnull BlockPos getPrecipitationHeight(@Nonnull BlockPos pos) {
		return this.wrapped.getPrecipitationHeight(pos);
	}

	@Override
	public @Nonnull BlockPos getTopSolidOrLiquidBlock(@Nonnull BlockPos pos) {
		return this.wrapped.getTopSolidOrLiquidBlock(pos);
	}

	@Override
	public float getStarBrightness(float partialTicks) {
		return this.wrapped.getStarBrightness(partialTicks);
	}

	@Override
	public float getStarBrightnessBody(float partialTicks) {
		return this.wrapped.getStarBrightnessBody(partialTicks);
	}

	@Override
	public boolean isUpdateScheduled(@Nonnull BlockPos pos, @Nonnull Block blk) {
		return this.wrapped.isUpdateScheduled(pos, blk);
	}

	@Override
	public void scheduleUpdate(@Nonnull BlockPos pos, @Nonnull Block blockIn, int delay) {
		this.wrapped.scheduleUpdate(pos, blockIn, delay);
	}

	@Override
	public void updateBlockTick(@Nonnull BlockPos pos, @Nonnull Block blockIn, int delay, int priority) {
		this.wrapped.updateBlockTick(pos, blockIn, delay, priority);
	}

	@Override
	public void scheduleBlockUpdate(@Nonnull BlockPos pos, @Nonnull Block blockIn, int delay, int priority) {
		this.wrapped.scheduleBlockUpdate(pos, blockIn, delay, priority);
	}

	@Override
	public void updateEntities() {
		this.wrapped.updateEntities();
	}

	@Override
	protected void tickPlayers() {
	}

	@Override
	public boolean addTileEntity(@Nonnull TileEntity tile) {
		return this.wrapped.addTileEntity(tile);
	}

	@Override
	public void addTileEntities(@Nonnull Collection<TileEntity> tileEntityCollection) {
		this.wrapped.addTileEntities(tileEntityCollection);
	}

	@Override
	public void updateEntity(@Nonnull Entity ent) {
		this.wrapped.updateEntity(ent);
	}

	@Override
	public void updateEntityWithOptionalForce(@Nonnull Entity entityIn, boolean forceUpdate) {
		this.wrapped.updateEntityWithOptionalForce(entityIn, forceUpdate);
	}

	@Override
	public boolean checkNoEntityCollision(@Nonnull AxisAlignedBB bb) {
		return this.wrapped.checkNoEntityCollision(bb);
	}

	@Override
	public boolean checkNoEntityCollision(@Nonnull AxisAlignedBB bb, @Nullable Entity entityIn) {
		return this.wrapped.checkNoEntityCollision(bb, entityIn);
	}

	@Override
	public boolean checkBlockCollision(@Nonnull AxisAlignedBB bb) {
		return this.wrapped.checkBlockCollision(bb);
	}

	@Override
	public boolean containsAnyLiquid(@Nonnull AxisAlignedBB bb) {
		return this.wrapped.containsAnyLiquid(bb);
	}

	@Override
	public boolean isFlammableWithin(@Nonnull AxisAlignedBB bb) {
		return this.wrapped.isFlammableWithin(bb);
	}

	@Override
	public boolean handleMaterialAcceleration(@Nonnull AxisAlignedBB bb, @Nonnull Material materialIn,
			@Nonnull Entity entityIn) {
		return this.wrapped.handleMaterialAcceleration(bb, materialIn, entityIn);
	}

	@Override
	public boolean isMaterialInBB(@Nonnull AxisAlignedBB bb, @Nonnull Material materialIn) {
		return this.wrapped.isMaterialInBB(bb, materialIn);
	}

	@Override
	public @Nonnull Explosion createExplosion(@Nullable Entity entityIn, double x, double y, double z, float strength,
			boolean isSmoking) {
		return this.wrapped.createExplosion(entityIn, x, y, z, strength, isSmoking);
	}

	@Override
	public @Nonnull Explosion newExplosion(@Nullable Entity entityIn, double x, double y, double z, float strength,
			boolean isFlaming, boolean isSmoking) {
		return this.wrapped.newExplosion(entityIn, x, y, z, strength, isFlaming, isSmoking);
	}

	@Override
	public float getBlockDensity(@Nonnull Vec3d vec, @Nonnull AxisAlignedBB bb) {
		return this.wrapped.getBlockDensity(vec, bb);
	}

	@Override
	public boolean extinguishFire(@Nullable EntityPlayer player1, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
		return this.wrapped.extinguishFire(player1, pos, side);
	}

	@Override
	public @Nonnull String getDebugLoadedEntities() {
		return this.wrapped.getDebugLoadedEntities();
	}

	@Override
	public @Nonnull String getProviderName() {
		return this.wrapped.getProviderName();
	}

	@Override
	@Nullable
	public TileEntity getTileEntity(@Nonnull BlockPos pos) {
		return this.wrapped.getTileEntity(pos);
	}

	@Override
	public void setTileEntity(@Nonnull BlockPos pos, @Nullable TileEntity tileEntityIn) {
		this.wrapped.setTileEntity(pos, tileEntityIn);
	}

	@Override
	public void removeTileEntity(@Nonnull BlockPos pos) {
		this.wrapped.removeTileEntity(pos);
	}

	@Override
	public void markTileEntityForRemoval(@Nonnull TileEntity tileEntityIn) {
		this.wrapped.markTileEntityForRemoval(tileEntityIn);
	}

	@Override
	public boolean isBlockFullCube(@Nonnull BlockPos pos) {
		return this.wrapped.isBlockFullCube(pos);
	}

	@Override
	public boolean isBlockNormalCube(@Nonnull BlockPos pos, boolean _default) {
		return this.wrapped.isBlockNormalCube(pos, _default);
	}

	@Override
	public void calculateInitialSkylight() {
		this.wrapped.calculateInitialSkylight();
	}

	@Override
	public void setAllowedSpawnTypes(boolean hostile, boolean peaceful) {
		this.wrapped.setAllowedSpawnTypes(hostile, peaceful);
	}

	@Override
	public void tick() {
		this.wrapped.tick();
	}

	@Override
	protected void calculateInitialWeather() {
	}

	@Override
	public void calculateInitialWeatherBody() {
		this.wrapped.calculateInitialWeatherBody();
	}

	@Override
	protected void updateWeather() {
	}

	@Override
	public void updateWeatherBody() {
		this.wrapped.updateWeatherBody();
	}

	@Override
	protected void playMoodSoundAndCheckLight(int p_147467_1_, int p_147467_2_, @Nonnull Chunk chunkIn) {
	}

	@Override
	protected void updateBlocks() {
	}

	@Override
	public void immediateBlockTick(@Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random random) {
		this.wrapped.immediateBlockTick(pos, state, random);
	}

	@Override
	public boolean canBlockFreezeWater(@Nonnull BlockPos pos) {
		return this.wrapped.canBlockFreezeWater(pos);
	}

	@Override
	public boolean canBlockFreezeNoWater(@Nonnull BlockPos pos) {
		return this.wrapped.canBlockFreezeNoWater(pos);
	}

	@Override
	public boolean canBlockFreeze(@Nonnull BlockPos pos, boolean noWaterAdj) {
		return this.wrapped.canBlockFreeze(pos, noWaterAdj);
	}

	@Override
	public boolean canBlockFreezeBody(@Nonnull BlockPos pos, boolean noWaterAdj) {
		return this.wrapped.canBlockFreezeBody(pos, noWaterAdj);
	}

	@Override
	public boolean canSnowAt(@Nonnull BlockPos pos, boolean checkLight) {
		return this.wrapped.canSnowAt(pos, checkLight);
	}

	@Override
	public boolean canSnowAtBody(@Nonnull BlockPos pos, boolean checkLight) {
		return this.wrapped.canSnowAtBody(pos, checkLight);
	}

	@Override
	public boolean checkLight(@Nonnull BlockPos pos) {
		return this.wrapped.checkLight(pos);
	}

	@Override
	public boolean checkLightFor(@Nonnull EnumSkyBlock lightType, @Nonnull BlockPos pos) {
		return this.wrapped.checkLightFor(lightType, pos);
	}

	@Override
	public boolean tickUpdates(boolean p_72955_1_) {
		return this.wrapped.tickUpdates(p_72955_1_);
	}

	@Override
	@Nullable
	public List<NextTickListEntry> getPendingBlockUpdates(@Nonnull Chunk chunkIn, boolean p_72920_2_) {
		return this.wrapped.getPendingBlockUpdates(chunkIn, p_72920_2_);
	}

	@Override
	@Nullable
	public List<NextTickListEntry> getPendingBlockUpdates(@Nonnull StructureBoundingBox structureBB,
			boolean p_175712_2_) {
		return this.wrapped.getPendingBlockUpdates(structureBB, p_175712_2_);
	}

	@Override
	public @Nonnull List<Entity> getEntitiesWithinAABBExcludingEntity(@Nullable Entity entityIn,
			@Nonnull AxisAlignedBB bb) {
		return this.wrapped.getEntitiesWithinAABBExcludingEntity(entityIn, bb);
	}

	@Override
	public @Nonnull List<Entity> getEntitiesInAABBexcluding(@Nullable Entity entityIn,
			@Nonnull AxisAlignedBB boundingBox, @Nullable Predicate<? super Entity> predicate) {
		return this.wrapped.getEntitiesInAABBexcluding(entityIn, boundingBox, predicate);
	}

	@Override
	public @Nonnull <T extends Entity> List<T> getEntities(@Nonnull Class<? extends T> entityType,
			@Nonnull Predicate<? super T> filter) {
		return this.wrapped.getEntities(entityType, filter);
	}

	@Override
	public @Nonnull <T extends Entity> List<T> getPlayers(@Nonnull Class<? extends T> playerType,
			@Nonnull Predicate<? super T> filter) {
		return this.wrapped.getPlayers(playerType, filter);
	}

	@Override
	public @Nonnull <T extends Entity> List<T> getEntitiesWithinAABB(@Nonnull Class<? extends T> classEntity,
			@Nonnull AxisAlignedBB bb) {
		return this.wrapped.getEntitiesWithinAABB(classEntity, bb);
	}

	@Override
	public @Nonnull <T extends Entity> List<T> getEntitiesWithinAABB(@Nonnull Class<? extends T> clazz,
			@Nonnull AxisAlignedBB aabb, @Nullable Predicate<? super T> filter) {
		return this.wrapped.getEntitiesWithinAABB(clazz, aabb, filter);
	}

	@Override
	@Nullable
	public <T extends Entity> T findNearestEntityWithinAABB(@Nonnull Class<? extends T> entityType,
			@Nonnull AxisAlignedBB aabb, @Nonnull T closestTo) {
		return this.wrapped.findNearestEntityWithinAABB(entityType, aabb, closestTo);
	}

	@Override
	@Nullable
	public Entity getEntityByID(int id) {
		return this.wrapped.getEntityByID(id);
	}

	@Override
	public @Nonnull List<Entity> getLoadedEntityList() {
		return this.wrapped.getLoadedEntityList();
	}

	@Override
	public void markChunkDirty(@Nonnull BlockPos pos, @Nonnull TileEntity unusedTileEntity) {
		this.wrapped.markChunkDirty(pos, unusedTileEntity);
	}

	@Override
	public int countEntities(@Nonnull Class<?> entityType) {
		return this.wrapped.countEntities(entityType);
	}

	@Override
	public void loadEntities(@Nonnull Collection<Entity> entityCollection) {
		this.wrapped.loadEntities(entityCollection);
	}

	@Override
	public void unloadEntities(@Nonnull Collection<Entity> entityCollection) {
		this.wrapped.unloadEntities(entityCollection);
	}

	@Override
	public int getSeaLevel() {
		return this.wrapped.getSeaLevel();
	}

	@Override
	public void setSeaLevel(int seaLevelIn) {
		this.wrapped.setSeaLevel(seaLevelIn);
	}

	@Override
	public int getStrongPower(@Nonnull BlockPos pos, @Nonnull EnumFacing direction) {
		return this.wrapped.getStrongPower(pos, direction);
	}

	@Override
	public @Nonnull WorldType getWorldType() {
		return this.wrapped.getWorldType();
	}

	@Override
	public int getStrongPower(@Nonnull BlockPos pos) {
		return this.wrapped.getStrongPower(pos);
	}

	@Override
	public boolean isSidePowered(@Nonnull BlockPos pos, @Nonnull EnumFacing side) {
		return this.wrapped.isSidePowered(pos, side);
	}

	@Override
	public int getRedstonePower(@Nonnull BlockPos pos, @Nonnull EnumFacing facing) {
		return this.wrapped.getRedstonePower(pos, facing);
	}

	@Override
	public boolean isBlockPowered(@Nonnull BlockPos pos) {
		return this.wrapped.isBlockPowered(pos);
	}

	@Override
	public int getRedstonePowerFromNeighbors(@Nonnull BlockPos pos) {
		return this.wrapped.getRedstonePowerFromNeighbors(pos);
	}

	@Override
	@Nullable
	public EntityPlayer getClosestPlayerToEntity(@Nonnull Entity entityIn, double distance) {
		return this.wrapped.getClosestPlayerToEntity(entityIn, distance);
	}

	@Override
	@Nullable
	public EntityPlayer getNearestPlayerNotCreative(@Nonnull Entity entityIn, double distance) {
		return this.wrapped.getNearestPlayerNotCreative(entityIn, distance);
	}

	@Override
	@Nullable
	public EntityPlayer getClosestPlayer(double posX, double posY, double posZ, double distance, boolean spectator) {
		return this.wrapped.getClosestPlayer(posX, posY, posZ, distance, spectator);
	}

	@Override
	public boolean isAnyPlayerWithinRangeAt(double x, double y, double z, double range) {
		return this.wrapped.isAnyPlayerWithinRangeAt(x, y, z, range);
	}

	@Override
	@Nullable
	public EntityPlayer getNearestAttackablePlayer(@Nonnull Entity entityIn, double maxXZDistance,
			double maxYDistance) {
		return this.wrapped.getNearestAttackablePlayer(entityIn, maxXZDistance, maxYDistance);
	}

	@Override
	@Nullable
	public EntityPlayer getNearestAttackablePlayer(@Nonnull BlockPos pos, double maxXZDistance, double maxYDistance) {
		return this.wrapped.getNearestAttackablePlayer(pos, maxXZDistance, maxYDistance);
	}

	@Override
	@Nullable
	public EntityPlayer getNearestAttackablePlayer(double posX, double posY, double posZ, double maxXZDistance,
			double maxYDistance, @Nullable Function<EntityPlayer, Double> playerToDouble,
			@Nullable Predicate<EntityPlayer> p_184150_12_) {
		return this.wrapped.getNearestAttackablePlayer(posX, posY, posZ, maxXZDistance, maxYDistance, playerToDouble,
				p_184150_12_);
	}

	@Override
	@Nullable
	public EntityPlayer getPlayerEntityByName(@Nonnull String name) {
		return this.wrapped.getPlayerEntityByName(name);
	}

	@Override
	@Nullable
	public EntityPlayer getPlayerEntityByUUID(@Nonnull UUID uuid) {
		return this.wrapped.getPlayerEntityByUUID(uuid);
	}

	@Override
	public void sendQuittingDisconnectingPacket() {
		this.wrapped.sendQuittingDisconnectingPacket();
	}

	@Override
	public void checkSessionLock() throws MinecraftException {
		this.wrapped.checkSessionLock();
	}

	@Override
	public void setTotalWorldTime(long worldTime) {
		this.wrapped.setTotalWorldTime(worldTime);
	}

	@Override
	public long getSeed() {
		return this.wrapped.getSeed();
	}

	@Override
	public long getTotalWorldTime() {
		return this.wrapped.getTotalWorldTime();
	}

	@Override
	public long getWorldTime() {
		return this.wrapped.getWorldTime();
	}

	@Override
	public void setWorldTime(long time) {
		this.wrapped.setWorldTime(time);
	}

	@Override
	public @Nonnull BlockPos getSpawnPoint() {
		return this.wrapped.getSpawnPoint();
	}

	@Override
	public void setSpawnPoint(@Nonnull BlockPos pos) {
		this.wrapped.setSpawnPoint(pos);
	}

	@Override
	public void joinEntityInSurroundings(@Nonnull Entity entityIn) {
		this.wrapped.joinEntityInSurroundings(entityIn);
	}

	@Override
	public boolean isBlockModifiable(@Nonnull EntityPlayer player1, @Nonnull BlockPos pos) {
		return this.wrapped.isBlockModifiable(player1, pos);
	}

	@Override
	public boolean canMineBlockBody(@Nonnull EntityPlayer player1, @Nonnull BlockPos pos) {
		return this.wrapped.canMineBlockBody(player1, pos);
	}

	@Override
	public void setEntityState(@Nonnull Entity entityIn, byte state) {
		this.wrapped.setEntityState(entityIn, state);
	}

	@Override
	public @Nonnull IChunkProvider getChunkProvider() {
		return this.wrapped.getChunkProvider();
	}

	@Override
	public void addBlockEvent(@Nonnull BlockPos pos, @Nonnull Block blockIn, int eventID, int eventParam) {
		this.wrapped.addBlockEvent(pos, blockIn, eventID, eventParam);
	}

	@Override
	public @Nonnull ISaveHandler getSaveHandler() {
		// value available from constructor
		return super.saveHandler;
	}

	@Override
	public @Nonnull WorldInfo getWorldInfo() {
		// value available from constructor
		return super.worldInfo;
	}

	@Override
	public @Nonnull GameRules getGameRules() {
		return this.wrapped.getGameRules();
	}

	@Override
	public void updateAllPlayersSleepingFlag() {
		this.wrapped.updateAllPlayersSleepingFlag();
	}

	@Override
	public float getThunderStrength(float delta) {
		return this.wrapped.getThunderStrength(delta);
	}

	@Override
	public void setThunderStrength(float strength) {
		this.wrapped.setThunderStrength(strength);
	}

	@Override
	public float getRainStrength(float delta) {
		return this.wrapped.getRainStrength(delta);
	}

	@Override
	public void setRainStrength(float strength) {
		this.wrapped.setRainStrength(strength);
	}

	@Override
	public boolean isThundering() {
		return this.wrapped.isThundering();
	}

	@Override
	public boolean isRaining() {
		return this.wrapped.isRaining();
	}

	@Override
	public boolean isRainingAt(@Nonnull BlockPos strikePosition) {
		return this.wrapped.isRainingAt(strikePosition);
	}

	@Override
	public boolean isBlockinHighHumidity(@Nonnull BlockPos pos) {
		return this.wrapped.isBlockinHighHumidity(pos);
	}

	@Override
	@Nullable
	public MapStorage getMapStorage() {
		return this.wrapped.getMapStorage();
	}

	@Override
	public int getUniqueDataId(@Nonnull String key) {
		return this.wrapped.getUniqueDataId(key);
	}

	@Override
	public void playBroadcastSound(int id, @Nonnull BlockPos pos, int data) {
		this.wrapped.playBroadcastSound(id, pos, data);
	}

	@Override
	public void playEvent(int type, @Nonnull BlockPos pos, int data) {
		this.wrapped.playEvent(type, pos, data);
	}

	@Override
	public void playEvent(@Nullable EntityPlayer player1, int type, @Nonnull BlockPos pos, int data) {
		this.wrapped.playEvent(player1, type, pos, data);
	}

	@Override
	public int getHeight() {
		return this.wrapped.getHeight();
	}

	@Override
	public int getActualHeight() {
		return this.wrapped.getActualHeight();
	}

	@Override
	public @Nonnull Random setRandomSeed(int p_72843_1_, int p_72843_2_, int p_72843_3_) {
		return this.wrapped.setRandomSeed(p_72843_1_, p_72843_2_, p_72843_3_);
	}

	@Override
	public @Nonnull CrashReportCategory addWorldInfoToCrashReport(@Nonnull CrashReport report) {
		return this.wrapped.addWorldInfoToCrashReport(report);
	}

	@Override
	public double getHorizon() {
		return this.wrapped.getHorizon();
	}

	@Override
	public void sendBlockBreakProgress(int breakerId, @Nonnull BlockPos pos, int progress) {
		this.wrapped.sendBlockBreakProgress(breakerId, pos, progress);
	}

	@Override
	public @Nonnull Calendar getCurrentDate() {
		return this.wrapped.getCurrentDate();
	}

	@Override
	public void makeFireworks(double x, double y, double z, double motionX, double motionY, double motionZ,
			@Nullable NBTTagCompound compund) {
		this.wrapped.makeFireworks(x, y, z, motionX, motionY, motionZ, compund);
	}

	@Override
	public @Nonnull Scoreboard getScoreboard() {
		return this.wrapped.getScoreboard();
	}

	@Override
	public void updateComparatorOutputLevel(@Nonnull BlockPos pos, @Nonnull Block blockIn) {
		this.wrapped.updateComparatorOutputLevel(pos, blockIn);
	}

	@Override
	public @Nonnull DifficultyInstance getDifficultyForLocation(@Nonnull BlockPos pos) {
		return this.wrapped.getDifficultyForLocation(pos);
	}

	@Override
	public @Nonnull EnumDifficulty getDifficulty() {
		return this.wrapped.getDifficulty();
	}

	@Override
	public int getSkylightSubtracted() {
		return this.wrapped.getSkylightSubtracted();
	}

	@Override
	public void setSkylightSubtracted(int newSkylightSubtracted) {
		this.wrapped.setSkylightSubtracted(newSkylightSubtracted);
	}

	@Override
	public int getLastLightningBolt() {
		return this.wrapped.getLastLightningBolt();
	}

	@Override
	public void setLastLightningBolt(int lastLightningBoltIn) {
		this.wrapped.setLastLightningBolt(lastLightningBoltIn);
	}

	@Override
	public @Nonnull VillageCollection getVillageCollection() {
		return this.wrapped.getVillageCollection();
	}

	@Override
	public @Nonnull WorldBorder getWorldBorder() {
		return this.wrapped.getWorldBorder();
	}

	@Override
	public boolean isSpawnChunk(int x, int z) {
		return this.wrapped.isSpawnChunk(x, z);
	}

	@Override
	public boolean isSideSolid(@Nonnull BlockPos pos, @Nonnull EnumFacing side) {
		return this.wrapped.isSideSolid(pos, side);
	}

	@Override
	public boolean isSideSolid(@Nonnull BlockPos pos, @Nonnull EnumFacing side, boolean _default) {
		return this.wrapped.isSideSolid(pos, side, _default);
	}

	@Override
	public @Nonnull ImmutableSetMultimap<ChunkPos, Ticket> getPersistentChunks() {
		return this.wrapped.getPersistentChunks();
	}

	@Override
	public @Nonnull Iterator<Chunk> getPersistentChunkIterable(@Nonnull Iterator<Chunk> chunkIterator) {
		return this.wrapped.getPersistentChunkIterable(chunkIterator);
	}

	@Override
	public int getBlockLightOpacity(@Nonnull BlockPos pos) {
		return this.wrapped.getBlockLightOpacity(pos);
	}

	@Override
	public int countEntities(@Nonnull EnumCreatureType type, boolean forSpawnCount) {
		return this.wrapped.countEntities(type, forSpawnCount);
	}

	@Override
	protected void initCapabilities() {
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
		return this.wrapped.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
		return this.wrapped.getCapability(capability, facing);
	}

	@Override
	public @Nonnull MapStorage getPerWorldStorage() {
		return this.wrapped.getPerWorldStorage();
	}

	@Override
	public void sendPacketToServer(@Nonnull Packet<?> packetIn) {
		this.wrapped.sendPacketToServer(packetIn);
	}

	@Override
	public @Nonnull LootTableManager getLootTableManager() {
		return this.wrapped.getLootTableManager();
	}

	@Override
	public @Nonnull Biome getBiome(@Nonnull BlockPos pos) {
		return this.wrapped.getBiome(pos);
	}

	@Override
	public boolean isChunkGeneratedAt(int x, int z) {
		return this.wrapped.isChunkGeneratedAt(x, z);
	}

	@Override
	public void notifyNeighborsRespectDebug(@Nonnull BlockPos pos, @Nonnull Block blockType, boolean p_175722_3_) {
		this.wrapped.notifyNeighborsRespectDebug(pos, blockType, p_175722_3_);
	}

	@Override
	public void updateObservingBlocksAt(@Nonnull BlockPos pos, @Nonnull Block blockType) {
		this.wrapped.updateObservingBlocksAt(pos, blockType);
	}

	@Override
	public void notifyNeighborsOfStateChange(@Nonnull BlockPos pos, @Nonnull Block blockType, boolean updateObservers) {
		this.wrapped.notifyNeighborsOfStateChange(pos, blockType, updateObservers);
	}

	@Override
	public void neighborChanged(@Nonnull BlockPos pos, @Nonnull Block p_190524_2_, @Nonnull BlockPos p_190524_3_) {
		this.wrapped.neighborChanged(pos, p_190524_2_, p_190524_3_);
	}

	@Override
	public void observedNeighborChanged(@Nonnull BlockPos pos, @Nonnull Block p_190529_2_,
			@Nonnull BlockPos p_190529_3_) {
		this.wrapped.observedNeighborChanged(pos, p_190529_2_, p_190529_3_);
	}

	@Override
	public int getHeight(int x, int z) {
		return this.wrapped.getHeight(x, z);
	}

	@Override
	public void spawnAlwaysVisibleParticle(int p_190523_1_, double p_190523_2_, double p_190523_4_, double p_190523_6_,
			double p_190523_8_, double p_190523_10_, double p_190523_12_, @Nonnull int... p_190523_14_) {
		this.wrapped.spawnAlwaysVisibleParticle(p_190523_1_, p_190523_2_, p_190523_4_, p_190523_6_, p_190523_8_,
				p_190523_10_, p_190523_12_, p_190523_14_);
	}

	@Override
	public boolean isInsideWorldBorder(@Nonnull Entity p_191503_1_) {
		return this.wrapped.isInsideWorldBorder(p_191503_1_);
	}

	@Override
	public boolean mayPlace(@Nonnull Block blockIn, @Nonnull BlockPos pos, boolean p_190527_3_,
			@Nonnull EnumFacing sidePlacedOn, @Nullable Entity placer) {
		return this.wrapped.mayPlace(blockIn, pos, p_190527_3_, sidePlacedOn, placer);
	}

	@Override
	@Nullable
	public EntityPlayer getClosestPlayer(double x, double y, double z, double p_190525_7_,
			@Nonnull Predicate<Entity> p_190525_9_) {
		return this.wrapped.getClosestPlayer(x, y, z, p_190525_7_, p_190525_9_);
	}

	@Override
	public void setData(@Nonnull String dataID, @Nonnull WorldSavedData worldSavedDataIn) {
		this.wrapped.setData(dataID, worldSavedDataIn);
	}

	@Override
	@Nullable
	public WorldSavedData loadData(@Nonnull Class<? extends WorldSavedData> clazz, @Nonnull String dataID) {
		return this.wrapped.loadData(clazz, dataID);
	}

	@Override
	@Nullable
	public BlockPos findNearestStructure(@Nonnull String p_190528_1_, @Nonnull BlockPos p_190528_2_,
			boolean p_190528_3_) {
		return this.wrapped.findNearestStructure(p_190528_1_, p_190528_2_, p_190528_3_);
	}

}