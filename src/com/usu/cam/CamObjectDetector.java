package com.usu.cam;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.minhld.utils.OpenCVUtils;
import com.minhld.utils.Settings;

import sensor_msgs.Image;

public class CamObjectDetector {
	static Size sz640 = new Size(640, 480);
	static Size sz320 = new Size(320, 240);
	
	// https://ratiler.wordpress.com/2014/09/08/detection-de-mouvement-avec-javacv/	
	/*
	public static HOGDescriptor hog;
	
	static {
		hog = new HOGDescriptor();
		hog.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector());
	}
	*/
	
	static Mat[] mats = new Mat[3];
	static int count = 0;

	public static Object[] processImage(Image source) {
		Mat orgMat = OpenCVUtils.openImage(source);
		Object[] rets = detectMotion(orgMat);
		BufferedImage resultImage = rets != null ? OpenCVUtils.createAwtImage((Mat) rets[0]) : null;
		if (resultImage != null) {
			rets[0] = resultImage;
			return rets;
		}
		return null;
	}

	
	public static Object[] processImage(Mat orgMat) {
		Object[] rets = detectMotion(orgMat);
		BufferedImage resultImage = rets != null ? OpenCVUtils.createAwtImage((Mat) rets[0]) : null;
		if (resultImage != null) {
			rets[0] = resultImage;
			return rets;
		}
		return null;
	}
	
	/*
	public static Object[] processImage(Image source) {
		// ------ 1. read the source to Mat ------
		long start = System.currentTimeMillis();
		
		// Mat orgMat = Imgcodecs.imread("samples/multiobjects.png");
		Mat orgMat = OpenCVUtils.openImage(source);
		
		long readImageTime = System.currentTimeMillis() - start;
	
		Mat drawnMat = detectPeople(orgMat);
		
		BufferedImage resultImage = OpenCVUtils.createAwtImage(drawnMat);

		return new Object[] { resultImage };
	}
	*/
	
	
	public static Object[] detectMotion(Mat frame) {
		Imgproc.resize(frame, frame, sz640);
		Mat orgMat = frame.clone();
		
		long start = System.currentTimeMillis();
		
		mats[count] = new Mat(orgMat.size(), CvType.CV_8UC1);
		Imgproc.cvtColor(orgMat, mats[count], Imgproc.COLOR_RGB2GRAY);
		Imgproc.GaussianBlur(mats[count], mats[count], new Size(3, 3), 0);
		
		long gaussianTime = System.currentTimeMillis() - start;
		
		// enough 3 images
		if (count < 1) {
			count++;
		} else {
			Mat diff = new Mat(orgMat.size(), CvType.CV_8UC1);
			
			start = System.currentTimeMillis();
			
			Core.subtract(mats[1], mats[0], diff);
			Imgproc.adaptiveThreshold(diff, diff, 255,
		                  Imgproc.ADAPTIVE_THRESH_MEAN_C,
		                  Imgproc.THRESH_BINARY_INV, 5, 2);
			
			long diffTime = System.currentTimeMillis() - start;
			
			Object[] cons = getContours(frame, diff);
			
			count = 0;
			
			return new Object[] { cons[0],
								  gaussianTime, 
								  diffTime, 
								  cons[1], cons[2], cons[3] }; 
		}
		
		return null;
	}
	
	/**
	 * get contours of the different parts 
	 * 
	 * @param frame
	 * @param diff
	 * @return
	 */
	private static Object[] getContours(Mat frame, Mat diff) {
		long start = System.currentTimeMillis();
		
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(diff, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		hierarchy.release();
		
		long findContourTime = System.currentTimeMillis() - start;
		
		start = System.currentTimeMillis();
		
		int contourCount = 0;
		
		if (contours.size() > 0) {
			MatOfPoint contour;
			double contourSize;
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
					Imgproc.rectangle(frame, locStart, locEnd, OpenCVUtils.BORDER_COLOR);
					contourCount++;
				}
			}
		}
		
		long drawContourTime = System.currentTimeMillis() - start;
		
		return new Object[] { frame, 
							  findContourTime, 
							  drawContourTime, 
							  contourCount };
	}
	
	/*
	public static Mat detectPeople(Mat orgMat) {
		MatOfRect foundLocations = new MatOfRect();
		MatOfDouble foundWeights = new MatOfDouble();
        Size winStride = new Size(8, 8);
        Size padding = new Size(32, 32);

        Point rectPoint1 = new Point();
        Point rectPoint2 = new Point();
        Point fontPoint = new Point();
        Scalar rectColor = new Scalar(0, 255, 0);
        int rectWidth = 2;
        
        int framesWithPeople = 0;

		hog.detectMultiScale(orgMat, foundLocations, foundWeights, 0.6, winStride, padding, 1.05, 2.0, false);
		
		if (foundLocations.rows() > 0) {
            framesWithPeople++;
            List<Double> weightList = foundWeights.toList();
            List<Rect> rectList = foundLocations.toList();
            int index = 0;
            for (Rect rect : rectList) {
                rectPoint1.x = rect.x;
                rectPoint1.y = rect.y;
                rectPoint2.x = rect.x + rect.width;
                rectPoint2.y = rect.y + rect.height;
                
                // Draw rectangle around fond object
                Imgproc.rectangle(orgMat, rectPoint1, rectPoint2, rectColor, rectWidth);
                fontPoint.x = rect.x;
                
                // illustration
                fontPoint.y = rect.y - 3;
                
                // Print weight
                // illustration
                Imgproc.putText(orgMat, String.format("%1.2f", weightList.get(index)), fontPoint,
                        Core.FONT_HERSHEY_PLAIN, 1.5, rectColor, rectWidth, Core.LINE_AA, false);
                index++;
            }
		}
		
		return orgMat;
	}
	*/
	
}
