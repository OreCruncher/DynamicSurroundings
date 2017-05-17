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
import javax.annotation.Nullable;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.api.events.SpeechTextEvent;
import org.blockartistry.DynSurround.client.fx.particle.ParticleBillboard;
import org.blockartistry.DynSurround.client.fx.particle.ParticleHelper;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.lib.Translations;
import org.blockartistry.lib.WorldUtils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
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

	public static SpeechBubbleHandler INSTANCE;

	private static final int MIN_TEXT_WIDTH = 60;
	private static final int MAX_TEXT_WIDTH = MIN_TEXT_WIDTH * 3;
	private static final String SPLASH_TOKEN = "$MINECRAFT$";
	private static final ResourceLocation SPLASH_TEXT = new ResourceLocation("texts/splashes.txt");

	private final TIntObjectHashMap<EntityBubbleContext> messages = new TIntObjectHashMap<EntityBubbleContext>();
	private final Translations xlate = new Translations();
	private final List<String> minecraftSplashText = new ArrayList<String>();

	private static class ExpireFilter implements Predicate<SpeechBubbleData> {

		private final long timeThreshold;

		public ExpireFilter(final long threshhold) {
			this.timeThreshold = threshhold;
		}

		@Override
		public boolean apply(@Nonnull final SpeechBubbleData input) {
			return this.timeThreshold > input.expires;
		}
	};

	private static class Stripper implements Function<Entry<String, String>, String> {

		private final Pattern WEIGHT_PATTERN = Pattern.compile("^([0-9]*),(.*)");

		@Override
		public String apply(@Nonnull final Entry<String, String> input) {
			final Matcher matcher = WEIGHT_PATTERN.matcher(input.getValue());
			return matcher.matches() ? matcher.group(2) : input.getValue();
		}

	}

	private static final Function<Integer, List<String>> ACCESSOR = new Function<Integer, List<String>>() {

		@Override
		public List<String> apply(@Nonnull final Integer input) {
			return INSTANCE.getMessages(input.intValue());
		}

	};

	private static class SpeechBubbleData {
		public final long expires = EnvironState.getTickCounter() + (long) (ModOptions.speechBubbleDuration * 20F);
		private String incomingText;
		private List<String> messages;

		public SpeechBubbleData(@Nonnull final String message) {
			this.incomingText = message.replaceAll("(\\xA7.)", "");
		}

		// Need to do lazy formatting of the text. Reason is that events
		// can be fired before the client is fully constructed meaning that
		// the font renderer would be non-existent.
		@Nonnull
		public List<String> getText() {
			if (this.messages == null) {
				final FontRenderer font = Minecraft.getMinecraft().getRenderManager().getFontRenderer();
				if (font == null)
					return ImmutableList.of();
				this.messages = font.listFormattedStringToWidth(this.incomingText, MAX_TEXT_WIDTH);
				this.incomingText = null;
			}
			return this.messages;
		}
	}

	private static class EntityBubbleContext {
		public final List<SpeechBubbleData> data = new ArrayList<SpeechBubbleData>();
		public ParticleBillboard bubble;
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
		INSTANCE = this;
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

		ctx.data.add(new SpeechBubbleData(message));

		if (ctx.bubble == null || ctx.bubble.shouldExpire()) {
			final ParticleBillboard particle = new ParticleBillboard(entity, ACCESSOR);
			ParticleHelper.addParticle(particle);
			ctx.bubble = particle;
		}
	}

	@Nullable
	private List<String> getMessages(final int entityId) {
		final EntityBubbleContext ctx = this.messages.get(entityId);
		if (ctx == null || ctx.data == null || ctx.data.isEmpty())
			return null;
		final List<String> result = new ArrayList<String>();
		for (final SpeechBubbleData entry : ctx.data)
			result.addAll(entry.getText());
		return result;
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
		final ExpireFilter filter = new ExpireFilter(EnvironState.getTickCounter());
		final TIntObjectIterator<EntityBubbleContext> entityData = this.messages.iterator();
		while (entityData.hasNext()) {
			entityData.advance();
			final EntityBubbleContext ctx = entityData.value();
			Iterables.removeIf(ctx.data, filter);
			if (ctx.data.isEmpty())
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
