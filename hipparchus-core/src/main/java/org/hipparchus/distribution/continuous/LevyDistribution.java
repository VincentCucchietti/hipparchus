/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * This is not the original file distributed by the Apache Software Foundation
 * It has been modified by the Hipparchus project
 */
package org.hipparchus.distribution.continuous;

import org.hipparchus.exception.MathIllegalArgumentException;
import org.hipparchus.special.Erf;
import org.hipparchus.util.FastMath;
import org.hipparchus.util.MathUtils;

/**
 * This class implements the <a href="http://en.wikipedia.org/wiki/L%C3%A9vy_distribution">
 * L&eacute;vy distribution</a>.
 */
public class LevyDistribution extends AbstractRealDistribution {

    /** Serializable UID. */
    private static final long serialVersionUID = 20130314L;

    /** Location parameter. */
    private final double mu;

    /** Scale parameter. */
    private final double c;  // Setting this to 1 returns a cumProb of 1.0

    /** Half of c (for calculations). */
    private final double halfC;

    /**
     * Build a new instance.
     *
     * @param mu location parameter
     * @param c scale parameter
     */
    public LevyDistribution(final double mu, final double c) {
        super();
        this.mu    = mu;
        this.c     = c;
        this.halfC = 0.5 * c;
    }


    /** {@inheritDoc}
    * <p>
    * From Wikipedia: The probability density function of the L&eacute;vy distribution
    * over the domain is
    * </p>
    * \[
    * f(x; \mu, c) = \sqrt{\frac{c}{2\pi}} \frac{e^{\frac{-c}{2 (x - \mu)}}}{(x - \mu)^\frac{3}{2}}
    * \]
    * <p>
    * For this distribution, {@code X}, this method returns {@code P(X < x)}.
    * If {@code x} is less than location parameter &mu;, {@code Double.NaN} is
    * returned, as in these cases the distribution is not defined.
    * </p>
    */
    @Override
    public double density(final double x) {
        if (x < mu) {
            return Double.NaN;
        }

        final double delta = x - mu;
        final double f     = halfC / delta;
        return FastMath.sqrt(f / FastMath.PI) * FastMath.exp(-f) /delta;
    }

    /** {@inheritDoc}
     *
     * See documentation of {@link #density(double)} for computation details.
     */
    @Override
    public double logDensity(double x) {
        if (x < mu) {
            return Double.NaN;
        }

        final double delta = x - mu;
        final double f     = halfC / delta;
        return 0.5 * FastMath.log(f / FastMath.PI) - f - FastMath.log(delta);
    }

    /** {@inheritDoc}
     * <p>
     * From Wikipedia: the cumulative distribution function is
     * </p>
     * <pre>
     * f(x; u, c) = erfc (&radic; (c / 2 (x - u )))
     * </pre>
     */
    @Override
    public double cumulativeProbability(final double x) {
        if (x < mu) {
            return Double.NaN;
        }
        return Erf.erfc(FastMath.sqrt(halfC / (x - mu)));
    }

    /** {@inheritDoc} */
    @Override
    public double inverseCumulativeProbability(final double p) throws MathIllegalArgumentException {
        MathUtils.checkRangeInclusive(p, 0, 1);
        final double t = Erf.erfcInv(p);
        return mu + halfC / (t * t);
    }

    /** Get the scale parameter of the distribution.
     * @return scale parameter of the distribution
     */
    public double getScale() {
        return c;
    }

    /** Get the location parameter of the distribution.
     * @return location parameter of the distribution
     */
    public double getLocation() {
        return mu;
    }

    /** {@inheritDoc} */
    @Override
    public double getNumericalMean() {
        return Double.POSITIVE_INFINITY;
    }

    /** {@inheritDoc} */
    @Override
    public double getNumericalVariance() {
        return Double.POSITIVE_INFINITY;
    }

    /** {@inheritDoc} */
    @Override
    public double getSupportLowerBound() {
        return mu;
    }

    /** {@inheritDoc} */
    @Override
    public double getSupportUpperBound() {
        return Double.POSITIVE_INFINITY;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSupportConnected() {
        return true;
    }

}
