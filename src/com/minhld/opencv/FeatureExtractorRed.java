package com.minhld.opencv;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import com.minhld.utils.OpenCVUtils;
import com.minhld.utils.Settings;

import sensor_msgs.Image;


public class FeatureExtractorRed {
	static FeatureDetector detector;
	static DescriptorExtractor descriptor;
	static DescriptorMatcher matcher;
	static Mat tplMat, tplDesc, outputTpl;
	static MatOfKeyPoint tplKeys;
	
	static {
		FeatureExtractorRed.init();
	}
	
	public static void init() {
		detector = FeatureDetector.create(FeatureDetector.ORB);
        descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

		tplMat = Imgcodecs.imread("samples/tpl11.png");
//		Imgproc.cvtColor(tplMat, tplMat, Imgproc.COLOR_BGR2GRAY);
		tplDesc = new Mat();
		tplKeys = new MatOfKeyPoint();
        detector.detect(tplMat, tplKeys);
        descriptor.compute(tplMat, tplKeys, tplDesc);
//        outputTpl = new Mat();
//        Features2d.drawKeypoints(tplMat, tplKeys, outputTpl);

	}
	
	public static Mat[] extractFeature3(Mat orgMat) { 
		Mat modMat = new Mat();
		
		// ------ define destination perspective matrix ------ 
		List<Point> destPoints = new ArrayList<>();
		destPoints.add(new Point(10, orgMat.cols() / 2));
		destPoints.add(new Point(orgMat.cols() / 2, 3));
		destPoints.add(new Point(orgMat.cols() - 10, orgMat.cols() / 2));
		destPoints.add(new Point(orgMat.cols() / 2, orgMat.cols() - 3));
		Mat destMat = Converters.vector_Point2f_to_Mat(destPoints);
		
		// turn to black-white
		Imgproc.cvtColor(orgMat, modMat, Imgproc.COLOR_BGR2GRAY);
		
		// threshold to eliminate a number of objects
		Imgproc.threshold(modMat, modMat, Settings.threshold, 255, Imgproc.THRESH_BINARY);
		
//		Imgproc.Canny(modMat, modMat, 10, 255);
		
		// find contours
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(modMat, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		hierarchy.release();
		
		List<Point> srcPoints = new ArrayList<>();

		Point maxPoint = new Point(0, 0);
		if (contours.size() >= 3) {
			MatOfPoint contour;
			double maxContourSize = 0, contourSize;
			
			for(int i = 0; i < contours.size(); i++) {
				contour = contours.get(i);
				contourSize = Imgproc.contourArea(contour);
				
				if (contourSize > 10) {
					Rect rect = Imgproc.boundingRect(contour);
					Point p = new Point(rect.x + rect.width / 2, rect.y + rect.height / 2);
					srcPoints.add(0, p);
					System.out.print("(" + p.x + "," + p.y + "," + contourSize + ")");
					
					// define the max area
					if (maxContourSize < contourSize) {
						maxContourSize = contourSize;
						maxPoint = p;
					}
				}
			}
		}
		
		System.out.println();
		Point symPoint = new Point(orgMat.cols() - maxPoint.x, orgMat.rows() - maxPoint.y);
		srcPoints.add(0, symPoint);
		
		if (srcPoints.size() == 4) {
			Mat srcMat = Converters.vector_Point2f_to_Mat(srcPoints);
			Mat persMat = Imgproc.getPerspectiveTransform(srcMat, destMat);
			Imgproc.warpPerspective(orgMat, modMat, persMat, new Size(orgMat.cols(), orgMat.cols()));
		}
		
		return new Mat[] { orgMat, modMat };
	}
	
	public static Mat[] extractFeature2(Mat orgMat) {
		try {
//			Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new  Size(3, 3));
//			Imgproc.dilate(orgMat, orgMat, element);
//			Imgproc.erode(orgMat, orgMat, element);

			Mat[] results = drawContours(orgMat);
			
	        return new Mat[] { results[0], results[1] };

		}catch (Exception e) {
			System.err.println(e.getMessage());
			return new Mat[] { orgMat };
		}
	}
	
	public static Mat[] extractFeature(Mat orgMat) {
		try {
			// Mat orgMat = OpenCVUtils.openImage(source);
			Imgproc.GaussianBlur(orgMat, orgMat, new Size(Settings.gaussianSize, Settings.gaussianSize), Settings.gaussianStandardDeviation);

			Mat modMat = new Mat();
			
			// turn to black-white
			Imgproc.cvtColor(orgMat, modMat, Imgproc.COLOR_BGR2GRAY);
			
			// threshold to eliminate a number of objects
			Imgproc.threshold(modMat, modMat, Settings.threshold, 255, Imgproc.THRESH_BINARY);
			
//			Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new  Size(Settings.dilateSize, Settings.dilateSize));
//			Imgproc.erode(modMat, modMat, element);
//			Imgproc.dilate(modMat, modMat, element);

			
			
////			Imgproc.cvtColor(orgMat, orgMat, Imgproc.COLOR_RGB2GRAY);
//			Mat orgDesc = new Mat();
//			MatOfKeyPoint orgKeys = new MatOfKeyPoint();
//	        detector.detect(orgMat, orgKeys);
//	        descriptor.compute(orgMat, orgKeys, orgDesc);
//	        
////	        Mat outputImg = new Mat();
////	        Features2d.drawKeypoints(orgMat, orgKeys, outputImg);
////	
//	        
//	        Mat outputTpl = new Mat();
//	        Features2d.drawKeypoints(tplMat, tplKeys, outputTpl);
//	
//	        // Matching
//	        MatOfDMatch matches = new MatOfDMatch();
//	        if (tplMat.type() == orgMat.type()) {
////	            matcher.match(tplDesc, orgDesc, matches);
//	        } 

	        // Imgproc.goodFeaturesToTrack
	        
//	        List<DMatch> matchesList = matches.toList();
//	
//	        Double max_dist = 0.0;
//	        Double min_dist = 100.0;
//	
//	        for (int i = 0; i < matchesList.size(); i++) {
//	            Double dist = (double) matchesList.get(i).distance;
//	            if (dist < min_dist)
//	                min_dist = dist;
//	            if (dist > max_dist)
//	                max_dist = dist;
//	        }
//	
//	        LinkedList<DMatch> good_matches = new LinkedList<DMatch>();
//	        for (int i = 0; i < matchesList.size(); i++) {
//	            if (matchesList.get(i).distance <= (1.5 * min_dist))
//	                good_matches.addLast(matchesList.get(i));
//	        }
//	
//	        MatOfDMatch goodMatches = new MatOfDMatch();
//	        goodMatches.fromList(good_matches);
//	        
//	        
//	        Mat outputImg = new Mat(tplMat.rows() + orgMat.rows(), tplMat.cols() + orgMat.cols(), orgMat.type());
//	        MatOfByte drawnMatches = new MatOfByte();
//
////	        Features2d.drawMatches(tplMat, tplKeys, orgMat, orgKeys, goodMatches, outputImg, 
////	        			OpenCVUtils.BORDER_COLOR, OpenCVUtils.BORDER_RED_COLOR, drawnMatches, Features2d.NOT_DRAW_SINGLE_POINTS);
//
//	        Features2d.drawMatches(tplMat, tplKeys, orgMat, orgKeys, matches, outputImg, 
//	        			OpenCVUtils.BORDER_COLOR, OpenCVUtils.BORDER_RED_COLOR, drawnMatches, Features2d.NOT_DRAW_SINGLE_POINTS);
//
////	        Imgproc.resize(outputImg, outputImg, orgMat.size() + tplMat.size());
//
////	        BufferedImage resultImage = OpenCVUtils.createAwtImage(outputImg);

			Mat[] results = drawContours(modMat);
			
	        return new Mat[] { results[0], results[1] };

		}catch (Exception e) {
			System.err.println(e.getMessage());
			return new Mat[] { orgMat };
		}
	}
	
	/**
	 * finding the circle - updated version
	 * 
	 * @param source
	 * @return
	 */
	public static Mat[] drawContours(Mat src) {
//		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new  Size(3, 3));
//		Imgproc.dilate(src, src, element);
//		Imgproc.erode(src, src, element);
		
		// find contours
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(src, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		hierarchy.release();

//		Point locStartMax = new Point(), locEndMax = new Point();
		
		ArrayList<MatOfPoint> maxContours = new ArrayList<MatOfPoint>();
		
		
		
		if (contours.size() > 0) {
			MatOfPoint maxContour = new MatOfPoint(), maxContour2 = new MatOfPoint(), contour;
			double maxContourSize = 0, contourSize;
//			Point locStart = new Point(), locEnd = new Point();
//			Rect rect;
				
			// fetch through the list of contours
			for(int i = 0; i < contours.size(); i++) {
				contour = contours.get(i);
				
				// get rid of the small objects found in the camera area
				contourSize = Imgproc.contourArea(contour);
//				if (contourSize > 100 && contourSize < 2000) {
//					rect = Imgproc.boundingRect(contour);
//					locStart = new Point(rect.x, rect.y);
//					locEnd = new Point(rect.x + rect.width, rect.y + rect.height);
					
//					maxContours.add(contour);
//				}
				// define the max area
				if (maxContourSize < contourSize) {
					maxContourSize = contourSize;
					
					maxContour2 = maxContour;
					maxContour = contour;
					
//					locStartMax = locStart;
//					locEndMax = locEnd;
					
				}
			}
//			System.out.println("contour max: " + maxContourSize);
			
			maxContours.add(maxContour2);
		}

		Mat src1 = new Mat(src.rows(), src.cols(), src.type(), new Scalar(255, 255, 255));
		
		Imgproc.drawContours(src1, maxContours, 0, OpenCVUtils.BORDER_RED_COLOR, 1);
       
        return new Mat[] { src, src1 } ;
	}
}
