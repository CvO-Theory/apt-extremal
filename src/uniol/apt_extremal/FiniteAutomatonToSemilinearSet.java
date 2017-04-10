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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uniol.apt.adt.automaton.DFAState;
import uniol.apt.adt.automaton.DeterministicFiniteAutomaton;
import uniol.apt.adt.automaton.FiniteAutomaton;
import uniol.apt.adt.automaton.Symbol;
import uniol.apt.util.Pair;

import uniol.apt_extremal.util.LinearSet;
import uniol.apt_extremal.util.SemilinearSet;

import static uniol.apt.adt.automaton.FiniteAutomatonUtility.minimize;
import static uniol.apt.adt.automaton.FiniteAutomatonUtility.statesIterable;

/**
 * Calculate a semi-linear set based on a finite automaton. Given a finite automaton, this class calculates the
 * semi-linear set containing all Parikh-vectors of words from the given language.
 * @author Uli Schlachter
 */
public class FiniteAutomatonToSemilinearSet {

	/**
	 * Calculate the semi-linear set containing all Parikh-vectors of words that the given automaton accepts.
	 * @param automaton The automaton describing the language to transform.
	 * @return A semi-linear set containing the Parikh-vectors of all words from the language.
	 */
	static public SemilinearSet toSemilinearSet(FiniteAutomaton automaton) {
		DeterministicFiniteAutomaton dfa = minimize(automaton);

		List<DFAState> states = new ArrayList<DFAState>();
		for (DFAState state : statesIterable(dfa))
			states.add(state);

		Map<Pair<DFAState, DFAState>, SemilinearSet> mapping = getInitialMapping(dfa);
		for (DFAState state : states)
			mapping = handleNextState(dfa, mapping, state);

		SemilinearSet result = SemilinearSet.EMPTY;
		for (DFAState state : states) {
			if (!state.isFinalState())
				continue;
			SemilinearSet set = getSemilinearSet(mapping, dfa.getInitialState(), state);
			assert(set != null);
			result = result.union(set);
		}

		return result;
	}

	static private Map<Pair<DFAState, DFAState>, SemilinearSet> getInitialMapping(DeterministicFiniteAutomaton dfa) {
		Map<Pair<DFAState, DFAState>, SemilinearSet> result = new HashMap<>();
		for (DFAState state : statesIterable(dfa)) {
			add(result, state, state, SemilinearSet.NULL);
			for (Symbol symbol : dfa.getAlphabet()) {
				add(result, state, state.getFollowingState(symbol), symbol);
			}
		}
		return result;
	}

	static private Map<Pair<DFAState, DFAState>, SemilinearSet> handleNextState(DeterministicFiniteAutomaton dfa,
			Map<Pair<DFAState, DFAState>, SemilinearSet> mapping, DFAState newState) {
		Map<Pair<DFAState, DFAState>, SemilinearSet> result = new HashMap<>(mapping);
		for (DFAState state1 : statesIterable(dfa)) {
			for (DFAState state2 : statesIterable(dfa)) {
				SemilinearSet state1ToNew = getSemilinearSet(mapping, state1, newState);
				SemilinearSet newToNew = getSemilinearSet(mapping, newState, newState);
				SemilinearSet newToState2 = getSemilinearSet(mapping, newState, state2);
				if (state1ToNew == null || newToState2 == null)
					continue;

				assert newToNew != null : "Each state must reach itself at least via epsilon";

				SemilinearSet newSet = state1ToNew.concatenate(newToNew.kleeneStar().concatenate(newToState2));
				add(result, state1, state2, newSet);
			}
		}
		return result;
	}

	static private void add(Map<Pair<DFAState, DFAState>, SemilinearSet> mapping, DFAState state1, DFAState state2, Symbol symbol) {
		if (symbol.isEpsilon()) {
			add(mapping, state1, state2, SemilinearSet.NULL);
			return;
		}

		String event = symbol.getEvent();
		assert !event.isEmpty();
		add(mapping, state1, state2, SemilinearSet.containingEvent(event));
	}

	static private void add(Map<Pair<DFAState, DFAState>, SemilinearSet> mapping, DFAState state1, DFAState state2, SemilinearSet newSet) {
		Pair<DFAState, DFAState> pair = new Pair<>(state1, state2);
		SemilinearSet set = mapping.get(pair);
		if (set == null) {
			set = SemilinearSet.EMPTY;
		}

		mapping.put(pair, set.union(newSet));
	}

	static private SemilinearSet getSemilinearSet(Map<Pair<DFAState, DFAState>, SemilinearSet> mapping, DFAState state1, DFAState state2) {
		return mapping.get(new Pair<>(state1, state2));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
