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

package org.blockartistry.mod.DynSurround.entity;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.mod.DynSurround.ModLog;

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

	static {
		actions.put(EntityAIAttackMelee.class, ActionState.ATTACKING);
		actions.put(EntityAIAttackRanged.class, ActionState.ATTACKING);
		actions.put(EntityAIAttackRangedBow.class, ActionState.ATTACKING);
		actions.put(EntityAIAvoidEntity.class, ActionState.PANIC);
		actions.put(EntityAIBeg.class, ActionState.BEGGING);
		actions.put(EntityAIBreakDoor.class, ActionState.NONE);
		actions.put(EntityAICreeperSwell.class, ActionState.EXPLODE);
		actions.put(EntityAIDefendVillage.class, ActionState.NONE);
		actions.put(EntityAIDoorInteract.class, ActionState.NONE);
		actions.put(EntityAIEatGrass.class, ActionState.EATING);
		actions.put(EntityAIFindEntityNearest.class, ActionState.LOOKING);
		actions.put(EntityAIFindEntityNearestPlayer.class, ActionState.LOOKING);
		actions.put(EntityAIFleeSun.class, ActionState.PANIC);
		actions.put(EntityAIFollowGolem.class, ActionState.FOLLOWING);
		actions.put(EntityAIFollowOwner.class, ActionState.FOLLOWING);
		actions.put(EntityAIFollowParent.class, ActionState.FOLLOWING);
		actions.put(EntityAIHarvestFarmland.class, ActionState.FARMING);
		actions.put(EntityAIHurtByTarget.class, ActionState.NONE);
		actions.put(EntityAILeapAtTarget.class, ActionState.ATTACKING);
		actions.put(EntityAILookAtTradePlayer.class, ActionState.LOOKING);
		actions.put(EntityAILookAtVillager.class, ActionState.LOOKING);
		actions.put(EntityAILookIdle.class, ActionState.IDLE);
		actions.put(EntityAIMate.class, ActionState.MATING);
		actions.put(EntityAIMoveIndoors.class, ActionState.MOVING);
		actions.put(EntityAIMoveThroughVillage.class, ActionState.MOVING);
		actions.put(EntityAIMoveToBlock.class, ActionState.MOVING);
		actions.put(EntityAIMoveTowardsRestriction.class, ActionState.MOVING);
		actions.put(EntityAIMoveTowardsTarget.class, ActionState.MOVING);
		actions.put(EntityAINearestAttackableTarget.class, ActionState.NONE);
		actions.put(EntityAIOcelotAttack.class, ActionState.ATTACKING);
		actions.put(EntityAIOcelotSit.class, ActionState.IDLE);
		actions.put(EntityAIOpenDoor.class, ActionState.IDLE);
		actions.put(EntityAIOwnerHurtByTarget.class, ActionState.ATTACKING);
		actions.put(EntityAIPanic.class, ActionState.PANIC);
		actions.put(EntityAIPlay.class, ActionState.PLAYING);
		actions.put(EntityAIRestrictOpenDoor.class, ActionState.NONE);
		actions.put(EntityAIRestrictSun.class, ActionState.NONE);
		actions.put(EntityAIRunAroundLikeCrazy.class, ActionState.CRAZY);
		actions.put(EntityAISit.class, ActionState.IDLE);
		actions.put(EntityAISkeletonRiders.class, ActionState.NONE);
		actions.put(EntityAISwimming.class, ActionState.MOVING);
		actions.put(EntityAITarget.class, ActionState.NONE);
		actions.put(EntityAITargetNonTamed.class, ActionState.NONE);
		actions.put(EntityAITempt.class, ActionState.TEMPT);
		actions.put(EntityAITradePlayer.class, ActionState.TRADING);
		actions.put(EntityAIVillagerInteract.class, ActionState.NONE);
		actions.put(EntityAIVillagerMate.class, ActionState.MATING);
		actions.put(EntityAIWander.class, ActionState.MOVING);
		actions.put(EntityAIWatchClosest.class, ActionState.LOOKING);
		actions.put(EntityAIWatchClosest2.class, ActionState.LOOKING);
		actions.put(EntityAIZombieAttack.class, ActionState.NONE);

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
		emojiMap.put(new EmojiKey(ActionState.PANIC, null), EmojiType.PANIC);
		emojiMap.put(new EmojiKey(ActionState.LOOKING, null), EmojiType.WATCH);
		emojiMap.put(new EmojiKey(null, EmotionalState.HAPPY), EmojiType.HAPPY);
		emojiMap.put(new EmojiKey(null, EmotionalState.SAD), EmojiType.SAD);
		emojiMap.put(new EmojiKey(null, EmotionalState.SICK), EmojiType.SICK);
		emojiMap.put(new EmojiKey(ActionState.FARMING, null), EmojiType.FARM);
		emojiMap.put(new EmojiKey(null, EmotionalState.BUSY), EmojiType.WORK);
		emojiMap.put(new EmojiKey(ActionState.TRADING, null), EmojiType.TRADE);
		emojiMap.put(new EmojiKey(null, EmotionalState.ANGRY), EmojiType.ANGRY);
		emojiMap.put(new EmojiKey(ActionState.EATING, null), EmojiType.EAT);
		emojiMap.put(new EmojiKey(ActionState.WORKING, null), EmojiType.WORK);
	}

	private static void registerSpecial(@Nonnull final Class<? extends EntityLiving> clazz, @Nonnull final String className,
			@Nonnull final ActionState state) {

		final Class<? extends EntityAIBase> ai = findInternalClass(clazz, className);
		if(ai != null)
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
