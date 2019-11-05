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
package org.orecruncher.dsurround.mixins;

import org.orecruncher.dsurround.client.gui.GuiFilteredConfigEntries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;

@Mixin(GuiConfig.class)
public class MixinGuiConfig {

	@Redirect(method = "<init>(Lnet/minecraft/client/gui/GuiScreen;Ljava/util/List;Ljava/lang/String;Ljava/lang/String;ZZLjava/lang/String;Ljava/lang/String;)V", at = @At(value = "NEW", target = "(Lnet/minecraftforge/fml/client/config/GuiConfig;Lnet/minecraft/client/Minecraft;)Lnet/minecraftforge/fml/client/config/GuiConfigEntries;"))
	public GuiConfigEntries constructEntries(GuiConfig parent, Minecraft mc) {
		return new GuiFilteredConfigEntries(parent, mc);
	}

	@Redirect(method = "initGui()V", at = @At(value = "NEW", target = "(Lnet/minecraftforge/fml/client/config/GuiConfig;Lnet/minecraft/client/Minecraft;)Lnet/minecraftforge/fml/client/config/GuiConfigEntries;"))
	public GuiConfigEntries constructEntries1(GuiConfig parent, Minecraft mc) {
		return new GuiFilteredConfigEntries(parent, mc);
	}
}
