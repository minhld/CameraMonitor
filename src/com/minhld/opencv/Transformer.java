package com.minhld.opencv;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class Transformer {
	
	public static void transformImage() {
		Mat orgMat = Imgcodecs.imread("samples/detect1.png");
		transformImage(orgMat);
	}
	
	public static void transformImage(Mat orgMat) {
		
	}
}
