/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015-2017  Uli Schlachter
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import ch.javasoft.metabolic.efm.config.Arithmetic;
import ch.javasoft.polco.adapter.Options;
import ch.javasoft.polco.adapter.PolcoAdapter;

/**
 * Representation of a polyhedral cone C(A, B) = { x \in Q^n where Ax >= 0 and Bx = 0}.
 * @author Uli Schlachter
 */
public class PolyhedralCone {
	private final int numVariables;
	private final Collection<List<BigInteger>> equations = new LinkedHashSet<>();
	private final Collection<List<BigInteger>> inequalities = new LinkedHashSet<>();

	/**
	 * Construct a new equation system.
	 * @param numVariables The number of variables in the equation system.
	 */
	public PolyhedralCone(int numVariables) {
		assert numVariables >= 0;

		this.numVariables = numVariables;
	}

	/**
	 * Add an equation Bx = 0 to the cone.
	 * @param coefficients List of coefficients for the equation. This must have exactly one entry for each
	 * variable.
	 */
	public void addEquation(Collection<BigInteger> coefficients) {
		assert coefficients.size() == numVariables;
		equations.add(new ArrayList<>(coefficients));
	}

	/**
	 * Add an equation Bx = 0 to the cone.
	 * @param coefficients List of coefficients for the equation. This must have exactly one entry for each
	 * variable.
	 */
	public void addEquation(BigInteger... coefficients) {
		addEquation(toBigIntegerList(coefficients));
	}

	/**
	 * Add an equation Bx = 0 to the cone.
	 * @param coefficients List of coefficients for the equation. This must have exactly one entry for each
	 * variable.
	 */
	public void addEquation(int... coefficients) {
		addEquation(toBigIntegerList(coefficients));
	}

	/**
	 * Add an inequality Ax >= 0 to the cone.
	 * @param coefficients List of coefficients for the inequality. This must have exactly one entry for each
	 * variable.
	 */
	public void addInequality(Collection<BigInteger> coefficients) {
		assert coefficients.size() == numVariables;
		inequalities.add(new ArrayList<>(coefficients));
	}

	/**
	 * Add an inequality Ax >= 0 to the cone.
	 * @param coefficients List of coefficients for the inequality. This must have exactly one entry for each
	 * variable.
	 */
	public void addInequality(BigInteger... coefficients) {
		addInequality(toBigIntegerList(coefficients));
	}

	/**
	 * Add an inequality Ax >= 0 to the cone.
	 * @param coefficients List of coefficients for the inequality. This must have exactly one entry for each
	 * variable.
	 */
	public void addInequality(int... coefficients) {
		addInequality(toBigIntegerList(coefficients));
	}

	static private Collection<BigInteger> toBigIntegerList(int... entries) {
		ArrayList<BigInteger> result = new ArrayList<>(entries.length);
		for (int i = 0; i < entries.length; i++)
			result.add(BigInteger.valueOf(entries[i]));
		return result;
	}

	static private Collection<BigInteger> toBigIntegerList(BigInteger... entries) {
		ArrayList<BigInteger> result = new ArrayList<>(entries.length);
		for (int i = 0; i < entries.length; i++)
			result.add(entries[i]);
		return result;
	}

	/**
	 * Calculate the extremal rays of this cone.
	 * @return The set of extremal rays.
	 */
	public Set<List<BigInteger>> findExtremalRays() {
		BigInteger[][] eq = toBigIntegerArray(equations);
		BigInteger[][] iq = toBigIntegerArray(inequalities);

		// http://www.csb.ethz.ch/tools/polco
		Options options = new Options();
		//options.setArithmetic(Arithmetic.bigint, Arithmetic.bigint, Arithmetic.bigint);
		options.setLoglevel(Level.OFF);
		PolcoAdapter adapter;
		try {
			adapter = new PolcoAdapter(options);
		} catch (Exception e) {
			// TODO: Better error handling
			throw new RuntimeException(e);
		}

		BigInteger[][] rays = adapter.getBigIntegerRays(eq, iq);
		Set<List<BigInteger>> result = new HashSet<>();
		for (int i = 0; i < rays.length; i++)
			result.add(Arrays.asList(rays[i]));
		return result;
	}

	static private BigInteger[][] toBigIntegerArray(Collection<List<BigInteger>> rows) {
		BigInteger[][] result = new BigInteger[rows.size()][];
		int index = 0;
		for (List<BigInteger> row : rows)
			result[index++] = row.toArray(new BigInteger[0]);
		return result;
	}

	static private void toStringHelper(StringBuilder buffer, Collection<List<BigInteger>> rows, String operation) {
		for (List<BigInteger> row : rows) {
			boolean first = true;
			for (int j = 0; j < row.size(); j++) {
				if (row.get(j).equals(BigInteger.ZERO))
					continue;

				if (!first)
					buffer.append(" + ");

				buffer.append(row.get(j)).append("*x[").append(j).append("]");
				first = false;
			}
			if (first)
				buffer.append("0");
			buffer.append(" ").append(operation).append(" 0\n");
		}
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("[\n");
		toStringHelper(buffer, equations, "=");
		toStringHelper(buffer, inequalities, ">=");
		buffer.append("]");
		return buffer.toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
