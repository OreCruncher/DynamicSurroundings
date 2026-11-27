/* This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
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

package org.orecruncher.dsurround.client.footsteps.accents;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.footsteps.IFootstepAccentProvider;
import org.orecruncher.dsurround.client.weather.GroundWetness;
import org.orecruncher.dsurround.client.weather.RainSurface;
import org.orecruncher.dsurround.registry.RegistryManager;
import org.orecruncher.dsurround.registry.acoustics.IAcoustic;
import org.orecruncher.dsurround.registry.acoustics.ScaledAcoustic;
import org.orecruncher.dsurround.registry.tags.BlockTags;
import org.orecruncher.lib.collections.ObjectArray;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * What a step on something wet adds to it, by what is underfoot, from the 26.x line. Watery blocks (a lily pad, the
 * {@code watery_step} tag) splash at full volume. Water over the feet splashes by its depth ({@link #wading}): lightly
 * over the shoes, louder deeper, nothing with the head under. Ground in the rain answers as it is soaked
 * ({@link GroundWetness}, so not under a roof or an overhang): ground that turns to mud (the
 * {@code tracks_mud_when_wet} tag: grass, dirt) squelches once it is muddy; hard ground ({@link RainSurface}: stone,
 * wood, metal, glass) gives a light wet slap; sand, gravel, snow and leaves take the rain in and add nothing. Every
 * step sounds, louder the wetter the ground, so a drizzle doesn't slosh like a downpour.
 * <p>
 * In 1.12.2 an entity in water deeper than about 0.4 of a block is swimming as far as footsteps go: the generator
 * plays the swim sound and no accents. So wading is mostly that sound's, and what reaches here is the shallow water of
 * a spreading flow. 26.x thins the splash to a slap with a filter (the bass to a fifth); 1.12.2 has none, so the
 * slap is its own sound ({@code footsteps/wet_slap}), made from the splash's lighter takes with that filter and more
 * of the low end taken out, shortened, and as loud at its peak as the filtered splash. Played as 26.x plays the
 * filtered splash.
 */
@SideOnly(Side.CLIENT)
public class WaterySurfaceAccent implements IFootstepAccentProvider {

	/**
	 * What a step on ground in the rain adds
	 */
	enum WetGround {
		// Grass, dirt: a squelch once muddy
		MUD,
		// Stone, wood, metal, glass: a light slap
		SLAP,
		// Sand, gravel, snow, leaves, and anything else: nothing
		NONE
	}

	/**
	 * What water at the feet makes of a step
	 */
	enum Wading {
		// No water over the feet: the ground decides
		DRY,
		// Over the shoes: a light, thinned splash
		SHALLOW,
		// Deeper: a splash, louder the deeper
		WADE,
		// The head under water: nothing, the swim sound has it
		UNDER
	}

	/**
	 * Water over the feet, in blocks, from which a step is in it: a little over nothing, so the edge of a surface
	 * doesn't count
	 */
	static final float WET_FEET = 0.02F;
	/**
	 * From this depth a step wades
	 */
	static final float WADING_FROM = 0.45F;
	static final float WADE_LOUDEST_AT = 0.9F;
	static final float WADE_QUIETEST = 0.6F;
	/**
	 * Shallow water's splash, thinned as a slap is: the slap's volume, as a share of the splash's
	 */
	static final float SHALLOW_VOLUME = 0.5F;
	/**
	 * Below this wetness, ground in the rain is too lightly wet to sound
	 */
	static final float DRY_BELOW = 0.15F;
	/**
	 * A slap on hard ground, from just wet enough to soaked: its volume, as a share of the splash's. Well under a splash
	 * even when soaked: a film of water, not a puddle.
	 */
	static final float SLAP_QUIETEST = 0.15F;
	static final float SLAP_LOUDEST = 0.45F;
	/**
	 * The slap a little higher, as 26.x plays its filtered splash
	 */
	static final float SLAP_PITCH = 1.1F;
	/**
	 * A squelch on muddy ground, from just muddy ({@link GroundWetness#MUDDY}) to soaked: its volume, as a share of its
	 * own
	 */
	static final float MUD_QUIETEST = 0.35F;
	/**
	 * How far above the feet water is looked for
	 */
	private static final int DEPTH_SCAN = 2;

	private final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

	@Override
	@Nonnull
	public String getName() {
		return "Watery Surface Accent";
	}

	@Override
	public void provide(@Nonnull final EntityLivingBase entity, @Nullable final BlockPos pos,
			@Nonnull final ObjectArray<IAcoustic> in) {
		if (!ModOptions.sound.enablePuddleSound || pos == null)
			return;
		final IAcoustic[] splash = RegistryManager.FOOTSTEPS.WET_SURFACE;
		final IAcoustic[] mud = RegistryManager.FOOTSTEPS.WET_MUD;
		final IAcoustic[] slap = RegistryManager.FOOTSTEPS.WET_SLAP;
		if (splash == null || mud == null || slap == null)
			return;

		final World world = entity.getEntityWorld();
		final BlockPos up = pos.up();
		final IBlockState state = world.getBlockState(pos);

		// Something watery on top of the block (a lily pad): the pad pressed into the water splashes, though the feet
		// are just above it
		if (RegistryManager.TAGS.is(BlockTags.WATERY_STEP, state)
				|| RegistryManager.TAGS.is(BlockTags.WATERY_STEP, world.getBlockState(up))) {
			in.addAll(splash);
			return;
		}

		// Water at the feet, by how deep it is over them
		final float depth = waterOverFeet(world, entity);
		switch (wading(depth, entity.isInsideOfMaterial(Material.WATER))) {
		case SHALLOW:
			in.add(new ScaledAcoustic(slap, SHALLOW_VOLUME, SLAP_PITCH));
			return;
		case WADE:
			in.add(new ScaledAcoustic(splash, wadeVolume(depth), 1F));
			return;
		case UNDER:
			return;
		case DRY:
		default:
			break;
		}

		final float wetness = GroundWetness.at(world, up);
		if (wetness < DRY_BELOW)
			return;

		switch (wetGround(RainSurface.of(world, pos), RegistryManager.TAGS.is(BlockTags.TRACKS_MUD_WHEN_WET, state))) {
		case MUD: {
			final float volume = mudVolume(wetness);
			if (volume > 0F)
				in.add(new ScaledAcoustic(mud, volume, 1F));
			break;
		}
		case SLAP:
			in.add(new ScaledAcoustic(slap, slapVolume(wetness), SLAP_PITCH));
			break;
		case NONE:
		default:
			break;
		}
	}

	/**
	 * How deep water stands over an entity's feet, in blocks: the surface of the water in its block and those above
	 */
	private float waterOverFeet(@Nonnull final World world, @Nonnull final EntityLivingBase entity) {
		final int x = MathHelper.floor(entity.posX);
		final int z = MathHelper.floor(entity.posZ);
		final int feet = MathHelper.floor(entity.posY);
		float surface = Float.NaN;
		for (int y = feet; y <= feet + DEPTH_SCAN; y++) {
			final IBlockState state = world.getBlockState(this.mutable.setPos(x, y, z));
			if (state.getMaterial() != Material.WATER)
				break;
			// A full block where there is water over it, as the game draws it
			surface = y + 1F;
			if (world.getBlockState(this.mutable.setPos(x, y + 1, z)).getMaterial() != Material.WATER)
				surface = y + 1F - liquidGap(state);
		}
		return Float.isNaN(surface) ? 0F : (float) (surface - entity.posY);
	}

	/**
	 * How far below the top of its block a water block's surface is
	 */
	private static float liquidGap(@Nonnull final IBlockState state) {
		try {
			return BlockLiquid.getLiquidHeightPercent(state.getValue(BlockLiquid.LEVEL));
		} catch (final IllegalArgumentException notLiquid) {
			// A mod's water block without a level
			return 1F / 9F;
		}
	}

	/**
	 * What water at the feet makes of a step, by how deep it is over them (blocks) and whether the head is under it
	 */
	static Wading wading(final float depth, final boolean eyeInWater) {
		if (eyeInWater)
			return Wading.UNDER;
		if (!(depth > WET_FEET))
			return Wading.DRY;
		return depth < WADING_FROM ? Wading.SHALLOW : Wading.WADE;
	}

	/**
	 * How loud wading is, as a share of the splash's volume: from {@link #WADE_QUIETEST} where wading starts, all of it
	 * by {@link #WADE_LOUDEST_AT}
	 */
	static float wadeVolume(final float depth) {
		final float t = MathHelper.clamp((depth - WADING_FROM) / (WADE_LOUDEST_AT - WADING_FROM), 0F, 1F);
		return WADE_QUIETEST + (1F - WADE_QUIETEST) * t;
	}

	/**
	 * What a step on ground in the rain adds, by what the rain is to the block and whether it turns to mud: ground
	 * that turns to mud squelches whatever its sound type, hard surfaces slap, the rest take the rain in
	 */
	static WetGround wetGround(@Nonnull final RainSurface surface, final boolean turnsToMud) {
		if (turnsToMud)
			return WetGround.MUD;
		switch (surface) {
		case HARD:
		case WOOD:
		case METAL:
		case GLASS:
			return WetGround.SLAP;
		default:
			return WetGround.NONE;
		}
	}

	/**
	 * How loud a slap on hard ground is, as a share of the splash's volume: nothing barely wet, then rising
	 */
	static float slapVolume(final float wetness) {
		return ramp(wetness, DRY_BELOW, SLAP_QUIETEST, SLAP_LOUDEST);
	}

	/**
	 * How loud a squelch on muddy ground is, as a share of its own volume: nothing until muddy, then rising to all
	 */
	static float mudVolume(final float wetness) {
		return ramp(wetness, GroundWetness.MUDDY, MUD_QUIETEST, 1F);
	}

	/**
	 * Nothing below {@code from}; from there {@code low}, rising to {@code high} when soaked (1)
	 */
	private static float ramp(final float wetness, final float from, final float low, final float high) {
		if (wetness < from)
			return 0F;
		final float t = Math.min((wetness - from) / (1F - from), 1F);
		return low + (high - low) * t;
	}
}
