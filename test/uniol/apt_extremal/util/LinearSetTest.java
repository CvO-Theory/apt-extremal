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

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import uniol.apt.adt.ts.ParikhVector;

public class LinearSetTest {
	@Test
	public void testNullSet() {
		LinearSet set = LinearSet.NULL;
		assertThat(set.getConstantPart(), equalTo(new ParikhVector()));
		assertThat(set.getRepeatedPart(), empty());
	}

	@Test
	public void testContainingEvent() {
		LinearSet set1 = LinearSet.containingEvent("a");
		LinearSet set3 = LinearSet.containingEvent("a", 3);

		assertThat(set1.getConstantPart(), equalTo(new ParikhVector("a")));
		assertThat(set1.getRepeatedPart(), empty());

		assertThat(set3.getConstantPart(), equalTo(new ParikhVector("a", "a", "a")));
		assertThat(set3.getRepeatedPart(), empty());
	}

	@Test
	public void testKleenePlus() {
		LinearSet set = LinearSet.containingEvent("a", 2).kleenePlus();
		assertThat(set.getConstantPart(), equalTo(new ParikhVector("a", "a")));
		assertThat(set.getRepeatedPart(), contains(new ParikhVector("a", "a")));
	}

	@Test
	public void testConcatenate() {
		LinearSet set1 = LinearSet.containingEvent("a", 2).kleenePlus();
		LinearSet set2 = LinearSet.containingEvent("b");
		LinearSet result = set1.concatenate(set2);

		assertThat(result.getConstantPart(), equalTo(new ParikhVector("a", "a", "b")));
		assertThat(result.getRepeatedPart(), contains(new ParikhVector("a", "a")));
	}

	@Test
	public void testEquals() {
		LinearSet setA = LinearSet.containingEvent("a");
		LinearSet setAA = LinearSet.containingEvent("a", 2);
		LinearSet setAAPlus = LinearSet.containingEvent("a", 2).kleenePlus();
		LinearSet setAAPlus2 = LinearSet.NULL.concatenate(setAA.kleenePlus());

		assertThat(setAAPlus, equalTo(setAAPlus2));
		assertThat(setAAPlus.hashCode(), equalTo(setAAPlus2.hashCode()));
		assertThat(setAAPlus, not(equalTo(setA)));
		assertThat(setAAPlus, not(equalTo(setAA)));
		assertThat(setA, not(equalTo(LinearSet.containingEvent("b"))));
		assertThat(setA, not(equalTo(new Object())));
	}

	@Test
	public void testToString() {
		LinearSet setA = LinearSet.containingEvent("a");
		LinearSet setAA = LinearSet.containingEvent("a", 2);
		LinearSet setAAPlus = LinearSet.containingEvent("a", 2).kleenePlus();

		assertThat(setA, hasToString("({a=1}+[]*)"));
		assertThat(setAA, hasToString("({a=2}+[]*)"));
		assertThat(setAAPlus, hasToString("({a=2}+[{a=2}]*)"));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
