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

package org.hipparchus.ode.sampling;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

import org.hipparchus.exception.DimensionMismatchException;
import org.hipparchus.exception.MaxCountExceededException;
import org.hipparchus.exception.NoBracketingException;
import org.hipparchus.exception.NumberIsTooSmallException;
import org.hipparchus.ode.ContinuousOutputModel;
import org.hipparchus.ode.TestProblem1;
import org.hipparchus.ode.TestProblem3;
import org.hipparchus.ode.nonstiff.AdamsBashforthIntegrator;
import org.hipparchus.ode.sampling.StepHandler;
import org.junit.Assert;
import org.junit.Test;

public class NordsieckStepInterpolatorTest {

    @Test
    public void derivativesConsistency()
        throws NumberIsTooSmallException, DimensionMismatchException,
               MaxCountExceededException, NoBracketingException {
        TestProblem3 pb = new TestProblem3();
        AdamsBashforthIntegrator integ = new AdamsBashforthIntegrator(4, 0.0, 1.0, 1.0e-10, 1.0e-10);
        StepInterpolatorTestUtils.checkDerivativesConsistency(integ, pb, 0.05, 2.8e-9);
    }

    @Test
    public void serialization()
    throws IOException, ClassNotFoundException,
           NumberIsTooSmallException, DimensionMismatchException,
           MaxCountExceededException, NoBracketingException {

        TestProblem1 pb = new TestProblem1();
        AdamsBashforthIntegrator integ = new AdamsBashforthIntegrator(4, 0.0, 1.0, 1.0e-10, 1.0e-10);
        integ.addStepHandler(new ContinuousOutputModel());
        integ.integrate(pb,
                        pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream    oos = new ObjectOutputStream(bos);
        for (StepHandler handler : integ.getStepHandlers()) {
            oos.writeObject(handler);
        }

        Assert.assertTrue(bos.size() > 47000);
        Assert.assertTrue(bos.size() < 48000);

        ByteArrayInputStream  bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream     ois = new ObjectInputStream(bis);
        ContinuousOutputModel cm  = (ContinuousOutputModel) ois.readObject();

        Random random = new Random(347588535632l);
        double maxError = 0.0;
        for (int i = 0; i < 1000; ++i) {
            double r = random.nextDouble();
            double time = r * pb.getInitialTime() + (1.0 - r) * pb.getFinalTime();
            cm.setInterpolatedTime(time);
            double[] interpolatedY = cm.getInterpolatedState();
            double[] theoreticalY  = pb.computeTheoreticalState(time);
            double dx = interpolatedY[0] - theoreticalY[0];
            double dy = interpolatedY[1] - theoreticalY[1];
            double error = dx * dx + dy * dy;
            if (error > maxError) {
                maxError = error;
            }
        }

        Assert.assertTrue(maxError < 1.0e-6);

    }

}
