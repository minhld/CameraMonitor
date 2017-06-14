package com.minhld.opencv;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.minhld.ros.controller.MoveInstructor;
import com.minhld.utils.OpenCVUtils;
import com.minhld.utils.Settings;

import sensor_msgs.Image;

public class ObjectDetectorRed {
	public static int THRESHOLD_MIN_AREA = 400;

	public static Mat tplMat;
	static int tplWidth, tplHeight;
	
	static {
		readTemplate();
	}
	
	public static void readTemplate() {
		tplMat = Imgcodecs.imread(Settings.templatePath);
		tplWidth = tplMat.cols();
		tplHeight = tplMat.rows();
		Imgproc.cvtColor(tplMat, tplMat, Imgproc.COLOR_BGR2GRAY);
		Imgproc.threshold(tplMat, tplMat, Settings.threshold, 255, Imgproc.THRESH_BINARY);
	}
	
	
	/**
	 * comparing with a known template
	 * using matchTemplate function 
	 * 
	 * @param source
	 * @return
	 */
	public static Object[] processImage(Image source) {
		// Mat orgMat = Imgcodecs.imread("samples/multiobjects.png");
		Mat orgMat = OpenCVUtils.openImage(source);
		
		Mat modMat = new Mat();
		
//		// set it blur (to remove noise)
//		if (Settings.gaussianSize % 2 == 1) {
//			Imgproc.GaussianBlur(orgMat, orgMat, new Size(Settings.gaussianSize, Settings.gaussianSize), 1);
//		}
		// Imgproc.GaussianBlur(orgMat, orgMat, new Size(9, 9), 2, 2);
		
		// turns it to HSV color image
		Imgproc.cvtColor(orgMat, modMat, Imgproc.COLOR_BGR2HSV);
		
		Mat lowMask = new Mat(), highMask = new Mat();
		Core.inRange(modMat, new Scalar(Settings.threshold, Settings.gaussianSize, Settings.contourSides), new Scalar(Settings.threshold + 10, 255, 255),lowMask);
		Core.inRange(modMat, new Scalar(Settings.colorThreshold, Settings.dilateSize, Settings.contourAreaMin), new Scalar(Settings.colorThreshold + 10, 255, 255),highMask);

		
		// merge the two masks
		Mat finalMask = new Mat();
		Core.addWeighted(lowMask, 1, highMask, 1, 0, finalMask);
		
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(finalMask, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		hierarchy.release();

		if (contours.size() > 0) {
			MatOfPoint contour;
//			double contourSize;
			for(int i = 0; i < contours.size(); i++) {
				// THRESHOLD_MIN_AREA
				contour = contours.get(i);
				if (Imgproc.contourArea(contour) > THRESHOLD_MIN_AREA) {
					Rect rect = Imgproc.boundingRect(contour);
					Imgproc.rectangle(orgMat, new Point(rect.x,rect.y), new Point(rect.x + rect.width, rect.y + rect.height), OpenCVUtils.BORDER_COLOR);
					
	            	// Imgproc.drawContours(orgMat, contours, i, OpenCVUtils.BORDER_COLOR, 1);
					
				}
				
//				contours.get(i).convertTo(c, CvType.CV_32F);
//				double cArea = Imgproc.arcLength(c, true);
//				Imgproc.approxPolyDP(c, approxC, 0.02 * cArea, true);
//
//				// if (approxC.size().height == 4) {
					
//		            // Rect rect = Imgproc.boundingRect(contours.get(i));
//		            // Imgproc.rectangle(orgMat, new Point(rect.x,rect.y), new Point(rect.x + rect.width, rect.y + rect.height), BORDER_COLOR);
//				//}
			}
		}
		
//		Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new  Size(Settings.dilateSize, Settings.dilateSize));
//		Imgproc.erode(finalMask, finalMask, erodeElement);
//		Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new  Size(Settings.dilateSize * 2, Settings.dilateSize * 2));
//		Imgproc.dilate(finalMask, finalMask, dilateElement);

//		Mat matchedMat = new Mat(orgMat.rows() - tplMat.rows(), orgMat.cols() - tplMat.cols(), CvType.CV_32FC1);	
//        Imgproc.matchTemplate(modMat, tplMat, matchedMat, Imgproc.TM_CCOEFF);
//        
//        MinMaxLocResult mmr = Core.minMaxLoc(matchedMat);
//     
//        // Core.normalize(matchedMat, matchedMat);
//        
//        Point locStart = mmr.maxLoc;
//        Point locEnd = new Point(locStart.x + tplWidth, locStart.y + tplHeight);
//    	
//        
//        // System.out.println("similarity: " + mmr.minVal + ", " + mmr.maxVal);
//        
//    	Mat capturedMat = new Mat(modMat, new Rect(locStart, locEnd));
//    	// Mat capturedMat = new Mat(orgMat, new Rect(locStart, locEnd));
//    	
////    	Object[] res = findPad(capturedMat);
////    	// Mat[] res2 = FeatureExtractor.extractFeature2(capturedMat);
////
////    	// if pad not found
////    	if (!(boolean)res[1]) { 
////    		locStart = new Point(0, 0);
////    	} else {
////    		Imgproc.rectangle(orgMat, locStart, locEnd, OpenCVUtils.BORDER_COLOR);
////    	}
//    	
//    	Imgproc.rectangle(orgMat, locStart, locEnd, OpenCVUtils.BORDER_COLOR);
//    	
//    	int moveInstructor = MoveInstructor.instruct(orgMat.cols(), locStart, locEnd);
    	
        // BufferedImage processImage = OpenCVUtils.createAwtImage(res2[0]); // OpenCVUtils.createAwtImage(modMat);
        // BufferedImage processImage = OpenCVUtils.createAwtImage(orgMat);
        // BufferedImage processImage = OpenCVUtils.createAwtImage(capturedMat); // OpenCVUtils.createAwtImage(modMat);
        BufferedImage resultImage = OpenCVUtils.createAwtImage(orgMat);
        BufferedImage processImage = OpenCVUtils.createAwtImage(finalMask);
        // BufferedImage capturedImage = OpenCVUtils.createAwtImage((Mat) res[0]);
//        BufferedImage capturedImage = OpenCVUtils.createAwtImage(capturedMat);
        
        return new Object[] { resultImage, processImage, null, MoveInstructor.MOVE_SEARCH };
	}
	
	public static Object[] findPad(Mat capturedMat) {
		Mat cannyMat = new Mat();
		Imgproc.Canny(capturedMat, cannyMat, 10, 250);

		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(cannyMat, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		hierarchy.release();
		
		boolean foundPad = false;
		
		if (contours.size() > 0) {
			MatOfPoint2f c = new MatOfPoint2f();
			MatOfPoint2f approxC = new MatOfPoint2f();
			MatOfPoint contour;
			
			for(int i = 0; i < contours.size(); i++) {
				contour = contours.get(i);
				if (Imgproc.contourArea(contour) > Settings.contourAreaMin) {
					contours.get(i).convertTo(c, CvType.CV_32F);
					double cArea = Imgproc.arcLength(c, true);
					Imgproc.approxPolyDP(c, approxC, 0.02 * cArea, true);

					if (approxC.size().height >= Settings.contourSides) {
						foundPad = true;
						break;
					}
				}
			}
			
		}
		
		// no pad found
		return new Object[] { cannyMat, foundPad };
	}
	
	
}
