/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2017  Uli Schlachter
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package uniol.apt_extremal.util;

import java.math.BigInteger;
import java.util.Arrays;

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SuppressWarnings("unchecked")
public class PolyhedralConeTest {
	private final static BigInteger ZERO = BigInteger.ZERO;
	private final static BigInteger ONE = BigInteger.ONE;

	@Test
	public void testTrivialEquality() {
		// This used to provoke an IllegalArgumentException from polco
		PolyhedralCone cone = new PolyhedralCone(2);
		cone.addEquation(0, 0);
		assertThat(cone.findExtremalRays(), empty());
	}

	@Test
	public void testBigIntError()
	throws Throwable {
		// This test fails when the arithmetic is set to bigint
		PolyhedralCone cone = new PolyhedralCone(3);
		cone.addEquation(0, 2, -2);
		cone.addInequality(1, 0, 0);
		cone.addInequality(0, 1, 0);
		cone.addInequality(0, 0, 1);
		cone.addInequality(1, 0, -1);
		cone.addInequality(1, 1, -2);

		assertThat(cone.findExtremalRays(), containsInAnyOrder(
					Arrays.asList(ONE, ZERO, ZERO),
					Arrays.asList(ONE, ONE, ONE)));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
