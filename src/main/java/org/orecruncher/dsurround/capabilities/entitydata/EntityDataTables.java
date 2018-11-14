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

package org.orecruncher.dsurround.capabilities.entitydata;

import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.capabilities.CapabilityEntityData;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAttackRanged;
import net.minecraft.entity.ai.EntityAIAttackRangedBow;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAICreeperSwell;
import net.minecraft.entity.ai.EntityAIFleeSun;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAIOcelotAttack;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAIRunAroundLikeCrazy;
import net.minecraft.entity.ai.EntityAIZombieAttack;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntityPolarBear;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.passive.EntityRabbit;

public final class EntityDataTables {

	private final static Set<Class<? extends EntityAIBase>> ATTACK_CLASSES = new ReferenceOpenHashSet<>();
	private final static Set<Class<? extends EntityAIBase>> FLEE_CLASSES = new ReferenceOpenHashSet<>();

	public static void add(@Nonnull final Class<? extends EntityAIBase> clazz, final boolean isAttack) {
		final Set<Class<? extends EntityAIBase>> theSet = isAttack ? ATTACK_CLASSES : FLEE_CLASSES;
		if (!theSet.contains(clazz))
			theSet.add(clazz);
	}

	@SuppressWarnings({ "unchecked" })
	private static Class<? extends EntityAIBase> findInternalClass(@Nonnull Class<? extends EntityLiving> clazz,
			@Nonnull final String className) {
		final Class<?>[] classes = clazz.getDeclaredClasses();
		for (final Class<?> c : classes) {
			if (c.getName().endsWith(className) && EntityAIBase.class.isAssignableFrom(c))
				return (Class<? extends EntityAIBase>) c;
		}

		return null;
	}

	private static void registerSpecial(@Nonnull final Class<? extends EntityLiving> clazz,
			@Nonnull final String className, final boolean isAttack) {
		final Class<? extends EntityAIBase> ai = findInternalClass(clazz, className);
		if (ai != null)
			add(ai, isAttack);
		else
			ModBase.log().warn("Unable to locate class '%s' inside [%s]", className, clazz.toGenericString());
	}

	static {
		add(EntityAIAttackMelee.class, true);
		add(EntityAIAttackRanged.class, true);
		add(EntityAIAttackRangedBow.class, true);
		add(EntityAIAvoidEntity.class, false);
		add(EntityAICreeperSwell.class, true);
		add(EntityAIFleeSun.class, false);
		add(EntityAIHurtByTarget.class, false);
		add(EntityAILeapAtTarget.class, true);
		add(EntityAIOcelotAttack.class, true);
		add(EntityAIOwnerHurtByTarget.class, true);
		add(EntityAIPanic.class, false);
		add(EntityAIRunAroundLikeCrazy.class, false);
		add(EntityAIZombieAttack.class, true);

		// Special embedded AI tasks
		registerSpecial(EntityEnderman.class, "AIFindPlayer", true);
		registerSpecial(EntityGhast.class, "AIFireballAttack", true);
		registerSpecial(EntityGuardian.class, "AIGuardianAttack", true);
		registerSpecial(EntityPolarBear.class, "AIMeleeAttack", true);
		registerSpecial(EntityPolarBear.class, "AIPanic", false);
		registerSpecial(EntityPolarBear.class, "AIAttackPlayer", true);
		registerSpecial(EntityShulker.class, "AIAttackNearest", true);
		registerSpecial(EntitySlime.class, "AISlimeAttack", true);
		registerSpecial(EntitySpider.class, "AISpiderAttack", true);
		registerSpecial(EntityRabbit.class, "AIAvoidEntity", false);
	}

	@Nonnull
	private static boolean eval(@Nonnull final EntityLiving entity,
			@Nonnull final Set<Class<? extends EntityAIBase>> markers) {
		return Stream.of(entity.tasks.executingTaskEntries, entity.targetTasks.executingTaskEntries)
				.flatMap(e -> e.stream()).map(t -> t.action.getClass()).filter(c -> markers.contains(c)).findFirst()
				.isPresent();
	}

	@Nonnull
	public static void assess(@Nonnull final EntityLiving entity) {
		final IEntityDataSettable data = (IEntityDataSettable) CapabilityEntityData.getCapability(entity);
		if (data != null) {
			final boolean isAttacking = eval(entity, ATTACK_CLASSES);
			final boolean isFleeing = eval(entity, FLEE_CLASSES);
			data.setAttacking(isAttacking);
			data.setFleeing(isFleeing);
		}
	}

}
