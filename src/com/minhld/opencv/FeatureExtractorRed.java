package com.minhld.opencv;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import com.minhld.utils.OpenCVUtils;
import com.minhld.utils.Settings;

public class FeatureExtractorRed {
	static FeatureDetector detector;
	static DescriptorExtractor descriptor;
	static DescriptorMatcher matcher;
	static Mat tplMat, tplDesc, outputTpl;
	static MatOfKeyPoint tplKeys;
	
//	static {
//		FeatureExtractorRed.init();
//	}
//	
//	public static void init() {
//		detector = FeatureDetector.create(FeatureDetector.ORB);
//        descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
//        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
//
//		tplMat = Imgcodecs.imread("samples/tpl11.png");
////		Imgproc.cvtColor(tplMat, tplMat, Imgproc.COLOR_BGR2GRAY);
//		tplDesc = new Mat();
//		tplKeys = new MatOfKeyPoint();
//        detector.detect(tplMat, tplKeys);
//        descriptor.compute(tplMat, tplKeys, tplDesc);
////        outputTpl = new Mat();
////        Features2d.drawKeypoints(tplMat, tplKeys, outputTpl);
//
//	}
	
	/**
	 * 
	 * @param padMat
	 * @return
	 */
	public static Object[] detectLocation(Mat padMat) {
		Object[] results = FeatureExtractorRed.extractFeature(padMat);
		if (results == null) return new Object[] { null, null, 0 };
		
		BufferedImage padEx1 = OpenCVUtils.createAwtImage((Mat) results[0]);
		BufferedImage padEx2 = OpenCVUtils.createAwtImage((Mat) results[1]);
		
		return new Object[] { padEx1, padEx2, results[2] };
	}
	
	/**
	 * 
	 * @param orgMat
	 * @return
	 */
	public static Object[] extractFeature(Mat orgMat) {
		// check if original matrix is null 
		if (orgMat == null) return null;
		
		boolean extractSuccessful = false;
		Mat modMat = new Mat();
		
		// ------ define destination perspective matrix ------ 
		List<Point> destPoints = new ArrayList<>();
		destPoints.add(new Point(5, orgMat.cols() / 2));
		destPoints.add(new Point(orgMat.cols() / 2, 5));
		destPoints.add(new Point(orgMat.cols() - 5, orgMat.cols() / 2));
		destPoints.add(new Point(orgMat.cols() / 2, orgMat.cols() - 5));
		Mat destMat = Converters.vector_Point2f_to_Mat(destPoints);
		
		// turn to black-white
		Imgproc.cvtColor(orgMat, modMat, Imgproc.COLOR_BGR2GRAY);
		
		// threshold to eliminate a number of objects
		Imgproc.threshold(modMat, modMat, Settings.threshold, 255, Imgproc.THRESH_BINARY);
		
		// find contours
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(modMat, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		hierarchy.release();
		
		List<Point> srcPoints = new ArrayList<>();

		Point maxPoint = new Point(0, 0);
		
		// check if the number of contours is more than 3 areas
		if (contours.size() >= 3) {
			MatOfPoint contour;
			double maxContourSize = 0, contourSize;
			
			for(int i = 0; i < contours.size(); i++) {
				contour = contours.get(i);
				contourSize = Imgproc.contourArea(contour);
				
				if (contourSize > 8) {
					Rect rect = Imgproc.boundingRect(contour);
					Point p = new Point(rect.x + rect.width / 2, rect.y + rect.height / 2);
					srcPoints.add(p);
					// System.out.print("(" + p.x + "," + p.y + "," + contourSize + ")");
					
					// define the max area
					if (maxContourSize < contourSize) {
						maxContourSize = contourSize;
						maxPoint = p;
					}
				}
			}
		}
		
		// System.out.println();
		Mat resultMat = new Mat(orgMat.cols(), orgMat.cols(), orgMat.type());
		resultMat.setTo(new Scalar(0, 0, 0));
		double angle = 0;
		
		// place the found points into the correct order for transformation 
		if (srcPoints.size() == 3) {
			List<Point> correctedDestPoints = getCorrectOrder(orgMat, srcPoints, maxPoint);
			
			// check if we are able to gather all enough 4 points for transformation
			// if there are not enough 4 points, the transformation will fail
			if (correctedDestPoints.size() == 4) {
				Mat srcMat = Converters.vector_Point2f_to_Mat(correctedDestPoints);
				Mat persMat = Imgproc.getPerspectiveTransform(srcMat, destMat);
				// Imgproc.warpPerspective(modMat, modMat, persMat, new Size(orgMat.cols(), orgMat.cols()));
				
				// draw markers on the modified matrix
				Point[] transPoints = (Point[]) getMainPoints(orgMat, persMat, maxPoint);
				Imgproc.drawMarker(resultMat, transPoints[0], new Scalar(255, 255, 255), Imgproc.MARKER_CROSS, 5, 1, Imgproc.LINE_8);
				Imgproc.drawMarker(resultMat, transPoints[1], new Scalar(255, 255, 255), Imgproc.MARKER_CROSS, 5, 1, Imgproc.LINE_8);
				Imgproc.drawMarker(resultMat, transPoints[2], new Scalar(255, 255, 255), Imgproc.MARKER_CROSS, 5, 1, Imgproc.LINE_8);

				angle = findAngle(transPoints);
				
				extractSuccessful = true;
			} 
		} 
		
		return extractSuccessful ? new Object[] { orgMat, resultMat, angle } : null;
	}
	
	/**
	 * find the exact view-point by knowing its distance to the
	 * center and the angle it made with Y-axis  
	 * 
	 * @param distance
	 * @param angle
	 * @return
	 */
	public static Point findPointByAngle(double distance, double angle) {
		Point p = new Point();
		double realAngle = 90 + angle;
		p.x = distance * Math.cos(realAngle * Math.PI / 180);
		p.y = distance * Math.sin(realAngle * Math.PI / 180);
		return p;
	}
	
	/**
	 * find the angle between the Y-axis and the view-point by knowing 3 points 
	 * (one on Y-axis, one at the center and one at the view-point)  
	 * 
	 * @param p
	 * @return
	 */
	private static double findAngle(Point[] p) {
		double angba = Math.atan((p[2].y - p[1].y) / (p[2].x - p[1].x));
		double angbc = Math.atan((p[0].y - p[1].y) / (p[0].x - p[1].y));
		double rslt = angba - angbc;
		return (rslt * 180) / Math.PI;
	}
	
	/**
	 * convert 3 main points using the same transformation matrix
	 * 
	 * @param orgMat
	 * @param transfMat
	 * @param maxPoint
	 * @return
	 */
	private static Point[] getMainPoints(Mat orgMat, Mat transfMat, Point maxPoint) {
		// --- 1. prepare for original points --- 
		List<Point> eyePoints = new ArrayList<>();
		// bottom point
		eyePoints.add(new Point(orgMat.cols() / 2, orgMat.rows() - 5));
		// central point
		eyePoints.add(new Point(orgMat.cols() / 2, orgMat.rows() / 2));
		// max point
		eyePoints.add(maxPoint);
		// Imgproc.drawMarker(orgMat, eyePoints.get(0), new Scalar(255, 255, 255));
		// Imgproc.drawMarker(orgMat, eyePoints.get(1), new Scalar(255, 255, 255));
		// Imgproc.drawMarker(orgMat, eyePoints.get(2), new Scalar(255, 255, 255));
		Mat eyePointMat = Converters.vector_Point2f_to_Mat(eyePoints);
		
		// --- 2. transform to find the corresponding transformed points --- 
		Mat transfPointMat = new Mat();
		// Imgproc.(eyePoint, eyeOrgPoint, persMat, new Size(eyePoint.rows(), eyePoint.cols()));
		// Imgproc.warpPerspective(eyePointMat, eyeOrgPointMat, persMat, new Size(1, 3));
		Core.perspectiveTransform(eyePointMat, transfPointMat, transfMat);

		List<Point> transfPoints = new ArrayList<>();
		Converters.Mat_to_vector_Point2f(transfPointMat, transfPoints);
		// System.out.println(transfPoints);
		
		return transfPoints.toArray(new Point[] {});
	}
	
	/**
	 * get the correct order of the three main areas on the pad
	 * (assume that the colors of the areas may change: white or blue) 
	 * 
	 * @param orgMat
	 * @param srcPoint
	 * @param maxPoint
	 * @return
	 */
	private static List<Point> getCorrectOrder(Mat orgMat, List<Point> srcPoint, Point maxPoint) {
		List<Point> points = new ArrayList<>();
		
		// 1. get first and second point, ordered by x-coordinate
		Point temp, firstPoint = new Point(0, 0), secondPoint = new Point(0, 0);
		for (int i = 0; i < srcPoint.size(); i++) {
			temp = srcPoint.get(i);
			
			// check if temp is not the max point
			if (temp.x != maxPoint.x && temp.y != maxPoint.y) {
				if (temp.x >= secondPoint.x) {
					firstPoint = secondPoint;
					secondPoint = temp;
				} else {
					firstPoint = temp;
				}
			}
		}
		
		// 2. establish the line equation going through the two indicated points
		// (y1 – y2)x + (x2 – x1)y + (x1y2 – x2y1) = 0
		
		// 3. check the position of the max point to the above line
		double eval = (firstPoint.y - secondPoint.y) * maxPoint.x + (secondPoint.x - firstPoint.x) * maxPoint.y + 
					(firstPoint.x * secondPoint.y - secondPoint.x * firstPoint.y);
		
		if (eval < 0) {
			// the max point is higher than the line
			// --> the order will be first point, max point and second point
			points.add(firstPoint);
			points.add(maxPoint);
			points.add(secondPoint);
		} else {
			// the max point is higher than the line
			// --> the order will be second point, max point and first point
			points.add(secondPoint);
			points.add(maxPoint);
			points.add(firstPoint);
		}
		
		// add the symmetric point of the max point
		Point symPoint = new Point(orgMat.cols() - maxPoint.x, orgMat.rows() - maxPoint.y);
		points.add(symPoint);

		return points;
	}
	
//	public static Mat[] extractFeature2(Mat orgMat) {
//		try {
////			Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new  Size(3, 3));
////			Imgproc.dilate(orgMat, orgMat, element);
////			Imgproc.erode(orgMat, orgMat, element);
//
//			Mat[] results = drawContours(orgMat);
//			
//	        return new Mat[] { results[0], results[1] };
//
//		}catch (Exception e) {
//			System.err.println(e.getMessage());
//			return new Mat[] { orgMat };
//		}
//	}
//	
//	public static Mat[] extractFeature3(Mat orgMat) {
//		try {
//			// Mat orgMat = OpenCVUtils.openImage(source);
//			Imgproc.GaussianBlur(orgMat, orgMat, new Size(Settings.gaussianSize, Settings.gaussianSize), Settings.gaussianStandardDeviation);
//
//			Mat modMat = new Mat();
//			
//			// turn to black-white
//			Imgproc.cvtColor(orgMat, modMat, Imgproc.COLOR_BGR2GRAY);
//			
//			// threshold to eliminate a number of objects
//			Imgproc.threshold(modMat, modMat, Settings.threshold, 255, Imgproc.THRESH_BINARY);
//			
//			Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new  Size(Settings.dilateSize, Settings.dilateSize));
//			Imgproc.erode(modMat, modMat, element);
//			Imgproc.dilate(modMat, modMat, element);
//
////			Imgproc.cvtColor(orgMat, orgMat, Imgproc.COLOR_RGB2GRAY);
//			Mat orgDesc = new Mat();
//			MatOfKeyPoint orgKeys = new MatOfKeyPoint();
//	        detector.detect(orgMat, orgKeys);
//	        descriptor.compute(orgMat, orgKeys, orgDesc);
//	        
//////	        Mat outputImg = new Mat();
//////	        Features2d.drawKeypoints(orgMat, orgKeys, outputImg);
//////	
////	        
////	        Mat outputTpl = new Mat();
////	        Features2d.drawKeypoints(tplMat, tplKeys, outputTpl);
////	
////	        // Matching
////	        MatOfDMatch matches = new MatOfDMatch();
////	        if (tplMat.type() == orgMat.type()) {
//////	            matcher.match(tplDesc, orgDesc, matches);
////	        } 
//
//	        // Imgproc.goodFeaturesToTrack
//	        
////	        List<DMatch> matchesList = matches.toList();
////	
////	        Double max_dist = 0.0;
////	        Double min_dist = 100.0;
////	
////	        for (int i = 0; i < matchesList.size(); i++) {
////	            Double dist = (double) matchesList.get(i).distance;
////	            if (dist < min_dist)
////	                min_dist = dist;
////	            if (dist > max_dist)
////	                max_dist = dist;
////	        }
////	
////	        LinkedList<DMatch> good_matches = new LinkedList<DMatch>();
////	        for (int i = 0; i < matchesList.size(); i++) {
////	            if (matchesList.get(i).distance <= (1.5 * min_dist))
////	                good_matches.addLast(matchesList.get(i));
////	        }
////	
////	        MatOfDMatch goodMatches = new MatOfDMatch();
////	        goodMatches.fromList(good_matches);
////	        
////	        
////	        Mat outputImg = new Mat(tplMat.rows() + orgMat.rows(), tplMat.cols() + orgMat.cols(), orgMat.type());
////	        MatOfByte drawnMatches = new MatOfByte();
////
//////	        Features2d.drawMatches(tplMat, tplKeys, orgMat, orgKeys, goodMatches, outputImg, 
//////	        			OpenCVUtils.BORDER_COLOR, OpenCVUtils.BORDER_RED_COLOR, drawnMatches, Features2d.NOT_DRAW_SINGLE_POINTS);
////
////	        Features2d.drawMatches(tplMat, tplKeys, orgMat, orgKeys, matches, outputImg, 
////	        			OpenCVUtils.BORDER_COLOR, OpenCVUtils.BORDER_RED_COLOR, drawnMatches, Features2d.NOT_DRAW_SINGLE_POINTS);
////
//////	        Imgproc.resize(outputImg, outputImg, orgMat.size() + tplMat.size());
////
//////	        BufferedImage resultImage = OpenCVUtils.createAwtImage(outputImg);
//
//			Mat[] results = drawContours(modMat);
//			
//	        return new Mat[] { results[0], results[1] };
//
//		}catch (Exception e) {
//			System.err.println(e.getMessage());
//			return new Mat[] { orgMat };
//		}
//	}
//	
//	/**
//	 * finding the circle - updated version
//	 * 
//	 * @param source
//	 * @return
//	 */
//	public static Mat[] drawContours(Mat src) {
////		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new  Size(3, 3));
////		Imgproc.dilate(src, src, element);
////		Imgproc.erode(src, src, element);
//		
//		// find contours
//		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//		Mat hierarchy = new Mat();
//		Imgproc.findContours(src, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
//		hierarchy.release();
//
////		Point locStartMax = new Point(), locEndMax = new Point();
//		
//		ArrayList<MatOfPoint> maxContours = new ArrayList<MatOfPoint>();
//		
//		
//		
//		if (contours.size() > 0) {
//			MatOfPoint maxContour = new MatOfPoint(), maxContour2 = new MatOfPoint(), contour;
//			double maxContourSize = 0, contourSize;
////			Point locStart = new Point(), locEnd = new Point();
////			Rect rect;
//				
//			// fetch through the list of contours
//			for(int i = 0; i < contours.size(); i++) {
//				contour = contours.get(i);
//				
//				// get rid of the small objects found in the camera area
//				contourSize = Imgproc.contourArea(contour);
////				if (contourSize > 100 && contourSize < 2000) {
////					rect = Imgproc.boundingRect(contour);
////					locStart = new Point(rect.x, rect.y);
////					locEnd = new Point(rect.x + rect.width, rect.y + rect.height);
//					
////					maxContours.add(contour);
////				}
//				// define the max area
//				if (maxContourSize < contourSize) {
//					maxContourSize = contourSize;
//					
//					maxContour2 = maxContour;
//					maxContour = contour;
//					
////					locStartMax = locStart;
////					locEndMax = locEnd;
//					
//				}
//			}
////			System.out.println("contour max: " + maxContourSize);
//			
//			maxContours.add(maxContour2);
//		}
//
//		Mat src1 = new Mat(src.rows(), src.cols(), src.type(), new Scalar(255, 255, 255));
//		
//		Imgproc.drawContours(src1, maxContours, 0, OpenCVUtils.BORDER_RED_COLOR, 1);
//       
//        return new Mat[] { src, src1 } ;
//	}
}
