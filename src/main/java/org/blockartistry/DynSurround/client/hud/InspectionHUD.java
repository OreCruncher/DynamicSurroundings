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

package org.blockartistry.DynSurround.client.hud;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.ClientRegistry;
import org.blockartistry.DynSurround.client.footsteps.implem.BlockMap;
import org.blockartistry.DynSurround.client.fx.BlockEffect;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.client.handlers.EffectManager;
import org.blockartistry.DynSurround.client.handlers.FxHandler;
import org.blockartistry.DynSurround.client.sound.SoundEffect;
import org.blockartistry.DynSurround.registry.BlockInfo;
import org.blockartistry.lib.ItemStackUtil;
import org.blockartistry.lib.MCHelper;
import org.blockartistry.lib.WorldUtils;
import org.blockartistry.lib.gui.Panel.Reference;
import org.blockartistry.lib.gui.TextPanel;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

@SideOnly(Side.CLIENT)
public class InspectionHUD extends GuiOverlay {

	private static final String TEXT_FOOTSTEP_ACOUSTICS = TextFormatting.DARK_PURPLE + "<Footstep Accoustics>";
	private static final String TEXT_BLOCK_EFFECTS = TextFormatting.DARK_PURPLE + "<Block Effects>";
	private static final String TEXT_ALWAYS_ON_EFFECTS = TextFormatting.DARK_PURPLE + "<Always On Effects>";
	private static final String TEXT_STEP_SOUNDS = TextFormatting.DARK_PURPLE + "<Step Sounds>";
	private static final String TEXT_BLOCK_SOUNDS = TextFormatting.DARK_PURPLE + "<Block Sounds>";
	private static final String TEXT_DICTIONARY_NAMES = TextFormatting.DARK_PURPLE + "<Dictionary Names>";

	private final BlockInfo.BlockInfoMutable block = new BlockInfo.BlockInfoMutable();

	private static List<String> gatherOreNames(final ItemStack stack) {
		final List<String> result = new ArrayList<>();
		if (ItemStackUtil.isValidItemStack(stack))
			for (final int i : OreDictionary.getOreIDs(stack))
				result.add(OreDictionary.getOreName(i));
		return result;
	}

	private static String getItemName(final ItemStack stack) {
		final Item item = stack.getItem();
		final String itemName = MCHelper.nameOf(item);

		if (itemName != null) {
			final StringBuilder builder = new StringBuilder();
			builder.append(itemName);
			if (stack.getHasSubtypes())
				builder.append(':').append(stack.getItemDamage());
			return builder.toString();
		}

		return null;
	}

	private List<String> gatherBlockText(final ItemStack stack, final List<String> text, final IBlockState state,
			final BlockPos pos) {

		if (ItemStackUtil.isValidItemStack(stack)) {
			text.add(TextFormatting.RED + stack.getDisplayName());
			final String itemName = getItemName(stack);
			if (itemName != null) {
				text.add("ITEM: " + itemName);
				text.add(TextFormatting.DARK_AQUA + stack.getItem().getClass().getName());
			}
		}

		if (state != null) {
			this.block.set(state);
			text.add("BLOCK: " + this.block.toString());
			text.add(TextFormatting.DARK_AQUA + this.block.getBlock().getClass().getName());
			text.add("Material: " + MCHelper.getMaterialName(state.getMaterial()));
			final SoundType st = state.getBlock().getSoundType(state, EnvironState.getWorld(), pos,
					EnvironState.getPlayer());
			if (st != null) {
				text.add("Step Sound: " + st.getStepSound().getSoundName().toString());
			}

			if (ClientRegistry.FOOTSTEPS.hasFootprint(state))
				text.add("Footprints Generated");

			final BlockMap bm = ClientRegistry.FOOTSTEPS.getBlockMap();
			if (bm != null) {
				final List<String> data = new ArrayList<>();
				bm.collectData(EnvironState.getWorld(), state, pos, data);
				if (data.size() > 0) {
					text.add(TEXT_FOOTSTEP_ACOUSTICS);
					for (final String s : data)
						text.add(TextFormatting.GOLD + s);
				}
			}

			BlockEffect[] effects = ClientRegistry.BLOCK.getEffects(state);
			if (effects.length > 0) {
				text.add(TEXT_BLOCK_EFFECTS);
				for (final BlockEffect e : effects) {
					text.add(TextFormatting.GOLD + e.getEffectType().getName());
				}
			}

			effects = ClientRegistry.BLOCK.getAlwaysOnEffects(state);
			if (effects.length > 0) {
				text.add(TEXT_ALWAYS_ON_EFFECTS);
				for (final BlockEffect e : effects) {
					text.add(TextFormatting.GOLD + e.getEffectType().getName());
				}
			}

			SoundEffect[] sounds = ClientRegistry.BLOCK.getAllStepSounds(state);
			if (sounds.length > 0) {
				text.add(TEXT_STEP_SOUNDS);
				text.add(TextFormatting.DARK_GREEN + "Chance: 1 in " + ClientRegistry.BLOCK.getStepSoundChance(state));
				for (final SoundEffect s : sounds)
					text.add(TextFormatting.GOLD + s.toString());
			}

			sounds = ClientRegistry.BLOCK.getAllSounds(state);
			if (sounds.length > 0) {
				text.add(TEXT_BLOCK_SOUNDS);
				text.add(TextFormatting.DARK_GREEN + "Chance: 1 in " + ClientRegistry.BLOCK.getSoundChance(state));
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
			text.add(TextFormatting.GOLD + "Effects");
			text.addAll(((FxHandler) EffectManager.instance().lookupService(FxHandler.class)).getEffects(entity));
		} catch (@Nonnull final Exception ex) {
			text.add(TextFormatting.RED + "!! ERROR !!");
		}
		return text;
	}

	private static final ItemStack tool = new ItemStack(Items.CARROT_ON_A_STICK);

	private static boolean isHolding() {
		final EntityPlayer player = EnvironState.getPlayer();
		return ItemStack.areItemStacksEqual(tool, player.getHeldItem(EnumHand.MAIN_HAND));
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

	@SubscribeEvent
	public static void tooltipEvent(@Nonnull final ItemTooltipEvent event) {
		if (ModOptions.logging.enableDebugLogging) {
			final ItemStack stack = event.getItemStack();
			if (stack != null) {
				final String itemName = getItemName(stack);
				event.getToolTip().add(TextFormatting.GOLD + itemName);
				event.getToolTip().add(TextFormatting.GOLD + stack.getItem().getClass().getName());
			}
		}
	}
}
