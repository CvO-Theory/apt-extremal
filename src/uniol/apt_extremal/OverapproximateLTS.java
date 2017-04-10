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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.synthesize.Region;
import uniol.apt.util.SpanningTree;
import uniol.apt.analysis.synthesize.RegionUtility;
import uniol.apt.analysis.synthesize.SynthesizePN;
import uniol.apt.analysis.synthesize.UnreachableException;
import uniol.apt.analysis.synthesize.separation.SeparationUtility;

import uniol.apt_extremal.util.PolyhedralCone;

/**
 * Overapproximate a transition system by a Petri net. See "Petri Net Synthesis" by Badouel, Bernardinello, Darondeau
 * for details (sections 7.2 and 7.3).
 * @author Uli Schlachter
 */
public class OverapproximateLTS {

	static public PetriNet overapproximateAndSimplifyPN(TransitionSystem ts) {
		RegionUtility utility = new RegionUtility(ts);
		return SynthesizePN.synthesizePetriNet(utility, overapproximateAndSimplify(utility));
	}

	/**
	 * Calculate the extremal regions of the given lts.
	 * @param ts The lts to over-approximate.
	 * @return A set of extremal regions
	 */
	static public Set<Region> overapproximateAndSimplify(RegionUtility utility) {
		Set<Region> result = overapproximate(utility);
		SynthesizePN.minimizeRegions(utility, result, false);
		return result;
	}

	/**
	 * Calculate the extremal regions of the given lts.
	 * @param ts The lts to over-approximate.
	 * @return A set of extremal regions
	 */
	static public Set<Region> overapproximate(RegionUtility utility) {
		TransitionSystem ts = utility.getTransitionSystem();
		SpanningTree<TransitionSystem, Arc, State> tree = utility.getSpanningTree();
		int numberOfEvents = utility.getNumberOfEvents();

		// Calculate the polyhedral cone
		PolyhedralCone cone = new PolyhedralCone(1 + 2*numberOfEvents);
		requireNonNegativeVariables(cone, 1 + 2*numberOfEvents);

		// Result must be cycle-consistent: After a cycle we reach the same marking again
		for (Arc chord : tree.getChords()) {
			try {
				cone.addEquation(toList(0, utility.getParikhVectorForEdge(chord)));
			} catch (UnreachableException e) {
				throw new AssertionError("A chord by definition belongs to reachable nodes, "
						+ "yet one of them was unreachable", e);
			}
		}

		// No existing arc may be disabled
		for (State state : ts.getNodes()) {
			List<BigInteger> inequality;
			try {
				inequality = toList(1, utility.getReachingParikhVector(state));
			} catch (UnreachableException e) {
				// Just skip unreachable states
				continue;
			}
			for (String event : utility.getEventList()) {
				if (!SeparationUtility.isEventEnabled(state, event))
					continue;

				List<BigInteger> inequality2 = new ArrayList<>(inequality);
				int idx = 1 + numberOfEvents + utility.getEventIndex(event);
				inequality2.set(idx, inequality2.get(idx).subtract(BigInteger.ONE));

				cone.addInequality(inequality2);
			}
		}

		return calculateExtremalRegions(utility, cone);
	}

	static private Set<Region> calculateExtremalRegions(RegionUtility utility, PolyhedralCone cone) {
		Set<Region> result = new HashSet<>();
		int numberOfEvents = utility.getNumberOfEvents();
		for (List<BigInteger> ray : cone.findExtremalRays()) {
			result.add(new Region.Builder(utility,
						ray.subList(numberOfEvents+1, 2*numberOfEvents+1),
						ray.subList(1, numberOfEvents+1))
					.withInitialMarking(ray.get(0)));
		}
		return result;
	}

	static private List<BigInteger> toList(int initial, List<BigInteger> weights) {
		List<BigInteger> result = new ArrayList<>(1 + 2*weights.size());
		List<BigInteger> part2 = new ArrayList<>(weights.size());

		result.add(BigInteger.valueOf(initial));
		for (BigInteger i : weights) {
			result.add(i);
			part2.add(i.negate());
		}
		result.addAll(part2);
		return result;
	}

	static private void requireNonNegativeVariables(PolyhedralCone cone, int numVariables) {
		int[] inequality = new int[numVariables];
		Arrays.fill(inequality, 0);

		for (int i = 0; i < numVariables; i++) {
			inequality[i] = 1;
			cone.addInequality(inequality);
			inequality[i] = 0;
		}
	}
}


// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
