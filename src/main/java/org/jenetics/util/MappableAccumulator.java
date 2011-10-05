/*
 * Java Genetic Algorithm Library (@!identifier!@).
 * Copyright (c) @!year!@ Franz Wilhelmstötter
 *  
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Author:
 *     Franz Wilhelmstötter (franz.wilhelmstoetter@gmx.at)
 *     
 */
package org.jenetics.util;

import static org.jenetics.util.object.eq;
import static org.jenetics.util.object.hashCodeOf;
import static org.jenetics.util.object.nonNull;

/**
 * Abstract implementation of the {@link Accumulator} interface which defines a
 * {@code samples} property which is incremented by the {@link #accumulate(Object)}
 * method.
 * 
 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz Wilhelmstötter</a>
 * @version $Id$
 */
public abstract class MappableAccumulator<T> 
	implements 
		Accumulator<T>,
		Cloneable
{

	/**
	 * The number of accumulated samples.
	 */
	protected long _samples = 0;
	
	protected MappableAccumulator() {
	}
	
	/**
	 * Return the number of samples accumulated so far.
	 * 
	 * @return the number of samples accumulated so far.
	 */
	public long getSamples() {
		return _samples;
	}
	
	@Override
	public void accumulate(final T value) {
		++_samples;
	}
	
	/**
	 * Return a view of this adapter with a different type {@code B}.
	 * 
	 * Usage example:
	 * [code]
	 * // Convert a string on the fly into a double value.
	 * final Converter<String, Double> converter = new Converter<String, Double>() {
	 *         public Double convert(final String value) {
	 *             return Double.valueOf(value);
	 *         }
	 *     };
	 *     
	 * // The values to accumulate
	 * final List<String> values = Arrays.asList("0", "1", "2", "3", "4", "5");
	 *     
	 * // Min accumulator extends the AbstractAccumulator
	 * final Accumulators.Min<Double> accumulator = new Accumulators.Min<Double>();
	 *     
	 * // No pain to accumulate collections of a different type.
	 * Accumulators.accumulate(values, accumulator.map(converter));
	 * [/code]
	 * 
	 * @param <B> the type of the returned adapter (view).
	 * @param converter the converter needed to map between the type of this
	 *        adapter and the adapter view type.
	 * @return the adapter view with the different type.
	 * @throws NullPointerException if the given {@code converter} is {@code null}.
	 */
	public <B> MappableAccumulator<B> map(final Function<B, T> converter) {
		return new AccumulatorAdapter<>(this, converter);
	}
	
	@Override
	public int hashCode() {
		return hashCodeOf(getClass()).and(_samples).value();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		
		final MappableAccumulator<?> accumulator = (MappableAccumulator<?>)obj;
		return eq(_samples, accumulator._samples);
	}
	
	@Override
	public String toString() {
		return String.format(
				"%s[samples=%d]", getClass().getName(), _samples
			);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public MappableAccumulator<T> clone() {
		try {
			return (MappableAccumulator<T>)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}
	
}

/**
 * Adapts an accumulator from type {@code A} to type {@code B}.
 * 
 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz Wilhelmstötter</a>
 * @version $Id$
 */
final class AccumulatorAdapter<A, B> extends MappableAccumulator<B> {
	private final Accumulator<? super A> _adoptee;
	private final Function<? super B, ? extends A> _converter;
	
	/**
	 * Create an new AccumulatorAdapter. 
	 * 
	 * @param adoptee the original, adapted, Accumulator.
	 * @param converter the converter needed to convert from type {@code A} to 
	 *        type {@code B}.
	 * @throws NullPointerException if one of the arguments is {@code null}.
	 */
	AccumulatorAdapter(
		final Accumulator<? super A> adoptee, 
		final Function<? super B, ? extends A> converter
	) {
		_adoptee = nonNull(adoptee);
		_converter = nonNull(converter);
	}
	
	@Override
	public void accumulate(final B value) {
		_adoptee.accumulate(_converter.apply(value));
		++_samples;
	}

	@Override
	public String toString() {
		return String.format(
				"%s[a=%s, c=%s]", 
				getClass().getSimpleName(), _adoptee, _converter
			);
	}


}