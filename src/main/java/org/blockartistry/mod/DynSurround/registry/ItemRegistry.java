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

package org.blockartistry.mod.DynSurround.registry;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.data.xface.ItemConfig;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;

public class ItemRegistry extends Registry {
	
	private final List<Class<?>> swordItems = new ArrayList<Class<?>>();
	private final List<Class<?>> axeItems = new ArrayList<Class<?>>();
	private final List<Class<?>> bowItems = new ArrayList<Class<?>>();
	
	public ItemRegistry(@Nonnull final Side side) {
		super(side);
	}

	@Override
	public void init() {
		swordItems.clear();
		axeItems.clear();
		bowItems.clear();
	}

	@Override
	public void initComplete() {

	}

	@Override
	public void fini() {

	}
	
	private void process(@Nonnull List<String> classes, final List<Class<?>> theList) {
		for(final String c: classes) {
			try {
				final Class<?> clazz = Class.forName(c, false, ItemRegistry.class.getClassLoader());
				theList.add(clazz);
			} catch (final ClassNotFoundException e) {
				ModLog.warn("Cannot locate class '%s' for ItemRegistry", c);
			} 
		}
	}
	
	public void register(@Nonnull final ItemConfig config) {
		process(config.axeSound, this.axeItems);
		process(config.bowSound, this.bowItems);
		process(config.swordSound, this.swordItems);
	}
	
	public boolean doSwordSound(@Nonnull final ItemStack stack) {
		if(stack == null || stack.getItem() == null)
			return false;
		return this.swordItems.contains(stack.getItem().getClass());
	}
	
	public boolean doAxeSound(@Nonnull final ItemStack stack) {
		if(stack == null || stack.getItem() == null)
			return false;
		return this.axeItems.contains(stack.getItem().getClass());
	}
	
	public boolean doBowSound(@Nonnull final ItemStack stack) {
		if(stack == null || stack.getItem() == null)
			return false;
		return this.bowItems.contains(stack.getItem().getClass());
	}

}
