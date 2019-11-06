/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package net.imagej.ops.filter.convolve;

import net.imagej.ops.Ops;
import net.imagej.ops.special.computer.BinaryComputerOp;
import net.imagej.ops.special.computer.Computers;
import net.imagej.ops.special.function.AbstractBinaryFunctionOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;

import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Function wrapper around {@link ConvolveFFTC} computer.
 * 
 * @author Curtis Rueden
 * @author Brian Northan
 */
@Plugin(type = Ops.Filter.Convolve.class, priority = Priority.LOW)
public class ConvolveFFTF<I extends RealType<I> & NativeType<I>, K extends RealType<K>, C extends ComplexType<C>>
	extends
	AbstractBinaryFunctionOp<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, RandomAccessibleInterval<I>>
	implements Ops.Filter.Convolve
{

	/**
	 * Buffer to be used to store FFTs for input. Size of fftInput must correspond
	 * to the fft size of raiExtendedInput
	 */
	@Parameter(required = false)
	private RandomAccessibleInterval<C> fftInput;

	/**
	 * Buffer to be used to store FFTs for kernel. Size of fftKernel must
	 * correspond to the fft size of raiExtendedKernel
	 */
	@Parameter(required = false)
	private RandomAccessibleInterval<C> fftKernel;

	/**
	 * boolean indicating that the input FFT has already been calculated
	 */
	@Parameter(required = false)
	private boolean performInputFFT = true;

	/**
	 * boolean indicating that the kernel FFT has already been calculated
	 */
	@Parameter(required = false)
	private boolean performKernelFFT = true;

	private BinaryComputerOp<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, RandomAccessibleInterval<I>> convolveFFTC;

	@Override
	public void initialize() {
		super.initialize();
		System.out.println("ConvolveFFTF: in1 = " + in1() + ", in2 = " + in2() + ", fftInput = " + fftInput + ", fftKernel = " + fftKernel);
		convolveFFTC = Computers.binary(ops(), Ops.Filter.Convolve.class, in1(),
			in1(), in2(), fftInput, fftKernel, performInputFFT, performKernelFFT);
//		convolveFFTC = (BinaryComputerOp) Computers.binary(ops(), Ops.Filter.Convolve.class, RandomAccessibleInterval.class,
//			RandomAccessibleInterval.class, RandomAccessibleInterval.class, fftInput, fftKernel, performInputFFT, performKernelFFT);
	}

	/**
	 * Call the linear filter that is set up to perform convolution
	 */
	@Override
	public RandomAccessibleInterval<I> calculate(final RandomAccessibleInterval<I> in,
		final RandomAccessibleInterval<K> kernel)
	{
		final Img<I> out = ops().create().img(in);
		convolveFFTC.compute(in, kernel, out);
		return out;
	}
}
