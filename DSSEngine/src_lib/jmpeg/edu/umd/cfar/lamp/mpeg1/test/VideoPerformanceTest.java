/***************************************
 *            ViPER-MPEG               *
 *  The Video Processing               *
 *         Evaluation Resource         *
 *             MPEG-1 Decoder          *
 * Distributed under the LGPL license  *
 *        Terms available at gnu.org.  *
 *                                     *
 *  Copyright University of Maryland,  *
 *                      College Park.  *
 ***************************************/

package edu.umd.cfar.lamp.mpeg1.test;

import java.io.IOException;

import edu.umd.cfar.lamp.mpeg1.Mpeg1VideoStream;
import edu.umd.cfar.lamp.mpeg1.MpegException;

/**
 * Extended tests for performance evaluation
 */
public class VideoPerformanceTest extends VideoFileTest {
	public VideoPerformanceTest(String testName) {
		super(testName);
	}

	protected void runMyTest(Mpeg1VideoStream stream) throws MpegException, IOException {
		long timeout = getVideoLengthInMillis(stream);
		long startTime = System.currentTimeMillis();
		seekThroughStream(stream);
		long totalTime = System.currentTimeMillis() - startTime;
		double fps = (getNumFrames(stream) * 1000.0) / totalTime;
		System.out.println("Averaged " + fps + "fpms");
	}
	public static void main(String args[]) throws MpegException, IOException {
		new VideoPerformanceTest("testLampVideo").testLampVideo();
	}
}
