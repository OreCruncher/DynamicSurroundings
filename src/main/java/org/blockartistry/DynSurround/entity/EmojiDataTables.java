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

package org.blockartistry.DynSurround.entity;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.ModLog;
import org.blockartistry.DynSurround.api.entity.ActionState;
import org.blockartistry.DynSurround.api.entity.EmojiType;
import org.blockartistry.DynSurround.api.entity.EmotionalState;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntityPolarBear;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.passive.EntityRabbit;

public final class EmojiDataTables {

	private EmojiDataTables() {

	}

	private final static class EmojiKey {

		private final ActionState action;
		private final EmotionalState emotion;

		public EmojiKey(final ActionState action, final EmotionalState emotion) {
			this.action = action;
			this.emotion = emotion;
		}

		@Override
		public boolean equals(@Nonnull final Object obj) {
			if (obj == this)
				return true;

			if (!(obj instanceof EmojiKey))
				return false;

			final EmojiKey key = (EmojiKey) obj;
			return this.action == key.action && this.emotion == key.emotion;
		}

		@Override
		public int hashCode() {
			if (this.action == null)
				return this.emotion.hashCode();
			else if (this.emotion == null)
				return this.action.hashCode();
			return this.action.hashCode() ^ this.emotion.hashCode();
		}
	}

	private final static Map<Class<? extends EntityAIBase>, ActionState> actions = new IdentityHashMap<>();
	private final static Map<EmojiKey, EmojiType> emojiMap = new HashMap<EmojiKey, EmojiType>();

	public static void add(@Nonnull final Class<? extends EntityAIBase> clazz, @Nonnull final ActionState state) {
		if (!actions.containsKey(clazz))
			actions.put(clazz, state);
	}

	static {
		add(EntityAIAttackMelee.class, ActionState.ATTACKING);
		add(EntityAIAttackRanged.class, ActionState.ATTACKING);
		add(EntityAIAttackRangedBow.class, ActionState.ATTACKING);
		add(EntityAIAvoidEntity.class, ActionState.PANIC);
		add(EntityAIBeg.class, ActionState.BEGGING);
		add(EntityAIBreakDoor.class, ActionState.NONE);
		add(EntityAICreeperSwell.class, ActionState.EXPLODE);
		add(EntityAIDefendVillage.class, ActionState.NONE);
		add(EntityAIDoorInteract.class, ActionState.NONE);
		add(EntityAIEatGrass.class, ActionState.EATING);
		add(EntityAIFindEntityNearest.class, ActionState.LOOKING);
		add(EntityAIFindEntityNearestPlayer.class, ActionState.LOOKING);
		add(EntityAIFleeSun.class, ActionState.PANIC);
		add(EntityAIFollowGolem.class, ActionState.FOLLOWING);
		add(EntityAIFollowOwner.class, ActionState.FOLLOWING);
		add(EntityAIFollowParent.class, ActionState.FOLLOWING);
		add(EntityAIHarvestFarmland.class, ActionState.FARMING);
		add(EntityAIHurtByTarget.class, ActionState.NONE);
		add(EntityAILeapAtTarget.class, ActionState.ATTACKING);
		add(EntityAILookAtTradePlayer.class, ActionState.LOOKING);
		add(EntityAILookAtVillager.class, ActionState.LOOKING);
		add(EntityAILookIdle.class, ActionState.IDLE);
		add(EntityAIMate.class, ActionState.MATING);
		add(EntityAIMoveIndoors.class, ActionState.MOVING);
		add(EntityAIMoveThroughVillage.class, ActionState.MOVING);
		add(EntityAIMoveToBlock.class, ActionState.MOVING);
		add(EntityAIMoveTowardsRestriction.class, ActionState.MOVING);
		add(EntityAIMoveTowardsTarget.class, ActionState.MOVING);
		add(EntityAINearestAttackableTarget.class, ActionState.NONE);
		add(EntityAIOcelotAttack.class, ActionState.ATTACKING);
		add(EntityAIOcelotSit.class, ActionState.IDLE);
		add(EntityAIOpenDoor.class, ActionState.IDLE);
		add(EntityAIOwnerHurtByTarget.class, ActionState.ATTACKING);
		add(EntityAIPanic.class, ActionState.PANIC);
		add(EntityAIPlay.class, ActionState.PLAYING);
		add(EntityAIRestrictOpenDoor.class, ActionState.NONE);
		add(EntityAIRestrictSun.class, ActionState.NONE);
		add(EntityAIRunAroundLikeCrazy.class, ActionState.CRAZY);
		add(EntityAISit.class, ActionState.IDLE);
		add(EntityAISkeletonRiders.class, ActionState.NONE);
		add(EntityAISwimming.class, ActionState.MOVING);
		add(EntityAITarget.class, ActionState.NONE);
		add(EntityAITargetNonTamed.class, ActionState.NONE);
		add(EntityAITempt.class, ActionState.TEMPT);
		add(EntityAITradePlayer.class, ActionState.TRADING);
		add(EntityAIVillagerInteract.class, ActionState.NONE);
		add(EntityAIVillagerMate.class, ActionState.MATING);
		add(EntityAIWander.class, ActionState.MOVING);
		add(EntityAIWatchClosest.class, ActionState.LOOKING);
		add(EntityAIWatchClosest2.class, ActionState.LOOKING);
		add(EntityAIZombieAttack.class, ActionState.NONE);

		// Special embedded AI tasks
		registerSpecial(EntityEnderman.class, "AIFindPlayer", ActionState.ANGRY);
		registerSpecial(EntityGhast.class, "AIFireballAttack", ActionState.ATTACKING);
		registerSpecial(EntityGuardian.class, "AIGuardianAttack", ActionState.ATTACKING);
		registerSpecial(EntityPolarBear.class, "AIMeleeAttack", ActionState.ATTACKING);
		registerSpecial(EntityPolarBear.class, "AIPanic", ActionState.PANIC);
		registerSpecial(EntityPolarBear.class, "AIAttackPlayer", ActionState.ATTACKING);
		registerSpecial(EntityShulker.class, "AIAttackNearest", ActionState.ATTACKING);
		registerSpecial(EntitySlime.class, "AISlimeAttack", ActionState.ATTACKING);
		registerSpecial(EntitySlime.class, "AISlimeHop", ActionState.MOVING);
		registerSpecial(EntitySpider.class, "AISpiderAttack", ActionState.ATTACKING);
		registerSpecial(EntityRabbit.class, "AIAvoidEntity", ActionState.PANIC);
		registerSpecial(EntityRabbit.class, "AIRaidFarm", ActionState.EATING);

		// Mappings to figure out an applicable EmojiType to display
		emojiMap.put(new EmojiKey(ActionState.ATTACKING, null), EmojiType.ATTACK);
		emojiMap.put(new EmojiKey(ActionState.EXPLODE, null), EmojiType.ANGRY);
		emojiMap.put(new EmojiKey(ActionState.PANIC, null), EmojiType.FLEE);
		emojiMap.put(new EmojiKey(ActionState.LOOKING, null), EmojiType.WATCH);
		emojiMap.put(new EmojiKey(null, EmotionalState.HAPPY), EmojiType.HAPPY);
		emojiMap.put(new EmojiKey(null, EmotionalState.SAD), EmojiType.SAD);
		emojiMap.put(new EmojiKey(null, EmotionalState.SICK), EmojiType.SICK);
		emojiMap.put(new EmojiKey(null, EmotionalState.HURT), EmojiType.HURT);
		emojiMap.put(new EmojiKey(ActionState.FARMING, null), EmojiType.FARM);
		emojiMap.put(new EmojiKey(null, EmotionalState.BUSY), EmojiType.WORK);
		emojiMap.put(new EmojiKey(ActionState.TRADING, null), EmojiType.TRADE);
		emojiMap.put(new EmojiKey(null, EmotionalState.ANGRY), EmojiType.ANGRY);
		emojiMap.put(new EmojiKey(ActionState.EATING, null), EmojiType.EAT);
		emojiMap.put(new EmojiKey(ActionState.WORKING, null), EmojiType.WORK);
	}

	private static void registerSpecial(@Nonnull final Class<? extends EntityLiving> clazz,
			@Nonnull final String className, @Nonnull final ActionState state) {

		final Class<? extends EntityAIBase> ai = findInternalClass(clazz, className);
		if (ai != null)
			actions.put(ai, state);
		else
			ModLog.warn("Unable to locate class '%s' inside [%s]", className, clazz.toGenericString());
	}

	@SuppressWarnings({ "unchecked" })
	private static Class<? extends EntityAIBase> findInternalClass(@Nonnull Class<? extends EntityLiving> clazz,
			@Nonnull final String className) {

		Class<?>[] classes = clazz.getDeclaredClasses();
		for (final Class<?> c : classes) {
			if (c.getName().endsWith(className) && EntityAIBase.class.isAssignableFrom(c))
				return (Class<? extends EntityAIBase>) c;
		}

		return null;

	}

	@Nonnull
	private static ActionState eval(@Nonnull final Set<EntityAITaskEntry> entries) {
		ActionState state = ActionState.NONE;

		for (final EntityAITaskEntry task : entries) {
			final ActionState candidate = actions.get(task.action.getClass());
			if (candidate != null) {
				if (state.getPriority() < candidate.getPriority())
					state = candidate;
			}
		}
		return state;
	}

	@Nonnull
	public static ActionState assess(@Nonnull final EntityLiving entity) {

		final ActionState first = eval(entity.tasks.executingTaskEntries);
		final ActionState second = eval(entity.targetTasks.executingTaskEntries);

		ActionState result = first.getPriority() > second.getPriority() ? first : second;
		if (entity.attackTarget != null && ActionState.ATTACKING.getPriority() > result.getPriority())
			result = ActionState.ATTACKING;

		return result;
	}

	@Nonnull
	public static EmojiType getEmoji(@Nullable ActionState action, @Nullable EmotionalState emotion) {
		EmojiType type = emojiMap.get(new EmojiKey(action, emotion));
		if (type == null)
			type = emojiMap.get(new EmojiKey(action, null));
		if (type == null)
			type = emojiMap.get(new EmojiKey(null, emotion));
		return type != null ? type : EmojiType.NONE;
	}

}
