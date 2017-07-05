package com.minhld.opencv;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

import com.minhld.utils.Settings;

public class DistanceEstimator {
	static final double STANDARD_DISTANCE = 50;			// in centimeters
	static final double STANDARD_PX_WIDTH = 200;		// in pixels
	static final double STANDARD_WIDTH = 15; 			// in centimeters
	static final double FEET_PER_CM = 30.48;

	public static double estimateDistance(Rect objRect) {
		double focalLength = (Settings.standardPixelWidth * Settings.standardDistance) / Settings.actualWidth;
		double distance = (Settings.actualWidth * focalLength) / (double) objRect.width;
		return distance / FEET_PER_CM;
	}
	
	public static double estimate(Mat object) {
		double distancedHeight = object.rows();
		double distance = (distancedHeight * Settings.standardDistance) / Settings.actualWidth;
		return distance / FEET_PER_CM;
	}

//	public static double estimateDistance(Rect objRect) {
//		double focalLength = (STANDARD_PX_WIDTH * STANDARD_DISTANCE) / STANDARD_WIDTH;
//		double distance = (STANDARD_WIDTH * focalLength) / (double) objRect.width;
//		return distance / FEET_PER_CM;
//	}
//	
//	public static double estimate(Mat object) {
//		double distancedHeight = object.rows();
//		double distance = (distancedHeight * STANDARD_DISTANCE) / STANDARD_WIDTH;
//		return distance / FEET_PER_CM;
//	}
	
}
