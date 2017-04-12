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

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import uniol.apt.adt.ts.ParikhVector;
import uniol.apt.util.equations.InequalitySystem;
import uniol.apt.util.equations.InequalitySystemSolver;

import uniol.apt_extremal.util.LinearSet;
import uniol.apt_extremal.util.SemilinearSet;

// This is a heuristics to check if two semilinear sets are equivalent
public class SemilinearSetEquivalenceMatcher extends TypeSafeDiagnosingMatcher<SemilinearSet> {
	static public Matcher<SemilinearSet> equivalentTo(SemilinearSet expected) {
		return new SemilinearSetEquivalenceMatcher(expected);
	}

	final private SemilinearSet expected;

	private SemilinearSetEquivalenceMatcher(SemilinearSet exp) {
		this.expected = exp;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("semi-linear set equivalent to ").appendValue(expected);
	}

	@Override
	public boolean matchesSafely(SemilinearSet set, Description description) {
		ParikhVector difference = isSubset(set, expected);
		if (difference == null)
			difference = isSubset(expected, expected);
		if (difference == null)
			return true;

		description.appendText("got: ").appendValue(set);
		description.appendText(" which is different e.g. in ").appendValue(difference);
		return false;
	}

	static private ParikhVector isSubset(SemilinearSet subset, SemilinearSet set) {
		for (LinearSet lset : subset) {
			ParikhVector difference = isSubset(lset, set);
			if (difference != null)
				return difference;
		}

		return null;
	}

	// Check if 'subset' is a subset of 'set'
	static private ParikhVector isSubset(LinearSet subset, SemilinearSet set) {
		// Cheap check to speed things up: If some linear set contained in 'set' contains 'subset', we are done
		for (LinearSet lset : set) {
			// A linear set surely contains another linear set if all periods of the smaller set are also
			// periods of the larger set and if the base vector of the smaller set can be represented by the
			// larger set. The word 'surely' in the previous sentence should indicate this is just a
			// heuristic: There are linear sets in a subset relationship where this check fails.
			if (lset.getRepeatedPart().containsAll(subset.getRepeatedPart()) &&
					contains(lset, subset.getConstantPart()))
				return null;
		}

		// Checking the subset relation between semilinear sets is complicated. Instead of doing this properly,
		// we just generate all entries up to some bound (see shouldCheck()) and check if they are contained in
		// the other set (see contains()). This should be good enough.

		Deque<ParikhVector> unhandled = new ArrayDeque<>();
		Set<ParikhVector> handled = new HashSet<>();
		unhandled.add(subset.getConstantPart());

		while (!unhandled.isEmpty()) {
			ParikhVector pv = unhandled.remove();
			if (!contains(set, pv))
				return pv;

			for (ParikhVector period : subset.getRepeatedPart()) {
				ParikhVector next = pv.add(period);
				if (shouldCheck(next) && handled.add(next))
					unhandled.add(next);
			}
		}

		return null;
	}

	// Since a linear set is infinitely large, we have to cut off our checks at some point.
	static private boolean shouldCheck(ParikhVector pv) {
		for (String label : pv.getLabels())
			if (pv.get(label) > 100)
				return false;
		return true;
	}

	static private boolean contains(SemilinearSet set, ParikhVector pv) {
		for (LinearSet lset : set)
			if (contains(lset, pv))
				return true;

		return false;
	}

	static private boolean contains(LinearSet set, ParikhVector pv) {
		// First some cheap checks
		ParikhVector constant = set.getConstantPart();
		if (constant.equals(pv))
			return true;
		if (constant.tryCompareTo(pv) >= 0)
			// Either incomparable (0) or greater (1), in both cases even larger vectors cannot be equal
			return false;
		if (set.getRepeatedPart().isEmpty())
			return false;

		// Assign indices to each period vector
		List<ParikhVector> periods = new ArrayList<>(set.getRepeatedPart());

		// Calculate the total alphabet of the Parikh vectors (yuk!)
		Set<String> alphabet = new HashSet<>();
		alphabet.addAll(pv.getLabels());
		alphabet.addAll(constant.getLabels());
		for (ParikhVector vector : periods)
			alphabet.addAll(vector.getLabels());

		// For each label require that (pv - c)(label) = (sum k_i*p_i)(label)
		InequalitySystem system = new InequalitySystem();
		for (String label : alphabet) {
			int[] array = new int[periods.size()];
			for (int i = 0; i < array.length; i++)
				array[i] = periods.get(i).get(label);
			system.addInequality(pv.get(label) - constant.get(label), "=", array);
		}

		// Is the system solvable?
		List<BigInteger> solution = new InequalitySystemSolver()
			.assertDisjunction(system)
			.findSolution();

		if (solution.isEmpty())
			return false;

		assert isSolution(solution, alphabet, pv, constant, periods);
		return true;
	}

	static private boolean isSolution(List<BigInteger> solution, Set<String> alphabet, ParikhVector pv,
			ParikhVector constant, List<ParikhVector> periods) {
		assert solution.size() == periods.size();

		for (String label : alphabet) {
			BigInteger sum = BigInteger.ZERO;
			for (int i = 0; i < periods.size(); i++)
				sum = sum.add(solution.get(i).multiply(BigInteger.valueOf(periods.get(i).get(label))));

			assert sum.equals(BigInteger.valueOf(pv.get(label) - constant.get(label)))
				: label + ": " + solution + " " + pv + " " + constant + " " + periods;
		}
		return true;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
