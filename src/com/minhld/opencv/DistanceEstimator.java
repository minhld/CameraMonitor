package com.minhld.opencv;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

public class DistanceEstimator {
	static final double STANDARD_DISTANCE = 50;			// in centimeters
	static final double STANDARD_PX_HEIGHT = 200;		// in pixels
	static final double STANDARD_HEIGHT = 15; 			// in centimeters
	static final double FEET_PER_CM = 30.48;
	
	public static double estimateDistance(Rect objRect) {
		double focalLength = (STANDARD_PX_HEIGHT * STANDARD_DISTANCE) / STANDARD_HEIGHT;
		double distance = (STANDARD_HEIGHT * focalLength) / (double) objRect.height;
		return distance / FEET_PER_CM;
	}
	
	public static double estimate(Mat object) {
		double distancedHeight = object.rows();
		double distance = (distancedHeight * STANDARD_DISTANCE) / STANDARD_HEIGHT;
		return distance / FEET_PER_CM;
	}
	
}
