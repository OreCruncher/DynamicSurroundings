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

import java.util.Random;

import javax.annotation.Nonnull;

import org.blockartistry.lib.random.XorShiftRandom;
import org.blockartistry.mod.DynSurround.registry.BiomeRegistry;
import org.blockartistry.mod.DynSurround.registry.BlockRegistry;
import org.blockartistry.mod.DynSurround.registry.DimensionRegistry;
import org.blockartistry.mod.DynSurround.registry.RegistryManager;
import org.blockartistry.mod.DynSurround.registry.RegistryManager.RegistryType;
import org.blockartistry.mod.DynSurround.registry.SoundRegistry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public abstract class EffectHandlerBase {
	
	protected final Random RANDOM = XorShiftRandom.current();
	
	// Used to obtain the name of the handler for logging purposes
	public abstract String getHandlerName();
	
	/**
	 * Override to provide pre process logic
	 */
	public void pre(@Nonnull final World world, @Nonnull final EntityPlayer player) {
		
	}
	
	// The meat of the processing logic.  Invoked every client tick.
	public abstract void process(final World world, final EntityPlayer player);
	
	/**
	 * Override to provide post process logic
	 */
	public void post(@Nonnull final World world, @Nonnull final EntityPlayer player) {
		
	}
	
	protected BiomeRegistry getBiomeRegistry() {
		return RegistryManager.get(RegistryType.BIOME);
	}
	
	protected BlockRegistry getBlockRegistry() {
		return RegistryManager.get(RegistryType.BLOCK);
	}
	
	protected DimensionRegistry getDimensionRegistry() {
		return RegistryManager.get(RegistryType.DIMENSION);
	}
	
	protected SoundRegistry getSoundRegistry() {
		return RegistryManager.get(RegistryType.SOUND);
	}
	
	// Called when the client is connecting to a server.  Useful for initializing
	// data to a baseline state (i.e. flushing out the crap).
	public void onConnect() {
	}
	
	// Called when the client disconnects from a server.  Useful for cleaning up
	// state space.
	public void onDisconnect() {
	}
	
	//////////////////////////////
	//
	//  DO NOT HOOK THESE EVENTS!
	//
	//////////////////////////////
	final void connect0() {
		this.onConnect();
	}
	
	final void disconnect0() {
		this.onDisconnect();
	}
	
}
