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

package org.orecruncher.dsurround.client.hud;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.capabilities.CapabilityEntityData;
import org.orecruncher.dsurround.capabilities.entitydata.IEntityData;
import org.orecruncher.dsurround.client.fx.BlockEffect;
import org.orecruncher.dsurround.client.handlers.EffectManager;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.dsurround.client.handlers.FxHandler;
import org.orecruncher.dsurround.client.sound.SoundEffect;
import org.orecruncher.dsurround.registry.RegistryManager;
import org.orecruncher.dsurround.registry.blockstate.BlockStateData;
import org.orecruncher.dsurround.registry.blockstate.BlockStateMatcher;
import org.orecruncher.dsurround.registry.blockstate.BlockStateUtil;
import org.orecruncher.dsurround.registry.footstep.BlockMap;
import org.orecruncher.lib.ItemStackUtil;
import org.orecruncher.lib.MCHelper;
import org.orecruncher.lib.WorldUtils;
import org.orecruncher.lib.gui.Panel.Reference;
import org.orecruncher.lib.gui.TextPanel;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

@SideOnly(Side.CLIENT)
public class InspectionHUD extends Gui implements IGuiOverlay {

	private static final String TEXT_BLOCKSTATE = TextFormatting.DARK_PURPLE + "<BlockState>";
	private static final String TEXT_FOOTSTEP_ACOUSTICS = TextFormatting.DARK_PURPLE + "<Footstep Accoustics>";
	private static final String TEXT_BLOCK_EFFECTS = TextFormatting.DARK_PURPLE + "<Block Effects>";
	private static final String TEXT_ALWAYS_ON_EFFECTS = TextFormatting.DARK_PURPLE + "<Always On Effects>";
	private static final String TEXT_BLOCK_SOUNDS = TextFormatting.DARK_PURPLE + "<Block Sounds>";
	private static final String TEXT_DICTIONARY_NAMES = TextFormatting.DARK_PURPLE + "<Dictionary Names>";

	private static List<String> gatherOreNames(final ItemStack stack) {
		final List<String> result = new ArrayList<>();
		if (ItemStackUtil.isValidItemStack(stack))
			for (final int i : OreDictionary.getOreIDs(stack))
				result.add(OreDictionary.getOreName(i));
		return result;
	}

	private List<String> gatherBlockText(final ItemStack stack, final List<String> text, final IBlockState state,
			final BlockPos pos) {

		if (ItemStackUtil.isValidItemStack(stack)) {
			text.add(TextFormatting.RED + stack.getDisplayName());
			final String itemName = ItemStackUtil.getItemName(stack);
			if (itemName != null) {
				text.add("ITEM: " + itemName);
				text.add(TextFormatting.DARK_AQUA + stack.getItem().getClass().getName());
			}
		}

		if (state != null) {
			final BlockStateMatcher info = BlockStateMatcher.create(state);
			text.add("BLOCK: " + info.toString());
			text.add(TextFormatting.DARK_AQUA + info.getBlock().getClass().getName());
			text.add("Material: " + MCHelper.getMaterialName(state.getMaterial()));
			final SoundType st = state.getBlock().getSoundType(state, EnvironState.getWorld(), pos,
					EnvironState.getPlayer());
			if (st != null) {
				text.add("Step Sound: " + st.getStepSound().getSoundName().toString());
			}

			if (RegistryManager.FOOTSTEPS.hasFootprint(state))
				text.add("Footprints Generated");

			text.add(TEXT_BLOCKSTATE);
			final NBTTagCompound nbt = new NBTTagCompound();
			NBTUtil.writeBlockState(nbt, state);
			text.add(nbt.toString());

			final BlockMap bm = RegistryManager.FOOTSTEPS.getBlockMap();
			if (bm != null) {
				final List<String> data = new ArrayList<>();
				bm.collectData(EnvironState.getWorld(), state, pos, data);
				if (data.size() > 0) {
					text.add(TEXT_FOOTSTEP_ACOUSTICS);
					for (final String s : data)
						text.add(TextFormatting.GOLD + s);
				}
			}

			final BlockStateData data = BlockStateUtil.getStateData(state);
			BlockEffect[] effects = data.getEffects();
			if (effects.length > 0) {
				text.add(TEXT_BLOCK_EFFECTS);
				for (final BlockEffect e : effects) {
					text.add(TextFormatting.GOLD + e.getEffectType().getName());
				}
			}

			effects = data.getAlwaysOnEffects();
			if (effects.length > 0) {
				text.add(TEXT_ALWAYS_ON_EFFECTS);
				for (final BlockEffect e : effects) {
					text.add(TextFormatting.GOLD + e.getEffectType().getName());
				}
			}

			final SoundEffect[] sounds = data.getSounds();
			if (sounds.length > 0) {
				text.add(TEXT_BLOCK_SOUNDS);
				text.add(TextFormatting.DARK_GREEN + "Chance: 1 in " + data.getChance());
				for (final SoundEffect s : sounds)
					text.add(TextFormatting.GOLD + s.toString());
			}
		}

		final List<String> oreNames = gatherOreNames(stack);
		if (oreNames.size() > 0) {
			text.add(TEXT_DICTIONARY_NAMES);
			for (final String ore : oreNames)
				text.add(TextFormatting.GOLD + ore);
		}

		return text;
	}

	private List<String> gatherEntityText(@Nonnull final Entity entity, @Nonnull final List<String> text) {
		try {
			final ResourceLocation key = EntityList.getKey(entity);
			final String keyName;
			if (key != null)
				keyName = key.toString();
			else
				keyName = "No ID Found";
			text.add(TextFormatting.DARK_AQUA + entity.getName());
			text.add(keyName);
			text.add(entity.getClass().getName());

			final Set<String> tags = entity.getTags();
			if (tags != null && tags.size() > 0) {
				text.add(TextFormatting.GOLD + "Entity Tags");
				text.addAll(tags);
			}

			final IEntityData data = CapabilityEntityData.getCapability(entity);
			if (data != null) {
				text.add(data.serializeNBT().toString());
			}

			text.add(TextFormatting.GOLD + "Effects");
			text.addAll(((FxHandler) EffectManager.instance().lookupService(FxHandler.class)).getEffects(entity));
		} catch (@Nonnull final Exception ex) {
			text.add(TextFormatting.RED + "!! ERROR !!");
		}
		return text;
	}

	private static boolean isHolding() {
		final ItemStack held = EnvironState.getPlayer().getHeldItem(EnumHand.MAIN_HAND);
		return !held.isEmpty() && held.getItem() == Items.CARROT_ON_A_STICK;
	}

	private final TextPanel textPanel;

	public InspectionHUD() {
		this.textPanel = new TextPanel();
	}

	@Override
	public void doTick(final int tickRef) {

		if (tickRef != 0 && tickRef % 5 == 0) {

			this.textPanel.resetText();

			if (ModOptions.logging.enableDebugLogging && isHolding()) {
				final List<String> data = new ArrayList<>();
				final RayTraceResult current = Minecraft.getMinecraft().objectMouseOver;
				if (current != null) {
					if (current.entityHit != null) {
						gatherEntityText(current.entityHit, data);
					} else {
						final BlockPos targetBlock = (current == null || current.getBlockPos() == null)
								? BlockPos.ORIGIN
								: current.getBlockPos();
						final IBlockState state = WorldUtils.getBlockState(EnvironState.getWorld(), targetBlock);

						if (!WorldUtils.isAirBlock(state)) {
							final ItemStack stack = state != null
									? state.getBlock().getPickBlock(state, current, EnvironState.getWorld(),
											targetBlock, EnvironState.getPlayer())
									: null;

							gatherBlockText(stack, data, state, targetBlock);
						}
					}
				}

				if (data.size() > 0)
					this.textPanel.setText(data);

			}

		}
	}

	@Override
	public void doRender(@Nonnull final RenderGameOverlayEvent.Pre event) {
		if (event.getType() == ElementType.TEXT && this.textPanel.hasText()) {
			final int centerX = event.getResolution().getScaledWidth() / 2;
			final int centerY = 80;
			this.textPanel.render(centerX, centerY, Reference.TOP_CENTER);
		}
	}

}
