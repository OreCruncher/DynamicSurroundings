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

package org.blockartistry.mod.DynSurround.client.waila;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.entity.EntityEmojiCapability;
import org.blockartistry.mod.DynSurround.entity.IEntityEmoji;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaEntityAccessor;
import mcp.mobius.waila.api.IWailaEntityProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.event.FMLInterModComms;

@Optional.Interface(iface = "mcp.mobius.waila.api.IWailaEntityProvider", modid = "Waila")
public class WailaEntityHandler implements IWailaEntityProvider {

	@Optional.Method(modid = "Waila")
	public static void callbackRegister(final IWailaRegistrar register) {
		ModLog.info("Registering Waila Entity handler...");
		final WailaEntityHandler instance = new WailaEntityHandler();
		register.registerBodyProvider(instance, Entity.class);
	}

	public WailaEntityHandler() {
	}

	@Override
	public Entity getWailaOverride(final IWailaEntityAccessor accessor, final IWailaConfigHandler config) {
		return null;
	}

	@Override
	public List<String> getWailaHead(final Entity entity, List<String> currenttip, final IWailaEntityAccessor accessor,
			final IWailaConfigHandler config) {
		return currenttip;
	}

	@Override
	public List<String> getWailaBody(final Entity entity, final List<String> currenttip,
			final IWailaEntityAccessor accessor, final IWailaConfigHandler config) {
		final Entity currentEntity = accessor.getEntity();
		final String entityName = EntityList.getEntityStringFromClass(currentEntity.getClass());
		if (!StringUtils.isEmpty(entityName)) {
			currenttip.add(TextFormatting.GOLD + entityName);
		}

		currenttip.add(TextFormatting.GOLD + "#" + entity.getEntityId());

		final IEntityEmoji emoji = entity.getCapability(EntityEmojiCapability.CAPABILIITY, null);
		if (emoji != null) {
			currenttip.add(TextFormatting.GREEN + "Action: " + emoji.getActionState().toString());
			currenttip.add(TextFormatting.GREEN + "Emotion: " + emoji.getEmotionalState().toString());
			currenttip.add(TextFormatting.GREEN + "Emoji: " + emoji.getEmojiType().toString());
		}

		return currenttip;
	}

	@Override
	public List<String> getWailaTail(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor,
			IWailaConfigHandler config) {
		return currenttip;
	}

	@Override
	public NBTTagCompound getNBTData(EntityPlayerMP player, Entity ent, NBTTagCompound tag, World world) {
		return null;
	}

	public static void register() {
		if (ModOptions.enableDebugLogging)
			FMLInterModComms.sendMessage("Waila", "register", WailaEntityHandler.class.getName() + ".callbackRegister");
	}
}