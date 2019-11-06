/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2015 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
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

package net.imagej.ops.coloc.saca;

import net.imagej.ops.coloc.ColocalisationTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.junit.Test;

/**
 * Tests {@link AdaptiveSmoothedKendallTau}.
 * 
 * @author Curtis Rueden
 * @author Ellen T Arena
 */
public final class AdaptiveSmoothedKendallTauTest extends ColocalisationTest {

	@Test
	public void testExecute() {
		final Img<UnsignedByteType> img = generateUnsignedByteArrayTestImg(true, 22, 13);
		IntervalView<UnsignedByteType> img1 = Views.interval(img, new long[] {0,  0}, new long [] {10, 12});
		IntervalView<UnsignedByteType> img2 = Views.zeroMin(Views.interval(img, new long[] {11,  0}, new long [] {21, 12}));
		double[][] data1 = extractDoubles(img1);
		double[][] data2 = extractDoubles(img2);
		Histogram1d<UnsignedByteType> hist1 = ops.image().histogram(img1);
		Histogram1d<UnsignedByteType> hist2 = ops.image().histogram(img2);
		UnsignedByteType thres1 = ops.threshold().otsu(hist1);
		UnsignedByteType thres2 = ops.threshold().otsu(hist2);
		AdaptiveSmoothedKendallTau algorithm = new AdaptiveSmoothedKendallTau(data1, data2, thres1.getRealDouble(), thres2.getRealDouble());
		double[][] result = algorithm.execute();
		double[] array = { -1.7250008445155562, -1.73135711204857,
			-1.7692567054330914, -1.7010656337613557, -1.619312112383386,
			-1.4525558673002097, -1.1861120398880622, -0.783101347503297,
			-0.37954116613571465, -0.09517055420041515, -0.09203049578306335,
			-1.7459174762742646, -1.7356172864291017, -1.7884529668257132,
			-1.6551004772880407, -1.582818535744789, -1.343308935728416,
			-0.8584970525635196, -0.4348620565846349, -0.1521187520806392,
			-0.24596125286527903, -0.006392617163386807, -1.9228286118542126,
			-1.8066347792601358, -1.6654546078677521, -1.4975143013880077,
			-1.3295075035171537, -0.9096057451762982, -0.05311970995097883,
			0.16824227092859945, 0.4104906216545407, 0.41852704548348896,
			0.4360783966058302, -1.8250214975847139, -1.8880715371758439,
			-1.692423904418704, -1.3276343018217405, -0.8219508027556683,
			-0.25157845228994224, 0.26516369019442465, 0.7798648533440188,
			0.8808996834667516, 0.681457341300692, 1.3538864008133902,
			-1.861671622176177, -1.9301031828076738, -1.85687352152997,
			-1.353989095692806, -0.7220973446097547, -0.2919968700888508,
			0.42290773010280713, 0.7408799812459382, 0.9833176572264575,
			1.253971237525779, 1.311327824351475, -1.7746019201554133,
			-1.7730272767606887, -1.7171896720800162, -0.8045392919259329,
			-0.5679695917394123, -0.22042138335737274, 0.1292315583276765,
			0.9513805863200251, 1.2554717004661036, 1.3142307972580167,
			1.2028576470450443, -1.6567211564019377, -1.6919667128334934,
			-1.4644792507189408, -0.7101706827431953, -0.36355873806394146,
			-0.09685724834739831, 0.6210991915712518, 0.8928873849199506,
			1.3877911627806352, 1.2721613079566068, 1.192230353198964,
			-1.5062258187243138, -1.17769657237071, -0.5845691460417369,
			-0.18459184723938632, 0.09316809367556181, -0.03823062780646647,
			0.19580553362395925, 0.7353135465679443, 1.3358223484373628,
			1.2409966672708523, 1.15136725915078, -0.19645695599208318,
			-0.16039352886692987, -0.09723159553105262, 0.26426166957072483,
			0.5888856023067829, 0.13873451682099774, 0.1449211808187819,
			0.9506847037421924, 1.248672402374278, 1.0220523295934796,
			1.1057819023438187, 0.21470832097470027, -0.015131824564180621,
			0.00798262678369198, 0.4921414149606152, 0.5350877288588405,
			0.2620166132857223, 1.0923110371505835, 1.1213699128693995,
			1.043632705419592, 1.0136252112390172, 0.89685340780153,
			0.7221425374112189, 0.3187850351263955, 0.24312920630334162,
			0.5707459918449566, 0.5512937884585409, 0.7890186841287252,
			1.0424103201771384, 0.90581777218861, 0.4865274773993299,
			0.588276423146171, 0.4762457424858461, 0.9247064970342787,
			0.4395352888251889, 0.3409109139520929, 0.5220590899985162,
			0.31552694887798677, 0.3228336696520999, 0.5318436370051721,
			0.01056464162222682, -0.24836298522267108, -0.10404013777806209,
			0.03283460224171222, 1.120759785092119, 0.5362630022806611,
			0.3034899644676543, 0.38835599292227196, 0.15808489018939986,
			0.23006029380366322, 0.2604897953870584, -0.09199067449406838,
			-0.4006581411282655, -0.36328473240543674, -0.22480815873910512 };
		Img<DoubleType> expected = ArrayImgs.doubles(array, 11, 13);
		assertIterationsEqual(expected, img(result));
	}

	private static Img<DoubleType> img(double[][] values) {
		int h = values.length;
		int w = values[0].length;
		Img<DoubleType> img = ArrayImgs.doubles(w, h);
		Cursor<DoubleType> c = Views.flatIterable(img).cursor();
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				c.next().setReal(values[y][x]);
			}
		}
		return img;
	}

	private static double[][] extractDoubles(RandomAccessibleInterval<UnsignedByteType> typedImg) {
		int w = (int) typedImg.dimension(0);
		int h = (int) typedImg.dimension(1);
		double[][] data = new double[h][w];
		Cursor<UnsignedByteType> c = Views.flatIterable(typedImg).cursor();
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				data[y][x] = c.next().getRealDouble();
			}
		}
		return data;
	}
}
