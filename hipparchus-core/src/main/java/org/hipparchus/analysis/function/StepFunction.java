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

package org.hipparchus.analysis.function;

import java.util.Arrays;

import org.hipparchus.analysis.UnivariateFunction;
import org.hipparchus.exception.LocalizedCoreFormats;
import org.hipparchus.exception.MathIllegalArgumentException;
import org.hipparchus.exception.NullArgumentException;
import org.hipparchus.util.MathArrays;

/**
 * <a href="http://en.wikipedia.org/wiki/Step_function">
 *  Step function</a>.
 *
 */
public class StepFunction implements UnivariateFunction {
    /** Abscissae. */
    private final double[] abscissa;
    /** Ordinates. */
    private final double[] ordinate;

    /**
     * Builds a step function from a list of arguments and the corresponding
     * values. Specifically, returns the function h(x) defined by <pre><code>
     * h(x) = y[0] for all x &lt; x[1]
     *        y[1] for x[1] &le; x &lt; x[2]
     *        ...
     *        y[y.length - 1] for x &ge; x[x.length - 1]
     * </code></pre>
     * The value of {@code x[0]} is ignored, but it must be strictly less than
     * {@code x[1]}.
     *
     * @param x Domain values where the function changes value.
     * @param y Values of the function.
     * @throws MathIllegalArgumentException
     * if the {@code x} array is not sorted in strictly increasing order.
     * @throws NullArgumentException if {@code x} or {@code y} are {@code null}.
     * @throws MathIllegalArgumentException if {@code x} or {@code y} are zero-length.
     * @throws MathIllegalArgumentException if {@code x} and {@code y} do not
     * have the same length.
     */
    public StepFunction(double[] x,
                        double[] y)
        throws MathIllegalArgumentException, NullArgumentException {
        if (x == null ||
            y == null) {
            throw new NullArgumentException();
        }
        if (x.length == 0 ||
            y.length == 0) {
            throw new MathIllegalArgumentException(LocalizedCoreFormats.NO_DATA);
        }
        MathArrays.checkEqualLength(y, x);
        MathArrays.checkOrder(x);

        abscissa = x.clone();
        ordinate = y.clone();
    }

    /** {@inheritDoc} */
    @Override
    public double value(double x) {
        final int index = Arrays.binarySearch(abscissa, x);

        if (index < -1) {
            // "x" is between "abscissa[-index-2]" and "abscissa[-index-1]".
            return ordinate[-index-2];
        } else if (index >= 0) {
            // "x" is exactly "abscissa[index]".
            return ordinate[index];
        } else {
            // Otherwise, "x" is smaller than the first value in "abscissa"
            // (hence the returned value should be "ordinate[0]").
            return ordinate[0];
        }

    }
}
