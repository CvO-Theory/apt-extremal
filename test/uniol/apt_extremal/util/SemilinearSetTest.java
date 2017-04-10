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

public class SemilinearSetTest {
	@Test
	public void testEmptySet() {
		assertThat(SemilinearSet.EMPTY, emptyIterable());
	}

	@Test
	public void testNullSet() {
		assertThat(SemilinearSet.NULL, contains(LinearSet.NULL));
	}

	@Test
	public void testContainingEvent() {
		SemilinearSet set1 = SemilinearSet.containingEvent("a");
		SemilinearSet set3 = SemilinearSet.containingEvent("a", 3);

		assertThat(set1, contains(LinearSet.containingEvent("a")));
		assertThat(set3, contains(LinearSet.containingEvent("a", 3)));
	}

	@Test
	public void testContaining() {
		LinearSet linear = LinearSet.containingEvent("a", 3).kleenePlus();
		SemilinearSet set = SemilinearSet.containing(linear);
		assertThat(set, contains(linear));

		assertThat(SemilinearSet.containing(LinearSet.NULL), equalTo(SemilinearSet.NULL));
	}

	@Test
	public void testUnion() {
		LinearSet l1 = LinearSet.containingEvent("a", 2);
		LinearSet l2 = LinearSet.containingEvent("a", 1);
		SemilinearSet s1 = SemilinearSet.containingEvent("a", 2);
		SemilinearSet s2 = SemilinearSet.containingEvent("a", 1);
		SemilinearSet s3 = SemilinearSet.containing(
				LinearSet.containingEvent("a").concatenate(LinearSet.containingEvent("a")));
		SemilinearSet set = s1.union(s2).union(s3);

		assertThat(set, containsInAnyOrder(l1, l2));
	}

	@Test
	public void testKleeneStar1() {
		SemilinearSet set = SemilinearSet.containingEvent("a", 2).kleeneStar();
		assertThat(set, containsInAnyOrder(
					LinearSet.NULL,
					LinearSet.containingEvent("a", 2).kleenePlus()));
	}

	@Test
	public void testKleeneStar2() {
		LinearSet a2 = LinearSet.containingEvent("a", 2);
		LinearSet b3 = LinearSet.containingEvent("b", 3);
		SemilinearSet set = SemilinearSet.containing(a2)
			.union(SemilinearSet.containing(b3))
			.kleeneStar();
		assertThat(set, containsInAnyOrder(
					LinearSet.NULL,
					a2.kleenePlus(),
					b3.kleenePlus(),
					a2.concatenate(b3).kleenePlus()));
	}

	@Test
	public void testConcatenate1() {
		SemilinearSet set1 = SemilinearSet.containingEvent("a", 2);
		SemilinearSet set2 = SemilinearSet.containingEvent("b");
		SemilinearSet result = set1.concatenate(set2);

		assertThat(result, contains(LinearSet.containingEvent("a", 2).concatenate(LinearSet.containingEvent("b"))));
	}

	@Test
	public void testConcatenate2() {
		LinearSet a2 = LinearSet.containingEvent("a", 2);
		LinearSet b3 = LinearSet.containingEvent("b", 3);
		SemilinearSet set1 = SemilinearSet.containing(a2).union(SemilinearSet.containing(b3));
		SemilinearSet set2 = SemilinearSet.containing(b3);
		SemilinearSet result = set1.concatenate(set2);

		assertThat(result, containsInAnyOrder(
					a2.concatenate(b3),
					b3.concatenate(b3)));
	}

	@Test
	public void testEquals() {
		SemilinearSet setA = SemilinearSet.containingEvent("a");
		SemilinearSet setAA = SemilinearSet.containingEvent("a", 2);
		SemilinearSet setAAPlus = SemilinearSet.containingEvent("a", 2).kleeneStar();
		SemilinearSet setAAPlus2 = SemilinearSet.NULL.concatenate(setAA.kleeneStar());
		SemilinearSet setAorAA = setA.union(setAA);

		assertThat(setAorAA, equalTo(setAorAA));
		assertThat(setAAPlus, equalTo(setAAPlus2));
		assertThat(setAAPlus.hashCode(), equalTo(setAAPlus2.hashCode()));
		assertThat(setAAPlus, not(equalTo(setA)));
		assertThat(setAAPlus, not(equalTo(setAA)));
		assertThat(setAAPlus, not(equalTo(setAorAA)));
		assertThat(setA, not(equalTo(SemilinearSet.containingEvent("b"))));
		assertThat(setA, not(equalTo(new Object())));
	}

	@Test
	public void testToString() {
		SemilinearSet setA = SemilinearSet.containingEvent("a");
		SemilinearSet setAA = SemilinearSet.containingEvent("a", 2);
		SemilinearSet setAAStar = SemilinearSet.containingEvent("a", 2).kleeneStar();

		assertThat(setA, hasToString("[({a=1}+[]*)]"));
		assertThat(setAA, hasToString("[({a=2}+[]*)]"));
		assertThat(setAAStar, hasToString("[({}+[]*), ({a=2}+[{a=2}]*)]"));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
