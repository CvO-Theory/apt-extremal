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
import java.util.List;

import uniol.apt.adt.automaton.DeterministicFiniteAutomaton;
import uniol.apt.adt.automaton.FiniteAutomaton;
import uniol.apt.adt.automaton.Symbol;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.adt.ts.ParikhVector;

import uniol.apt_extremal.util.LinearSet;
import uniol.apt_extremal.util.PolyhedralCone;
import uniol.apt_extremal.util.SemilinearSet;

import static uniol.apt.adt.automaton.FiniteAutomatonUtility.*;
import static uniol.apt.util.DebugUtil.debug;
import static uniol.apt.util.DebugUtil.debugFormat;

/**
 * Overapproximate a regular language by a Petri net.
 * @author Uli Schlachter
 */
public class OverapproximateLanguage {

	public enum Mode {
		PURE {
			@Override
			protected PolyhedralCone createCone(List<Symbol> alphabet) {
				int numVariables = 1 + alphabet.size();
				PolyhedralCone cone = new PolyhedralCone(numVariables);
				int[] inequality = new int[numVariables];
				Arrays.fill(inequality, 0);
				inequality[0] = 1;
				cone.addInequality(inequality);

				return cone;
			}

			@Override
			protected int[] getVectorFromPV(List<Symbol> alphabet, ParikhVector pv) {
				int[] vector = new int[1 + alphabet.size()];
				vector[0] = 0;

				int index = 0;
				for (Symbol sym : alphabet) {
					int count = pv.get(sym.getEvent());
					vector[1 + index] = count;

					index++;
				}
				return vector;
			}

			@Override
			protected int[] getVectorEnablingWord(List<Symbol> alphabet, ParikhVector pv, Symbol toEnable) {
				int[] vector = getVectorFromPV(alphabet, pv);
				vector[0] = 1;
				return vector;
			}

			@Override
			protected void createPlace(List<Symbol> alphabet, List<BigInteger> vector, PetriNet pn) {
				Place place = pn.createPlace();
				place.setInitialToken(vector.get(0).intValue());

				int idx = 0;
				for (Symbol sym : alphabet) {
					Transition transition = pn.getTransition(sym.getEvent());
					int weight = vector.get(1 + idx).intValue();
					if (weight > 0)
						pn.createFlow(transition, place, weight);
					if (weight < 0)
						pn.createFlow(place, transition, -weight);

					idx++;
				}
			}
		},
		IMPURE {
			@Override
			protected PolyhedralCone createCone(List<Symbol> alphabet) {
				int numVariables = 1 + 2*alphabet.size();
				PolyhedralCone cone = new PolyhedralCone(numVariables);
				int[] inequality = new int[numVariables];
				Arrays.fill(inequality, 0);

				for (int i = 0; i < numVariables; i++) {
					inequality[i] = 1;
					cone.addInequality(inequality);
					inequality[i] = 0;
				}

				return cone;
			}

			@Override
			protected int[] getVectorFromPV(List<Symbol> alphabet, ParikhVector pv) {
				int[] vector = new int[1 + 2*alphabet.size()];
				vector[0] = 0;

				int index = 0;
				for (Symbol sym : alphabet) {
					int count = pv.get(sym.getEvent());
					vector[1 + index] = count;
					vector[1 + alphabet.size() + index] = -count;

					index++;
				}
				return vector;
			}

			@Override
			protected int[] getVectorEnablingWord(List<Symbol> alphabet, ParikhVector pv, Symbol toEnable) {
				int[] vector = getVectorFromPV(alphabet, pv);
				vector[0] = 1;
				vector[1 + alphabet.indexOf(toEnable)] -= 1;
				return vector;
			}

			@Override
			protected void createPlace(List<Symbol> alphabet, List<BigInteger> vector, PetriNet pn) {
				Place place = pn.createPlace();
				place.setInitialToken(vector.get(0).intValue());

				int idx = 0;
				for (Symbol sym : alphabet) {
					Transition transition = pn.getTransition(sym.getEvent());
					int forwardWeight = vector.get(1 + idx).intValue();
					int backwardWeight = vector.get(1 + alphabet.size() + idx).intValue();
					pn.createFlow(transition, place, forwardWeight);
					pn.createFlow(place, transition, backwardWeight);

					idx++;
				}
			}
		};

		abstract protected PolyhedralCone createCone(List<Symbol> alphabet);

		abstract protected int[] getVectorFromPV(List<Symbol> alphabet, ParikhVector pv);

		abstract protected int[] getVectorEnablingWord(List<Symbol> alphabet, ParikhVector pv, Symbol toEnable);

		abstract protected void createPlace(List<Symbol> alphabet, List<BigInteger> vector, PetriNet pn);
	}

	/**
	 * Calculate the minimal Petri net overapproximation of the regular language represented by the given finite
	 * automaton. The resulting Petri net is unique up to language equivalence.
	 * @param automaton The automaton to overapproximate.
	 * @param mode The mode to use for generating the Petri net.
	 * @param bounded Only consider bounded Petri nets
	 * @return An overapproximating Petri net.
	 */
	static public PetriNet overapproximate(FiniteAutomaton automaton, Mode mode, boolean bounded) {
		// Prepare the automatons
		DeterministicFiniteAutomaton dea = constructDFA(prefixClosure(automaton));
		List<Symbol> alphabet = new ArrayList<>(dea.getAlphabet());

		FiniteAutomaton sigmaStar = getEmptyLanguage();
		for (Symbol sym : alphabet) {
			sigmaStar = union(sigmaStar, getAtomicLanguage(sym));
		}
		sigmaStar = constructDFA(kleeneStar(sigmaStar));

		// Calculate the polyhedral cone
		PolyhedralCone cone = mode.createCone(alphabet);
		for (Symbol sym : alphabet) {
			addInequalitiesFor(cone, mode, alphabet, dea, sigmaStar, sym, bounded);
		}

		// Generate a Petri net
		debug("cone:");
		debug(cone);
		PetriNet pn = new PetriNet();
		for (Symbol sym : alphabet)
			pn.createTransition(sym.getEvent());

		debug("rays:");
		for (List<BigInteger> ray : cone.findExtremalRays()) {
			debug("  ", ray);
			mode.createPlace(alphabet, ray, pn);
		}

		return pn;
	}

	static private void addInequalitiesFor(PolyhedralCone cone, Mode mode, List<Symbol> alphabet,
			DeterministicFiniteAutomaton dea, FiniteAutomaton sigmaStar, Symbol sym, boolean bounded) {
		// Calculate an automaton for all words ending with the given symbol
		dea = intersection(dea, constructDFA(concatenate(sigmaStar, getAtomicLanguage(sym))));

		SemilinearSet set = FiniteAutomatonToSemilinearSet.toSemilinearSet(dea);
		debugFormat("Words ending with %s are semi-linear set %s", sym, set);
		for (LinearSet linear : set) {
			cone.addInequality(mode.getVectorEnablingWord(alphabet, linear.getConstantPart(), sym));

			for (ParikhVector pv : linear.getRepeatedPart()) {
				int[] vector = mode.getVectorFromPV(alphabet, pv);
				cone.addInequality(vector);
				if (bounded) {
					for (int i = 0; i < vector.length; i++)
						vector[i] = -vector[i];
					cone.addInequality(vector);
				}
			}
		}
	}
}


// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
