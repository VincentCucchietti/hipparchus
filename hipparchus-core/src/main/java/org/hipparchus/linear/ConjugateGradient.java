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

/**
 * <p>
 * This is an implementation of the conjugate gradient method for
 * {@link RealLinearOperator}. It follows closely the template by <a
 * href="#BARR1994">Barrett et al. (1994)</a> (figure 2.5). The linear system at
 * hand is A &middot; x = b, and the residual is r = b - A &middot; x.
 * </p>
 * <p><strong>Default stopping criterion</strong></p>
 * <p>
 * A default stopping criterion is implemented. The iterations stop when || r ||
 * &le; &delta; || b ||, where b is the right-hand side vector, r the current
 * estimate of the residual, and &delta; a user-specified tolerance. It should
 * be noted that r is the so-called <em>updated</em> residual, which might
 * differ from the true residual due to rounding-off errors (see e.g. <a
 * href="#STRA2002">Strakos and Tichy, 2002</a>).
 * </p>
 * <p><strong>Iteration count</strong></p>
 * <p>
 * In the present context, an iteration should be understood as one evaluation
 * of the matrix-vector product A &middot; x. The initialization phase therefore
 * counts as one iteration.
 * </p>
 * <p><strong><a id="context">Exception context</a></strong></p>
 * <p>
 * Besides standard {@link MathIllegalArgumentException}, this class might throw
 * {@link MathIllegalArgumentException} if the linear operator or
 * the preconditioner are not positive definite.
 * </p>
 * <ul>
 * <li>key {@code "operator"} points to the offending linear operator, say L,</li>
 * <li>key {@code "vector"} points to the offending vector, say x, such that
 * x<sup>T</sup> &middot; L &middot; x &lt; 0.</li>
 * </ul>
 * <p><strong>References</strong></p>
 * <dl>
 * <dt>Barret et al. (1994)</dt>
 * <dd>R. Barrett, M. Berry, T. F. Chan, J. Demmel, J. M. Donato, J. Dongarra,
 * V. Eijkhout, R. Pozo, C. Romine and H. Van der Vorst,
 * <a href="http://www.netlib.org/linalg/html_templates/Templates.html"><em>
 * Templates for the Solution of Linear Systems: Building Blocks for Iterative
 * Methods</em></a>, SIAM</dd>
 * <dt>Strakos and Tichy (2002)</dt>
 * <dd>Z. Strakos and P. Tichy, <a
 * href="http://etna.mcs.kent.edu/vol.13.2002/pp56-80.dir/pp56-80.pdf">
 * <em>On error estimation in the conjugate gradient method and why it works
 * in finite precision computations</em></a>, Electronic Transactions on
 * Numerical Analysis 13: 56-80, 2002</dd>
 * </dl>
 *
 */
public class ConjugateGradient
    extends PreconditionedIterativeLinearSolver {

    /** Key for the <a href="#context">exception context</a>. */
    public static final String OPERATOR = "operator";

    /** Key for the <a href="#context">exception context</a>. */
    public static final String VECTOR = "vector";

    /**
     * {@code true} if positive-definiteness of matrix and preconditioner should
     * be checked.
     */
    private boolean check;

    /** The value of &delta;, for the default stopping criterion. */
    private final double delta;

    /**
     * Creates a new instance of this class, with <a href="#stopcrit">default
     * stopping criterion</a>.
     *
     * @param maxIterations the maximum number of iterations
     * @param delta the &delta; parameter for the default stopping criterion
     * @param check {@code true} if positive definiteness of both matrix and
     * preconditioner should be checked
     */
    public ConjugateGradient(final int maxIterations, final double delta,
                             final boolean check) {
        super(maxIterations);
        this.delta = delta;
        this.check = check;
    }

    /**
     * Creates a new instance of this class, with <a href="#stopcrit">default
     * stopping criterion</a> and custom iteration manager.
     *
     * @param manager the custom iteration manager
     * @param delta the &delta; parameter for the default stopping criterion
     * @param check {@code true} if positive definiteness of both matrix and
     * preconditioner should be checked
     * @throws NullArgumentException if {@code manager} is {@code null}
     */
    public ConjugateGradient(final IterationManager manager,
                             final double delta, final boolean check)
        throws NullArgumentException {
        super(manager);
        this.delta = delta;
        this.check = check;
    }

    /**
     * Returns {@code true} if positive-definiteness should be checked for both
     * matrix and preconditioner.
     *
     * @return {@code true} if the tests are to be performed
     * @since 1.4
     */
    public final boolean shouldCheck() {
        return check;
    }

    /**
     * {@inheritDoc}
     *
     * @throws MathIllegalArgumentException if {@code a} or {@code m} is
     * not positive definite
     */
    @Override
    public RealVector solveInPlace(final RealLinearOperator a,
                                   final RealLinearOperator m,
                                   final RealVector b,
                                   final RealVector x0)
        throws MathIllegalArgumentException, NullArgumentException,
        MathIllegalStateException {
        checkParameters(a, m, b, x0);
        final IterationManager manager = getIterationManager();
        // Initialization of default stopping criterion
        manager.resetIterationCount();
        final double rmax = delta * b.getNorm();
        final RealVector bro = RealVector.unmodifiableRealVector(b);

        // Initialization phase counts as one iteration.
        manager.incrementIterationCount();
        // p and x are constructed as copies of x0, since presumably, the type
        // of x is optimized for the calculation of the matrix-vector product
        // A.x.
        final RealVector x = x0;
        final RealVector xro = RealVector.unmodifiableRealVector(x);
        final RealVector p = x.copy();
        RealVector q = a.operate(p);

        final RealVector r = b.combine(1, -1, q);
        final RealVector rro = RealVector.unmodifiableRealVector(r);
        double rnorm = r.getNorm();
        RealVector z;
        if (m == null) {
            z = r;
        } else {
            z = null;
        }
        IterativeLinearSolverEvent evt;
        evt = new DefaultIterativeLinearSolverEvent(this,
            manager.getIterations(), xro, bro, rro, rnorm);
        manager.fireInitializationEvent(evt);
        if (rnorm <= rmax) {
            manager.fireTerminationEvent(evt);
            return x;
        }
        double rhoPrev = 0.;
        while (true) {
            manager.incrementIterationCount();
            evt = new DefaultIterativeLinearSolverEvent(this,
                manager.getIterations(), xro, bro, rro, rnorm);
            manager.fireIterationStartedEvent(evt);
            if (m != null) {
                z = m.operate(r);
            }
            final double rhoNext = r.dotProduct(z);
            if (check && (rhoNext <= 0.)) {
                throw new MathIllegalArgumentException(LocalizedCoreFormats.NON_POSITIVE_DEFINITE_OPERATOR);
            }
            if (manager.getIterations() == 2) {
                p.setSubVector(0, z);
            } else {
                p.combineToSelf(rhoNext / rhoPrev, 1., z);
            }
            q = a.operate(p);
            final double pq = p.dotProduct(q);
            if (check && (pq <= 0.)) {
                throw new MathIllegalArgumentException(LocalizedCoreFormats.NON_POSITIVE_DEFINITE_OPERATOR);
            }
            final double alpha = rhoNext / pq;
            x.combineToSelf(1., alpha, p);
            r.combineToSelf(1., -alpha, q);
            rhoPrev = rhoNext;
            rnorm = r.getNorm();
            evt = new DefaultIterativeLinearSolverEvent(this,
                manager.getIterations(), xro, bro, rro, rnorm);
            manager.fireIterationPerformedEvent(evt);
            if (rnorm <= rmax) {
                manager.fireTerminationEvent(evt);
                return x;
            }
        }
    }
}
