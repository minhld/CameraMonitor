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
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.minhld.ros.controller.MoveInstructor2;
import com.minhld.utils.OpenCVUtils;
import com.minhld.utils.Settings;

import sensor_msgs.Image;

public class ObjectDetectorRed {
	public static int THRESHOLD_MIN_AREA = 200;

	public static Mat tplMat;
	static int tplWidth, tplHeight;
	
//	static {
//		readTemplate();
//	}
//	
//	public static void readTemplate() {
//		tplMat = Imgcodecs.imread(Settings.templatePath);
//		tplWidth = tplMat.cols();
//		tplHeight = tplMat.rows();
//		Imgproc.cvtColor(tplMat, tplMat, Imgproc.COLOR_BGR2GRAY);
//		Imgproc.threshold(tplMat, tplMat, Settings.threshold, 255, Imgproc.THRESH_BINARY);
//	}
//	
	
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
		
		// set it blur (to remove noise)
		if (Settings.gaussianEnable == 1) {
			Imgproc.GaussianBlur(orgMat, orgMat, new Size(Settings.gaussianSize, Settings.gaussianSize), Settings.gaussianStandardDeviation);
		}
		// Imgproc.GaussianBlur(orgMat, orgMat, new Size(3, 3), 1);
		
		// turns it to HSV color image
		Imgproc.cvtColor(orgMat, modMat, Imgproc.COLOR_BGR2HSV);
		
		Mat lowMask = new Mat(), highMask = new Mat();
		Core.inRange(modMat, new Scalar(Settings.lowHColor, Settings.lowSColor, Settings.lowVColor), new Scalar(Settings.lowHColor + 10, 255, 255), lowMask);
		Core.inRange(modMat, new Scalar(Settings.highHColor, Settings.highSColor, Settings.highVColor), new Scalar(Settings.highHColor + 10, 255, 255), highMask);

		
		// merge the two masks
		Mat finalMask = new Mat();
		Core.addWeighted(lowMask, 1, highMask, 1, 0, finalMask);

		if (Settings.dilateEnable == 1) {
			Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new  Size(Settings.dilateSize, Settings.dilateSize));
			Imgproc.dilate(finalMask, finalMask, element);
			Imgproc.erode(finalMask, finalMask, element);
		}

		
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(finalMask, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		hierarchy.release();

		Point locStartMax = new Point(), locEndMax = new Point();
		
		
		if (contours.size() > 0) {
			MatOfPoint contour;
			double maxContourSize = 0, contourSize;
			Point locStart = new Point(), locEnd = new Point();
			Rect rect;
				
			// fetch through the list of contours
			for(int i = 0; i < contours.size(); i++) {
				contour = contours.get(i);
				
				// get rid of the small objects found in the camera area
				contourSize = Imgproc.contourArea(contour);
				if (contourSize > Settings.contourAreaMin) {
					rect = Imgproc.boundingRect(contour);
					locStart = new Point(rect.x, rect.y);
					locEnd = new Point(rect.x + rect.width, rect.y + rect.height);
					
					
				}
				// define the max area
				if (maxContourSize < contourSize) {
					maxContourSize = contourSize;
					locStartMax = locStart;
					locEndMax = locEnd;
				}
			}
			System.out.println("contour max: " + maxContourSize);
		}

		// draw the rectangle surrounding the object
		Imgproc.rectangle(orgMat, locStartMax, locEndMax, OpenCVUtils.BORDER_COLOR);

//		// capture the image containing the object
//		Mat capturedMat = new Mat(Settings.TEMPLATE_WIDTH, Settings.TEMPLATE_HEIGHT, orgMat.type());
//		if (locEndMax.x > 0 && locEndMax.y > 0) {
//			int centerX = (int) (locStartMax.x + locEndMax.x) / 2;
//			int centerY = (int) (locEndMax.y + locEndMax.y) / 2;
//			if (centerX - Settings.TEMPLATE_WIDTH / 2 > 0 && centerY - Settings.TEMPLATE_HEIGHT / 2 > 0) {
//				capturedMat = new Mat(orgMat, new Rect(new Point(centerX - Settings.TEMPLATE_WIDTH / 2, centerY - Settings.TEMPLATE_HEIGHT / 2), 
//												new Point(centerX + Settings.TEMPLATE_WIDTH / 2, centerY + Settings.TEMPLATE_HEIGHT / 2)));
//			}
//		}
		
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
    	int moveInstructor = MoveInstructor2.instruct(orgMat.cols(), locStartMax, locEndMax);
    	
        BufferedImage resultImage = OpenCVUtils.createAwtImage(orgMat);
        BufferedImage processImage = OpenCVUtils.createAwtImage(finalMask);
        // BufferedImage capturedImage = OpenCVUtils.createAwtImage(capturedMat);
        
        return new Object[] { resultImage, processImage, null, moveInstructor };
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
