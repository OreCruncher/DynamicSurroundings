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
package org.blockartistry.DynSurround.client.handlers.effects;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.fx.particle.ParticleTextPopOff;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.event.PopoffEvent;
import org.blockartistry.lib.Color;
import org.blockartistry.lib.WorldUtils;
import org.blockartistry.lib.effects.EventEffect;
import org.blockartistry.lib.effects.IEventEffectLibraryState;
import org.blockartistry.lib.math.MathStuff;
import org.blockartistry.lib.random.XorShiftRandom;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PopoffEventEffect extends EventEffect {

	private static final double DISTANCE = 32;
	private static final Color CRITICAL_TEXT_COLOR = Color.MC_GOLD;
	private static final Color HEAL_TEXT_COLOR = Color.MC_GREEN;
	private static final Color DAMAGE_TEXT_COLOR = Color.MC_RED;

	// In case you want to know....
	// http://www.66batmania.com/trivia/bat-fight-words/
	private static final String[] POWER_WORDS = new String[] { "AIEEE", "AIIEEE", "ARRGH", "AWK", "AWKKKKKK", "BAM",
			"BANG", "BANG-ETH", "BIFF", "BLOOP", "BLURP", "BOFF", "BONK", "CLANK", "CLANK-EST", "CLASH", "CLUNK",
			"CLUNK-ETH", "CRRAACK", "CRASH", "CRRAACK", "CRUNCH", "CRUNCH-ETH", "EEE-YOW", "FLRBBBBB", "GLIPP",
			"GLURPP", "KAPOW", "KAYO", "KER-SPLOOSH", "KERPLOP", "KLONK", "KLUNK", "KRUNCH", "OOOFF", "OOOOFF", "OUCH",
			"OUCH-ETH", "OWWW", "OW-ETH", "PAM", "PLOP", "POW", "POWIE", "QUNCKKK", "RAKKK", "RIP", "SLOSH", "SOCK",
			"SPLATS", "SPLATT", "SPLOOSH", "SWAAP", "SWISH", "SWOOSH", "THUNK", "THWACK", "THWACKE", "THWAPE", "THWAPP",
			"UGGH", "URKKK", "VRONK", "WHACK", "WHACK-ETH", "WHAM-ETH", "WHAMM", "WHAMMM", "WHAP", "Z-ZWAP", "ZAM",
			"ZAMM", "ZAMMM", "ZAP", "ZAP-ETH", "ZGRUPPP", "ZLONK", "ZLOPP", "ZLOTT", "ZOK", "ZOWIE", "ZWAPP", "ZZWAP",
			"ZZZZWAP", "ZZZZZWAP" };

	private String getPowerWord() {
		return POWER_WORDS[XorShiftRandom.current().nextInt(POWER_WORDS.length)] + "!";
	}

	public PopoffEventEffect(@Nonnull final IEventEffectLibraryState state) {
		super(state);
	}

	@SubscribeEvent
	public void onEvent(@Nonnull final PopoffEvent data) {
		if (!ModOptions.player.enableDamagePopoffs || EnvironState.isPlayer(data.entityId))
			return;

		// Don't want to display if too far away.
		final double distance = EnvironState.distanceToPlayer(data.posX, data.posY, data.posZ);
		if (distance >= (DISTANCE * DISTANCE))
			return;

		final World world = EnvironState.getWorld();

		// Calculate the location of where it should display
		final Entity entity = WorldUtils.locateEntity(world, data.entityId);
		if (entity != null) {
			final AxisAlignedBB bb = entity.getEntityBoundingBox();
			final double posX = entity.posX;
			final double posY = bb.maxY + 0.5D;
			final double posZ = entity.posZ;

			ParticleTextPopOff particle = null;
			if (data.isCritical && ModOptions.player.showCritWords) {
				particle = new ParticleTextPopOff(world, getPowerWord(), CRITICAL_TEXT_COLOR, posX, posY + 0.5D, posZ);
				this.getState().addParticle(particle);
			}

			final String text = String.valueOf(MathStuff.abs(data.amount));
			final Color color = data.amount < 0 ? HEAL_TEXT_COLOR : DAMAGE_TEXT_COLOR;
			particle = new ParticleTextPopOff(world, text, color, posX, posY, posZ);
			this.getState().addParticle(particle);
		}
	}

}
