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

package org.hipparchus.util;

import java.util.NoSuchElementException;

import org.hipparchus.exception.LocalizedCoreFormats;
import org.hipparchus.exception.MathIllegalArgumentException;

/**
 * Converter between unidimensional storage structure and multidimensional
 * conceptual structure.
 * This utility will convert from indices in a multidimensional structure
 * to the corresponding index in a one-dimensional array. For example,
 * assuming that the ranges (in 3 dimensions) of indices are 2, 4 and 3,
 * the following correspondences, between 3-tuples indices and unidimensional
 * indices, will hold:
 * <ul>
 *  <li>(0, 0, 0) corresponds to 0</li>
 *  <li>(0, 0, 1) corresponds to 1</li>
 *  <li>(0, 0, 2) corresponds to 2</li>
 *  <li>(0, 1, 0) corresponds to 3</li>
 *  <li>...</li>
 *  <li>(1, 0, 0) corresponds to 12</li>
 *  <li>...</li>
 *  <li>(1, 3, 2) corresponds to 23</li>
 * </ul>
 */
public class MultidimensionalCounter implements Iterable<Integer> {
    /**
     * Number of dimensions.
     */
    private final int dimension;
    /**
     * Offset for each dimension.
     */
    private final int[] uniCounterOffset;
    /**
     * Counter sizes.
     */
    private final int[] size;
    /**
     * Total number of (one-dimensional) slots.
     */
    private final int totalSize;
    /**
     * Index of last dimension.
     */
    private final int last;

    /**
     * Perform iteration over the multidimensional counter.
     */
    public class Iterator implements java.util.Iterator<Integer> {
        /**
         * Multidimensional counter.
         */
        private final int[] counter = new int[dimension];
        /**
         * Unidimensional counter.
         */
        private int count = -1;
        /**
         * Maximum value for {@link #count}.
         */
        private final int maxCount = totalSize - 1;

        /**
         * Create an iterator
         * @see #iterator()
         */
        Iterator() {
            counter[last] = -1;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {
            return count < maxCount;
        }

        /**
         * @return the unidimensional count after the counter has been
         * incremented by {@code 1}.
         * @throws NoSuchElementException if {@link #hasNext()} would have
         * returned {@code false}.
         */
        @Override
        public Integer next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            for (int i = last; i >= 0; i--) {
                if (counter[i] == size[i] - 1) {
                    counter[i] = 0;
                } else {
                    ++counter[i];
                    break;
                }
            }

            return ++count;
        }

        /**
         * Get the current unidimensional counter slot.
         *
         * @return the index within the unidimensionl counter.
         */
        public int getCount() {
            return count;
        }
        /**
         * Get the current multidimensional counter slots.
         *
         * @return the indices within the multidimensional counter.
         */
        public int[] getCounts() {
            return counter.clone();
        }

        /**
         * Get the current count in the selected dimension.
         *
         * @param dim Dimension index.
         * @return the count at the corresponding index for the current state
         * of the iterator.
         * @throws IndexOutOfBoundsException if {@code index} is not in the
         * correct interval (as defined by the length of the argument in the
         * {@link MultidimensionalCounter#MultidimensionalCounter(int[])
         * constructor of the enclosing class}).
         */
        public int getCount(int dim) {
            return counter[dim];
        }

        /** {@inheritDoc} */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Create a counter.
     *
     * @param size Counter sizes (number of slots in each dimension).
     * @throws MathIllegalArgumentException if one of the sizes is
     * negative or zero.
     */
    public MultidimensionalCounter(int... size) throws MathIllegalArgumentException {
        dimension = size.length;
        this.size = size.clone();

        uniCounterOffset = new int[dimension];

        last = dimension - 1;
        int tS = size[last];
        for (int i = 0; i < last; i++) {
            int count = 1;
            for (int j = i + 1; j < dimension; j++) {
                count *= size[j];
            }
            uniCounterOffset[i] = count;
            tS *= size[i];
        }
        uniCounterOffset[last] = 0;

        if (tS <= 0) {
            throw new MathIllegalArgumentException(LocalizedCoreFormats.NUMBER_TOO_SMALL_BOUND_EXCLUDED,
                                                   tS, 0);
        }

        totalSize = tS;
    }

    /**
     * Create an iterator over this counter.
     *
     * @return the iterator.
     */
    @Override
    public Iterator iterator() {
        return new Iterator();
    }

    /**
     * Get the number of dimensions of the multidimensional counter.
     *
     * @return the number of dimensions.
     */
    public int getDimension() {
        return dimension;
    }

    /**
     * Convert to multidimensional counter.
     *
     * @param index Index in unidimensional counter.
     * @return the multidimensional counts.
     * @throws MathIllegalArgumentException if {@code index} is not between
     * {@code 0} and the value returned by {@link #getSize()} (excluded).
     */
    public int[] getCounts(int index) throws MathIllegalArgumentException {
        MathUtils.checkRangeInclusive(index, 0, totalSize - 1);

        final int[] indices = new int[dimension];

        int count = 0;
        for (int i = 0; i < last; i++) {
            int idx = 0;
            final int offset = uniCounterOffset[i];
            while (count <= index) {
                count += offset;
                ++idx;
            }
            --idx;
            count -= offset;
            indices[i] = idx;
        }

        indices[last] = index - count;

        return indices;
    }

    /**
     * Convert to unidimensional counter.
     *
     * @param c Indices in multidimensional counter.
     * @return the index within the unidimensionl counter.
     * @throws MathIllegalArgumentException if the size of {@code c}
     * does not match the size of the array given in the constructor.
     * @throws MathIllegalArgumentException if a value of {@code c} is not in
     * the range of the corresponding dimension, as defined in the
     * {@link MultidimensionalCounter#MultidimensionalCounter(int...) constructor}.
     */
    public int getCount(int ... c) throws MathIllegalArgumentException {
        MathUtils.checkDimension(c.length, dimension);
        int count = 0;
        for (int i = 0; i < dimension; i++) {
            final int index = c[i];
            MathUtils.checkRangeInclusive(index, 0, size[i] - 1);
            count += uniCounterOffset[i] * c[i];
        }
        return count + c[last];
    }

    /**
     * Get the total number of elements.
     *
     * @return the total size of the unidimensional counter.
     */
    public int getSize() {
        return totalSize;
    }
    /**
     * Get the number of multidimensional counter slots in each dimension.
     *
     * @return the sizes of the multidimensional counter in each dimension.
     */
    public int[] getSizes() {
        return size.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dimension; i++) {
            sb.append('[').append(getCount(i)).append(']');
        }
        return sb.toString();
    }
}
