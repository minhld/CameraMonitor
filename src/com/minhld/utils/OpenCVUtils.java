package com.minhld.utils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import sensor_msgs.Image;

public class OpenCVUtils {
	public static Scalar BORDER_COLOR = new Scalar(0, 255, 0);
	public static Scalar BORDER_RED_COLOR = new Scalar(0, 0, 255);
	
	private static ArrayList<MatOfPoint> tplContours;
	
	private static Mat srcTpl;
	
	/**
	 * prematurely load template before processing 
	 */
	static {
		// preProcess();
		// preProcess2();
		preProcess6();
	}
	
	protected static void preProcess6() {
		Mat orgTplMat = Imgcodecs.imread("samples/tpl9.png");
		Mat grayOrgTplMat = new Mat(orgTplMat.rows(), orgTplMat.cols(), CvType.CV_8UC1);
		Imgproc.cvtColor(orgTplMat, grayOrgTplMat, Imgproc.COLOR_BGR2GRAY);
		
		Mat blurGrayTplMat = new Mat();
		Imgproc.GaussianBlur(grayOrgTplMat, blurGrayTplMat, new Size(3, 3), 0);
		srcTpl = new Mat();
		Imgproc.Canny(blurGrayTplMat, srcTpl, 80, 100);
	}
	
	protected static void preProcess2() {
		Mat orgMat = Imgcodecs.imread("samples/tpl2.png");
		Mat grayOrgMat = new Mat(orgMat.rows(), orgMat.cols(), CvType.CV_8UC1);
		Imgproc.cvtColor(orgMat, grayOrgMat, Imgproc.COLOR_BGR2GRAY);
		
		Mat blurGrayMat = new Mat();
		Imgproc.GaussianBlur(grayOrgMat, blurGrayMat, new Size(3, 3), 0);
		Mat cannyBlurGrayMat = new Mat();
		Imgproc.Canny(blurGrayMat, cannyBlurGrayMat, 80, 100);
		
		tplContours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(cannyBlurGrayMat, tplContours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		hierarchy.release();
		
		System.out.println("tpl contours: " + tplContours.size());
	}
	
	protected static void preProcess() {
		Mat tplMat = Imgcodecs.imread("samples/tpl6.png");
		Mat grayTplMat = new Mat(tplMat.rows(), tplMat.cols(), CvType.CV_8UC1);
		Imgproc.cvtColor(tplMat, grayTplMat, Imgproc.COLOR_BGR2GRAY);
		srcTpl = new Mat(tplMat.rows(), tplMat.cols(), CvType.CV_8UC1);
		Imgproc.threshold(grayTplMat, srcTpl, 230, 255, Imgproc.THRESH_TOZERO + Imgproc.THRESH_BINARY);
	}
	
	// All about it: https://www.intorobotics.com/how-to-detect-and-track-object-with-opencv/
	// http://kazuar.github.io/light-detection-opencv/
	// http://opencv-srf.blogspot.ro/2010/09/object-detection-using-color-seperation.html
	// https://github.com/introlab/find-object/wiki/MultiDetection
	// Android: https://github.com/ongzx/Android_OpenCV_ShapesDetection/blob/master/src/org/opencv/samples/tutorial1/Tutorial1Activity.java
	// http://opencv-python-tutroals.readthedocs.io/en/latest/py_tutorials/py_feature2d/py_feature_homography/py_feature_homography.html
	// Android: Face detection: http://www.embedded.com/design/programming-languages-and-tools/4406164/Developing-OpenCV-computer-vision-apps-for-the-Android-platform
	// Android: https://developer.sonymobile.com/knowledge-base/tutorials/android_tutorial/get-started-with-opencv-on-android/
	
	public static Object[] processImage7(Image source) {
		Mat orgMat = openImage(source);
		// Mat orgMat = Imgcodecs.imread("samples/example.jpg");

		// make it gray
		Mat grayOrgMat = new Mat();
		Imgproc.cvtColor(orgMat, grayOrgMat, Imgproc.COLOR_BGR2GRAY);

		// make it blur - to remove noise
		Mat blurGrayMat = new Mat();
		Imgproc.GaussianBlur(grayOrgMat, blurGrayMat, new Size(5, 5), 5);

		// make it binary - to avoid many objects
		Mat binOrgMat = new Mat();
		Imgproc.threshold(blurGrayMat, binOrgMat, 235, 255, Imgproc.THRESH_BINARY);

		// find edges of the image
		Mat cannyBlurGrayMat = new Mat();
		Imgproc.Canny(blurGrayMat, cannyBlurGrayMat, 10, 250);
		
//		Mat closedMat = new Mat();
//		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(7, 7));
//		Imgproc.morphologyEx(cannyBlurGrayMat, closedMat, Imgproc.MORPH_CLOSE, kernel);
//		
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(cannyBlurGrayMat, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		hierarchy.release();
		
		if (contours.size() > 0) {
			MatOfPoint2f c = new MatOfPoint2f();
			MatOfPoint2f approxC = new MatOfPoint2f();
			
			for(int i = 0; i < contours.size(); i++) {
				// THRESHOLD_MIN_AREA
				contours.get(i).convertTo(c, CvType.CV_32F);
				double cArea = Imgproc.arcLength(c, true);
				Imgproc.approxPolyDP(c, approxC, 0.02 * cArea, true);

				// if (approxC.size().height == 4) {
					Imgproc.drawContours(orgMat, contours, i, BORDER_COLOR, 1);
		            // Rect rect = Imgproc.boundingRect(contours.get(i));
		            // Imgproc.rectangle(orgMat, new Point(rect.x,rect.y), new Point(rect.x + rect.width, rect.y + rect.height), BORDER_COLOR);
				//}
			}
		}
        
        BufferedImage resultImage = createAwtImage(binOrgMat);
        
        return new Object[] { resultImage, false };
	}
	
	
	/**
	 * idea is from here 
	 * - https://pythontips.com/2015/03/11/a-guide-to-finding-books-in-images-using-python-and-opencv/
	 * 
	 * @param source
	 * @return
	 */
	public static Object[] processImage6(Image source) {
		Mat orgMat = openImage(source);
		// Mat orgMat = Imgcodecs.imread("samples/example.jpg");

		// make it gray
		Mat grayOrgMat = new Mat();
		Imgproc.cvtColor(orgMat, grayOrgMat, Imgproc.COLOR_BGR2GRAY);

		// make it blur - to remove noise
		Mat blurGrayMat = new Mat();
		Imgproc.GaussianBlur(grayOrgMat, blurGrayMat, new Size(3, 3), 0);

//		// make it binary - to avoid many objects
//		Mat binOrgMat = new Mat();
//		Imgproc.threshold(blurGrayMat, binOrgMat, 200, 255, Imgproc.THRESH_TOZERO + Imgproc.THRESH_BINARY);

		// find edges of the image
		Mat cannyBlurGrayMat = new Mat();
		Imgproc.Canny(blurGrayMat, cannyBlurGrayMat, 10, 250);
		
		Mat closedMat = new Mat();
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(7, 7));
		Imgproc.morphologyEx(cannyBlurGrayMat, closedMat, Imgproc.MORPH_CLOSE, kernel);
		
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(closedMat, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		hierarchy.release();
		
		if (contours.size() > 0) {
			MatOfPoint2f c = new MatOfPoint2f();
			MatOfPoint2f approxC = new MatOfPoint2f();
			
			for(int i = 0; i < contours.size(); i++) {
				// THRESHOLD_MIN_AREA
				contours.get(i).convertTo(c, CvType.CV_32F);
				double cArea = Imgproc.arcLength(c, true);
				Imgproc.approxPolyDP(c, approxC, 0.02 * cArea, true);

				if (approxC.size().height == 4) {
					Imgproc.drawContours(orgMat, contours, i, BORDER_COLOR, 1);
		            // Rect rect = Imgproc.boundingRect(contours.get(i));
		            // Imgproc.rectangle(orgMat, new Point(rect.x,rect.y), new Point(rect.x + rect.width, rect.y + rect.height), BORDER_COLOR);
				}
			}
		}
        
		BufferedImage processImage = createAwtImage(cannyBlurGrayMat);
        BufferedImage resultImage = createAwtImage(orgMat);
        
        return new Object[] { processImage, resultImage, false };
	}
	
	public static Object[] processImage3(Image source) {
		Mat orgMat = openImage(source);
		Mat grayOrgMat = new Mat(orgMat.rows(), orgMat.cols(), CvType.CV_8UC1);
		Imgproc.cvtColor(orgMat, grayOrgMat, Imgproc.COLOR_BGR2GRAY);
		Mat binOrgMat = new Mat(orgMat.rows(), orgMat.cols(), CvType.CV_8UC1);
		Imgproc.threshold(grayOrgMat, binOrgMat, 220, 255, Imgproc.THRESH_TOZERO + Imgproc.THRESH_BINARY);
		
		Mat matchedMat = new Mat();	
        Imgproc.matchTemplate(binOrgMat, srcTpl, matchedMat, Imgproc.TM_CCOEFF);
        
        MinMaxLocResult mmr = Core.minMaxLoc(matchedMat);
     
        Point matchLoc = mmr.maxLoc;
        Imgproc.rectangle(binOrgMat, matchLoc, new Point(matchLoc.x + srcTpl.cols(), matchLoc.y + srcTpl.rows()), BORDER_COLOR);

//        Imgproc.rectangle(img, mmr.minLoc, mmr.maxLoc, new Scalar(255, 255, 255));
        System.out.println("similarity: " + mmr.minVal + ", " + mmr.maxVal);
        
        BufferedImage resultImage = createAwtImage(binOrgMat);
        
        return new Object[] { resultImage, false };
	}
	
	/**
	 * second process image to detect charging pad
	 * 
	 * @param source
	 * @return
	 */
	public static Object[] processImage2(Image source) {
		Mat srcMat = openImage(source);
	
		Mat outputImage = new Mat();	
   	
        Imgproc.matchTemplate(srcMat, srcTpl, outputImage, Imgproc.TM_CCOEFF);
        
        MinMaxLocResult mmr = Core.minMaxLoc(outputImage);

        Imgproc.rectangle(srcMat, mmr.maxLoc, new Point(mmr.maxLoc.x + srcTpl.cols(), mmr.maxLoc.y + srcTpl.rows()), BORDER_COLOR);
        // Imgproc.rectangle(srcMat, mmr.minLoc, mmr.maxLoc, new Scalar(255, 255, 255));
        System.out.println("similarity: " + mmr.minVal + ", " + mmr.maxVal);
        
        BufferedImage resultImage = createAwtImage(srcMat);
        
        return new Object[] { resultImage, false };
		
	}
	
	
	
	public static Object[] processImage(Image source) {
		BufferedImage resultImage = null;
		Mat img = openImage(source);
		Mat binImg = new Mat(img.rows(), img.cols(), img.type());
		Imgproc.threshold(img, binImg, 235, 255, Imgproc.THRESH_TOZERO + Imgproc.THRESH_BINARY);
		Mat binImg2 = new Mat(img.rows(), img.cols(), CvType.CV_8UC1);
		Imgproc.cvtColor(binImg, binImg2, Imgproc.COLOR_BGR2GRAY);
		
		Mat loc = new Mat();
		Core.findNonZero(binImg2, loc);
		
		// find the coordinates of the charging pad
		double[] coord;
		double minX = 100000, maxX = 0, minY = 100000, maxY = 0;
		for (int i = 0; i < loc.rows(); i++) {
			coord = loc.get(i, 0);
			if (minX > coord[0]) minX = coord[0];
			if (maxX < coord[0]) maxX = coord[0];
			if (minY > coord[1]) minY = coord[1];
			if (maxY < coord[1]) maxY = coord[1];
		}
		
		int avgX = (int) (minX + maxX) / 2;
		int centerX = img.rows() / 2;
		
		boolean isCenter = avgX > centerX - 50 && avgX < centerX + 50;
		
		resultImage = createAwtImage(binImg2);
		
		return new Object[] { resultImage, isCenter };
	}
	
//	public static Object[] processImage(BufferedImage source) {
//		BufferedImage resultImage = null;
//		Mat img = openImage(source);
//		Mat binImg = new Mat(img.rows(), img.cols(), img.type());
//		Imgproc.threshold(img, binImg, 235, 255, Imgproc.THRESH_TOZERO + Imgproc.THRESH_BINARY);
//
////		Mat loc = new Mat();
////		Core.findNonZero(binImg, loc);
////		
////		// find the coordinates of the charging pad
////		double[] coord;
////		double minX = 100000, maxX = 0, minY = 100000, maxY = 0;
////		for (int i = 0; i < loc.rows(); i++) {
////			coord = loc.get(i, 0);
////			if (minX > coord[0]) minX = coord[0];
////			if (maxX < coord[0]) maxX = coord[0];
////			if (minY > coord[1]) minY = coord[1];
////			if (maxY < coord[1]) maxY = coord[1];
////		}
////		
////		int avgX = (int) (minX + maxX) / 2;
////		int centerX = img.rows() / 2;
//		
//		boolean isCenter = false;// avgX > centerX - 50 && avgX < centerX + 50;
//		
//		resultImage = createAwtImage(binImg);
//		
//		return new Object[] { resultImage, isCenter };
//	}
	
	public static BufferedImage createAwtImage(Mat mat) {

	    int type = 0;
	    if (mat.channels() == 1) {
	        type = BufferedImage.TYPE_BYTE_GRAY;
	    } else if (mat.channels() == 3) {
	        type = BufferedImage.TYPE_3BYTE_BGR;
	    } else {
	        return null;
	    }

	    BufferedImage image = new BufferedImage(mat.width(), mat.height(), type);
	    WritableRaster raster = image.getRaster();
	    DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
	    byte[] data = dataBuffer.getData();
	    mat.get(0, 0, data);

	    return image;
	}
	
	public static BufferedImage getBufferedImage(Image image) {
		Mat matImg = openImage(image);
		return createAwtImage(matImg);
	}
	
//	public static Mat openImage(BufferedImage source) {
//		byte[] pixels = ((DataBufferByte) source.getRaster().getDataBuffer()).getData();
//        Mat cvImage = new Mat(source.getHeight(),source.getWidth(), CvType.CV_8UC3);
//        cvImage.put(0, 0, pixels);
//        return cvImage;
//	}
	
	public static Mat openImage(Image source) {
		byte[] imageInBytes = source.getData().array();
        imageInBytes = Arrays.copyOfRange(imageInBytes,source.getData().arrayOffset(),imageInBytes.length);
        Mat cvImage = new Mat(source.getHeight(),source.getWidth(), CvType.CV_8UC3);
        cvImage.put(0, 0, imageInBytes);
        return cvImage;
        
	}
}
