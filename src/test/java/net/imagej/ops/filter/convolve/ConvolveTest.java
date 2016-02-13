/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2016 Board of Regents of the University of
 * Wisconsin-Madison, University of Konstanz and Brian Northan.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import net.imagej.ops.AbstractOpTest;
import net.imagej.ops.Op;
import net.imagej.ops.Ops;
import net.imagej.ops.filter.CreateFFTFilterMemory;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.complex.ComplexFloatType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.Test;

/**
 * Tests involving convolvers.
 */
public class ConvolveTest extends AbstractOpTest {

	/** Tests that the correct convolver is selected when using a small kernel. */
	@Test
	public void testConvolveMethodSelection() {

		final Img<ByteType> in =
			new ArrayImgFactory<ByteType>().create(new int[] { 20, 20 },
				new ByteType());

		// use a small kernel
		int[] kernelSize = new int[] { 3, 3 };
		Img<FloatType> kernel =
			new ArrayImgFactory<FloatType>().create(kernelSize, new FloatType());

		Op op = ops.op(Ops.Filter.Convolve.class, in, kernel);

		// we should get ConvolveNaive
		assertSame(ConvolveNaiveImg.class, op.getClass());

		// make sure it runs
		@SuppressWarnings("unchecked")
		final Img<FloatType> out1 = (Img<FloatType>) ops.run(ConvolveNaiveImg.class,
			in, kernel);

		assertEquals(out1.dimension(0), 20);

		// use a bigger kernel
		kernelSize = new int[] { 30, 30 };
		kernel =
			new ArrayImgFactory<FloatType>().create(kernelSize, new FloatType());

		op = ops.op(Ops.Filter.Convolve.class, in, kernel);

		// this time we should get ConvolveFFT
		assertSame(ConvolveFFTImg.class, op.getClass());

		// make sure it runs
		@SuppressWarnings("unchecked")
		final Img<FloatType> out2 = (Img<FloatType>) ops.run(ConvolveFFTImg.class,
			in, kernel);

		assertEquals(out2.dimension(0), 20);

	}

	/** tests fft based convolve */
	@Test
	public void testConvolve() {

		float delta = 0.0001f;

		int[] size = new int[] { 225, 167 };
		int[] kernelSize = new int[] { 27, 39 };

		long[] borderSize = new long[] { 10, 10 };

		// create an input with a small sphere at the center
		Img<FloatType> in =
			new ArrayImgFactory<FloatType>().create(size, new FloatType());
		placeSphereInCenter(in);

		// create a kernel with a small sphere in the center
		Img<FloatType> kernel =
			new ArrayImgFactory<FloatType>().create(kernelSize, new FloatType());
		placeSphereInCenter(kernel);

		// create variables to hold the image sums
		FloatType inSum = new FloatType();
		FloatType kernelSum = new FloatType();
		FloatType outSum = new FloatType();
		FloatType outSum2 = new FloatType();
		FloatType outSum3 = new FloatType();

		// calculate sum of input and kernel
		ops.stats().sum(inSum, in);
		ops.stats().sum(kernelSum, kernel);

		// convolve and calculate the sum of output
		@SuppressWarnings("unchecked")
		final Img<FloatType> out = (Img<FloatType>) ops.run(ConvolveFFTImg.class,
			null, in, kernel, borderSize);

		// create an output for the next test
		Img<FloatType> out2 =
			new ArrayImgFactory<FloatType>().create(size, new FloatType());

		// create an output for the next test
		Img<FloatType> out3 =
			new ArrayImgFactory<FloatType>().create(size, new FloatType());

		// this time create reusable fft memory first
		@SuppressWarnings("unchecked")
		final CreateFFTFilterMemory<FloatType, FloatType, FloatType, ComplexFloatType> createMemory =
			ops.op(CreateFFTFilterMemory.class, in, kernel);

		createMemory.run();

		// run convolve using the rai version with the memory created above
		ops.run(ConvolveFFTRAI.class, createMemory.getRAIExtendedInput(),
			createMemory.getRAIExtendedKernel(), createMemory.getFFTImg(),
			createMemory.getFFTKernel(), out2);

		ops.run(ConvolveFFTRAI.class, createMemory.getRAIExtendedInput(), null,
			createMemory.getFFTImg(), createMemory.getFFTKernel(), out3, true, false);

		ops.stats().sum(outSum, out);
		ops.stats().sum(outSum2, out2);
		ops.stats().sum(outSum3, out3);

		// multiply input sum by kernelSum and assert it is the same as outSum
		inSum.mul(kernelSum);

		assertEquals(inSum.get(), outSum.get(), delta);
		assertEquals(inSum.get(), outSum2.get(), delta);
		assertEquals(inSum.get(), outSum3.get(), delta);

		assertEquals(size[0], out.dimension(0));
		assertEquals(size[0], out2.dimension(0));
	}

	// utility to place a small sphere at the center of the image
	private void placeSphereInCenter(Img<FloatType> img) {

		final Point center = new Point(img.numDimensions());

		for (int d = 0; d < img.numDimensions(); d++)
			center.setPosition(img.dimension(d) / 2, d);

		HyperSphere<FloatType> hyperSphere = new HyperSphere<>(img, center, 2);

		for (final FloatType value : hyperSphere) {
			value.setReal(1);
		}
	}
	
	/** tests fft based convolve */
	@Test
	public void testCreateAndConvolvePoints() {
		
		final int xSize=128;
		final int ySize=128;
		final int zSize=128;
		
		int[] size = new int[] { xSize, ySize, zSize };
		
		Img<DoubleType> phantom = ops.create().img(size);

		RandomAccess<DoubleType> randomAccess=phantom.randomAccess();

		randomAccess.setPosition(new long[]{xSize/2, ySize/2, zSize/2});
		randomAccess.get().setReal(255.0);
		
		randomAccess.setPosition(new long[]{xSize/4, ySize/4, zSize/4});
		randomAccess.get().setReal(255.0);

		Point location = new Point(phantom.numDimensions());
		location.setPosition(new long[]{3*xSize/4, 3*ySize/4, 3*zSize/4});

		HyperSphere<DoubleType> hyperSphere = new HyperSphere<>(phantom, location, 5);
				
		for (DoubleType value : hyperSphere) {
			value.setReal(16);
		}
		
		// create psf using the gaussian kernel op (alternatively PSF could be an input to the script)
		RandomAccessibleInterval<DoubleType> psf= ops.create().kernelGauss(new double[]{5, 5, 5}, new DoubleType());

		// convolve psf with phantom
		Img<DoubleType> convolved=ops.filter().convolve(phantom, psf);
		
		DoubleType sum = new DoubleType();
		DoubleType max = new DoubleType();
		DoubleType min = new DoubleType();
		
		ops.stats().sum(sum, convolved);
		ops.stats().max(max, convolved);
		ops.stats().min(min, convolved);
		
		assertEquals(sum.getRealDouble(), 8750.00, 0.001);
		assertEquals(max.getRealDouble(), 3.155, 0.001);
		assertEquals(min.getRealDouble(), 2.978E-7, 0.001);
		
	}
	
}
