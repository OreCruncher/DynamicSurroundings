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

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.lib.compat.EntityLivingUtil;

import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAttackRanged;
import net.minecraft.entity.ai.EntityAIAttackRangedBow;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIBeg;
import net.minecraft.entity.ai.EntityAIBreakDoor;
import net.minecraft.entity.ai.EntityAICreeperSwell;
import net.minecraft.entity.ai.EntityAIDefendVillage;
import net.minecraft.entity.ai.EntityAIDoorInteract;
import net.minecraft.entity.ai.EntityAIEatGrass;
import net.minecraft.entity.ai.EntityAIFindEntityNearest;
import net.minecraft.entity.ai.EntityAIFindEntityNearestPlayer;
import net.minecraft.entity.ai.EntityAIFleeSun;
import net.minecraft.entity.ai.EntityAIFollowGolem;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAIHarvestFarmland;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAILookAtTradePlayer;
import net.minecraft.entity.ai.EntityAILookAtVillager;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAIMoveThroughVillage;
import net.minecraft.entity.ai.EntityAIMoveToBlock;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIMoveTowardsTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIOcelotAttack;
import net.minecraft.entity.ai.EntityAIOcelotSit;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAIPlay;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAIRestrictSun;
import net.minecraft.entity.ai.EntityAIRunAroundLikeCrazy;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAISkeletonRiders;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.ai.EntityAITargetNonTamed;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAITradePlayer;
import net.minecraft.entity.ai.EntityAIVillagerInteract;
import net.minecraft.entity.ai.EntityAIVillagerMate;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.ai.EntityAIZombieAttack;
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

	private static int emojiIdx(@Nullable final ActionState action, @Nullable final EmotionalState emotion) {
		final int aIdx = action != null ? action.ordinal() + 1 : 0;
		final int eIdx = emotion != null ? emotion.ordinal() + 1 : 0;
		return (aIdx << 8) | eIdx;
	}

	private final static Map<Class<? extends EntityAIBase>, ActionState> actions = new IdentityHashMap<>();
	private final static TIntObjectHashMap<EmojiType> emojiMap = new TIntObjectHashMap<>();

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
		add(EntityAIHurtByTarget.class, ActionState.ANGRY);
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
		registerSpecial(EntityRabbit.class, "AIAvoidEntity", ActionState.CRAZY);
		registerSpecial(EntityRabbit.class, "AIRaidFarm", ActionState.EATING);

		// Mappings to figure out an applicable EmojiType to display
		emojiMap.put(emojiIdx(ActionState.ATTACKING, null), EmojiType.ATTACK);
		emojiMap.put(emojiIdx(ActionState.EXPLODE, null), EmojiType.ANGRY);
		emojiMap.put(emojiIdx(ActionState.PANIC, null), EmojiType.FLEE);
		emojiMap.put(emojiIdx(ActionState.CRAZY, null), EmojiType.FLEE);
		emojiMap.put(emojiIdx(ActionState.LOOKING, null), EmojiType.WATCH);
		emojiMap.put(emojiIdx(null, EmotionalState.HAPPY), EmojiType.HAPPY);
		emojiMap.put(emojiIdx(null, EmotionalState.SAD), EmojiType.SAD);
		emojiMap.put(emojiIdx(null, EmotionalState.SICK), EmojiType.SICK);
		emojiMap.put(emojiIdx(null, EmotionalState.HURT), EmojiType.HURT);
		emojiMap.put(emojiIdx(ActionState.FARMING, null), EmojiType.FARM);
		emojiMap.put(emojiIdx(null, EmotionalState.BUSY), EmojiType.WORK);
		emojiMap.put(emojiIdx(ActionState.TRADING, null), EmojiType.TRADE);
		emojiMap.put(emojiIdx(null, EmotionalState.ANGRY), EmojiType.ANGRY);
		emojiMap.put(emojiIdx(ActionState.EATING, null), EmojiType.EAT);
		emojiMap.put(emojiIdx(ActionState.WORKING, null), EmojiType.WORK);
	}

	private static void registerSpecial(@Nonnull final Class<? extends EntityLiving> clazz,
			@Nonnull final String className, @Nonnull final ActionState state) {

		final Class<? extends EntityAIBase> ai = findInternalClass(clazz, className);
		if (ai != null)
			actions.put(ai, state);
		else
			DSurround.log().warn("Unable to locate class '%s' inside [%s]", className, clazz.toGenericString());
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
		if (EntityLivingUtil.getAttackTarget(entity) != null
				&& ActionState.ATTACKING.getPriority() > result.getPriority())
			result = ActionState.ATTACKING;

		return result;
	}

	@Nonnull
	public static EmojiType getEmoji(@Nullable ActionState action, @Nullable EmotionalState emotion) {
		EmojiType type = emojiMap.get(emojiIdx(action, emotion));
		if (type == null)
			type = emojiMap.get(emojiIdx(action, null));
		if (type == null)
			type = emojiMap.get(emojiIdx(null, emotion));
		return type != null ? type : EmojiType.NONE;
	}

}
