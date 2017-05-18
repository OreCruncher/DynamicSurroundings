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

package org.blockartistry.DynSurround.client.handlers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.api.events.SpeechTextEvent;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.client.handlers.bubbles.EntityBubbleContext;
import org.blockartistry.DynSurround.client.handlers.bubbles.SpeechBubbleData;
import org.blockartistry.lib.Translations;
import org.blockartistry.lib.WorldUtils;

import com.google.common.base.Function;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SpeechBubbleHandler extends EffectHandlerBase {

	private static final String SPLASH_TOKEN = "$MINECRAFT$";
	private static final ResourceLocation SPLASH_TEXT = new ResourceLocation("texts/splashes.txt");

	private final TIntObjectHashMap<EntityBubbleContext> messages = new TIntObjectHashMap<EntityBubbleContext>();
	private final Translations xlate = new Translations();
	private final List<String> minecraftSplashText = new ArrayList<String>();

	private static class Stripper implements Function<Entry<String, String>, String> {

		private final Pattern WEIGHT_PATTERN = Pattern.compile("^([0-9]*),(.*)");

		@Override
		public String apply(@Nonnull final Entry<String, String> input) {
			final Matcher matcher = WEIGHT_PATTERN.matcher(input.getValue());
			return matcher.matches() ? matcher.group(2) : input.getValue();
		}

	}

	private void loadText() {
		final String[] langs;
		if (Minecraft.getMinecraft().gameSettings.language.equals(Translations.DEFAULT_LANGUAGE))
			langs = new String[] { Translations.DEFAULT_LANGUAGE };
		else
			langs = new String[] { Translations.DEFAULT_LANGUAGE, Minecraft.getMinecraft().gameSettings.language };

		this.xlate.load("/assets/dsurround/data/chat/", langs);
		this.xlate.transform(new Stripper());

		try (final IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(SPLASH_TEXT)) {
			final BufferedReader bufferedreader = new BufferedReader(
					new InputStreamReader(resource.getInputStream(), Charsets.UTF_8));
			String s;

			while ((s = bufferedreader.readLine()) != null) {
				s = s.trim();

				if (!s.isEmpty()) {
					this.minecraftSplashText.add(s);
				}
			}

		} catch (final Throwable t) {
			;
		}
	}

	public SpeechBubbleHandler() {
		loadText();
	}

	private void addSpeechBubbleFormatted(@Nonnull final Entity entity, @Nonnull final String message,
			final Object... parms) {
		String xlated = this.xlate.format(message, parms);
		if (SPLASH_TOKEN.equals(xlated))
			xlated = this.minecraftSplashText.get(RANDOM.nextInt(this.minecraftSplashText.size()));
		addSpeechBubble(entity, xlated);
	}

	private void addSpeechBubble(@Nonnull final Entity entity, @Nonnull final String message) {
		if (StringUtils.isEmpty(message))
			return;

		EntityBubbleContext ctx = this.messages.get(entity.getEntityId());
		if (ctx == null) {
			this.messages.put(entity.getEntityId(), ctx = new EntityBubbleContext());
		}

		final int expiry = EnvironState.getTickCounter() + (int) (ModOptions.speechBubbleDuration * 20F);
		ctx.add(new SpeechBubbleData(message, expiry));
		ctx.handleBubble(entity);
	}

	@Override
	public String getHandlerName() {
		return "SpeechBubbleHandler";
	}

	@Override
	public void process(@Nonnull final World world, @Nonnull final EntityPlayer player) {

		if (this.messages.size() == 0)
			return;

		// Go through the cached messages and get rid of those
		// that expire.
		final int currentTick = EnvironState.getTickCounter();
		final TIntObjectIterator<EntityBubbleContext> entityData = this.messages.iterator();
		while (entityData.hasNext()) {
			entityData.advance();
			final EntityBubbleContext ctx = entityData.value();
			if (ctx.clean(currentTick))
				entityData.remove();
		}
	}

	@Override
	public void onConnect() {
		this.messages.clear();
	}

	@Override
	public void onDisconnect() {
		this.messages.clear();
	}

	@SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
	public void onSpeechTextEvent(@Nonnull final SpeechTextEvent event) {

		final Entity entity = WorldUtils.locateEntity(EnvironState.getWorld(), event.entityId);
		if (entity == null)
			return;
		else if ((entity instanceof EntityPlayer) && !ModOptions.enableSpeechBubbles)
			return;
		else if (!ModOptions.enableEntityChat)
			return;

		if (event.translate)
			addSpeechBubbleFormatted(entity, event.message);
		else
			addSpeechBubble(entity, event.message);
	}
}
