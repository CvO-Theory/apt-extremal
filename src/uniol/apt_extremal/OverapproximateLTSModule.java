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

import uniol.apt.util.Pair;
import uniol.apt.adt.ts.TransitionSystem;
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
 * Provide lts over-approximation as a module.
 * @author Uli Schlachter
 */
@AptModule
public class OverapproximateLTSModule extends AbstractModule implements Module {
	@Override
	public String getShortDescription() {
		return "Calculate the minimal Petri net overapproximation of an lts";
	}

	@Override
	public String getLongDescription() {
		return getShortDescription() + ".\n\n"
			+ "Supported options are pure and optimise.";
	}

	@Override
	public String getName() {
		return "lts_overapproximate";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("options", String.class, "Comma separated list of options");
		inputSpec.addParameter("lts", TransitionSystem.class, "The lts that should be over-approximated");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("pn", PetriNet.class,
				ModuleOutputSpec.PROPERTY_FILE, ModuleOutputSpec.PROPERTY_RAW);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		Pair<Boolean, Boolean> options = parseOptions(input.getParameter("options", String.class));
		TransitionSystem lts = input.getParameter("lts", TransitionSystem.class);
		PetriNet pn;
		boolean pure = options.getFirst();
		if (options.getSecond())
			pn = OverapproximateLTS.overapproximateAndSimplifyPN(lts, pure);
		else
			pn = OverapproximateLTS.overapproximatePN(lts, pure);
		output.setReturnValue("pn", PetriNet.class, pn);
	}

	static private Pair<Boolean, Boolean> parseOptions(String options) throws ModuleException {
		// Explicitly allow empty string
		options = options.trim();
		if (options.isEmpty())
			return new Pair<Boolean, Boolean>(false, false);

		boolean pure = false;
		boolean optimise = false;

		for (String opt : options.split(",")) {
			switch (opt.trim().toLowerCase()) {
				case "none":
					break;
				case "pure":
					pure = true;
					break;
				case "optimise":
				case "optimize":
					optimise = true;
					break;
				default:
					throw new ModuleException("Cannot parse '" + opt + "': Unknown option");
			}
		}
		return new Pair<Boolean, Boolean>(pure, optimise);
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.PN};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
