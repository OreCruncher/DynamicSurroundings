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

package org.blockartistry.DynSurround.client.swing;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.blockartistry.DynSurround.client.ClientRegistry;
import org.blockartistry.DynSurround.client.footsteps.implem.BlockMap;
import org.blockartistry.DynSurround.client.fx.BlockEffect;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.client.sound.SoundEffect;
import org.blockartistry.DynSurround.client.weather.WeatherProperties;
import org.blockartistry.DynSurround.expression.ExpressionEngine;
import org.blockartistry.DynSurround.registry.BlockInfo.BlockInfoMutable;
import org.blockartistry.lib.MCHelper;
import org.blockartistry.lib.expression.IDynamicVariant;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

/**
 * OK - it's a hack. Don't want to mess with the core state too much so these
 * proxies server as a way to glue the mod data to the GUI.
 */
@SideOnly(Side.CLIENT)
public abstract class DataProxy extends Observable {

	public static final List<Observable> dataPools = new ArrayList<Observable>();

	public static void update() {
		for (final Observable o : dataPools)
			o.notifyObservers();
	}

	@Override
	public void notifyObservers() {
		this.setChanged();
		super.notifyObservers();
	}

	@SideOnly(Side.CLIENT)
	public static class ScriptVariableData extends DataProxy {

		public ScriptVariableData() {
			dataPools.add(this);
		}

		public List<IDynamicVariant<?>> getVariables() {
			return ExpressionEngine.instance().getVariables();
		}

	}

	@SideOnly(Side.CLIENT)
	public static class WeatherData extends DataProxy {

		public WeatherData() {
			dataPools.add(this);
		}

		public String getRainStatus() {
			return WeatherProperties.getIntensity().name();
		}

		public float getRainIntensity() {
			return WeatherProperties.getIntensityLevel();
		}

		public int getRainTime() {
			return WeatherProperties.getNextRainChange();
		}

		public float getThunderStrength() {
			return WeatherProperties.getThunderStrength();
		}

		public int getThunderTime() {
			return WeatherProperties.getNextThunderChange();
		}

		public int getNextThunderEvent() {
			return WeatherProperties.getNextThunderEvent();
		}
	}

	@SideOnly(Side.CLIENT)
	public static class ViewedBlockData extends DataProxy {

		protected final BlockInfoMutable mutable = new BlockInfoMutable();

		protected BlockPos targetBlock = BlockPos.ORIGIN;
		protected IBlockState state;

		public ViewedBlockData() {
			dataPools.add(this);
		}

		@Override
		public void notifyObservers() {
			final RayTraceResult current = Minecraft.getMinecraft().objectMouseOver;
			if (current == null || current.getBlockPos() == null)
				this.targetBlock = BlockPos.ORIGIN;
			else
				this.targetBlock = current.getBlockPos();

			this.state = EnvironState.getWorld().getBlockState(this.targetBlock);
			this.mutable.set(this.state);
			super.notifyObservers();
		}

		public String getBlockName() {
			return this.mutable.toString();
		}

		public String getBlockMaterial() {
			return MCHelper.getMaterialName(this.state.getMaterial());
		}

		public List<String> getFootstepAcoustics() {
			final List<String> result = new ArrayList<String>();
			final BlockMap bm = ClientRegistry.FOOTSTEPS.getBlockMap();
			if (bm != null) {
				final List<String> data = new ArrayList<String>();
				bm.collectData(EnvironState.getWorld(), this.state, this.targetBlock, data);
				result.addAll(data);
			}
			return result;
		}

		public List<String> getBlockEffects() {
			final List<String> result = new ArrayList<String>();
			BlockEffect[] effects = ClientRegistry.BLOCK.getEffects(state);
			for (final BlockEffect e : effects) {
				result.add(e.getEffectType().getName());
			}

			effects = ClientRegistry.BLOCK.getAlwaysOnEffects(state);
			for (final BlockEffect e : effects) {
				result.add(e.getEffectType().getName() + " (Always on)");
			}
			return result;
		}

		public List<String> getBlockSounds() {
			final List<String> result = new ArrayList<String>();
			SoundEffect[] sounds = ClientRegistry.BLOCK.getAllSounds(this.state);
			for (final SoundEffect s : sounds)
				result.add(s.toString());

			sounds = ClientRegistry.BLOCK.getAllStepSounds(this.state);
			if (sounds.length > 0)
				for (final SoundEffect s : sounds)
					result.add(s.toString() + " (Step Sound)");

			return result;
		}

		public List<String> getBlockOreDictionary() {
			final List<String> result = new ArrayList<String>();
			final Item item = Item.getItemFromBlock(this.state.getBlock());
			if (item != null) {
				final ItemStack stack = this.state.getBlock().getPickBlock(this.state,
						Minecraft.getMinecraft().objectMouseOver, EnvironState.getWorld(), this.targetBlock,
						EnvironState.getPlayer());
				if (stack != null && !stack.isEmpty())
					for (int i : OreDictionary.getOreIDs(stack))
						result.add(OreDictionary.getOreName(i));
			}
			return result;
		}

	}

}
