//
// From Choonster's TestMod3!
//

package org.orecruncher.lib.capability;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * A simple implementation of {@link ICapabilityProvider} and
 * {@link INBTSerializable} that supports a single {@link Capability} handler
 * instance.
 * <p>
 * Uses the {@link Capability}'s {@link IStorage} to serialise/deserialise NBT.
 *
 * @author Choonster
 */
public class CapabilityProviderSerializable<H> extends CapabilityProviderSimple<H>
		implements INBTSerializable<NBTBase> {

	/**
	 * Create a provider for the default handler instance.
	 *
	 * @param capability
	 *                       The Capability instance to provide the handler for
	 * @param facing
	 *                       The EnumFacing to provide the handler for
	 */
	public CapabilityProviderSerializable(final Capability<H> capability, @Nullable final EnumFacing facing) {
		this(capability, facing, capability.getDefaultInstance());
	}

	/**
	 * Create a provider for the specified handler instance.
	 *
	 * @param capability
	 *                       The Capability instance to provide the handler for
	 * @param facing
	 *                       The EnumFacing to provide the handler for
	 * @param instance
	 *                       The handler instance to provide
	 */
	public CapabilityProviderSerializable(final Capability<H> capability, @Nullable final EnumFacing facing,
			@Nullable final H instance) {
		super(capability, facing, instance);
	}

	@Nullable
	@Override
	public NBTBase serializeNBT() {
		return getCapability().writeNBT(getInstance(), getFacing());
	}

	@Override
	public void deserializeNBT(final NBTBase nbt) {
		getCapability().readNBT(getInstance(), getFacing(), nbt);
	}

}