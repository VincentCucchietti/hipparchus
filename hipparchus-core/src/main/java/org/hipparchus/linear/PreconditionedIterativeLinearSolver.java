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
package org.hipparchus.linear;

import org.hipparchus.exception.LocalizedCoreFormats;
import org.hipparchus.exception.MathIllegalArgumentException;
import org.hipparchus.exception.MathIllegalStateException;
import org.hipparchus.exception.NullArgumentException;
import org.hipparchus.util.IterationManager;
import org.hipparchus.util.MathUtils;

/**
 * <p>
 * This abstract class defines preconditioned iterative solvers. When A is
 * ill-conditioned, instead of solving system A &middot; x = b directly, it is
 * preferable to solve either
 * \[
 * (M \cdot A) \cdot x = M \cdot b
 * \]
 * (left preconditioning), or
 * \[
 * (A \cdot M) \cdot y = b, \text{followed by} M \cdot y = x
 * \]
 * </p>
 * <p>
 * (right preconditioning), where M approximates in some way A<sup>-1</sup>,
 * while matrix-vector products of the type \(M \cdot y\) remain comparatively
 * easy to compute. In this library, M (not M<sup>-1</sup>!) is called the
 * <em>preconditioner</em>.
 * </p>
 * <p>
 * Concrete implementations of this abstract class must be provided with the
 * preconditioner M, as a {@link RealLinearOperator}.
 * </p>
 *
 */
public abstract class PreconditionedIterativeLinearSolver
    extends IterativeLinearSolver {

    /**
     * Creates a new instance of this class, with default iteration manager.
     *
     * @param maxIterations the maximum number of iterations
     */
    public PreconditionedIterativeLinearSolver(final int maxIterations) {
        super(maxIterations);
    }

    /**
     * Creates a new instance of this class, with custom iteration manager.
     *
     * @param manager the custom iteration manager
     * @throws NullArgumentException if {@code manager} is {@code null}
     */
    public PreconditionedIterativeLinearSolver(final IterationManager manager)
        throws NullArgumentException {
        super(manager);
    }

    /**
     * Returns an estimate of the solution to the linear system A &middot; x =
     * b.
     *
     * @param a the linear operator A of the system
     * @param m the preconditioner, M (can be {@code null})
     * @param b the right-hand side vector
     * @param x0 the initial guess of the solution
     * @return a new vector containing the solution
     * @throws NullArgumentException if one of the parameters is {@code null}
     * @throws MathIllegalArgumentException if {@code a} or {@code m} is not
     * square
     * @throws MathIllegalArgumentException if {@code m}, {@code b} or
     * {@code x0} have dimensions inconsistent with {@code a}
     * @throws MathIllegalStateException at exhaustion of the iteration count,
     * unless a custom
     * {@link org.hipparchus.util.Incrementor.MaxCountExceededCallback callback}
     * has been set at construction of the {@link IterationManager}
     */
    public RealVector solve(final RealLinearOperator a,
        final RealLinearOperator m, final RealVector b, final RealVector x0)
        throws MathIllegalArgumentException, NullArgumentException, MathIllegalStateException {
        MathUtils.checkNotNull(x0);
        return solveInPlace(a, m, b, x0.copy());
    }

    /** {@inheritDoc} */
    @Override
    public RealVector solve(final RealLinearOperator a, final RealVector b)
        throws MathIllegalArgumentException, NullArgumentException, MathIllegalStateException {
        MathUtils.checkNotNull(a);
        final RealVector x = new ArrayRealVector(a.getColumnDimension());
        x.set(0.);
        return solveInPlace(a, null, b, x);
    }

    /** {@inheritDoc} */
    @Override
    public RealVector solve(final RealLinearOperator a, final RealVector b,
                            final RealVector x0)
        throws MathIllegalArgumentException, NullArgumentException, MathIllegalStateException {
        MathUtils.checkNotNull(x0);
        return solveInPlace(a, null, b, x0.copy());
    }

    /**
     * Performs all dimension checks on the parameters of
     * {@link #solve(RealLinearOperator, RealLinearOperator, RealVector, RealVector) solve}
     * and
     * {@link #solveInPlace(RealLinearOperator, RealLinearOperator, RealVector, RealVector) solveInPlace},
     * and throws an exception if one of the checks fails.
     *
     * @param a the linear operator A of the system
     * @param m the preconditioner, M (can be {@code null})
     * @param b the right-hand side vector
     * @param x0 the initial guess of the solution
     * @throws NullArgumentException if one of the parameters is {@code null}
     * @throws MathIllegalArgumentException if {@code a} or {@code m} is not
     * square
     * @throws MathIllegalArgumentException if {@code m}, {@code b} or
     * {@code x0} have dimensions inconsistent with {@code a}
     */
    protected static void checkParameters(final RealLinearOperator a,
        final RealLinearOperator m, final RealVector b, final RealVector x0)
        throws MathIllegalArgumentException, NullArgumentException {
        checkParameters(a, b, x0);
        if (m != null) {
            if (m.getColumnDimension() != m.getRowDimension()) {
                throw new MathIllegalArgumentException(LocalizedCoreFormats.NON_SQUARE_OPERATOR,
                                                       m.getColumnDimension(), m.getRowDimension());
            }
            if (m.getRowDimension() != a.getRowDimension()) {
                throw new MathIllegalArgumentException(LocalizedCoreFormats.DIMENSIONS_MISMATCH,
                                                       m.getRowDimension(), a.getRowDimension());
            }
        }
    }

    /**
     * Returns an estimate of the solution to the linear system A &middot; x =
     * b.
     *
     * @param a the linear operator A of the system
     * @param m the preconditioner, M (can be {@code null})
     * @param b the right-hand side vector
     * @return a new vector containing the solution
     * @throws NullArgumentException if one of the parameters is {@code null}
     * @throws MathIllegalArgumentException if {@code a} or {@code m} is not
     * square
     * @throws MathIllegalArgumentException if {@code m} or {@code b} have
     * dimensions inconsistent with {@code a}
     * @throws MathIllegalStateException at exhaustion of the iteration count,
     * unless a custom
     * {@link org.hipparchus.util.Incrementor.MaxCountExceededCallback callback}
     * has been set at construction of the {@link IterationManager}
     */
    public RealVector solve(RealLinearOperator a, RealLinearOperator m,
        RealVector b) throws MathIllegalArgumentException, NullArgumentException, MathIllegalStateException {
        MathUtils.checkNotNull(a);
        final RealVector x = new ArrayRealVector(a.getColumnDimension());
        return solveInPlace(a, m, b, x);
    }

    /**
     * Returns an estimate of the solution to the linear system A &middot; x =
     * b. The solution is computed in-place (initial guess is modified).
     *
     * @param a the linear operator A of the system
     * @param m the preconditioner, M (can be {@code null})
     * @param b the right-hand side vector
     * @param x0 the initial guess of the solution
     * @return a reference to {@code x0} (shallow copy) updated with the
     * solution
     * @throws NullArgumentException if one of the parameters is {@code null}
     * @throws MathIllegalArgumentException if {@code a} or {@code m} is not
     * square
     * @throws MathIllegalArgumentException if {@code m}, {@code b} or
     * {@code x0} have dimensions inconsistent with {@code a}
     * @throws MathIllegalStateException at exhaustion of the iteration count,
     * unless a custom
     * {@link org.hipparchus.util.Incrementor.MaxCountExceededCallback callback}
     * has been set at construction of the {@link IterationManager}
     */
    public abstract RealVector solveInPlace(RealLinearOperator a,
        RealLinearOperator m, RealVector b, RealVector x0) throws
        MathIllegalArgumentException, NullArgumentException, MathIllegalStateException;

    /** {@inheritDoc} */
    @Override
    public RealVector solveInPlace(final RealLinearOperator a,
        final RealVector b, final RealVector x0) throws
        MathIllegalArgumentException, NullArgumentException, MathIllegalStateException {
        return solveInPlace(a, null, b, x0);
    }
}
