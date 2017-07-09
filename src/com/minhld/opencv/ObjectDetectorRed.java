package com.minhld.opencv;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.TreeMap;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.minhld.utils.OpenCVUtils;
import com.minhld.utils.Settings;

import sensor_msgs.Image;

public class ObjectDetectorRed {
	public static final int NUM_MAX_CONTOURS = 3;
	
	/**
	 * comparing with a known template
	 * using matchTemplate function 
	 * 
	 * @param source
	 * @return
	 */
	public static Object[] processImage(Image source) {
		// ------ 1. read the source to Mat ------
		long start = System.currentTimeMillis();
		
		// Mat orgMat = Imgcodecs.imread("samples/multiobjects.png");
		Mat orgMat = OpenCVUtils.openImage(source);
		
		long readImageTime = System.currentTimeMillis() - start;
		
		Mat modMat = new Mat();
		
		// ------ 2. apply gaussian convolution ------ 
		start = System.currentTimeMillis();
		
		// set it blur (to remove noise)
		if (Settings.gaussianEnable == 1) {
			Imgproc.GaussianBlur(orgMat, orgMat, new Size(Settings.gaussianSize, Settings.gaussianSize), Settings.gaussianStandardDeviation);
		}
		// Imgproc.GaussianBlur(orgMat, orgMat, new Size(3, 3), 1);
		
		long gaussianTime = System.currentTimeMillis() - start;
		
		// ------ 3. turns it to HSV color image ------
		start = System.currentTimeMillis();
		
		// 3.1. convert the image to HSV color template
		Imgproc.cvtColor(orgMat, modMat, Imgproc.COLOR_BGR2HSV);
		
		// 3.2. filter out the red color in a wide range 
		Mat lowMask = new Mat(), highMask = new Mat();
		Core.inRange(modMat, new Scalar(Settings.lowHColor, Settings.lowSColor, Settings.lowVColor), new Scalar(Settings.lowHColor + 10, 255, 255), lowMask);
		Core.inRange(modMat, new Scalar(Settings.highHColor, Settings.highSColor, Settings.highVColor), new Scalar(Settings.highHColor + 10, 255, 255), highMask);
		
		// 3.3 merge the two masks
		Mat finalMask = new Mat();
		Core.addWeighted(lowMask, 1, highMask, 1, 0, finalMask);
		
		long convertHSVTime = System.currentTimeMillis() - start;
		
		// ------ 4. remove more noise ------
		start = System.currentTimeMillis();
		
		if (Settings.dilateEnable == 1) {
			Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new  Size(Settings.dilateSize, Settings.dilateSize));
			Imgproc.dilate(finalMask, finalMask, element);
			Imgproc.erode(finalMask, finalMask, element);
		}

		long dilateTime = System.currentTimeMillis() - start;
		
		// ------ 5. find the contour of the pad ------ 
		start = System.currentTimeMillis();
		
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
		}
		
		long coutourTime = System.currentTimeMillis() - start;

		// ------ 6. draw the rectangle surrounding the object ------ 
		Mat rectMat = new Mat();
		orgMat.copyTo(rectMat);
		Imgproc.rectangle(rectMat, locStartMax, locEndMax, OpenCVUtils.BORDER_COLOR);
		
		// ------ 7. capture the image containing the object ------ 
		Mat capturedMat = new Mat(1, 1, orgMat.type());
		if (locEndMax.x > 0 && locEndMax.y > 0) {
			int centerX = (int) (locStartMax.x + locEndMax.x) / 2;
			int centerY = (int) (locStartMax.y + locEndMax.y) / 2;

			// redefine the capture screen
			int startPointX = (centerX - Settings.TEMPLATE_WIDTH >= 0) ? centerX - Settings.TEMPLATE_WIDTH : 0;
			int startPointY = (centerY - Settings.TEMPLATE_HEIGHT >= 0) ? centerY - Settings.TEMPLATE_HEIGHT : 0;
			int endPointX = (centerX + Settings.TEMPLATE_WIDTH <= orgMat.cols()) ? centerX + Settings.TEMPLATE_WIDTH : orgMat.cols(); 
			int endPointY = (centerY + Settings.TEMPLATE_HEIGHT <= orgMat.rows()) ? centerY + Settings.TEMPLATE_HEIGHT : orgMat.rows();
		
			capturedMat = new Mat(orgMat, new Rect(startPointX, startPointY, endPointX - startPointX, endPointY - startPointY));
			// capturedMat = new Mat(finalMask, new Rect(startPointX, startPointY, endPointX - startPointX, endPointY - startPointY));
		}

		// ------ 8. prepare to flush out the output results ------ 
		start = System.currentTimeMillis();

		BufferedImage resultImage = OpenCVUtils.createAwtImage(rectMat);
        BufferedImage processImage = OpenCVUtils.createAwtImage(finalMask);
        BufferedImage capturedImage = OpenCVUtils.createAwtImage(capturedMat);

        long bufferImageTime = System.currentTimeMillis() - start;
        
        // ------ 9. get the matrix containing the pad ------ 
        Mat padMat = (locEndMax.x > 0 && locEndMax.y > 0) ? new Mat(orgMat, new Rect(locStartMax, locEndMax)) : null;

        return new Object[] { 	resultImage, 
        						processImage, 
        						capturedImage, 
        						new Rect(locStartMax, locEndMax), 
        						padMat, 
        						new double[] {	readImageTime, 
        										gaussianTime,
        										convertHSVTime,
        										dilateTime,
        										coutourTime,
        										bufferImageTime
        									} };
        
	}
	
	private static Object[] selectMaxContours(ArrayList<MatOfPoint> contours) {
		TreeMap<Double, MatOfPoint> maxContours = new TreeMap<>();
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
		}
		
		return null;
	}
	
	private static boolean checkPad() {
		return true;
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
