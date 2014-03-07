/*
 * Java Genetic Algorithm Library (@__identifier__@).
 * Copyright (c) @__year__@ Franz Wilhelmstötter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author:
 *    Franz Wilhelmstötter (franz.wilhelmstoetter@gmx.at)
 */
package org.jenetics.stat;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.jenetics.internal.util.object.eq;

import java.io.Serializable;
import java.util.Locale;

import org.jscience.mathematics.number.Float64;

import org.jenetics.internal.util.HashBuilder;

import org.jenetics.util.Function;
import org.jenetics.util.Range;


/**
 * <a href="http://en.wikipedia.org/wiki/Uniform_distribution_%28continuous%29">
 * Uniform distribution</a> class.
 *
 * @see LinearDistribution
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz Wilhelmstötter</a>
 * @since 1.0
 * @version 1.0 &mdash; <em>$Date$</em>
 */
public class UniformDistribution<
	N extends Number & Comparable<? super N>
>
	implements Distribution<N>
{

	/**
	 * <p>
	 * <img
	 *     src="doc-files/uniform-pdf.gif"
	 *     alt="f(x)=\left\{\begin{matrix}
	 *          \frac{1}{max-min} & for & x \in [min, max] \\
	 *          0 & & otherwise \\
	 *          \end{matrix}\right."
	 * />
	 * </p>
	 *
	 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz Wilhelmstötter</a>
	 * @since 1.0
	 * @version 1.0 &mdash; <em>$Date$</em>
	 */
	static final class PDF<N extends Number & Comparable<? super N>>
		implements
			Function<N, Float64>,
			Serializable
	{
		private static final long serialVersionUID = 1L;

		private final double _min;
		private final double _max;
		private final Float64 _probability;

		public PDF(final Range<N> domain) {
			_min = domain.getMin().doubleValue();
			_max = domain.getMax().doubleValue();
			_probability = Float64.valueOf(1.0/(_max - _min));
		}

		@Override
		public Float64 apply(final N value) {
			final double x = value.doubleValue();

			Float64 result = Float64.ZERO;
			if (x >= _min && x <= _max) {
				result = _probability;
			}

			return result;
		}

		@Override
		public String toString() {
			return format(Locale.ENGLISH, "p(x) = %s", _probability);
		}

	}

	/**
	 * <p>
	 * <img
	 *     src="doc-files/uniform-cdf.gif"
	 *     alt="f(x)=\left\{\begin{matrix}
	 *         0 & for & x < min \\
	 *         \frac{x-min}{max-min} & for & x \in [min, max] \\
	 *         1 & for & x > max  \\
	 *         \end{matrix}\right."
	 * />
	 * </p>
	 *
	 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz Wilhelmstötter</a>
	 * @since 1.0
	 * @version 1.0 &mdash; <em>$Date$</em>
	 */
	static final class CDF<N extends Number & Comparable<? super N>>
		implements
			Function<N, Float64>,
			Serializable
	{
		private static final long serialVersionUID = 1L;


		private final double _min;
		private final double _max;
		private final double _divisor;

		public CDF(final Range<N> domain) {
			_min = domain.getMin().doubleValue();
			_max = domain.getMax().doubleValue();
			_divisor = _max - _min;
			assert (_divisor > 0);
		}

		@Override
		public Float64 apply(final N value) {
			final double x = value.doubleValue();

			Float64 result = Float64.ZERO;
			if (x < _min) {
				result = Float64.ZERO;
			} else if (x > _max) {
				result = Float64.ONE;
			} else {
				result = Float64.valueOf((x - _min)/_divisor);
			}

			return result;
		}

		@Override
		public String toString() {
			return format(
				Locale.ENGLISH,
				"P(x) = (x - %1$s)/(%2$s - %1$s)", _min, _max
			);
		}

	}


	private final Range<N> _domain;
	private final Function<N, Float64> _cdf;
	private final Function<N, Float64> _pdf;

	/**
	 * Create a new uniform distribution with the given {@code domain}.
	 *
	 * @param domain the domain of the distribution.
	 * @throws NullPointerException if the {@code domain} is {@code null}.
	 */
	public UniformDistribution(final Range<N> domain) {
		_domain = requireNonNull(domain, "Domain");
		_cdf = new CDF<>(_domain);
		_pdf = new PDF<>(_domain);
	}

	/**
	 * Create a new uniform distribution with the given min and max values.
	 *
	 * @param min the minimum value of the domain.
	 * @param max the maximum value of the domain.
	 * @throws IllegalArgumentException if {@code min >= max}
	 * @throws NullPointerException if one of the arguments is {@code null}.
	 */
	public UniformDistribution(final N min, final N max) {
		this(new Range<>(min, max));
	}

	@Override
	public Range<N> getDomain() {
		return _domain;
	}

	/**
	 * Return a new PDF object.
	 *
	 * <p>
	 * <img
	 *     src="doc-files/uniform-pdf.gif"
	 *     alt="f(x)=\left\{\begin{matrix}
	 *          \frac{1}{max-min} & for & x \in [min, max] \\
	 *          0 & & otherwise \\
	 *          \end{matrix}\right."
	 * />
	 * </p>
	 *
	 */
	@Override
	public Function<N, Float64> getPDF() {
		return _pdf;
	}

	/**
	 * Return a new CDF object.
	 *
	 * <p>
	 * <img
	 *     src="doc-files/uniform-cdf.gif"
	 *     alt="f(x)=\left\{\begin{matrix}
	 *         0 & for & x < min \\
	 *         \frac{x-min}{max-min} & for & x \in [min, max] \\
	 *         1 & for & x > max  \\
	 *         \end{matrix}\right."
	 * />
	 * </p>
	 *
	 */
	@Override
	public Function<N, Float64> getCDF() {
		return _cdf;
	}

	@Override
	public int hashCode() {
		return HashBuilder.of(getClass()).and(_domain).value();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		final UniformDistribution<?> dist = (UniformDistribution<?>)obj;
		return eq(_domain, dist._domain);
	}

	@Override
	public String toString() {
		return format("UniformDistribution[%s]", _domain);
	}

}
