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
package org.orecruncher.dsurround.client.handlers.effects;

import java.util.List;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.effects.EntityEffect;
import org.orecruncher.dsurround.client.effects.IEntityEffectFactory;
import org.orecruncher.dsurround.client.effects.IEntityEffectFactoryFilter;
import org.orecruncher.dsurround.client.effects.IEntityEffectHandlerState;
import org.orecruncher.dsurround.client.fx.particle.ParticleTextPopOff;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.dsurround.registry.EntityEffectInfo;
import org.orecruncher.lib.Color;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.lib.random.XorShiftRandom;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityHealthPopoffEffect extends EntityEffect {

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

	protected float lastHealth;

	@Override
	public void intitialize(@Nonnull final IEntityEffectHandlerState state) {
		super.intitialize(state);
		final EntityLivingBase entity = (EntityLivingBase) getState().subject().get();
		this.lastHealth = entity.getHealth();
	}

	@Override
	public String name() {
		return "Health Tracker";
	}

	@Override
	public boolean receiveLastCall() {
		return true;
	}

	@Override
	public void update(@Nonnull final Entity subject) {
		if (!ModOptions.player.enableDamagePopoffs)
			return;

		final EntityLivingBase entity = (EntityLivingBase) subject;
		if (this.lastHealth != entity.getHealth()) {
			final World world = EnvironState.getWorld();
			final int adjustment = MathHelper.ceil(entity.getHealth() - this.lastHealth);

			this.lastHealth = entity.getHealth();

			// Don't display if it is the current player in first person view
			if (!EnvironState.isPlayer(subject) || !isFirstPersonView()) {

				final int delta = Math.max(1, MathStuff.abs(adjustment));
				final int criticalAmount = (int) (entity.getMaxHealth() / 2.5F);

				final AxisAlignedBB bb = entity.getEntityBoundingBox();
				final double posX = entity.posX;
				final double posY = bb.maxY + 0.5D;
				final double posZ = entity.posZ;
				final String text = String.valueOf(delta);
				final Color color = adjustment > 0 ? HEAL_TEXT_COLOR : DAMAGE_TEXT_COLOR;

				ParticleTextPopOff particle = null;
				if (ModOptions.player.showCritWords && adjustment < 0 && delta >= criticalAmount) {
					particle = new ParticleTextPopOff(world, getPowerWord(), CRITICAL_TEXT_COLOR, posX, posY + 0.5D,
							posZ);
					getState().addParticle(particle);
				}
				particle = new ParticleTextPopOff(world, text, color, posX, posY, posZ);
				getState().addParticle(particle);
			}
		}
	}

	// Currently restricted to the active player. Have stuff to unwind in the
	// footprint code.
	public static final IEntityEffectFactoryFilter DEFAULT_FILTER = (@Nonnull final Entity e,
			@Nonnull final EntityEffectInfo eei) -> e instanceof EntityLivingBase;

	public static class Factory implements IEntityEffectFactory {

		@Override
		public List<EntityEffect> create(@Nonnull final Entity entity, @Nonnull final EntityEffectInfo eei) {
			return ImmutableList.of(new EntityHealthPopoffEffect());
		}
	}

}
