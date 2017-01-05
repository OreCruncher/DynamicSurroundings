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

package org.blockartistry.mod.DynSurround.client.fx.particle;

import java.util.concurrent.ThreadLocalRandom;

import org.blockartistry.mod.DynSurround.util.Color;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class ParticleCriticalPopOff extends ParticleTextPopOff {
	
	public static final Color TEXT_COLOR = Color.getColor(TextFormatting.GOLD);

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

	private static String getPowerWord() {
		return POWER_WORDS[ThreadLocalRandom.current().nextInt(POWER_WORDS.length)] + "!";
	}

	public ParticleCriticalPopOff(final World world, final double x, final double y, final double z) {
		super(world, getPowerWord(), TEXT_COLOR, 1.0F, x, y, z, 0.001D, 0.05D * BOUNCE_STRENGTH, 0.001D);
		this.particleGravity = -0.04F;
		this.scale = 0.5F;
	}
}
