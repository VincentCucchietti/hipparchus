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

package org.hipparchus.analysis.solvers;

import org.hipparchus.analysis.UnivariateFunction;
import org.hipparchus.exception.MathIllegalArgumentException;
import org.hipparchus.exception.MathIllegalStateException;
import org.hipparchus.exception.NullArgumentException;
import org.hipparchus.util.Incrementor;
import org.hipparchus.util.MathUtils;

/**
 * Provide a default implementation for several functions useful to generic
 * solvers.
 * The default values for relative and function tolerances are 1e-14
 * and 1e-15, respectively. It is however highly recommended to not
 * rely on the default, but rather carefully consider values that match
 * user's expectations, as well as the specifics of each implementation.
 *
 * @param <F> Type of function to solve.
 *
 */
public abstract class BaseAbstractUnivariateSolver<F extends UnivariateFunction>
    implements BaseUnivariateSolver<F> {
    /** Default relative accuracy. */
    private static final double DEFAULT_RELATIVE_ACCURACY = 1e-14;
    /** Default function value accuracy. */
    private static final double DEFAULT_FUNCTION_VALUE_ACCURACY = 1e-15;
    /** Function value accuracy. */
    private final double functionValueAccuracy;
    /** Absolute accuracy. */
    private final double absoluteAccuracy;
    /** Relative accuracy. */
    private final double relativeAccuracy;
    /** Evaluations counter. */
    private Incrementor evaluations;
    /** Lower end of search interval. */
    private double searchMin;
    /** Higher end of search interval. */
    private double searchMax;
    /** Initial guess. */
    private double searchStart;
    /** Function to solve. */
    private F function;

    /**
     * Construct a solver with given absolute accuracy.
     *
     * @param absoluteAccuracy Maximum absolute error.
     */
    protected BaseAbstractUnivariateSolver(final double absoluteAccuracy) {
        this(DEFAULT_RELATIVE_ACCURACY,
             absoluteAccuracy,
             DEFAULT_FUNCTION_VALUE_ACCURACY);
    }

    /**
     * Construct a solver with given accuracies.
     *
     * @param relativeAccuracy Maximum relative error.
     * @param absoluteAccuracy Maximum absolute error.
     */
    protected BaseAbstractUnivariateSolver(final double relativeAccuracy,
                                           final double absoluteAccuracy) {
        this(relativeAccuracy,
             absoluteAccuracy,
             DEFAULT_FUNCTION_VALUE_ACCURACY);
    }

    /**
     * Construct a solver with given accuracies.
     *
     * @param relativeAccuracy Maximum relative error.
     * @param absoluteAccuracy Maximum absolute error.
     * @param functionValueAccuracy Maximum function value error.
     */
    protected BaseAbstractUnivariateSolver(final double relativeAccuracy,
                                           final double absoluteAccuracy,
                                           final double functionValueAccuracy) {
        this.absoluteAccuracy      = absoluteAccuracy;
        this.relativeAccuracy      = relativeAccuracy;
        this.functionValueAccuracy = functionValueAccuracy;
        this.evaluations           = new Incrementor();
    }

    /** {@inheritDoc} */
    @Override
    public int getEvaluations() {
        return evaluations.getCount();
    }
    /**
     * @return the lower end of the search interval.
     */
    public double getMin() {
        return searchMin;
    }
    /**
     * @return the higher end of the search interval.
     */
    public double getMax() {
        return searchMax;
    }
    /**
     * @return the initial guess.
     */
    public double getStartValue() {
        return searchStart;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public double getAbsoluteAccuracy() {
        return absoluteAccuracy;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public double getRelativeAccuracy() {
        return relativeAccuracy;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public double getFunctionValueAccuracy() {
        return functionValueAccuracy;
    }

    /**
     * Compute the objective function value.
     *
     * @param point Point at which the objective function must be evaluated.
     * @return the objective function value at specified point.
     * @throws MathIllegalStateException if the maximal number of evaluations
     * is exceeded.
     */
    protected double computeObjectiveValue(double point)
        throws MathIllegalStateException {
        incrementEvaluationCount();
        return function.value(point);
    }

    /**
     * Prepare for computation.
     * Subclasses must call this method if they override any of the
     * {@code solve} methods.
     *
     * @param f Function to solve.
     * @param min Lower bound for the interval.
     * @param max Upper bound for the interval.
     * @param startValue Start value to use.
     * @param maxEval Maximum number of evaluations.
     * @exception NullArgumentException if f is null
     */
    protected void setup(int maxEval,
                         F f,
                         double min, double max,
                         double startValue)
        throws NullArgumentException {
        // Checks.
        MathUtils.checkNotNull(f);

        // Reset.
        searchMin = min;
        searchMax = max;
        searchStart = startValue;
        function = f;
        evaluations = evaluations.withMaximalCount(maxEval);
    }

    /** {@inheritDoc} */
    @Override
    public double solve(int maxEval, F f, double min, double max, double startValue)
        throws MathIllegalArgumentException, MathIllegalStateException {
        // Initialization.
        setup(maxEval, f, min, max, startValue);

        // Perform computation.
        return doSolve();
    }

    /** {@inheritDoc} */
    @Override
    public double solve(int maxEval, F f, double min, double max) {
        return solve(maxEval, f, min, max, min + 0.5 * (max - min));
    }

    /** {@inheritDoc} */
    @Override
    public double solve(int maxEval, F f, double startValue)
        throws MathIllegalArgumentException, MathIllegalStateException {
        return solve(maxEval, f, Double.NaN, Double.NaN, startValue);
    }

    /**
     * Method for implementing actual optimization algorithms in derived
     * classes.
     *
     * @return the root.
     * @throws MathIllegalStateException if the maximal number of evaluations
     * is exceeded.
     * @throws MathIllegalArgumentException if the initial search interval does not bracket
     * a root and the solver requires it.
     */
    protected abstract double doSolve()
        throws MathIllegalArgumentException, MathIllegalStateException;

    /**
     * Check whether the function takes opposite signs at the endpoints.
     *
     * @param lower Lower endpoint.
     * @param upper Upper endpoint.
     * @return {@code true} if the function values have opposite signs at the
     * given points.
     */
    protected boolean isBracketing(final double lower,
                                   final double upper) {
        return UnivariateSolverUtils.isBracketing(function, lower, upper);
    }

    /**
     * Check whether the arguments form a (strictly) increasing sequence.
     *
     * @param start First number.
     * @param mid Second number.
     * @param end Third number.
     * @return {@code true} if the arguments form an increasing sequence.
     */
    protected boolean isSequence(final double start,
                                 final double mid,
                                 final double end) {
        return UnivariateSolverUtils.isSequence(start, mid, end);
    }

    /**
     * Check that the endpoints specify an interval.
     *
     * @param lower Lower endpoint.
     * @param upper Upper endpoint.
     * @throws MathIllegalArgumentException if {@code lower >= upper}.
     */
    protected void verifyInterval(final double lower,
                                  final double upper)
        throws MathIllegalArgumentException {
        UnivariateSolverUtils.verifyInterval(lower, upper);
    }

    /**
     * Check that {@code lower < initial < upper}.
     *
     * @param lower Lower endpoint.
     * @param initial Initial value.
     * @param upper Upper endpoint.
     * @throws MathIllegalArgumentException if {@code lower >= initial} or
     * {@code initial >= upper}.
     */
    protected void verifySequence(final double lower,
                                  final double initial,
                                  final double upper)
        throws MathIllegalArgumentException {
        UnivariateSolverUtils.verifySequence(lower, initial, upper);
    }

    /**
     * Check that the endpoints specify an interval and the function takes
     * opposite signs at the endpoints.
     *
     * @param lower Lower endpoint.
     * @param upper Upper endpoint.
     * @throws NullArgumentException if the function has not been set.
     * @throws MathIllegalArgumentException if the function has the same sign at
     * the endpoints.
     */
    protected void verifyBracketing(final double lower,
                                    final double upper)
        throws MathIllegalArgumentException, NullArgumentException {
        UnivariateSolverUtils.verifyBracketing(function, lower, upper);
    }

    /**
     * Increment the evaluation count by one.
     * Method {@link #computeObjectiveValue(double)} calls this method internally.
     * It is provided for subclasses that do not exclusively use
     * {@code computeObjectiveValue} to solve the function.
     * See e.g. {@link AbstractUnivariateDifferentiableSolver}.
     *
     * @throws MathIllegalStateException when the allowed number of function
     * evaluations has been exhausted.
     */
    protected void incrementEvaluationCount()
        throws MathIllegalStateException {
        evaluations.increment();
    }
}
