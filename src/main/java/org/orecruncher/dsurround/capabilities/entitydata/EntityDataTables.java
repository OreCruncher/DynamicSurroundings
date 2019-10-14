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

import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.capabilities.CapabilityEntityData;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
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
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIOcelotAttack;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAIRunAroundLikeCrazy;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.ai.EntityAIZombieAttack;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityEvoker;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntityPolarBear;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpellcasterIllager;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityVex;
import net.minecraft.entity.monster.EntityVindicator;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntityWolf;

public final class EntityDataTables {

	private enum ClassType {
		Neither, Attack, Flee
	}

	private final static Map<Class<?>, ClassType> AI_TASKS = new Reference2ObjectOpenHashMap<>();
	private final static Map<String, ClassType> MAPPINGS = new Object2ObjectOpenHashMap<>();

	private static void add(@Nonnull final Class<?> clazz, @Nonnull ClassType ct) {
		AI_TASKS.put(clazz, ct);
		MAPPINGS.put(resolveName(clazz), ct);
	}

	private static String resolveName(@Nonnull final Class<?> clazz) {
		String n = clazz.getName();
		int i = n.lastIndexOf('.');
		return n.substring(i + 1);
	}

	private static ClassType find(@Nonnull final EntityAIBase aiTask) {
		String name = resolveName(aiTask.getClass());
		ClassType ct = MAPPINGS.get(name);

		if (ct == null) {
			
			try {
				for (Entry<Class<?>, ClassType> kvp : AI_TASKS.entrySet()) {
					if (kvp.getKey().isInstance(aiTask)) {
						MAPPINGS.put(name, ct = kvp.getValue());
						return ct;
					}
				}
			} catch(final Exception ex) {
				// Weird.  Maybe Eclipse J9 - it's whacky
				ModBase.log().catching(ex);
			}
			
			MAPPINGS.put(name, ct = ClassType.Neither);
		}

		return ct;
	}

	private static void add(@Nonnull final Class<? extends EntityLiving> clazz, @Nonnull final String className,
			final ClassType ct) {
		String name = resolveName(clazz) + "$" + className;
		MAPPINGS.put(name, ct);
	}

	static {
		add(EntityAIAttackMelee.class, ClassType.Attack);
		add(EntityAIAttackRanged.class, ClassType.Attack);
		add(EntityAIAttackRangedBow.class, ClassType.Attack);
		add(EntityAICreeperSwell.class, ClassType.Attack);
		add(EntityAILeapAtTarget.class, ClassType.Attack);
		add(EntityAIOcelotAttack.class, ClassType.Attack);
		add(EntityAIOwnerHurtByTarget.class, ClassType.Attack);
		add(EntityAIZombieAttack.class, ClassType.Attack);
		add(EntityAINearestAttackableTarget.class, ClassType.Attack);

		add(EntityAIAvoidEntity.class, ClassType.Flee);
		add(EntityAIFleeSun.class, ClassType.Flee);
		add(EntityAIHurtByTarget.class, ClassType.Flee);
		add(EntityAIPanic.class, ClassType.Flee);
		add(EntityAIRunAroundLikeCrazy.class, ClassType.Flee);

		// Specials because they are inner classes

		add(EntityEnderman.class, "AIFindPlayer", ClassType.Attack);
		add(EntityGhast.class, "AIFireballAttack", ClassType.Attack);
		add(EntityGuardian.class, "AIGuardianAttack", ClassType.Attack);
		add(EntityPolarBear.class, "AIMeleeAttack", ClassType.Attack);
		add(EntityPolarBear.class, "AIAttackPlayer", ClassType.Attack);
		add(EntityShulker.class, "AIAttackNearest", ClassType.Attack);
		add(EntitySlime.class, "AISlimeAttack", ClassType.Attack);
		add(EntitySpider.class, "AISpiderAttack", ClassType.Attack);
		add(EntityBlaze.class, "AIFireballAttack", ClassType.Attack);
		add(EntityEvoker.class, "AICastingSpell", ClassType.Attack);
		add(EntityEvoker.class, "AISummonSpell", ClassType.Attack);
		add(EntityEvoker.class, "AIAttackSpell", ClassType.Attack);
		add(EntityEvoker.class, "AIWololoSpell", ClassType.Attack);
		add(EntitySpellcasterIllager.class, "AICastingApell", ClassType.Attack);
		add(EntitySpellcasterIllager.class, "AIMirriorSpell", ClassType.Attack);
		add(EntitySpellcasterIllager.class, "AIBlindnessSpell", ClassType.Attack);
		add(EntityShulker.class, "AIAttack", ClassType.Attack);
		add(EntityVex.class, "AIChargeAttack", ClassType.Attack);
		add(EntityVindicator.class, "AIJohnnyAttack", ClassType.Attack);

		add(EntityPolarBear.class, "AIPanic", ClassType.Flee);
		add(EntityRabbit.class, "AIAvoidEntity", ClassType.Flee);
		add(EntityWolf.class, "AIAvoidEntity", ClassType.Flee);
	}

	@Nonnull
	private static boolean eval(@Nonnull final EntityLiving entity, @Nonnull ClassType desiredType) {

		for (EntityAITaskEntry task : entity.tasks.executingTaskEntries) {
			if (find(task.action) == desiredType)
				return true;
		}

		for (EntityAITaskEntry task : entity.targetTasks.executingTaskEntries) {
			if (find(task.action) == desiredType)
				return true;
		}

		return false;
	}

	@Nonnull
	public static void assess(@Nonnull final EntityLiving entity) {
		final IEntityDataSettable data = (IEntityDataSettable) CapabilityEntityData.getCapability(entity);
		if (data != null) {
			final boolean isAttacking = eval(entity, ClassType.Attack);
			final boolean isFleeing = eval(entity, ClassType.Flee);
			data.setAttacking(isAttacking);
			data.setFleeing(isFleeing);
		}
	}

}
