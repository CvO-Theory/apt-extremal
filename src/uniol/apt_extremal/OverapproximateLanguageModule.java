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

import uniol.apt.adt.automaton.FiniteAutomaton;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.module.AbstractModule;
import uniol.apt.module.AptModule;
import uniol.apt.module.Category;
import uniol.apt.module.Module;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

/**
 * Provide regular language overapproximation as a module.
 * @author Uli Schlachter
 */
@AptModule
public class OverapproximateLanguageModule extends AbstractModule implements Module {
	@Override
	public String getShortDescription() {
		return "Calculate the minimal Petri net overapproximation of a regular language";
	}

	@Override
	public String getLongDescription() {
		return getShortDescription() + ".\n\n"
			+ "Supported options is pure.";
	}

	@Override
	public String getName() {
		return "regular_overapproximate";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("options", String.class, "Comma separated list of options");
		inputSpec.addParameter("language", FiniteAutomaton.class, "The language that should be transformed");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("pn", PetriNet.class,
				ModuleOutputSpec.PROPERTY_FILE, ModuleOutputSpec.PROPERTY_RAW);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		boolean pure = parseOptions(input.getParameter("options", String.class));
		FiniteAutomaton language = input.getParameter("language", FiniteAutomaton.class);
		OverapproximateLanguage.Mode mode = pure ? OverapproximateLanguage.Mode.PURE
			: OverapproximateLanguage.Mode.IMPURE;
		PetriNet pn = OverapproximateLanguage.overapproximate(language, mode);
		output.setReturnValue("pn", PetriNet.class, pn);
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.PN};
	}

	static private boolean parseOptions(String options) throws ModuleException {
		// Explicitly allow empty string
		options = options.trim();
		if (options.isEmpty())
			return false;

		boolean pure = false;

		for (String opt : options.split(",")) {
			switch (opt.trim().toLowerCase()) {
				case "none":
					break;
				case "pure":
					pure = true;
					break;
				default:
					throw new ModuleException("Cannot parse '" + opt + "': Unknown option");
			}
		}
		return pure;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
