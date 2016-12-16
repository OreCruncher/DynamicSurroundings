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

package org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.interfaces;

import org.blockartistry.mod.DynSurround.client.footsteps.engine.interfaces.EventType;
import org.blockartistry.mod.DynSurround.client.footsteps.game.system.Association;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface ISolver {
	/**
	 * Play an association.
	 */
	public void playAssociation(final EntityPlayer ply, final Association assos, final EventType eventType);
	
	/**
	 * Find an association for a player particular foot. This will fetch the
	 * player angle and use it as a basis to find out what block is below their
	 * feet (or which block is likely to be below their feet if the player is
	 * walking on the edge of a block when walking over non-emitting blocks like
	 * air or water).<br>
	 * <br>
	 * Returns null if no blocks are valid emitting blocks.<br>
	 * Returns a string that begins with "_NO_ASSOCIATION" if a matching block
	 * was found, but has no association in the blockmap.
	 */
	public Association findAssociationForPlayer(final EntityPlayer ply, final double verticalOffsetAsMinus, final boolean isRightFoot);
	
	/**
	 * Find an association for a player. This will take the block right below
	 * the center of the player (or which block is likely to be below them if
	 * the player is walking on the edge of a block when walking over
	 * non-emitting blocks like air or water).<br>
	 * <br>
	 * Returns null if no blocks are valid emitting blocks.<br>
	 * Returns a string that begins with "_NO_ASSOCIATION" if a matching block
	 * was found, but has no association in the blockmap.
	 */
	public Association findAssociationForPlayer(final EntityPlayer ply, final double verticalOffsetAsMinus);
	
	/**
	 * Find an association for a player, and a location. This will try to find
	 * the best matching block on that location, or near that location, for
	 * instance if the player is walking on the edge of a block when walking
	 * over non-emitting blocks like air or water)<br>
	 * <br>
	 * Returns null if no blocks are valid emitting blocks.<br>
	 * Returns a string that begins with "_NO_ASSOCIATION" if a matching block
	 * was found, but has no association in the blockmap.
	 */
	public Association findAssociationForLocation(final EntityPlayer ply, final BlockPos pos);
	
	/**
	 * Find an association for a certain block assuming the player is standing
	 * on it. This will sometimes select the block above because some block act
	 * like carpets. This also applies when the block targeted by the location
	 * is actually not emitting, such as lilypads on water.<br>
	 * <br>
	 * Returns null if the block is not a valid emitting block (this causes the
	 * engine to continue looking for valid blocks). This also happens if the
	 * carpet is non-emitting.<br>
	 * Returns a string that begins with "_NO_ASSOCIATION" if the block is
	 * valid, but has no association in the blockmap. If the carpet was
	 * selected, this solves to the carpet.
	 */
	public Association findAssociationForBlock(final BlockPos pos);
	
	/**
	 * Find an association for a certain block assuming the player is standing on it,
	 * using a custom strategy which strategies are defined by the solver.
	 */
	public Association findAssociationMessyFoliage(final BlockPos pos);
	
	/**
	 * Play special sounds that must stop the usual footstep figuring things out process.
	 */
	public boolean playSpecialStoppingConditions(final EntityPlayer ply);
	
	/**
	 * Tells if footsteps can be played.
	 */
	public boolean hasSpecialStoppingConditions(final EntityPlayer ply);
	
}