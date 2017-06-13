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
import org.opencv.core.Size;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.minhld.ros.controller.MoveInstructor;
import com.minhld.utils.OpenCVUtils;
import com.minhld.utils.Settings;

import sensor_msgs.Image;

public class ObjectDetector {
	public static int THRESHOLD_MIN_AREA = 400;

	public static Mat tplMat;
	static int tplWidth, tplHeight;
	
	static {
		// readTemplate11();
		// readTemplate0();
		readTemplate();
	}
	
	public static void readTemplate11() {
		tplMat = Imgcodecs.imread("samples/tpl9.png");
		Imgproc.cvtColor(tplMat, tplMat, Imgproc.COLOR_BGR2GRAY);
		Imgproc.threshold(tplMat, tplMat, Settings.threshold, 255, Imgproc.THRESH_BINARY);
		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new  Size(30, 30));
//		Imgproc.dilate(tplMat, tplMat, element);
		Imgproc.dilate(tplMat, tplMat, element);
	}
	
	public static void readTemplate() {
		tplMat = Imgcodecs.imread(Settings.templatePath);
		tplWidth = tplMat.cols();
		tplHeight = tplMat.rows();
		Imgproc.cvtColor(tplMat, tplMat, Imgproc.COLOR_BGR2GRAY);
		Imgproc.threshold(tplMat, tplMat, Settings.threshold, 255, Imgproc.THRESH_BINARY);
	}
	
	public static void readTemplate0() {
		tplMat = Imgcodecs.imread("samples/tpl9.png");
		Imgproc.cvtColor(tplMat, tplMat, Imgproc.COLOR_BGR2GRAY);
	}

	/**
	 * finding the circle - updated version
	 * 
	 * @param source
	 * @return
	 */
	public static Object[] processImage21(Image source) {
		// Mat orgMat = OpenCVUtils.openImage(source);
		Mat orgMat = Imgcodecs.imread("/home/lee/Downloads/test1.jpg");
		
		Mat modMat = new Mat();
		
		// turn to black-white
		Imgproc.cvtColor(orgMat, modMat, Imgproc.COLOR_BGR2GRAY);
		
		// threshold to eliminate a number of objects
		Imgproc.threshold(modMat, modMat, 128, 255, Imgproc.THRESH_BINARY);

		// find contours
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(modMat, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		hierarchy.release();

		if (contours.size() > 0) {
//			MatOfPoint2f c = new MatOfPoint2f();
//			MatOfPoint2f approxC = new MatOfPoint2f();
			
			MatOfPoint contour;
//			double contourSize;
			for(int i = 0; i < contours.size(); i++) {
				// THRESHOLD_MIN_AREA
				contour = contours.get(i);
				if (Imgproc.contourArea(contour) > THRESHOLD_MIN_AREA) {
					Rect rect = Imgproc.boundingRect(contour);
//		            System.out.println(rect.height);
		            if (rect.height > 8 && rect.height <= 20){
//		            	Imgproc.rectangle(orgMat, new Point(rect.x,rect.height), new Point(rect.y,rect.width), OpenCVUtils.BORDER_RED_COLOR);
		            	Imgproc.drawContours(orgMat, contours, i, OpenCVUtils.BORDER_RED_COLOR, 1);
		            }
					
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
		
		
		BufferedImage processImage = OpenCVUtils.createAwtImage(modMat);
        BufferedImage resultImage = OpenCVUtils.createAwtImage(orgMat);
        
        
        return new Object[] { processImage, resultImage, false };
	}
	
	/**
	 * finding the circle
	 * 
	 * @param source
	 * @return
	 */
	public static Object[] processImage2(Image source) {
		// Mat orgMat = OpenCVUtils.openImage(source);
		Mat orgMat = Imgcodecs.imread("/home/lee/Downloads/test1.jpg");
		
		Mat modMat = new Mat();
		
		// turn to black-white
		Imgproc.cvtColor(orgMat, modMat, Imgproc.COLOR_BGR2GRAY);
		
		// blur
		Imgproc.GaussianBlur(modMat, modMat, new Size(9, 9), 2, 2);
		
//		// threshold to eliminate a number of objects
//		Imgproc.threshold(modMat, modMat, 225, 255, Imgproc.THRESH_BINARY);
//		
//		// purify the threshold
//		Imgproc.adaptiveThreshold(modMat, modMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 11, 1);
		
		Mat circles = new Mat();
		
		Imgproc.HoughCircles(modMat, circles, Imgproc.CV_HOUGH_GRADIENT, 3, 100, 220, 100, 20, 100);
		
		for (int i = 0; i < circles.cols(); i++) {
			double vCircle[] = circles.get(0, i);

			Point center = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
			int radius = (int) Math.round(vCircle[2]);
			// draw the circle center
			Imgproc.circle(orgMat, center, radius, OpenCVUtils.BORDER_COLOR, 1);
		}
		
		BufferedImage processImage = OpenCVUtils.createAwtImage(modMat);
        BufferedImage resultImage = OpenCVUtils.createAwtImage(orgMat);
        
        
        return new Object[] { processImage, resultImage, false };
	}
	

	/**
	 * comparing with a known template
	 * using matchTemplate function 
	 * 
	 * @param source
	 * @return
	 */
	public static Object[] processImage11(Image source) {
		Mat orgMat = OpenCVUtils.openImage(source);
		
		Mat modMat = new Mat();
		Imgproc.cvtColor(orgMat, modMat, Imgproc.COLOR_BGR2GRAY);
		
//		Imgproc.GaussianBlur(modMat, modMat, new Size(9, 9), 2, 2);
//		
		Imgproc.threshold(modMat, modMat, 220, 255, Imgproc.THRESH_BINARY);
		
		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new  Size(20, 20));
		Imgproc.dilate(modMat, modMat, element);
//		Imgproc.erode(modMat, modMat, element);
		
		Mat matchedMat = new Mat();	
        Imgproc.matchTemplate(modMat, tplMat, matchedMat, Imgproc.TM_CCOEFF);
        
        MinMaxLocResult mmr = Core.minMaxLoc(matchedMat);
     
        // Point leftTop = new Point(mmr.maxLoc.x - tplMat.cols() / 2, mmr.maxLoc.y - tplMat.rows() / 2);
        // Point rightBottom = new Point(mmr.maxLoc.x + tplMat.cols() * 3 / 2, mmr.maxLoc.y + tplMat.rows() * 3 / 2);
        Point leftTop = new Point(mmr.maxLoc.x, mmr.maxLoc.y);
        Point rightBottom = new Point(mmr.maxLoc.x + tplMat.cols(), mmr.maxLoc.y + tplMat.rows());
        
    	Imgproc.rectangle(orgMat, leftTop, rightBottom, OpenCVUtils.BORDER_COLOR);
        
        System.out.println("similarity: " + mmr.minVal + ", " + mmr.maxVal);
        
        BufferedImage processImage = OpenCVUtils.createAwtImage(modMat);
        BufferedImage resultImage = OpenCVUtils.createAwtImage(orgMat);
        
        
        return new Object[] { processImage, resultImage, false };
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
		Imgproc.cvtColor(orgMat, modMat, Imgproc.COLOR_BGR2GRAY);
		
		if (Settings.gaussianSize % 2 == 1) {
			Imgproc.GaussianBlur(modMat, modMat, new Size(Settings.gaussianSize, Settings.gaussianSize), 1);
		}
		
		Imgproc.threshold(modMat, modMat, Settings.threshold, 255, Imgproc.THRESH_BINARY);
		
		Mat matchedMat = new Mat(orgMat.rows() - tplMat.rows(), orgMat.cols() - tplMat.cols(), CvType.CV_32FC1);	
        Imgproc.matchTemplate(modMat, tplMat, matchedMat, Imgproc.TM_CCOEFF);
        
        MinMaxLocResult mmr = Core.minMaxLoc(matchedMat);
     
        // Core.normalize(matchedMat, matchedMat);
        
        Point locStart = mmr.maxLoc;
        Point locEnd = new Point(locStart.x + tplWidth, locStart.y + tplHeight);
    	
        
        // System.out.println("similarity: " + mmr.minVal + ", " + mmr.maxVal);
        
    	Mat capturedMat = new Mat(modMat, new Rect(locStart, locEnd));
    	// Mat capturedMat = new Mat(orgMat, new Rect(locStart, locEnd));
    	
//    	Object[] res = findPad(capturedMat);
//    	// Mat[] res2 = FeatureExtractor.extractFeature2(capturedMat);
//
//    	// if pad not found
//    	if (!(boolean)res[1]) { 
//    		locStart = new Point(0, 0);
//    	} else {
//    		Imgproc.rectangle(orgMat, locStart, locEnd, OpenCVUtils.BORDER_COLOR);
//    	}
    	
    	Imgproc.rectangle(orgMat, locStart, locEnd, OpenCVUtils.BORDER_COLOR);
    	
    	int moveInstructor = MoveInstructor.instruct(orgMat.cols(), locStart, locEnd);
    	
        // BufferedImage processImage = OpenCVUtils.createAwtImage(res2[0]); // OpenCVUtils.createAwtImage(modMat);
        // BufferedImage processImage = OpenCVUtils.createAwtImage(orgMat);
        // BufferedImage processImage = OpenCVUtils.createAwtImage(capturedMat); // OpenCVUtils.createAwtImage(modMat);
        BufferedImage resultImage = OpenCVUtils.createAwtImage(orgMat);
        BufferedImage processImage = OpenCVUtils.createAwtImage(modMat);
        // BufferedImage capturedImage = OpenCVUtils.createAwtImage((Mat) res[0]);
        BufferedImage capturedImage = OpenCVUtils.createAwtImage(capturedMat);
        
        return new Object[] { resultImage, processImage, capturedImage, moveInstructor };
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
	
	/**
	 * simple comparing with a known template
	 * using matchTemplate function 
	 * 
	 * @param source
	 * @return
	 */
	public static Object[] processImage0(Image source) {
		Mat orgMat = OpenCVUtils.openImage(source);
		
		Mat modMat = new Mat();
		Imgproc.cvtColor(orgMat, modMat, Imgproc.COLOR_BGR2GRAY);
		
		Mat matchedMat = new Mat();	
        Imgproc.matchTemplate(modMat, tplMat, matchedMat, Imgproc.TM_CCOEFF);
        
        MinMaxLocResult mmr = Core.minMaxLoc(matchedMat);
     
        Point leftTop = new Point(mmr.maxLoc.x, mmr.maxLoc.y);
        Point rightBottom = new Point(mmr.maxLoc.x + tplMat.cols(), mmr.maxLoc.y + tplMat.rows());
        
    	Imgproc.rectangle(orgMat, leftTop, rightBottom, OpenCVUtils.BORDER_COLOR);
        
        BufferedImage processImage = OpenCVUtils.createAwtImage(modMat);
        BufferedImage resultImage = OpenCVUtils.createAwtImage(orgMat);
       
        
        return new Object[] { processImage, resultImage, false };
	}
}
