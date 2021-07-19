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
package org.hipparchus.special.elliptic.legendre;

import org.hipparchus.CalculusFieldElement;
import org.hipparchus.complex.Complex;
import org.hipparchus.complex.FieldComplex;
import org.hipparchus.special.elliptic.carlson.CarlsonEllipticIntegral;
import org.hipparchus.util.FastMath;
import org.hipparchus.util.MathUtils;

/** Complete and incomplete elliptic integrals in Legendre form.
 * <p>
 * The elliptic integrals are related to Jacobi elliptic functions.
 * </p>
 * <p>
 * There are different conventions to interpret the arguments of
 * Legendre elliptic integrals. In mathematical texts, these conventions show
 * up using the separator between arguments. So for example for the incomplete
 * integral of the first kind F we have:
 * <ul>
 *   <li>F(φ, k): the first argument φ is an angle and the second argument k
 *       is the elliptic modulus: this is the trigonometric form of the integral</li>
 *   <li>F(φ; m): the first argument φ is an angle and the second argument m=k²
 *       is the parameter: this is also a trigonometric form of the integral</li>
 *   <li>F(x|m): the first argument x=sin(φ) is not an angle anymore and the
 *       second argument m=k² is the parameter: this is the Legendre form</li>
 *   <li>F(φ\α): the first argument φ is an angle and the second argument α is the
 *       modular angle</li>
 * </ul>
 * As we have no separator in a method call, we have to adopt one convention
 * and stick to it. In Hipparchus, we adopted the Legendre form (i.e. F(x|m),
 * with x=sin(φ) and m=k². These conventions are consistent with Wolfram Alpha
 * functions EllipticF, EllipticE, ElliptiPI…
 * </p>
 * @see <a href="https://en.wikipedia.org/wiki/Elliptic_integral">Elliptic Integrals (Wikipedia)</a>
 * @see <a href="https://mathworld.wolfram.com/CompleteEllipticIntegraloftheFirstKind.html">Complete Elliptic Integrals of the First Kind (MathWorld)</a>
 * @see <a href="https://mathworld.wolfram.com/CompleteEllipticIntegraloftheSecondKind.html">Complete Elliptic Integrals of the Second Kind (MathWorld)</a>
 * @see <a href="https://mathworld.wolfram.com/EllipticIntegraloftheFirstKind.html">Elliptic Integrals of the First Kind (MathWorld)</a>
 * @see <a href="https://mathworld.wolfram.com/EllipticIntegraloftheSecondKind.html">Elliptic Integrals of the Second Kind (MathWorld)</a>
 * @see <a href="https://mathworld.wolfram.com/EllipticIntegraloftheThirdKind.html">Elliptic Integrals of the Third Kind (MathWorld)</a>
 * @since 2.0
 */
public class LegendreEllipticIntegral {

    /** Private constructor for a utility class.
     */
    private LegendreEllipticIntegral() {
        // nothing to do
    }

    /** Get the nome q.
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @return nome q
     */
    public static double nome(final double m) {
        if (m < 1.0e-16) {
            // first terms of infinite series in Abramowitz and Stegun 17.3.21
            final double m16 = m * 0.0625;
            return m16 * (1 + 8 * m16);
        } else {
            return FastMath.exp(-FastMath.PI * bigKPrime(m) / bigK(m));
        }
    }

    /** Get the nome q.
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @param <T> the type of the field elements
     * @return nome q
     */
    public static <T extends CalculusFieldElement<T>> T nome(final T m) {
        final T one = m.getField().getOne();
        if (m.norm() < 100 * one.ulp().getReal()) {
            // first terms of infinite series in Abramowitz and Stegun 17.3.21
            final T m16 = m.multiply(0.0625);
            return m16.multiply(m16.multiply(8).add(1));
        } else {
            return FastMath.exp(bigKPrime(m).divide(bigK(m)).multiply(one.getPi().negate()));
        }
    }

    /** Get the complete elliptic integral of the first kind K(m).
     * <p>
     * The complete elliptic integral of the first kind K(m) is
     * \[
     *    \int_0^{\frac{\pi}{2}} \frac{d\theta}{\sqrt{1-m \sin^2\theta}}
     * \]
     * it corresponds to the real quarter-period of Jacobi elliptic functions
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @return complete elliptic integral of the first kind K(m)
     * @see #bigKPrime(double)
     * @see #bigF(double, double)
     * @see <a href="https://mathworld.wolfram.com/CompleteEllipticIntegraloftheFirstKind.html">Complete Elliptic Integrals of the First Kind (MathWorld)</a>
     * @see <a href="https://en.wikipedia.org/wiki/Elliptic_integral">Elliptic Integrals (Wikipedia)</a>
     */
    public static double bigK(final double m) {
        if (m < 1.0e-8) {
            // first terms of infinite series in Abramowitz and Stegun 17.3.11
            return (1 + 0.25 * m) * MathUtils.SEMI_PI;
        } else {
            return CarlsonEllipticIntegral.rF(0, 1.0 - m, 1);
        }
    }

    /** Get the complete elliptic integral of the first kind K(m).
     * <p>
     * The complete elliptic integral of the first kind K(m) is
     * \[
     *    \int_0^{\frac{\pi}{2}} \frac{d\theta}{\sqrt{1-m \sin^2\theta}}
     * \]
     * it corresponds to the real quarter-period of Jacobi elliptic functions
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @param <T> the type of the field elements
     * @return complete elliptic integral of the first kind K(m)
     * @see #bigKPrime(CalculusFieldElement)
     * @see #bigF(CalculusFieldElement, CalculusFieldElement)
     * @see <a href="https://mathworld.wolfram.com/CompleteEllipticIntegraloftheFirstKind.html">Complete Elliptic Integrals of the First Kind (MathWorld)</a>
     * @see <a href="https://en.wikipedia.org/wiki/Elliptic_integral">Elliptic Integrals (Wikipedia)</a>
     */
    public static <T extends CalculusFieldElement<T>> T bigK(final T m) {
        final T zero = m.getField().getZero();
        final T one  = m.getField().getOne();
        if (m.norm() < 1.0e7 * one.ulp().getReal()) {

            // first terms of infinite series in Abramowitz and Stegun 17.3.11
            return one.add(m.multiply(0.25)).multiply(zero.getPi().multiply(0.5));

        } else {
            return CarlsonEllipticIntegral.rF(zero, one.subtract(m), one);
        }
    }

    /** Get the complete elliptic integral of the first kind K(m).
     * <p>
     * The complete elliptic integral of the first kind K(m) is
     * \[
     *    \int_0^{\frac{\pi}{2}} \frac{d\theta}{\sqrt{1-m \sin^2\theta}}
     * \]
     * it corresponds to the real quarter-period of Jacobi elliptic functions
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @return complete elliptic integral of the first kind K(m)
     * @see #bigKPrime(Complex)
     * @see #bigF(Complex, Complex)
     * @see <a href="https://mathworld.wolfram.com/CompleteEllipticIntegraloftheFirstKind.html">Complete Elliptic Integrals of the First Kind (MathWorld)</a>
     * @see <a href="https://en.wikipedia.org/wiki/Elliptic_integral">Elliptic Integrals (Wikipedia)</a>
     */
    public static Complex bigK(final Complex m) {
        if (m.norm() < 1.0e-8) {
            // first terms of infinite series in Abramowitz and Stegun 17.3.11
            return Complex.ONE.add(m.multiply(0.25)).multiply(MathUtils.SEMI_PI);
        } else {
            return CarlsonEllipticIntegral.rF(Complex.ZERO, Complex.ONE.subtract(m), Complex.ONE);
        }
    }

    /** Get the complete elliptic integral of the first kind K(m).
     * <p>
     * The complete elliptic integral of the first kind K(m) is
     * \[
     *    \int_0^{\frac{\pi}{2}} \frac{d\theta}{\sqrt{1-m \sin^2\theta}}
     * \]
     * it corresponds to the real quarter-period of Jacobi elliptic functions
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @param <T> the type of the field elements
     * @return complete elliptic integral of the first kind K(m)
     * @see #bigKPrime(FieldComplex)
     * @see #bigF(FieldComplex, FieldComplex)
     * @see <a href="https://mathworld.wolfram.com/CompleteEllipticIntegraloftheFirstKind.html">Complete Elliptic Integrals of the First Kind (MathWorld)</a>
     * @see <a href="https://en.wikipedia.org/wiki/Elliptic_integral">Elliptic Integrals (Wikipedia)</a>
     */
    public static <T extends CalculusFieldElement<T>> FieldComplex<T> bigK(final FieldComplex<T> m) {
        final FieldComplex<T> zero = m.getField().getZero();
        final FieldComplex<T> one  = m.getField().getOne();
        if (m.norm() < 1.0e7 * one.ulp().getReal()) {

            // first terms of infinite series in Abramowitz and Stegun 17.3.11
            return one.add(m.multiply(0.25)).multiply(zero.getPi().multiply(0.5));

        } else {
            return CarlsonEllipticIntegral.rF(zero, one.subtract(m), one);
        }
    }

    /** Get the complete elliptic integral of the first kind K'(m).
     * <p>
     * The complete elliptic integral of the first kind K'(m) is
     * \[
     *    \int_0^{\frac{\pi}{2}} \frac{d\theta}{\sqrt{1-(1-m) \sin^2\theta}}
     * \]
     * it corresponds to the imaginary quarter-period of Jacobi elliptic functions
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @return complete elliptic integral of the first kind K'(m)
     * @see #bigK(double)
     * @see <a href="https://mathworld.wolfram.com/CompleteEllipticIntegraloftheFirstKind.html">Complete Elliptic Integrals of the First Kind (MathWorld)</a>
     * @see <a href="https://en.wikipedia.org/wiki/Elliptic_integral">Elliptic Integrals (Wikipedia)</a>
     */
    public static double bigKPrime(final double m) {
        return CarlsonEllipticIntegral.rF(0, m, 1);
    }

    /** Get the complete elliptic integral of the first kind K'(m).
     * <p>
     * The complete elliptic integral of the first kind K'(m) is
     * \[
     *    \int_0^{\frac{\pi}{2}} \frac{d\theta}{\sqrt{1-(1-m) \sin^2\theta}}
     * \]
     * it corresponds to the imaginary quarter-period of Jacobi elliptic functions
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @param <T> the type of the field elements
     * @return complete elliptic integral of the first kind K'(m)
     * @see #bigK(CalculusFieldElement)
     * @see <a href="https://mathworld.wolfram.com/CompleteEllipticIntegraloftheFirstKind.html">Complete Elliptic Integrals of the First Kind (MathWorld)</a>
     * @see <a href="https://en.wikipedia.org/wiki/Elliptic_integral">Elliptic Integrals (Wikipedia)</a>
     */
    public static <T extends CalculusFieldElement<T>> T bigKPrime(final T m) {
        final T zero = m.getField().getZero();
        final T one  = m.getField().getOne();
        return CarlsonEllipticIntegral.rF(zero, m, one);
    }

    /** Get the complete elliptic integral of the first kind K'(m).
     * <p>
     * The complete elliptic integral of the first kind K'(m) is
     * \[
     *    \int_0^{\frac{\pi}{2}} \frac{d\theta}{\sqrt{1-(1-m) \sin^2\theta}}
     * \]
     * it corresponds to the imaginary quarter-period of Jacobi elliptic functions
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @return complete elliptic integral of the first kind K'(m)
     * @see #bigK(Complex)
     * @see <a href="https://mathworld.wolfram.com/CompleteEllipticIntegraloftheFirstKind.html">Complete Elliptic Integrals of the First Kind (MathWorld)</a>
     * @see <a href="https://en.wikipedia.org/wiki/Elliptic_integral">Elliptic Integrals (Wikipedia)</a>
     */
    public static Complex bigKPrime(final Complex m) {
        return CarlsonEllipticIntegral.rF(Complex.ZERO, m, Complex.ONE);
    }

    /** Get the complete elliptic integral of the first kind K'(m).
     * <p>
     * The complete elliptic integral of the first kind K'(m) is
     * \[
     *    \int_0^{\frac{\pi}{2}} \frac{d\theta}{\sqrt{1-(1-m) \sin^2\theta}}
     * \]
     * it corresponds to the imaginary quarter-period of Jacobi elliptic functions
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @param <T> the type of the field elements
     * @return complete elliptic integral of the first kind K'(m)
     * @see #bigK(FieldComplex)
     * @see <a href="https://mathworld.wolfram.com/CompleteEllipticIntegraloftheFirstKind.html">Complete Elliptic Integrals of the First Kind (MathWorld)</a>
     * @see <a href="https://en.wikipedia.org/wiki/Elliptic_integral">Elliptic Integrals (Wikipedia)</a>
     */
    public static <T extends CalculusFieldElement<T>> FieldComplex<T> bigKPrime(final FieldComplex<T> m) {
        final FieldComplex<T> zero = m.getField().getZero();
        final FieldComplex<T> one  = m.getField().getOne();
        return CarlsonEllipticIntegral.rF(zero, m, one);
    }

    /** Get the complete elliptic integral of the second kind E(m).
     * <p>
     * The complete elliptic integral of the second kind E(m) is
     * \[
     *    \int_0^{\frac{\pi}{2}} \sqrt{1-m \sin^2\theta} d\theta
     * \]
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @return complete elliptic integral of the second kind E(m)
     * @see #bigE(double, double)
     * @see <a href="https://mathworld.wolfram.com/CompleteEllipticIntegraloftheSecondKind.html">Complete Elliptic Integrals of the Second Kind (MathWorld)</a>
     * @see <a href="https://en.wikipedia.org/wiki/Elliptic_integral">Elliptic Integrals (Wikipedia)</a>
     */
    public static double bigE(final double m) {
        return CarlsonEllipticIntegral.rG(0, 1 - m, 1) * 2;
    }

    /** Get the complete elliptic integral of the second kind E(m).
     * <p>
     * The complete elliptic integral of the second kind E(m) is
     * \[
     *    \int_0^{\frac{\pi}{2}} \sqrt{1-m \sin^2\theta} d\theta
     * \]
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @param <T> the type of the field elements
     * @return complete elliptic integral of the second kind E(m)
     * @see #bigE(CalculusFieldElement, CalculusFieldElement)
     * @see <a href="https://mathworld.wolfram.com/CompleteEllipticIntegraloftheSecondKind.html">Complete Elliptic Integrals of the Second Kind (MathWorld)</a>
     * @see <a href="https://en.wikipedia.org/wiki/Elliptic_integral">Elliptic Integrals (Wikipedia)</a>
     */
    public static <T extends CalculusFieldElement<T>> T bigE(final T m) {
        final T zero = m.getField().getZero();
        final T one  = m.getField().getOne();
        return CarlsonEllipticIntegral.rG(zero, one.subtract(m), one).multiply(2);
    }

    /** Get the complete elliptic integral of the second kind E(m).
     * <p>
     * The complete elliptic integral of the second kind E(m) is
     * \[
     *    \int_0^{\frac{\pi}{2}} \sqrt{1-m \sin^2\theta} d\theta
     * \]
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @return complete elliptic integral of the second kind E(m)
     * @see #bigE(Complex, Complex)
     * @see <a href="https://mathworld.wolfram.com/CompleteEllipticIntegraloftheSecondKind.html">Complete Elliptic Integrals of the Second Kind (MathWorld)</a>
     * @see <a href="https://en.wikipedia.org/wiki/Elliptic_integral">Elliptic Integrals (Wikipedia)</a>
     */
    public static Complex bigE(final Complex m) {
        return CarlsonEllipticIntegral.rG(Complex.ZERO,
                                          Complex.ONE.subtract(m),
                                          Complex.ONE).multiply(2);
    }

    /** Get the complete elliptic integral of the second kind E(m).
     * <p>
     * The complete elliptic integral of the second kind E(m) is
     * \[
     *    \int_0^{\frac{\pi}{2}} \sqrt{1-m \sin^2\theta} d\theta
     * \]
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @param <T> the type of the field elements
     * @return complete elliptic integral of the second kind E(m)
     * @see #bigE(FieldComplex, FieldComplex)
     * @see <a href="https://mathworld.wolfram.com/CompleteEllipticIntegraloftheSecondKind.html">Complete Elliptic Integrals of the Second Kind (MathWorld)</a>
     * @see <a href="https://en.wikipedia.org/wiki/Elliptic_integral">Elliptic Integrals (Wikipedia)</a>
     */
    public static <T extends CalculusFieldElement<T>> FieldComplex<T> bigE(final FieldComplex<T> m) {
        final FieldComplex<T> zero = m.getField().getZero();
        final FieldComplex<T> one  = m.getField().getOne();
        return CarlsonEllipticIntegral.rG(zero, one.subtract(m), one).multiply(2);
    }

    /** Get the complete elliptic integral D(m) = [K(m) - E(m)]/m.
     * <p>
     * The complete elliptic integral D(m) is
     * \[
     *    \int_0^{\frac{\pi}{2}} \frac{\sin^2\theta}{\sqrt{1-m \sin^2\theta}} d\theta
     * \]
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @return complete elliptic integral D(m)
     * @see #bigD(double, double)
     */
    public static double bigD(final double m) {
        return CarlsonEllipticIntegral.rD(0, 1 - m, 1) / 3;
    }

    /** Get the complete elliptic integral D(m) = [K(m) - E(m)]/m.
     * <p>
     * The complete elliptic integral D(m) is
     * \[
     *    \int_0^{\frac{\pi}{2}} \frac{\sin^2\theta}{\sqrt{1-m \sin^2\theta}} d\theta
     * \]
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @param <T> the type of the field elements
     * @return complete elliptic integral D(m)
     * @see #bigD(CalculusFieldElement, CalculusFieldElement)
     */
    public static <T extends CalculusFieldElement<T>> T bigD(final T m) {
        final T zero = m.getField().getZero();
        final T one  = m.getField().getOne();
        return CarlsonEllipticIntegral.rD(zero, one.subtract(m), one).divide(3);
    }

    /** Get the complete elliptic integral D(m) = [K(m) - E(m)]/m.
     * <p>
     * The complete elliptic integral D(m) is
     * \[
     *    \int_0^{\frac{\pi}{2}} \frac{\sin^2\theta}{\sqrt{1-m \sin^2\theta}} d\theta
     * \]
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @return complete elliptic integral D(m)
     * @see #bigD(Complex, Complex)
     */
    public static Complex bigD(final Complex m) {
        return CarlsonEllipticIntegral.rD(Complex.ZERO, Complex.ONE.subtract(m), Complex.ONE).divide(3);
    }

    /** Get the complete elliptic integral D(m) = [K(m) - E(m)]/m.
     * <p>
     * The complete elliptic integral D(m) is
     * \[
     *    \int_0^{\frac{\pi}{2}} \frac{\sin^2\theta}{\sqrt{1-m \sin^2\theta}} d\theta
     * \]
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @param <T> the type of the field elements
     * @return complete elliptic integral D(m)
     * @see #bigD(FieldComplex, FieldComplex)
     */
    public static <T extends CalculusFieldElement<T>> FieldComplex<T> bigD(final FieldComplex<T> m) {
        final FieldComplex<T> zero = m.getField().getZero();
        final FieldComplex<T> one  = m.getField().getOne();
        return CarlsonEllipticIntegral.rD(zero, one.subtract(m), one).divide(3);
    }

    /** Get the complete elliptic integral of the third kind Π(α², m).
     * <p>
     * The complete elliptic integral of the third kind Π(α², m) is
     * \[
     *    \int_0^{\frac{\pi}{2}} \frac{d\theta}{\sqrt{1-m \sin^2\theta}(1-\alpha^2 \sin^2\theta)}
     * \]
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param alpha2 α² parameter (already squared)
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @return complete elliptic integral of the third kind Π(α², m)
     * @see #bigPi(double, double, double)
     * @see <a href="https://mathworld.wolfram.com/EllipticIntegraloftheThirdKind.html">Elliptic Integrals of the Third Kind (MathWorld)</a>
     * @see <a href="https://en.wikipedia.org/wiki/Elliptic_integral">Elliptic Integrals (Wikipedia)</a>
     */
    public static double bigPi(final double alpha2, final double m) {
        final double kPrime2 = 1 - m;
        return CarlsonEllipticIntegral.rF(0, kPrime2, 1) +
               CarlsonEllipticIntegral.rJ(0, kPrime2, 1, 1 - alpha2) * alpha2 / 3;
    }

    /** Get the complete elliptic integral of the third kind Π(α², m).
     * <p>
     * The complete elliptic integral of the third kind Π(α², m) is
     * \[
     *    \int_0^{\frac{\pi}{2}} \frac{d\theta}{\sqrt{1-m \sin^2\theta}(1-\alpha^2 \sin^2\theta)}
     * \]
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param alpha2 α² parameter (already squared)
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @param <T> the type of the field elements
     * @return complete elliptic integral of the third kind Π(α², m)
     * @see #bigPi(CalculusFieldElement, CalculusFieldElement, CalculusFieldElement)
     * @see <a href="https://mathworld.wolfram.com/EllipticIntegraloftheThirdKind.html">Elliptic Integrals of the Third Kind (MathWorld)</a>
     * @see <a href="https://en.wikipedia.org/wiki/Elliptic_integral">Elliptic Integrals (Wikipedia)</a>
     */
    public static <T extends CalculusFieldElement<T>> T bigPi(final T alpha2, final T m) {
        final T zero    = m.getField().getZero();
        final T one     = m.getField().getOne();
        final T kPrime2 = one.subtract(m);
        return CarlsonEllipticIntegral.rF(zero, kPrime2, one).
               add(CarlsonEllipticIntegral.rJ(zero, kPrime2, one, one.subtract(alpha2)).multiply(alpha2).divide(3));
    }

    /** Get the complete elliptic integral of the third kind Π(α², m).
     * <p>
     * The complete elliptic integral of the third kind Π(α², m) is
     * \[
     *    \int_0^{\frac{\pi}{2}} \frac{d\theta}{\sqrt{1-m \sin^2\theta}(1-\alpha^2 \sin^2\theta)}
     * \]
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param alpha2 α² parameter (already squared)
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @return complete elliptic integral of the third kind Π(α², m)
     * @see #bigPi(Complex, Complex, Complex)
     * @see <a href="https://mathworld.wolfram.com/EllipticIntegraloftheThirdKind.html">Elliptic Integrals of the Third Kind (MathWorld)</a>
     * @see <a href="https://en.wikipedia.org/wiki/Elliptic_integral">Elliptic Integrals (Wikipedia)</a>
     */
    public static Complex bigPi(final Complex alpha2, final Complex m) {
        final Complex kPrime2 = Complex.ONE.subtract(m);
        return CarlsonEllipticIntegral.rF(Complex.ZERO, kPrime2, Complex.ONE).
               add(CarlsonEllipticIntegral.rJ(Complex.ZERO, kPrime2, Complex.ONE, Complex.ONE.subtract(alpha2)).multiply(alpha2).divide(3));
    }

    /** Get the complete elliptic integral of the third kind Π(α², m).
     * <p>
     * The complete elliptic integral of the third kind Π(α², m) is
     * \[
     *    \int_0^{\frac{\pi}{2}} \frac{d\theta}{\sqrt{1-m \sin^2\theta}(1-\alpha^2 \sin^2\theta)}
     * \]
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param alpha2 α² parameter (already squared)
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @param <T> the type of the field elements
     * @return complete elliptic integral of the third kind Π(α², m)
     * @see #bigPi(FieldComplex, FieldComplex, FieldComplex)
     * @see <a href="https://mathworld.wolfram.com/EllipticIntegraloftheThirdKind.html">Elliptic Integrals of the Third Kind (MathWorld)</a>
     * @see <a href="https://en.wikipedia.org/wiki/Elliptic_integral">Elliptic Integrals (Wikipedia)</a>
     */
    public static <T extends CalculusFieldElement<T>> FieldComplex<T> bigPi(final FieldComplex<T> alpha2, final FieldComplex<T> m) {
        final FieldComplex<T> zero = m.getField().getZero();
        final FieldComplex<T> one  = m.getField().getOne();
        final FieldComplex<T> kPrime2 = one.subtract(m);
        return CarlsonEllipticIntegral.rF(zero, kPrime2, one).
               add(CarlsonEllipticIntegral.rJ(zero, kPrime2, one, one.subtract(alpha2)).multiply(alpha2).divide(3));
    }

    /** Get the incomplete elliptic integral of the first kind F(Φ, m).
     * <p>
     * The incomplete elliptic integral of the first kind F(Φ, m) is
     * \[
     *    \int_0^{\phi} \frac{d\theta}{\sqrt{1-m \sin^2\theta}}
     * \]
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param phi amplitude (i.e. upper bound of the integral)
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @return incomplete elliptic integral of the first kind F(Φ, m)
     * @see #bigK(double)
     * @see <a href="https://mathworld.wolfram.com/EllipticIntegraloftheFirstKind.html">Elliptic Integrals of the First Kind (MathWorld)</a>
     * @see <a href="https://en.wikipedia.org/wiki/Elliptic_integral">Elliptic Integrals (Wikipedia)</a>
     */
    public static double bigF(final double phi, final double m) {
        final double csc = 1.0 / FastMath.sin(phi);
        final double c   = csc * csc;
        final double cM1 = c - 1.0;
        final double cMm = c - m;
        return CarlsonEllipticIntegral.rF(cM1, cMm, c);
    }

    /** Get the incomplete elliptic integral of the first kind F(Φ, m).
     * <p>
     * The incomplete elliptic integral of the first kind F(Φ, m) is
     * \[
     *    \int_0^{\phi} \frac{d\theta}{\sqrt{1-m \sin^2\theta}}
     * \]
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param phi amplitude (i.e. upper bound of the integral)
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @param <T> the type of the field elements
     * @return incomplete elliptic integral of the first kind F(Φ, m)
     * @see #bigK(CalculusFieldElement)
     * @see <a href="https://mathworld.wolfram.com/EllipticIntegraloftheFirstKind.html">Elliptic Integrals of the First Kind (MathWorld)</a>
     * @see <a href="https://en.wikipedia.org/wiki/Elliptic_integral">Elliptic Integrals (Wikipedia)</a>
     */
    public static <T extends CalculusFieldElement<T>> T bigF(final T phi, final T m) {
        final T one = m.getField().getOne();
        final T csc = FastMath.sin(phi).reciprocal();
        final T c   = csc.multiply(csc);
        final T cM1 = c.subtract(one);
        final T cMm = c.subtract(m);
        return CarlsonEllipticIntegral.rF(cM1, cMm, c);
    }

    /** Get the incomplete elliptic integral of the first kind F(Φ, m).
     * <p>
     * The incomplete elliptic integral of the first kind F(Φ, m) is
     * \[
     *    \int_0^{\phi} \frac{d\theta}{\sqrt{1-m \sin^2\theta}}
     * \]
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param phi amplitude (i.e. upper bound of the integral)
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @return incomplete elliptic integral of the first kind F(Φ, m)
     * @see #bigK(Complex)
     * @see <a href="https://mathworld.wolfram.com/EllipticIntegraloftheFirstKind.html">Elliptic Integrals of the First Kind (MathWorld)</a>
     * @see <a href="https://en.wikipedia.org/wiki/Elliptic_integral">Elliptic Integrals (Wikipedia)</a>
     */
    public static Complex bigF(final Complex phi, final Complex m) {
        final Complex csc = FastMath.sin(phi).reciprocal();
        final Complex c   = csc.multiply(csc);
        final Complex cM1 = c.subtract(Complex.ONE);
        final Complex cMm = c.subtract(m);
        return CarlsonEllipticIntegral.rF(cM1, cMm, c);
    }

    /** Get the incomplete elliptic integral of the first kind F(Φ, m).
     * <p>
     * The incomplete elliptic integral of the first kind F(Φ, m) is
     * \[
     *    \int_0^{\phi} \frac{d\theta}{\sqrt{1-m \sin^2\theta}}
     * \]
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param phi amplitude (i.e. upper bound of the integral)
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @param <T> the type of the field elements
     * @return incomplete elliptic integral of the first kind F(Φ, m)
     * @see #bigK(CalculusFieldElement)
     * @see <a href="https://mathworld.wolfram.com/EllipticIntegraloftheFirstKind.html">Elliptic Integrals of the First Kind (MathWorld)</a>
     * @see <a href="https://en.wikipedia.org/wiki/Elliptic_integral">Elliptic Integrals (Wikipedia)</a>
     */
    public static <T extends CalculusFieldElement<T>> FieldComplex<T> bigF(final FieldComplex<T> phi, final FieldComplex<T> m) {
        final FieldComplex<T> one = m.getField().getOne();
        final FieldComplex<T> csc = FastMath.sin(phi).reciprocal();
        final FieldComplex<T> c   = csc.multiply(csc);
        final FieldComplex<T> cM1 = c.subtract(one);
        final FieldComplex<T> cMm = c.subtract(m);
        return CarlsonEllipticIntegral.rF(cM1, cMm, c);
    }

    /** Get the incomplete elliptic integral of the second kind E(Φ, m).
     * <p>
     * The incomplete elliptic integral of the second kind E(Φ, m) is
     * \[
     *    \int_0^{\phi} \sqrt{1-m \sin^2\theta} d\theta
     * \]
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param phi amplitude (i.e. upper bound of the integral)
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @return incomplete elliptic integral of the second kind E(Φ, m)
     * @see #bigE(double)
     * @see <a href="https://mathworld.wolfram.com/EllipticIntegraloftheSecondKind.html">Elliptic Integrals of the Second Kind (MathWorld)</a>
     * @see <a href="https://en.wikipedia.org/wiki/Elliptic_integral">Elliptic Integrals (Wikipedia)</a>
     */
    public static double bigE(final double phi, final double m) {
        final double csc = 1.0 / FastMath.sin(phi);
        final double c   = csc * csc;
        final double cM1 = c - 1.0;
        final double cMm = c - m;
        return CarlsonEllipticIntegral.rF(cM1, cMm, c) -
               CarlsonEllipticIntegral.rD(cM1, cMm, c) * (m / 3);
    }

    /** Get the incomplete elliptic integral of the second kind E(Φ, m).
     * <p>
     * The incomplete elliptic integral of the second kind E(Φ, m) is
     * \[
     *    \int_0^{\phi} \sqrt{1-m \sin^2\theta} d\theta
     * \]
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param phi amplitude (i.e. upper bound of the integral)
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @param <T> the type of the field elements
     * @return incomplete elliptic integral of the second kind E(Φ, m)
     * @see #bigE(CalculusFieldElement)
     * @see <a href="https://mathworld.wolfram.com/EllipticIntegraloftheSecondKind.html">Elliptic Integrals of the Second Kind (MathWorld)</a>
     * @see <a href="https://en.wikipedia.org/wiki/Elliptic_integral">Elliptic Integrals (Wikipedia)</a>
     */
    public static <T extends CalculusFieldElement<T>> T bigE(final T phi, final T m) {
        final T one = m.getField().getOne();
        final T csc = FastMath.sin(phi).reciprocal();
        final T c   = csc.multiply(csc);
        final T cM1 = c.subtract(one);
        final T cMm = c.subtract(m);
        return CarlsonEllipticIntegral.rF(cM1, cMm, c).
               subtract(CarlsonEllipticIntegral.rD(cM1, cMm, c).multiply(m.divide(3)));
    }

    /** Get the incomplete elliptic integral of the second kind E(Φ, m).
     * <p>
     * The incomplete elliptic integral of the second kind E(Φ, m) is
     * \[
     *    \int_0^{\phi} \sqrt{1-m \sin^2\theta} d\theta
     * \]
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param phi amplitude (i.e. upper bound of the integral)
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @return incomplete elliptic integral of the second kind E(Φ, m)
     * @see #bigE(Complex)
     * @see <a href="https://mathworld.wolfram.com/EllipticIntegraloftheSecondKind.html">Elliptic Integrals of the Second Kind (MathWorld)</a>
     * @see <a href="https://en.wikipedia.org/wiki/Elliptic_integral">Elliptic Integrals (Wikipedia)</a>
     */
    public static Complex bigE(final Complex phi, final Complex m) {
        final Complex csc = FastMath.sin(phi).reciprocal();
        final Complex c   = csc.multiply(csc);
        final Complex cM1 = c.subtract(Complex.ONE);
        final Complex cMm = c.subtract(m);
        return CarlsonEllipticIntegral.rF(cM1, cMm, c).
               subtract(CarlsonEllipticIntegral.rD(cM1, cMm, c).multiply(m.divide(3)));
    }

    /** Get the incomplete elliptic integral of the second kind E(Φ, m).
     * <p>
     * The incomplete elliptic integral of the second kind E(Φ, m) is
     * \[
     *    \int_0^{\phi} \sqrt{1-m \sin^2\theta} d\theta
     * \]
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param phi amplitude (i.e. upper bound of the integral)
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @param <T> the type of the field elements
     * @return incomplete elliptic integral of the second kind E(Φ, m)
     * @see #bigE(FieldComplex)
     * @see <a href="https://mathworld.wolfram.com/EllipticIntegraloftheSecondKind.html">Elliptic Integrals of the Second Kind (MathWorld)</a>
     * @see <a href="https://en.wikipedia.org/wiki/Elliptic_integral">Elliptic Integrals (Wikipedia)</a>
     */
    public static <T extends CalculusFieldElement<T>> FieldComplex<T> bigE(final FieldComplex<T> phi, final FieldComplex<T> m) {
        final FieldComplex<T> one = m.getField().getOne();
        final FieldComplex<T> csc = FastMath.sin(phi).reciprocal();
        final FieldComplex<T> c   = csc.multiply(csc);
        final FieldComplex<T> cM1 = c.subtract(one);
        final FieldComplex<T> cMm = c.subtract(m);
        return CarlsonEllipticIntegral.rF(cM1, cMm, c).
               subtract(CarlsonEllipticIntegral.rD(cM1, cMm, c).multiply(m.divide(3)));
    }

    /** Get the incomplete elliptic integral D(Φ, m) = [F(Φ, m) - E(Φ, m)]/m.
     * <p>
     * The incomplete elliptic integral D(Φ, m) is
     * \[
     *    \int_0^{\phi} \frac{\sin^2\theta}{\sqrt{1-m \sin^2\theta}} d\theta
     * \]
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param phi amplitude (i.e. upper bound of the integral)
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @return incomplete elliptic integral D(Φ, m)
     * @see #bigD(double)
     */
    public static double bigD(final double phi, final double m) {
        final double csc = 1.0 / FastMath.sin(phi);
        final double c   = csc * csc;
        final double cM1 = c - 1.0;
        final double cMm = c - m;
        return CarlsonEllipticIntegral.rD(cM1, cMm, 1) / 3;
    }

    /** Get the incomplete elliptic integral D(Φ, m) = [F(Φ, m) - E(Φ, m)]/m.
     * <p>
     * The incomplete elliptic integral D(Φ, m) is
     * \[
     *    \int_0^{\phi} \frac{\sin^2\theta}{\sqrt{1-m \sin^2\theta}} d\theta
     * \]
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param phi amplitude (i.e. upper bound of the integral)
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @param <T> the type of the field elements
     * @return incomplete elliptic integral D(Φ, m)
     * @see #bigD(CalculusFieldElement)
     */
    public static <T extends CalculusFieldElement<T>> T bigD(final T phi, final T m) {
        final T one = m.getField().getOne();
        final T csc = FastMath.sin(phi).reciprocal();
        final T c   = csc.multiply(csc);
        final T cM1 = c.subtract(one);
        final T cMm = c.subtract(m);
        return CarlsonEllipticIntegral.rD(cM1, cMm, one).divide(3);
    }

    /** Get the incomplete elliptic integral D(Φ, m) = [F(Φ, m) - E(Φ, m)]/m.
     * <p>
     * The incomplete elliptic integral D(Φ, m) is
     * \[
     *    \int_0^{\phi} \frac{\sin^2\theta}{\sqrt{1-m \sin^2\theta}} d\theta
     * \]
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param phi amplitude (i.e. upper bound of the integral)
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @return incomplete elliptic integral D(Φ, m)
     * @see #bigD(Complex)
     */
    public static Complex bigD(final Complex phi, final Complex m) {
        final Complex csc = FastMath.sin(phi).reciprocal();
        final Complex c   = csc.multiply(csc);
        final Complex cM1 = c.subtract(Complex.ONE);
        final Complex cMm = c.subtract(m);
        return CarlsonEllipticIntegral.rD(cM1, cMm, Complex.ONE).divide(3);
    }

    /** Get the incomplete elliptic integral D(Φ, m) = [F(Φ, m) - E(Φ, m)]/m.
     * <p>
     * The incomplete elliptic integral D(Φ, m) is
     * \[
     *    \int_0^{\phi} \frac{\sin^2\theta}{\sqrt{1-m \sin^2\theta}} d\theta
     * \]
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param phi amplitude (i.e. upper bound of the integral)
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @param <T> the type of the field elements
     * @return incomplete elliptic integral D(Φ, m)
     * @see #bigD(CalculusFieldElement)
     */
    public static <T extends CalculusFieldElement<T>> FieldComplex<T> bigD(final FieldComplex<T> phi, final FieldComplex<T> m) {
        final FieldComplex<T> one = m.getField().getOne();
        final FieldComplex<T> csc = FastMath.sin(phi).reciprocal();
        final FieldComplex<T> c   = csc.multiply(csc);
        final FieldComplex<T> cM1 = c.subtract(one);
        final FieldComplex<T> cMm = c.subtract(m);
        return CarlsonEllipticIntegral.rD(cM1, cMm, one).divide(3);
    }

    /** Get the incomplete elliptic integral of the third kind Π(Φ, α², m).
     * <p>
     * The incomplete elliptic integral of the third kind Π(Φ, α², m) is
     * \[
     *    \int_0^{\phi} \frac{d\theta}{\sqrt{1-m \sin^2\theta}(1-\alpha^2 \sin^2\theta)}
     * \]
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param phi amplitude (i.e. upper bound of the integral)
     * @param alpha2 α² parameter (already squared)
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @return incomplete elliptic integral of the third kind Π(Φ, α², m)
     * @see #bigPi(double, double)
     * @see <a href="https://mathworld.wolfram.com/EllipticIntegraloftheThirdKind.html">Elliptic Integrals of the Third Kind (MathWorld)</a>
     * @see <a href="https://en.wikipedia.org/wiki/Elliptic_integral">Elliptic Integrals (Wikipedia)</a>
     */
    public static double bigPi(final double phi, final double alpha2, final double m) {
        final double csc  = 1.0 / FastMath.sin(phi);
        final double c    = csc * csc;
        final double cM1  = c - 1.0;
        final double cMm  = c - m;
        final double cMa2 = c - alpha2;
        return bigF(phi, m) +
               CarlsonEllipticIntegral.rJ(cM1, cMm, c, cMa2) * alpha2 / 3;
    }

    /** Get the incomplete elliptic integral of the third kind Π(Φ, α², m).
     * <p>
     * The incomplete elliptic integral of the third kind Π(Φ, α², m) is
     * \[
     *    \int_0^{\phi} \frac{d\theta}{\sqrt{1-m \sin^2\theta}(1-\alpha^2 \sin^2\theta)}
     * \]
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param phi amplitude (i.e. upper bound of the integral)
     * @param alpha2 α² parameter (already squared)
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @param <T> the type of the field elements
     * @return incomplete elliptic integral of the third kind Π(Φ, α², m)
     * @see #bigPi(CalculusFieldElement, CalculusFieldElement)
     * @see <a href="https://mathworld.wolfram.com/EllipticIntegraloftheThirdKind.html">Elliptic Integrals of the Third Kind (MathWorld)</a>
     * @see <a href="https://en.wikipedia.org/wiki/Elliptic_integral">Elliptic Integrals (Wikipedia)</a>
     */
    public static <T extends CalculusFieldElement<T>> T bigPi(final T phi, final T alpha2, final T m) {
        final T one  = m.getField().getOne();
        final T csc  = FastMath.sin(phi).reciprocal();
        final T c    = csc.multiply(csc);
        final T cM1  = c.subtract(one);
        final T cMm  = c.subtract(m);
        final T cMa2 = c.subtract(alpha2);
        return bigF(phi, m).
               add(CarlsonEllipticIntegral.rJ(cM1, cMm, c, cMa2).multiply(alpha2).divide(3));
    }

    /** Get the incomplete elliptic integral of the third kind Π(Φ, α², m).
     * <p>
     * The incomplete elliptic integral of the third kind Π(Φ, α², m) is
     * \[
     *    \int_0^{\phi} \frac{d\theta}{\sqrt{1-m \sin^2\theta}(1-\alpha^2 \sin^2\theta)}
     * \]
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param phi amplitude (i.e. upper bound of the integral)
     * @param alpha2 α² parameter (already squared)
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @return incomplete elliptic integral of the third kind Π(Φ, α², m)
     * @see #bigPi(Complex, Complex)
     * @see <a href="https://mathworld.wolfram.com/EllipticIntegraloftheThirdKind.html">Elliptic Integrals of the Third Kind (MathWorld)</a>
     * @see <a href="https://en.wikipedia.org/wiki/Elliptic_integral">Elliptic Integrals (Wikipedia)</a>
     */
    public static Complex bigPi(final Complex phi, final Complex alpha2, final Complex m) {
        final Complex one  = m.getField().getOne();
        final Complex csc  = FastMath.sin(phi).reciprocal();
        final Complex c    = csc.multiply(csc);
        final Complex cM1  = c.subtract(one);
        final Complex cMm  = c.subtract(m);
        final Complex cMa2 = c.subtract(alpha2);
        return bigF(phi, m).
               add(CarlsonEllipticIntegral.rJ(cM1, cMm, c, cMa2).multiply(alpha2).divide(3));
    }

    /** Get the incomplete elliptic integral of the third kind Π(Φ, α², m).
     * <p>
     * The incomplete elliptic integral of the third kind Π(Φ, α², m) is
     * \[
     *    \int_0^{\phi} \frac{d\theta}{\sqrt{1-m \sin^2\theta}(1-\alpha^2 \sin^2\theta)}
     * \]
     * </p>
     * <p>
     * The algorithm for evaluating the functions is based on {@link CarlsonEllipticIntegral
     * Carlson elliptic integrals}.
     * </p>
     * @param phi amplitude (i.e. upper bound of the integral)
     * @param alpha2 α² parameter (already squared)
     * @param m parameter (m=k² where k is the elliptic modulus)
     * @param <T> the type of the field elements
     * @return incomplete elliptic integral of the third kind Π(Φ, α², m)
     * @see #bigPi(FieldComplex, FieldComplex)
     * @see <a href="https://mathworld.wolfram.com/EllipticIntegraloftheThirdKind.html">Elliptic Integrals of the Third Kind (MathWorld)</a>
     * @see <a href="https://en.wikipedia.org/wiki/Elliptic_integral">Elliptic Integrals (Wikipedia)</a>
     */
    public static <T extends CalculusFieldElement<T>> FieldComplex<T> bigPi(final FieldComplex<T> phi,
                                                                            final FieldComplex<T> alpha2,
                                                                            final FieldComplex<T> m) {
        final FieldComplex<T> one  = m.getField().getOne();
        final FieldComplex<T> csc  = FastMath.sin(phi).reciprocal();
        final FieldComplex<T> c    = csc.multiply(csc);
        final FieldComplex<T> cM1  = c.subtract(one);
        final FieldComplex<T> cMm  = c.subtract(m);
        final FieldComplex<T> cMa2 = c.subtract(alpha2);
        return bigF(phi, m).
               add(CarlsonEllipticIntegral.rJ(cM1, cMm, c, cMa2).multiply(alpha2).divide(3));
    }

}
