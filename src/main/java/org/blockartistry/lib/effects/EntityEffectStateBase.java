package org.blockartistry.lib.effects;

import java.lang.ref.WeakReference;
import java.util.Optional;

import net.minecraft.entity.Entity;

public class EntityEffectStateBase extends EffectStateBase implements IEntityEffectState {

	protected final WeakReference<Entity> subject;

	public EntityEffectStateBase(Entity entity) {
		super();
		
		this.subject = new WeakReference<Entity>(entity);
	}
	
	/**
	 * The Entity subject the EntityEffectHandler is associated with. May be null if
	 * the Entity is no longer in scope.
	 * 
	 * @return Optional with a reference to the subject Entity, if any.
	 */
	@Override
	public Optional<Entity> subject() {
		return Optional.ofNullable(this.subject.get());
	}

	/**
	 * Determines the distance between the Entity subject and the specified Entity.
	 * 
	 * @param entity
	 *            The Entity to which the distance is measured.
	 * @return The distance between the two Entities in blocks, squared.
	 */
	@Override
	public double distanceSq(final Entity player) {
		final Entity e = this.subject.get();
		if (e == null)
			return Double.MAX_VALUE;
		return e.getDistanceSqToEntity(player);
	}

	/**
	 * Returns the total world time, in ticks, the entity belongs to.
	 * 
	 * @return Total world time
	 */
	@Override
	public long getWorldTime() {
		final Entity e = this.subject.get();
		return e == null ? 0 : e.getEntityWorld().getTotalWorldTime();
	}

}
