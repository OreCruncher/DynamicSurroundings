
/*
 * Leveraged from CreativeMD's AmbientSounds
 * https://github.com/CreativeMD/AmbientSounds/blob/1.12/src/main/java/com/creativemd/ambientsounds/soundfix/SoundFixMethods.java
 */

package org.orecruncher.dsurround.client.sound.fix;

import java.lang.reflect.Field;

import net.minecraftforge.fml.relauncher.ReflectionHelper;
import paulscode.sound.Source;

public class SoundFixMethods {

	public static final Field removed = ReflectionHelper.findField(Source.class, "removed");

	private SoundFixMethods() {

	}

	public static Source removeSource(final Source source) {
		try {
			if (removed.getBoolean(source)) {
				source.cleanup();
				return null;
			}
		} catch (final IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return source;
	}

	public static void cleanupSource(final Source source) {
		if (source.toStream) {
			try {
				removed.setBoolean(source, true);
			} catch (final IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			source.cleanup();
		}
	}

}