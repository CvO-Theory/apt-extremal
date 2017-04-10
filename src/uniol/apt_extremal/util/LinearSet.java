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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import uniol.apt.adt.ts.ParikhVector;

/**
 * Representation of a linear set base * repeated^* of Parikh vectors.
 * @author Uli Schlachter
 */
public class LinearSet {
	private final ParikhVector base;
	private final Set<ParikhVector> repeatedParts;

	/** The linear set containing just the null vector */
	static public final LinearSet NULL = new LinearSet();

	/**
	 * Create a linear set containing the given event once.
	 * @param event The event to contain.
	 * @return A linear set containing the given event once and nothing else.
	 */
	static public LinearSet containingEvent(String event) {
		return containingEvent(event, 1);
	}

	/**
	 * Create a linear set containing the given event a number of times.
	 * @param event The event to contain.
	 * @param count The number of times to contain the given element.
	 * @return A linear set containing the given event and nothing else.
	 */
	static public LinearSet containingEvent(String event, int count) {
		return new LinearSet(event, count);
	}

	private LinearSet() {
		base = new ParikhVector();
		repeatedParts = Collections.emptySet();
	}

	private LinearSet(String event, int count) {
		base = new ParikhVector(Collections.nCopies(count, event));
		repeatedParts = Collections.emptySet();
	}

	private LinearSet(ParikhVector base, Set<ParikhVector> repeatedParts) {
		this.base = base;
		this.repeatedParts = new HashSet<>(repeatedParts);
		this.repeatedParts.remove(new ParikhVector());
	}

	/**
	 * Get the constant part of this linear set.
	 * @return the constant part.
	 */
	public ParikhVector getConstantPart() {
		return base;
	}

	/**
	 * Get the repeated part of this linear set.
	 * @return the repeated prat.
	 */
	public Set<ParikhVector> getRepeatedPart() {
		return Collections.unmodifiableSet(repeatedParts);
	}

	/**
	 * Concatenate two linear sets
	 * @param other The linear set to concatenate with
	 * @return A linear set describing the result
	 */
	public LinearSet concatenate(LinearSet other) {
		Set<ParikhVector> repeatedParts = new HashSet<>(this.repeatedParts);
		repeatedParts.addAll(other.repeatedParts);
		return new LinearSet(base.add(other.base), repeatedParts);
	}

	/**
	 * Create the Kleene plus closure of this linear set.
	 * @return A linear set describing the result
	 */
	public LinearSet kleenePlus() {
		Set<ParikhVector> repeatedParts = new HashSet<>(this.repeatedParts);
		repeatedParts.add(this.base);
		return new LinearSet(this.base, repeatedParts);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof LinearSet))
			return false;
		LinearSet other = (LinearSet) o;
		return base.equals(other.base) && repeatedParts.equals(other.repeatedParts);
	}

	@Override
	public int hashCode() {
		return base.hashCode() + repeatedParts.hashCode();
	}

	@Override
	public String toString() {
		return "(" + base + "+" + repeatedParts + "*)";
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
