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
import java.util.Set;

import javax.annotation.Nonnull;

import org.orecruncher.lib.ReflectedField.ObjectField;

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
import net.minecraft.entity.ai.EntityAITasks;
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

	//formatter:off
	private static final ObjectField<EntityAITasks, Set<EntityAITasks.EntityAITaskEntry>> executingTasks = new ObjectField<>(
			EntityAITasks.class,
			"executingTaskEntries",
			"field_75780_b"
		);
	//formatter:on
	
	// Describes the type of EntityAIBase task instance
	private enum TaskType {
		//@formatter:off
		None,
		Attack,
		Flee
		//@formatter:on
	}

	// Class types mapped to a TaskType
	private final static Map<Class<?>, TaskType> AI_TASKS = new Reference2ObjectOpenHashMap<>(128);

	// Name mappings to types. Need this because some of the AI task classes are not
	// accessible
	// to Dynamic Surroundings. I'm looking at you Eclipse Open J9...
	private final static Map<String, TaskType> MAPPINGS = new Object2ObjectOpenHashMap<>();

	private static void add(@Nonnull final Class<?> clazz, @Nonnull TaskType ct) {
		AI_TASKS.put(clazz, ct);
	}

	private static void add(@Nonnull final Class<? extends EntityLiving> clazz, @Nonnull final TaskType ct,
			@Nonnull final String... className) {
		for (final String cn : className) {
			final String name = resolveName(clazz) + "$" + cn;
			MAPPINGS.put(name, ct);
		}
	}

	private static String resolveName(@Nonnull final Class<?> clazz) {
		// Possible that the class name does not have dots because of
		// obsfucation.
		final String n = clazz.getName();
		final int i = n.lastIndexOf('.');
		return i > 0 ? n.substring(i + 1) : n;
	}

	@Nonnull
	private static TaskType find(@Nonnull final EntityAIBase aiTask) {
		final Class<?> clazz = aiTask.getClass();
		TaskType ct = AI_TASKS.get(clazz);
		if (ct == null) {
			// Find a match by inheritance
			for (final Entry<Class<?>, TaskType> kvp : AI_TASKS.entrySet()) {
				if (kvp.getKey().isInstance(aiTask)) {
					ct = kvp.getValue();
					break;
				}
			}

			// If we can't find by inheritance look in name mappings
			if (ct == null) {
				final String name = resolveName(clazz);
				ct = MAPPINGS.get(name);
				if (ct == null) {
					// Not something we are interested in
					ct = TaskType.None;
				}
			}

			// Stick it in the map so we don't have to repeat
			AI_TASKS.put(clazz, ct);
		}

		return ct;
	}

	static {
		// General/generic AI tasks
		add(EntityAIAttackMelee.class, TaskType.Attack);
		add(EntityAIAttackRanged.class, TaskType.Attack);
		add(EntityAIAttackRangedBow.class, TaskType.Attack);
		add(EntityAICreeperSwell.class, TaskType.Attack);
		add(EntityAILeapAtTarget.class, TaskType.Attack);
		add(EntityAIOcelotAttack.class, TaskType.Attack);
		add(EntityAIOwnerHurtByTarget.class, TaskType.Attack);
		add(EntityAIZombieAttack.class, TaskType.Attack);
		add(EntityAINearestAttackableTarget.class, TaskType.Attack);
		add(EntitySpellcasterIllager.AICastingApell.class, TaskType.Attack);
		add(EntitySpellcasterIllager.AIUseSpell.class, TaskType.Attack);
		add(EntityAIAvoidEntity.class, TaskType.Flee);
		add(EntityAIFleeSun.class, TaskType.Flee);
		add(EntityAIHurtByTarget.class, TaskType.Flee);
		add(EntityAIPanic.class, TaskType.Flee);
		add(EntityAIRunAroundLikeCrazy.class, TaskType.Flee);

		// Specials because they are inaccessible inner classes. Have to take into
		// account
		// obsfucation.
		add(EntityRabbit.class, TaskType.Attack, "AIEvilAttack", "a");
		add(EntityRabbit.class, TaskType.Flee, "AIAvoidEntity", "b");
		add(EntityRabbit.class, TaskType.Flee, "AIPanic", "f");

		add(EntityPolarBear.class, TaskType.Attack, "AIMeleeAttack", "d");
		add(EntityPolarBear.class, TaskType.Attack, "AIAttackPlayer", "a");
		add(EntityPolarBear.class, TaskType.Flee, "AIPanic", "e");

		add(EntityShulker.class, TaskType.Attack, "AIAttack", "a");
		add(EntityShulker.class, TaskType.Attack, "AIDefenseAttack", "c");
		add(EntityShulker.class, TaskType.Attack, "AIAttackNearest", "d");

		add(EntityEvoker.class, TaskType.Attack, "AICastingSpell", "b");
		add(EntityEvoker.class, TaskType.Attack, "AISummonSpell", "c");
		add(EntityEvoker.class, TaskType.Attack, "AIAttackSpell", "a");
		add(EntityEvoker.class, TaskType.Attack, "AIWololoSpell", "d");

		add(EntityEnderman.class, TaskType.Attack, "AIFindPlayer", "b");
		add(EntityGhast.class, TaskType.Attack, "AIFireballAttack", "c");
		add(EntityGuardian.class, TaskType.Attack, "AIGuardianAttack", "a");
		add(EntitySlime.class, TaskType.Attack, "AISlimeAttack", "a");
		add(EntitySpider.class, TaskType.Attack, "AISpiderAttack", "a");
		add(EntityBlaze.class, TaskType.Attack, "AIFireballAttack", "a");
		add(EntityVex.class, TaskType.Attack, "AIChargeAttack", "a");
		add(EntityVindicator.class, TaskType.Attack, "AIJohnnyAttack", "a");
		add(EntityWolf.class, TaskType.Flee, "AIAvoidEntity", "a");
	}

	private static boolean eval(@Nonnull final EntityLiving entity, @Nonnull TaskType desiredType) {

		
		for (final EntityAITaskEntry task : executingTasks.get(entity.tasks)) {
			if (find(task.action) == desiredType)
				return true;
		}

		for (final EntityAITaskEntry task : executingTasks.get(entity.targetTasks)) {
			if (find(task.action) == desiredType)
				return true;
		}

		return false;
	}

	public static void assess(@Nonnull final IEntityDataSettable data) {
		final EntityLiving entity = data.getEntity();
		final boolean isAttacking = eval(entity, TaskType.Attack);
		final boolean isFleeing = eval(entity, TaskType.Flee);
		data.setAttacking(isAttacking);
		data.setFleeing(isFleeing);
	}

}
