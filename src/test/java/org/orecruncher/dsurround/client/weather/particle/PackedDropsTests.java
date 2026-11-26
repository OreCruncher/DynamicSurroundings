/* This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
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

package org.orecruncher.dsurround.client.weather.particle;

import org.orecruncher.dsurround.client.weather.wind.Wind;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.orecruncher.lib.random.XorShiftRandom;

/**
 * Tests for the drops packed for building their quads ({@link PackedDrops}): each record holds what the field's arrays
 * say of its drop, rain and snow ahead of the dust, and the packing follows the drops as they change. Ported from the
 * 26.x line's tests.
 */
public class PackedDropsTests {

	@Test
	public void lightAndColorsArePackedAsWholeNumbers() {
		// As the int's bits, sky light below 8 or a color with little red was a denormal float, which a GPU may read
		// as 0: as whole numbers, nothing but 0 itself is under the smallest normal float
		for (final int value : new int[] { 0xF0, 0x000010, 0x102030, 0xF000F0, 0xFFFFFF }) {
			final float packed = PackedDrops.whole(value);
			assertEquals(value, (int) packed);
			assertTrue(packed >= Float.MIN_NORMAL, Integer.toHexString(value));
		}
		assertEquals(0x123456, (int) PackedDrops.whole(0xFF123456), "only the color, not its alpha");
	}

	private static final double CAMERA_Y = 70.5;

	// Rain to the west, snow in the middle, dust to the east
	private static final class Columns implements IColumnSource {
		@Override
		public int groundHeight(final int x, final int z) {
			return 64;
		}

		@Override
		public PrecipitationKind precipitation(final int x, final int y, final int z) {
			return x < -4 ? PrecipitationKind.RAIN : x < 4 ? PrecipitationKind.SNOW : PrecipitationKind.DUST;
		}

		@Override
		public int light(final int x, final int y, final int z) {
			return 0xF000F0;
		}

		@Override
		public int dustColor(final int x, final int y, final int z) {
			return x >= 4 ? 0xC08040 : NOT_A_DUST_SOURCE;
		}

		@Override
		public int fallbackDustColor(final int x, final int y, final int z) {
			return 0xC08040;
		}
	}

	private static PrecipitationField field(final double cameraX, final double cameraZ) {
		final PrecipitationField field = new PrecipitationField(4000);
		final Random random = new XorShiftRandom(17);
		for (int t = 0; t < 40; t++)
			field.tick(new Columns(), cameraX, CAMERA_Y, cameraZ, 1F, 1F, new Wind(), random);
		return field;
	}

	private static void checkRecords(final PrecipitationField field, final double cameraX, final double cameraZ) {
		final PrecipitationField.Drops d = field.drops();
		final PackedDrops packed = field.packed(cameraX + 0.3, CAMERA_Y, cameraZ - 0.7);
		assertEquals(d.count - d.dustCount, packed.solid);
		assertEquals(d.dustCount, packed.dust);

		// The drops' order, rain and snow first, then the dust
		final List<Integer> order = new ArrayList<>();
		for (int i = 0; i < d.count; i++)
			if (d.kind[i] != PrecipitationField.DUST)
				order.add(i);
		for (int i = 0; i < d.count; i++)
			if (d.kind[i] == PrecipitationField.DUST)
				order.add(i);

		final float partialTick = 0.25F;
		final float[] r = packed.data;
		for (int n = 0; n < order.size(); n++) {
			final int i = order.get(n), at = n * PackedDrops.STRIDE;
			final byte kind = d.kind[i];
			assertEquals(kind, (byte) r[at + PackedDrops.KIND]);
			final double x = packed.anchorX + r[at + PackedDrops.PREV_X] + partialTick * r[at + PackedDrops.MOVE_X];
			final double y = packed.anchorY + r[at + PackedDrops.PREV_Y] + partialTick * r[at + PackedDrops.MOVE_Y];
			final double z = packed.anchorZ + r[at + PackedDrops.PREV_Z] + partialTick * r[at + PackedDrops.MOVE_Z];
			assertEquals(d.prevX[i] + partialTick * (d.x[i] - d.prevX[i]), x, 1e-4);
			assertEquals(d.prevY[i] + partialTick * (d.y[i] - d.prevY[i]), y, 1e-4);
			assertEquals(d.prevZ[i] + partialTick * (d.z[i] - d.prevZ[i]), z, 1e-4);
			assertEquals(d.light[i], (int) r[at + PackedDrops.LIGHT]);
			assertEquals(d.thinning[i], r[at + PackedDrops.THINNING]);
			assertEquals(PrecipitationField.fade(kind, d.age[i], d.lifetime[i], d.covered[i], partialTick),
					PrecipitationField.fade(kind, r[at + PackedDrops.AGE], r[at + PackedDrops.LIFETIME], r[at + PackedDrops.COVERED], partialTick));
			assertEquals(PrecipitationField.pick(d, i), r[at + PackedDrops.PICK]);
			switch (kind) {
			case PrecipitationField.RAIN: {
				assertEquals(PrecipitationField.rainSize(d, i), r[at + PackedDrops.SIZE]);
				final float a = r[at + PackedDrops.A], b = r[at + PackedDrops.B], c = r[at + PackedDrops.C];
				assertEquals(1F, a * a + b * b + c * c, 1e-3F, "the way it falls, a unit vector");
				assertTrue(b < 0F, "falling");
				break;
			}
			case PrecipitationField.SNOW:
				assertEquals(PrecipitationField.snowSize(d, i), r[at + PackedDrops.SIZE]);
				assertEquals(d.spinCos[i], r[at + PackedDrops.A]);
				assertEquals(d.spinSin[i], r[at + PackedDrops.B]);
				break;
			default:
				assertEquals(PrecipitationField.dustSize(d, i), r[at + PackedDrops.SIZE]);
				assertEquals(d.color[i], (int) r[at + PackedDrops.C]);
				break;
			}
		}
	}

	@Test
	public void eachRecordHoldsItsDrop() {
		final PrecipitationField field = field(0.5, 0.5);
		final PrecipitationField.Drops d = field.drops();
		assertTrue(d.dustCount > 0 && d.dustCount < d.count, "rain or snow, and dust");
		checkRecords(field, 0.5, 0.5);
	}

	@Test
	public void farFromTheWorldsMiddleTheyAreAsExact() {
		final double far = 1_500_000.5;
		checkRecords(field(far, -far), far, -far);
	}

	@Test
	public void theyArePackedAgainOnceTheDropsMove() {
		final PrecipitationField field = field(0.5, 0.5);
		final float before = field.packed(0.5, CAMERA_Y, 0.5).data[PackedDrops.PREV_Y];
		field.tick(new Columns(), 0.5, CAMERA_Y, 0.5, 1F, 1F, new Wind(), new XorShiftRandom(3));
		checkRecords(field, 0.5, 0.5);
		assertNotEquals(before, field.packed(0.5, CAMERA_Y, 0.5).data[PackedDrops.PREV_Y]);

		field.clear();
		final PackedDrops packed = field.packed(0.5, CAMERA_Y, 0.5);
		assertEquals(0, packed.solid + packed.dust, "none after clearing");
	}
}
