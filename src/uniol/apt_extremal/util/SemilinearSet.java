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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import uniol.apt.util.PowerSet;

/**
 * Representation of a semi-linear set. A semi-linear set is a finite union of linear sets.
 * @author Uli Schlachter
 */
public class SemilinearSet implements Iterable<LinearSet> {
	private final Set<LinearSet> linearSets;

	/** The empty semi-linear set */
	static public final SemilinearSet EMPTY = new SemilinearSet();

	/** The semi-linear set containing just the null vector */
	static public final SemilinearSet NULL = containing(LinearSet.NULL);

	private SemilinearSet() {
		linearSets = Collections.emptySet();
	}

	private SemilinearSet(LinearSet linearSet) {
		linearSets = Collections.singleton(linearSet);
	}

	private SemilinearSet(Set<LinearSet> sets) {
		linearSets = sets;
	}

	/**
	 * Create a semi-linear set containing the given event once.
	 * @param event The event to contain.
	 * @return A semi-linear set containing the given event once and nothing else.
	 */
	static public SemilinearSet containingEvent(String event) {
		return containingEvent(event, 1);
	}

	/**
	 * Create a semi-linear set containing the given event a number of times.
	 * @param event The event to contain.
	 * @param count The number of times to contain the given element.
	 * @return A semi-linear set containing the given event and nothing else.
	 */
	static public SemilinearSet containingEvent(String event, int count) {
		return containing(LinearSet.containingEvent(event, count));
	}

	/**
	 * Create a semi-linear set containing just the given linear set.
	 * @param linearSet The linear set to contain
	 * @return A semi-linear set containing the given linear set and nothing else.
	 */
	static public SemilinearSet containing(LinearSet linearSet) {
		return new SemilinearSet(linearSet);
	}

	/**
	 * Create the union of two semi linear set
	 * @param other A semi-linear set to unify with
	 * @return A semi-linear set describing the union of the two semi-linear sets.
	 */
	public SemilinearSet union(SemilinearSet other) {
		Set<LinearSet> result = new HashSet<>();
		result.addAll(linearSets);
		result.addAll(other.linearSets);
		return new SemilinearSet(result);
	}

	/**
	 * Create the concatenation of two semi linear set
	 * @param other A semi-linear set to concatenate with
	 * @return A semi-linear set describing the concatenation of the two semi-linear sets.
	 */
	public SemilinearSet concatenate(SemilinearSet other) {
		Set<LinearSet> result = new HashSet<>();
		for (LinearSet first : linearSets)
			for (LinearSet second : other.linearSets)
				result.add(first.concatenate(second));
		return new SemilinearSet(result);
	}

	/**
	 * Create the Kleene star closure of this semi-linear set.
	 * @return A semi-linear set describing the result
	 */
	public SemilinearSet kleeneStar() {
		Set<LinearSet> result = new HashSet<>();
		for (Collection<LinearSet> subset : new PowerSet<>(linearSets)) {
			LinearSet set = LinearSet.NULL;
			for (LinearSet entry : subset)
				set = set.concatenate(entry.kleenePlus());
			result.add(set);
		}
		return new SemilinearSet(result);
	}

	@Override
	public Iterator<LinearSet> iterator() {
		return Collections.unmodifiableSet(linearSets).iterator();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SemilinearSet))
			return false;
		return linearSets.equals(((SemilinearSet) o).linearSets);
	}

	@Override
	public int hashCode() {
		return linearSets.hashCode();
	}

	@Override
	public String toString() {
		return linearSets.toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
