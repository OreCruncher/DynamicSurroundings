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
package org.orecruncher.dsurround.registry.item;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.client.footsteps.implem.AcousticsManager;
import org.orecruncher.dsurround.client.footsteps.interfaces.IAcoustic;
import org.orecruncher.dsurround.registry.DataRegistryEvent;
import org.orecruncher.dsurround.registry.RegistryManager;
import org.orecruncher.dsurround.registry.footstep.FootstepsRegistry;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(modid = ModBase.MOD_ID, value = Side.CLIENT)
public class SimpleArmorItemData extends SimpleItemData implements IArmorItemData {

	private static final Map<ItemClass, IAcoustic> ARMOR = new Reference2ObjectOpenHashMap<>();
	private static final Map<ItemClass, IAcoustic> FOOT = new Reference2ObjectOpenHashMap<>();

	@SubscribeEvent
	public static void registryReload(@Nonnull final DataRegistryEvent.Reload event) {
		if (event.reg instanceof FootstepsRegistry) {
			ARMOR.clear();
			FOOT.clear();

			final AcousticsManager reg = RegistryManager.FOOTSTEPS.getAcousticManager();
			ARMOR.put(ItemClass.LEATHER, reg.getAcoustic("armor_light"));
			ARMOR.put(ItemClass.CHAIN, reg.getAcoustic("armor_medium"));
			ARMOR.put(ItemClass.CRYSTAL, reg.getAcoustic("armor_crystal"));
			ARMOR.put(ItemClass.PLATE, reg.getAcoustic("armor_heavy"));

			FOOT.put(ItemClass.LEATHER, reg.getAcoustic("armor_light"));
			FOOT.put(ItemClass.CHAIN, reg.getAcoustic("medium_foot"));
			FOOT.put(ItemClass.CRYSTAL, reg.getAcoustic("crystal_foot"));
			FOOT.put(ItemClass.PLATE, reg.getAcoustic("heavy_foot"));
		}
	}

	public SimpleArmorItemData(@Nonnull final ItemClass ic) {
		super(ic);
	}

	@Override
	@Nullable
	public IAcoustic getArmorSound(@Nonnull final ItemStack stack) {
		return ARMOR.get(this.itemClass);
	}

	@Override
	@Nullable
	public IAcoustic getFootArmorSound(@Nonnull final ItemStack stack) {
		return FOOT.get(this.itemClass);
	}

}
