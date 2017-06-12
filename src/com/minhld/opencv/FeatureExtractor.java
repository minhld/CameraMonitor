package com.minhld.opencv;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.minhld.utils.OpenCVUtils;

import sensor_msgs.Image;

public class FeatureExtractor {
	static FeatureDetector detector;
	static DescriptorExtractor descriptor;
	static DescriptorMatcher matcher;
	static Mat tplMat, tplDesc;
	static MatOfKeyPoint tplKeys;
	
	static {
		FeatureExtractor.init2();
		// FeatureExtractor.init();
	}
	
	public static void init2() {
		detector = FeatureDetector.create(FeatureDetector.ORB);
        descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

		Mat tpl = Imgcodecs.imread("samples/tpl11.png");
		tplMat = new Mat();
		Imgproc.cvtColor(tpl, tplMat, Imgproc.COLOR_BGR2GRAY);
		tplDesc = new Mat();
		tplKeys = new MatOfKeyPoint();
        detector.detect(tplMat, tplKeys);
        descriptor.compute(tplMat, tplKeys, tplDesc);
	}
	
//	public static void init() {
//		detector = FeatureDetector.create(FeatureDetector.ORB);
//        descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
//        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
//
//		Mat tpl = Imgcodecs.imread("samples/tpl3.png");
//		tplMat = new Mat();
//		Imgproc.cvtColor(tpl, tplMat, Imgproc.COLOR_BGR2GRAY);
//		tplDesc = new Mat();
//		tplKeys = new MatOfKeyPoint();
//        detector.detect(tplMat, tplKeys);
//        descriptor.compute(tplMat, tplKeys, tplDesc);
//	}
	
	public static Mat[] processImage2(Mat orgMat) {
		try {
			//Imgproc.cvtColor(orgMat, orgMat, Imgproc.COLOR_RGB2GRAY);
			Mat orgDesc = new Mat();
			MatOfKeyPoint orgKeys = new MatOfKeyPoint();
	        detector.detect(orgMat, orgKeys);
	        descriptor.compute(orgMat, orgKeys, orgDesc);
	        
	        Mat outputTpl = new Mat();
	        Features2d.drawKeypoints(tplMat, tplKeys, outputTpl);
	
	        // Matching
	        MatOfDMatch matches = new MatOfDMatch();
	        if (tplMat.type() == orgMat.type()) {
	            matcher.match(tplDesc, orgDesc, matches);
	        } 
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
	
//	        LinkedList<DMatch> good_matches = new LinkedList<DMatch>();
//	        for (int i = 0; i < matchesList.size(); i++) {
//	            if (matchesList.get(i).distance <= (1.5 * min_dist))
//	                good_matches.addLast(matchesList.get(i));
//	        }
	
//	        MatOfDMatch goodMatches = new MatOfDMatch();
//	        goodMatches.fromList(good_matches);
	        Mat outputImg = new Mat(tplMat.rows() + orgMat.rows(), tplMat.cols() + orgMat.cols(), orgMat.type());
	        MatOfByte drawnMatches = new MatOfByte();

	        Features2d.drawMatches(tplMat, tplKeys, orgMat, orgKeys, matches, outputImg, 
	        			OpenCVUtils.BORDER_COLOR, OpenCVUtils.BORDER_RED_COLOR, drawnMatches, Features2d.NOT_DRAW_SINGLE_POINTS);
	        // Imgproc.resize(outputImg, outputImg, orgMat.size() + tplMat.size());
	
//			
//	        
//	        BufferedImage resultImage = OpenCVUtils.createAwtImage(outputImg);

	        return new Mat[] { outputImg };

		}catch (Exception e) {
			System.err.println(e.getMessage());
			return new Mat[] { orgMat };
		}
	}
	
//	public static Object[] processImage(Image source) {
//		Mat orgMat = OpenCVUtils.openImage(source);
//		try {
//			
//			Imgproc.cvtColor(orgMat, orgMat, Imgproc.COLOR_RGB2GRAY);
//			Mat descriptors2 = new Mat();
//			MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
//	        detector.detect(orgMat, keypoints2);
//	        descriptor.compute(orgMat, keypoints2, descriptors2);
//	
//	        // Matching
//	        MatOfDMatch matches = new MatOfDMatch();
//	        if (tplMat.type() == orgMat.type()) {
//	            matcher.match(tplDesc, descriptors2, matches);
//	        } 
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
//	            if (matchesList.get(i).distance <= (1.1 * min_dist))
//	                good_matches.addLast(matchesList.get(i));
//	        }
//	
//	        MatOfDMatch goodMatches = new MatOfDMatch();
//	        goodMatches.fromList(good_matches);
//	        Mat outputImg = new Mat();
//	        MatOfByte drawnMatches = new MatOfByte();
//	//        if (orgMat.empty() || orgMat.cols() < 1 || orgMat.rows() < 1) {
//	//            return aInputFrame;
//	//        }
//	        Features2d.drawMatches(tplMat, tplKeys, orgMat, keypoints2, goodMatches, outputImg, 
//	        			OpenCVUtils.BORDER_COLOR, OpenCVUtils.BORDER_RED_COLOR, drawnMatches, Features2d.NOT_DRAW_SINGLE_POINTS);
//	        Imgproc.resize(outputImg, outputImg, orgMat.size());
//	
//			
//	        
//	        BufferedImage resultImage = OpenCVUtils.createAwtImage(outputImg);
//	        return new Object[] { resultImage, false };
//
//		}catch (Exception e) {
//			System.err.println(e.getMessage());
//			return new Object[] { OpenCVUtils.createAwtImage(orgMat), false };
//		}
//	}
}
