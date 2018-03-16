/*
 * Licensed to the Hipparchus project under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The Hipparchus project licenses this file to You under the Apache License, Version 2.0
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

package org.hipparchus.filtering.kalman.linear;

import org.hipparchus.exception.MathRuntimeException;
import org.hipparchus.filtering.kalman.AbstractKalmanFilter;
import org.hipparchus.filtering.kalman.Measurement;
import org.hipparchus.filtering.kalman.ProcessEstimate;
import org.hipparchus.linear.MatrixDecomposer;
import org.hipparchus.linear.RealMatrix;
import org.hipparchus.linear.RealVector;

/**
 * Kalman filter for {@link LinearProcess linear process}.
 * @since 1.3
 */
public class LinearKalmanFilter extends AbstractKalmanFilter {

    /** Process to be estimated. */
    private final LinearProcess process;

    /** Simple constructor.
     * @param decomposer decomposer to use for the correction phase
     * @param process linear process to estimate
     * @param initialState initial state
     */
    public LinearKalmanFilter(final MatrixDecomposer decomposer,
                              final LinearProcess process,
                              final ProcessEstimate initialState) {
        super(decomposer, initialState);
        this.process = process;
    }

    /** {@inheritDoc} */
    @Override
    public ProcessEstimate estimationStep(final Measurement measurement)
        throws MathRuntimeException {

        // prediction phase
        final LinearEvolution evolution = process.getEvolution(measurement);
        final RealMatrix      a         = evolution.getStateTransitionMatrix();
        final RealMatrix      b         = evolution.getControlMatrix();
        final RealVector      u         = (b == null) ? null : evolution.getCommand();
        final RealMatrix      q         = evolution.getProcessNoiseMatrix();

        RealVector predXk = a.operate(getCorrected().getState());
        if (b != null) {
            predXk = predXk.add(b.operate(u));
        }

        predict(measurement.getTime(), predXk, a, q);

        // correction phase
        correct(measurement, evolution.getMeasurementJacobian());
        return getCorrected();

    }

}
