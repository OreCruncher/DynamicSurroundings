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

package org.blockartistry.mod.DynSurround.client.swing;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import javax.annotation.Nullable;

import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.client.footsteps.implem.BlockMap;
import org.blockartistry.mod.DynSurround.client.fx.BlockEffect;
import org.blockartistry.mod.DynSurround.client.handlers.ExpressionStateHandler;
import org.blockartistry.mod.DynSurround.client.handlers.ExpressionStateHandler.IDynamicVariable;
import org.blockartistry.mod.DynSurround.client.sound.SoundEffect;
import org.blockartistry.mod.DynSurround.client.weather.WeatherProperties;
import org.blockartistry.mod.DynSurround.registry.BlockRegistry;
import org.blockartistry.mod.DynSurround.registry.FootstepsRegistry;
import org.blockartistry.mod.DynSurround.registry.RegistryManager;
import org.blockartistry.mod.DynSurround.registry.RegistryManager.RegistryType;
import org.blockartistry.mod.DynSurround.util.MCHelper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

/**
 * OK - it's a hack. Don't want to mess with the core state too much so these
 * proxies server as a way to glue the mod data to the GUI.
 */
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

	public static class ScriptVariableData extends DataProxy {

		public ScriptVariableData() {
			dataPools.add(this);
		}

		public List<IDynamicVariable> getVariables() {
			return ExpressionStateHandler.getVariables();
		}

	}

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

	public static class ViewedBlockData extends DataProxy {

		protected final BlockRegistry blocks = RegistryManager.get(RegistryType.BLOCK);
		protected final FootstepsRegistry footsteps = RegistryManager.get(RegistryType.FOOTSTEPS);

		protected BlockPos targetBlock = BlockPos.ORIGIN;
		protected IBlockState state;

		public ViewedBlockData() {
			dataPools.add(this);
		}

		@Override
		public void notifyObservers() {
			final BlockPos previous = this.targetBlock;
			final RayTraceResult current = Minecraft.getMinecraft().objectMouseOver;
			if (current == null || current.getBlockPos() == null)
				this.targetBlock = BlockPos.ORIGIN;
			else
				this.targetBlock = current.getBlockPos();

			if (previous.equals(this.targetBlock))
				return;

			this.state = EnvironState.getWorld().getBlockState(this.targetBlock);
			super.notifyObservers();
		}

		@Nullable
		public IBlockState getBlockState() {
			if (this.state == null)
				this.state = EnvironState.getWorld().getBlockState(this.targetBlock);
			return this.state;
		}

		public String getBlockName() {
			final String blockName = MCHelper.nameOf(this.state.getBlock());
			if (blockName != null) {
				final StringBuilder builder = new StringBuilder();
				builder.append(blockName);
				if (MCHelper.hasVariants(this.state.getBlock()))
					builder.append(':').append(this.state.getBlock().getMetaFromState(this.state));
				return builder.toString();
			}
			return "Unknown";
		}

		public String getBlockMaterial() {
			return MCHelper.getMaterialName(this.state.getMaterial());
		}

		public List<String> getFootstepAcoustics() {
			final List<String> result = new ArrayList<String>();
			final BlockMap bm = footsteps.getBlockMap();
			if (bm != null) {
				final List<String> data = new ArrayList<String>();
				bm.collectData(this.state, data);
				result.addAll(data);
			}
			return result;
		}

		public List<String> getBlockEffects() {
			final List<String> result = new ArrayList<String>();
			List<BlockEffect> effects = this.blocks.getEffects(state);
			for (final BlockEffect e : effects) {
				result.add(e.getEffectType().getName());
			}

			effects = this.blocks.getAlwaysOnEffects(state);
			for (final BlockEffect e : effects) {
				result.add(e.getEffectType().getName() + " (Always on)");
			}
			return result;
		}
		
		public List<String> getBlockSounds() {
			final List<String> result = new ArrayList<String>();
			List<SoundEffect> sounds = this.blocks.getAllSounds(this.state);
			for(final SoundEffect s: sounds)
				result.add(s.toString());
			
			sounds = this.blocks.getAllStepSounds(this.state);
			if(sounds.size() > 0)
				for(final SoundEffect s: sounds)
					result.add(s.toString() + " (Step Sound)");
			
			return result;
		}
		
	}

}
