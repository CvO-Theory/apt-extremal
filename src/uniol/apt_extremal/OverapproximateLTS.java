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

	/**
	 * Calculate the minimal Petri net over-approximation containing all extremal regions of the given lts.
	 * @param ts The lts to over-approximate.
	 * @param pure Should only pure regions be considered?
	 * @return The minimal over-approximation containing all extremal regions.
	 */
	static public PetriNet overapproximatePN(TransitionSystem ts, boolean pure) {
		RegionUtility utility = new RegionUtility(ts);
		return SynthesizePN.synthesizePetriNet(utility, pure ? overapproximatePure(utility) : overapproximateImpure(utility));
	}

	/**
	 * Calculate the minimal Petri net over-approximation containing some of the extremal regions of the given lts.
	 * @param ts The lts to over-approximate.
	 * @param pure Should only pure regions be considered?
	 * @return The minimal over-approximation containing some extremal regions.
	 */
	static public PetriNet overapproximateAndSimplifyPN(TransitionSystem ts, boolean pure) {
		RegionUtility utility = new RegionUtility(ts);
		return SynthesizePN.synthesizePetriNet(utility, overapproximateAndSimplify(utility, pure));
	}

	/**
	 * Calculate the extremal regions of the given lts.
	 * @param ts The lts to over-approximate.
	 * @param pure Should only pure regions be considered?
	 * @return A set of extremal regions
	 */
	static public Set<Region> overapproximateAndSimplify(RegionUtility utility, boolean pure) {
		Set<Region> result = pure ? overapproximatePure(utility) : overapproximateImpure(utility);
		SynthesizePN.minimizeRegions(utility, result, false);
		return result;
	}

	/**
	 * Calculate the extremal regions of the given lts.
	 * @param ts The lts to over-approximate.
	 * @return A set of extremal regions
	 */
	static public Set<Region> overapproximateImpure(RegionUtility utility) {
		TransitionSystem ts = utility.getTransitionSystem();
		SpanningTree<TransitionSystem, Arc, State> tree = utility.getSpanningTree();
		int numberOfEvents = utility.getNumberOfEvents();

		// Calculate the polyhedral cone
		PolyhedralCone cone = new PolyhedralCone(1 + 2*numberOfEvents);
		requireNonNegativeVariables(cone, 1 + 2*numberOfEvents);

		// Result must be cycle-consistent: After a cycle we reach the same marking again
		for (Arc chord : tree.getChords()) {
			try {
				cone.addEquation(toListImpure(0, utility.getParikhVectorForEdge(chord)));
			} catch (UnreachableException e) {
				throw new AssertionError("A chord by definition belongs to reachable nodes, "
						+ "yet one of them was unreachable", e);
			}
		}

		// No existing arc may be disabled
		for (State state : ts.getNodes()) {
			List<BigInteger> inequality;
			try {
				inequality = toListImpure(1, utility.getReachingParikhVector(state));
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

		return calculateExtremalRegions(utility, cone, false);
	}

	/**
	 * Calculate the pure extremal regions of the given lts.
	 * @param ts The lts to over-approximate.
	 * @return A set of extremal regions
	 */
	static public Set<Region> overapproximatePure(RegionUtility utility) {
		TransitionSystem ts = utility.getTransitionSystem();
		SpanningTree<TransitionSystem, Arc, State> tree = utility.getSpanningTree();
		int numberOfEvents = utility.getNumberOfEvents();

		// Calculate the polyhedral cone
		PolyhedralCone cone = new PolyhedralCone(1 + numberOfEvents);

		// require initial marking to be non-negative
		int[] initialInequality = new int[1 + numberOfEvents];
		Arrays.fill(initialInequality, 0);
		initialInequality[0] = 1;
		cone.addInequality(initialInequality);

		// Result must be cycle-consistent: After a cycle we reach the same marking again
		for (Arc chord : tree.getChords()) {
			try {
				cone.addEquation(toListPure(0, utility.getParikhVectorForEdge(chord)));
			} catch (UnreachableException e) {
				throw new AssertionError("A chord by definition belongs to reachable nodes, "
						+ "yet one of them was unreachable", e);
			}
		}

		// No existing arc may be disabled
		for (State state : ts.getNodes()) {
			List<BigInteger> inequality;
			try {
				inequality = toListPure(1, utility.getReachingParikhVector(state));
			} catch (UnreachableException e) {
				// Just skip unreachable states
				continue;
			}
			for (String event : utility.getEventList()) {
				if (!SeparationUtility.isEventEnabled(state, event))
					continue;

				List<BigInteger> inequality2 = new ArrayList<>(inequality);
				int idx = 1 + utility.getEventIndex(event);
				inequality2.set(idx, inequality2.get(idx).add(BigInteger.ONE));

				cone.addInequality(inequality2);
			}
		}

		return calculateExtremalRegions(utility, cone, true);
	}

	static private Set<Region> calculateExtremalRegions(RegionUtility utility, PolyhedralCone cone, boolean pure) {
		Set<Region> result = new HashSet<>();
		int numberOfEvents = utility.getNumberOfEvents();
		for (List<BigInteger> ray : cone.findExtremalRays()) {
			if (!pure) {
				result.add(new Region.Builder(utility,
							ray.subList(numberOfEvents+1, 2*numberOfEvents+1),
							ray.subList(1, numberOfEvents+1))
						.withInitialMarking(ray.get(0)));
			} else {
				Region.Builder builder = new Region.Builder(utility);
				for (int index = 0; index < numberOfEvents; index++)
					builder.addWeightOn(index, ray.get(index + 1));
				result.add(builder.withInitialMarking(ray.get(0)));
			}
		}
		return result;
	}

	static private List<BigInteger> toListImpure(int initial, List<BigInteger> weights) {
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

	static private List<BigInteger> toListPure(int initial, List<BigInteger> weights) {
		List<BigInteger> result = new ArrayList<>(1 + weights.size());

		result.add(BigInteger.valueOf(initial));
		result.addAll(weights);
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
