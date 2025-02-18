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
package org.hipparchus.optim.nonlinear.vector.leastsquares;

import org.hipparchus.linear.RealVector;
import org.hipparchus.optim.ConvergenceChecker;
import org.hipparchus.util.Incrementor;

/**
 * An adapter that delegates to another implementation of {@link LeastSquaresProblem}.
 *
 */
public class LeastSquaresAdapter implements LeastSquaresProblem {

    /** the delegate problem */
    private final LeastSquaresProblem problem;

    /**
     * Delegate the {@link LeastSquaresProblem} interface to the given implementation.
     *
     * @param problem the delegate
     */
    public LeastSquaresAdapter(final LeastSquaresProblem problem) {
        this.problem = problem;
    }

    /** {@inheritDoc} */
    @Override
    public RealVector getStart() {
        return problem.getStart();
    }

    /** {@inheritDoc} */
    @Override
    public int getObservationSize() {
        return problem.getObservationSize();
    }

    /** {@inheritDoc} */
    @Override
    public int getParameterSize() {
        return problem.getParameterSize();
    }

    /** {@inheritDoc} */
    @Override
    public Evaluation evaluate(final RealVector point) {
        return problem.evaluate(point);
    }

    /** {@inheritDoc} */
    @Override
    public Incrementor getEvaluationCounter() {
        return problem.getEvaluationCounter();
    }

    /** {@inheritDoc} */
    @Override
    public Incrementor getIterationCounter() {
        return problem.getIterationCounter();
    }

    /** {@inheritDoc} */
    @Override
    public ConvergenceChecker<Evaluation> getConvergenceChecker() {
        return problem.getConvergenceChecker();
    }
}
