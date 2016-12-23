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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.event.SpeechTextEvent;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleBillboard;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleHelper;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.client.speech.SpeechBubbleRenderer;
import org.blockartistry.mod.DynSurround.client.speech.SpeechBubbleRenderer.RenderingInfo;
import org.blockartistry.mod.DynSurround.util.Translations;
import org.blockartistry.mod.DynSurround.util.WorldUtils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SpeechBubbleHandler extends EffectHandlerBase {

	public static SpeechBubbleHandler INSTANCE;

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
			final List<String> result = new ArrayList<String>();
			final List<RenderingInfo> info = INSTANCE.getMessages(input.intValue());

			if (info != null)
				for (final RenderingInfo ri : info)
					result.addAll(ri.getText());

			return result;
		}

	};

	private final TIntObjectHashMap<SpeechBubbleContext> messages = new TIntObjectHashMap<SpeechBubbleContext>();
	private final Translations xlate = new Translations();

	private void processTranslations() {
		final String[] langs;
		if (Minecraft.getMinecraft().gameSettings.language.equals(Translations.DEFAULT_LANGUAGE))
			langs = new String[] { Translations.DEFAULT_LANGUAGE };
		else
			langs = new String[] { Translations.DEFAULT_LANGUAGE, Minecraft.getMinecraft().gameSettings.language };

		this.xlate.load("/assets/dsurround/data/chat/", langs);
		this.xlate.transform(new Stripper());
	}

	protected static class SpeechBubbleData {
		public final long expires = EnvironState.getTickCounter() + (long) (ModOptions.speechBubbleDuration * 20F);
		public final RenderingInfo messages;

		public SpeechBubbleData(@Nonnull final String message) {
			this.messages = SpeechBubbleRenderer.generateRenderInfo(message);
		}
	}
	
	protected static class SpeechBubbleContext {
		public final List<SpeechBubbleData> data = new ArrayList<SpeechBubbleData>();
		public WeakReference<ParticleBillboard> bubble;
	}

	public SpeechBubbleHandler() {
		INSTANCE = this;
		processTranslations();
	}

	private void addSpeechBubbleFormatted(@Nonnull final UUID entityId, @Nonnull final String message,
			final Object... parms) {
		if (!ModOptions.enableSpeechBubbles && !ModOptions.enableEntityChat)
			return;

		final String xlated = xlate.format(message, parms);
		addSpeechBubble(entityId, xlated);
	}

	private void addSpeechBubble(@Nonnull final UUID entityId, @Nonnull final String message) {
		if (!(ModOptions.enableSpeechBubbles || ModOptions.enableEntityChat) || entityId == null
				|| StringUtils.isEmpty(message))
			return;

		final Entity entity = WorldUtils.locateEntity(EnvironState.getWorld(), entityId);
		if (entity == null)
			return;

		SpeechBubbleContext ctx = this.messages.get(entity.getEntityId());
		if(ctx == null) {
			this.messages.put(entity.getEntityId(), ctx = new SpeechBubbleContext());
		}
		
		ctx.data.add(new SpeechBubbleData(message));
		
		if (ctx.bubble == null || ctx.bubble.isEnqueued()) {
			final ParticleBillboard particle = new ParticleBillboard(entity, ACCESSOR);
			ParticleHelper.addParticle(particle);
			ctx.bubble = new WeakReference<ParticleBillboard>(particle);
		}
	}

	@Nullable
	public List<RenderingInfo> getMessages(@Nonnull final Entity entity) {
		return getMessages(entity.getEntityId());
	}

	@Nullable
	public List<RenderingInfo> getMessages(final int entityId) {
		final SpeechBubbleContext ctx = this.messages.get(entityId);
		if (ctx == null || ctx.data == null || ctx.data.isEmpty())
			return null;
		final List<RenderingInfo> result = new ArrayList<RenderingInfo>();
		for (final SpeechBubbleData entry : ctx.data)
			result.add(entry.messages);
		return result;
	}

	@Override
	public String getHandlerName() {
		return "SpeechBubbleHandler";
	}

	@Override
	public void process(@Nonnull final World world, @Nonnull final EntityPlayer player) {
		final ExpireFilter filter = new ExpireFilter(EnvironState.getTickCounter());
		final TIntObjectIterator<SpeechBubbleContext> entityData = messages.iterator();
		while (entityData.hasNext()) {
			entityData.advance();
			if (!entityData.value().data.isEmpty())
				Iterables.removeIf(entityData.value().data, filter);
			else
				entityData.remove();
		}
	}

	@Override
	public void onConnect() {
		messages.clear();
	}

	@Override
	public void onDisconnect() {
		messages.clear();
	}

	@SubscribeEvent
	public void onSpeechTextEvent(@Nonnull final SpeechTextEvent event) {
		if (event.translate)
			addSpeechBubbleFormatted(event.entityId, event.message);
		else
			addSpeechBubble(event.entityId, event.message);
	}
}
