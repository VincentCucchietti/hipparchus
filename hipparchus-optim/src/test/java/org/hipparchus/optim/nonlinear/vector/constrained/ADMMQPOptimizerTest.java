/*
 * Licensed to the Hipparchus project under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The Hipparchus project licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hipparchus.optim.nonlinear.vector.constrained;

import org.hipparchus.optim.nonlinear.scalar.ObjectiveFunction;
import org.junit.Test;

public class ADMMQPOptimizerTest extends AbstractConstrainedOptimizerTest {

    protected ConstraintOptimizer buildOptimizer() {
        return new ADMMQPOptimizer();
    }

    @Test
    public void test1() {
        QuadraticFunction q = new QuadraticFunction(new double[][] { { 4.0, -2.0 }, { -2.0, 4.0 } },
                                                    new double[] { 6.0, 0.0 },
                                                    0.0);

        // y = 1
        LinearEqualityConstraint eqc = new LinearEqualityConstraint(new double[][] { { 0.0, 1.0 } },
                                                                    new double[] { 1.0 });


        // x > 0, y > 0, x + y > 2
        LinearInequalityConstraint ineqc = new LinearInequalityConstraint(new double[][] { { 1.0, 0.0 }, { 0.0, 1.0 }, { 1.0, 1.0 } },
                                                                          new double[] { 0.0, 0.0, 2.0 });


        doTestProblem(new double[] {  1, 1 },       2.5e-5,
                      new double[] { -6, 0, 0, 8 }, 2.6e-4,
                      8, 2.0e-4,
                      new ObjectiveFunction(q),
                      null,
                      eqc, ineqc);

    }

    @Test
    public void test2() {
        QuadraticFunction q = new QuadraticFunction(new double[][] { { 6.0, 2.0 }, { 2.0, 8.0 } },
                                                    new double[] { 5.0, 1.0 },
                                                    0.0);

        // constraint x + 2y = 1
        LinearEqualityConstraint eqc = new LinearEqualityConstraint(new double[][] { { 1.0, 2.0 } },
                                                                    new double[] { 1.0 });

        // x > 0, y > 0
        LinearInequalityConstraint ineqc = new LinearInequalityConstraint(new double[][] { { 1.0, 0.0 }, { 0.0, 1.0 } },
                                                                          new double[] { 0.0, 0.0 });


        doTestProblem(new double[] {  0, 0.5 },     1.1e-5,
                      new double[] { 2.5, 3.5, 0 }, 8.5e-6,
                      1.5, 6.3e-5,
                      new ObjectiveFunction(q),
                      null,
                      eqc, ineqc);

    }

    @Test
    public void testBounded() {
        QuadraticFunction q = new QuadraticFunction(new double[][] { { 1.0, 0.0 }, { 0, 1.0 } },
                                                    new double[] { -3.0, -1.0 },
                                                    5.0);

        // 0 < x < 2, 0 < y < 2
        LinearBoundedConstraint ineqc = new LinearBoundedConstraint(new double[][] { { 1.0, 0.0 },{ 0.0, 1.0 }},
                                                                     new double[] { 0.0,0.0 }, new double[] { 2.0,2.0 });


        doTestProblem(new double[] { 2, 1 }, 4.6e-6,
                      new double[] { 1, 0 }, 3.1e-5,
                      0.5, 4.6e-6,
                      new ObjectiveFunction(q),
                      null,
                      ineqc);

    }

}
