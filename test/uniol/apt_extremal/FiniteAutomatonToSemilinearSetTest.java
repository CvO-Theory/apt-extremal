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

package uniol.apt_extremal;

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import uniol.apt.adt.automaton.FiniteAutomaton;
import uniol.apt.adt.automaton.Symbol;
import static uniol.apt.adt.automaton.FiniteAutomatonUtility.*;

import uniol.apt_extremal.util.LinearSet;
import uniol.apt_extremal.util.SemilinearSet;
import static uniol.apt_extremal.FiniteAutomatonToSemilinearSet.toSemilinearSet;
import static uniol.apt_extremal.SemilinearSetEquivalenceMatcher.equivalentTo;

public class FiniteAutomatonToSemilinearSetTest {
	@Test
	public void testEmptyLanguage() {
		FiniteAutomaton aut = getEmptyLanguage();
		assertThat(toSemilinearSet(aut), equalTo(SemilinearSet.EMPTY));
	}

	@Test
	public void testAtomicLanguageEpsilon() {
		FiniteAutomaton aut = getAtomicLanguage(Symbol.EPSILON);
		assertThat(toSemilinearSet(aut), equalTo(SemilinearSet.NULL));
	}

	@Test
	public void testA() {
		FiniteAutomaton aut = getAtomicLanguage(new Symbol("a"));
		assertThat(toSemilinearSet(aut), equalTo(SemilinearSet.containingEvent("a")));
	}

	@Test
	public void testAAA() {
		FiniteAutomaton aut = getAtomicLanguage(new Symbol("a"));
		aut = concatenate(aut, concatenate(aut, aut));
		assertThat(toSemilinearSet(aut), equalTo(SemilinearSet.containingEvent("a", 3)));
	}

	@Test
	public void testAPlus() {
		FiniteAutomaton aut = kleenePlus(getAtomicLanguage(new Symbol("a")));
		SemilinearSet set = SemilinearSet.containing(LinearSet.containingEvent("a").kleenePlus());
		assertThat(toSemilinearSet(aut), equivalentTo(set));
	}

	@Test
	public void testAStar() {
		FiniteAutomaton aut = kleeneStar(getAtomicLanguage(new Symbol("a")));
		SemilinearSet set = SemilinearSet.containingEvent("a").kleeneStar();
		assertThat(toSemilinearSet(aut), equivalentTo(set));
	}

	@Test
	public void testOptionalA() {
		FiniteAutomaton aut = optional(getAtomicLanguage(new Symbol("a")));
		SemilinearSet set = SemilinearSet.containingEvent("a").union(SemilinearSet.NULL);
		assertThat(toSemilinearSet(aut), equalTo(set));
	}

	@Test
	public void testComplicated() {
		// Construct (a|ab)*
		FiniteAutomaton a = getAtomicLanguage(new Symbol("a"));
		FiniteAutomaton b = getAtomicLanguage(new Symbol("b"));
		FiniteAutomaton aut = kleeneStar(union(a, concatenate(a, b)));

		SemilinearSet sa = SemilinearSet.containingEvent("a");
		SemilinearSet saa = SemilinearSet.containingEvent("a", 2);
		SemilinearSet sb = SemilinearSet.containingEvent("b");
		SemilinearSet set = sa.union(sa.concatenate(sb)).kleeneStar();
		assertThat(toSemilinearSet(aut), equivalentTo(set));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
