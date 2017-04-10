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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import uniol.apt.TestTSCollection;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.synthesize.RegionUtility;
import static uniol.apt.analysis.synthesize.matcher.Matchers.*;

@SuppressWarnings("unchecked")
public class OverapproximateLTSTest {
	private final BigInteger ZERO = BigInteger.ZERO;
	private final BigInteger ONE = BigInteger.ONE;
	private final BigInteger M_ONE = BigInteger.ONE.negate();

	@Test
	public void testSingleStateTS() {
		TransitionSystem ts = TestTSCollection.getSingleStateTS();
		assertThat(OverapproximateLTS.overapproximate(new RegionUtility(ts)),
				contains(regionWithInitialMarking(1)));
	}

	@Test
	public void testSingleStateWithUnreachableTS() {
		TransitionSystem ts = TestTSCollection.getSingleStateWithUnreachableTS();
		assertThat(OverapproximateLTS.overapproximate(new RegionUtility(ts)),
				containsInAnyOrder(
					both(regionWithInitialMarking(1)).and(pureRegionWithWeight("NotA", 0)),
					both(regionWithInitialMarking(0)).and(pureRegionWithWeight("NotA", 1)),
					both(regionWithInitialMarking(0)).and(pureRegionWithWeight("NotA", -1))));
	}

	@Test
	public void testSingleStateTSWithLoop() {
		TransitionSystem ts = TestTSCollection.getSingleStateTSWithLoop();
		assertThat(OverapproximateLTS.overapproximate(new RegionUtility(ts)),
				containsInAnyOrder(
					both(regionWithInitialMarking(1)).and(pureRegionWithWeight("a", 0)),
					both(regionWithInitialMarking(1)).and(impureRegionWithWeight("a", 1, 1))));
	}

	@Test
	public void testTwoStateCycleSameLabelTS() {
		TransitionSystem ts = TestTSCollection.getTwoStateCycleSameLabelTS();
		assertThat(OverapproximateLTS.overapproximate(new RegionUtility(ts)),
				containsInAnyOrder(
					both(regionWithInitialMarking(1)).and(pureRegionWithWeight("a", 0)),
					both(regionWithInitialMarking(1)).and(impureRegionWithWeight("a", 1, 1))));
	}

	@Test
	public void testThreeStatesTwoEdgesTS() {
		TransitionSystem ts = TestTSCollection.getThreeStatesTwoEdgesTS();
		List<String> alphabet = Arrays.asList("a", "b");
		assertThat(OverapproximateLTS.overapproximate(new RegionUtility(ts)),
				containsInAnyOrder(
					// { init=0, 0:a:0, 0:b:1 },
					both(regionWithInitialMarking(0)).and(pureRegionWithWeights(alphabet, Arrays.asList(ZERO, ONE))),
					// { init=1, 1:a:0, 1:b:0 },
					both(regionWithInitialMarking(1)).and(pureRegionWithWeights(alphabet, Arrays.asList(M_ONE, M_ONE))),
					// { init=1, 1:a:0, 0:b:0 },
					both(regionWithInitialMarking(1)).and(pureRegionWithWeights(alphabet, Arrays.asList(M_ONE, ZERO))),
					// { init=0, 0:a:1, 0:b:0 },
					both(regionWithInitialMarking(0)).and(pureRegionWithWeights(alphabet, Arrays.asList(ONE, ZERO))),
					// { init=1, 0:a:0, 0:b:0 },
					both(regionWithInitialMarking(1)).and(pureRegionWithWeights(alphabet, Arrays.asList(ZERO, ZERO))),
					// { init=1, 0:a:0, 1:b:0 },
					both(regionWithInitialMarking(1)).and(pureRegionWithWeights(alphabet, Arrays.asList(ZERO, M_ONE)))));
	}

	@Test
	public void testPathTS() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		List<String> alphabet = Arrays.asList("a", "b", "c");
		assertThat(OverapproximateLTS.overapproximate(new RegionUtility(ts)),
				containsInAnyOrder(
					// { init=1, 0:a:0, 0:b:0, 1:c:0 }
					both(regionWithInitialMarking(1)).and(pureRegionWithWeights(alphabet, Arrays.asList(ZERO, ZERO, M_ONE))),
					// { init=1, 0:a:0, 1:b:1, 1:c:1 }
					both(regionWithInitialMarking(1)).and(impureRegionWithWeights(alphabet, Arrays.asList(ZERO, ZERO, ONE, ONE, ONE, ONE))),
					// { init=1, 0:a:0, 1:b:1, 0:c:0 }
					both(regionWithInitialMarking(1)).and(impureRegionWithWeights(alphabet, Arrays.asList(ZERO, ZERO, ONE, ONE, ZERO, ZERO))),
					// { init=1, 0:a:0, 0:b:0, 0:c:0 }
					both(regionWithInitialMarking(1)).and(pureRegionWithWeights(alphabet, Arrays.asList(ZERO, ZERO, ZERO))),
					// { init=0, 0:a:1, 0:b:0, 1:c:0 }
					both(regionWithInitialMarking(0)).and(pureRegionWithWeights(alphabet, Arrays.asList(ONE, ZERO, M_ONE))),
					// { init=0, 0:a:1, 1:b:1, 1:c:0 }
					both(regionWithInitialMarking(0)).and(impureRegionWithWeights(alphabet, Arrays.asList(ZERO, ONE, ONE, ONE, ONE, ZERO))),
					// { init=1, 1:a:0, 0:b:0, 0:c:1 }
					both(regionWithInitialMarking(1)).and(pureRegionWithWeights(alphabet, Arrays.asList(M_ONE, ZERO, ONE))),
					// { init=1, 1:a:1, 1:b:1, 0:c:0 }
					both(regionWithInitialMarking(1)).and(impureRegionWithWeights(alphabet, Arrays.asList(ONE, ONE, ONE, ONE, ZERO, ZERO))),
					// { init=1, 1:a:1, 0:b:0, 0:c:0 }
					both(regionWithInitialMarking(1)).and(impureRegionWithWeights(alphabet, Arrays.asList(ONE, ONE, ZERO, ZERO, ZERO, ZERO))),
					// { init=2, 1:a:0, 0:b:0, 0:c:0 }
					both(regionWithInitialMarking(2)).and(pureRegionWithWeights(alphabet, Arrays.asList(M_ONE, ZERO, ZERO))),
					// { init=0, 0:a:1, 0:b:0, 0:c:0 }
					both(regionWithInitialMarking(0)).and(pureRegionWithWeights(alphabet, Arrays.asList(ONE, ZERO, ZERO))),
					// { init=0, 0:a:1, 1:b:1, 0:c:0 }
					both(regionWithInitialMarking(0)).and(impureRegionWithWeights(alphabet, Arrays.asList(ZERO, ONE, ONE, ONE, ZERO, ZERO))),
					// { init=0, 0:a:0, 0:b:0, 0:c:1 }
					both(regionWithInitialMarking(0)).and(pureRegionWithWeights(alphabet, Arrays.asList(ZERO, ZERO, ONE)))));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
