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
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import sensor_msgs.Image;

public class OpenCVUtils {
	private static Scalar drawColor = new Scalar(255, 255, 255);
	
	private static Mat tplMat = Imgcodecs.imread("samples/design6.png");
	// private static Mat tplMat = Imgcodecs.imread("samples/tpl8.png");
	
	private static Mat srcTpl;
	
	/**
	 * prematurely load template before processing 
	 */
	static {
		Mat grayTplMat = new Mat(tplMat.rows(), tplMat.cols(), CvType.CV_8UC1);
		Imgproc.cvtColor(tplMat, grayTplMat, Imgproc.COLOR_BGR2GRAY);
		srcTpl = new Mat(tplMat.rows(), tplMat.cols(), CvType.CV_8UC1);
		Imgproc.threshold(grayTplMat, srcTpl, 230, 255, Imgproc.THRESH_TOZERO + Imgproc.THRESH_BINARY);
	}
	
	public static Object[] processImage6(Image source) {
		Mat img = openImage(source);
		Mat grayMat = new Mat(img.rows(), img.cols(), CvType.CV_8UC1);
		Imgproc.cvtColor(img, grayMat, Imgproc.COLOR_BGR2GRAY);
//		
//		Mat blurMat = new Mat();
//		
//		Imgproc.GaussianBlur(grayMat, blurMat, new Size(3, 3), 12);
        
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(grayMat, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

		hierarchy.release();
		
		if (contours.size() > 0) {
			for(int i = 0; i < contours.size(); i++) {
				// Imgproc.drawContours(blurMat, contours, i, drawColor, 3);
	            Rect rect = Imgproc.boundingRect(contours.get(i));
	            Imgproc.rectangle(grayMat, new Point(rect.x,rect.height), new Point(rect.y,rect.width), drawColor);
			}
		}
		
		
        // Point matchLoc = mmr.maxLoc;
        // Imgproc.rectangle(img, matchLoc, new Point(matchLoc.x + srcTpl.cols(), matchLoc.y + srcTpl.rows()), 
        // 											new Scalar(255, 255, 255));

        // Imgproc.rectangle(img, mmr.minLoc, mmr.maxLoc, new Scalar(255, 255, 255));
        // System.out.println("similarity: " + mmr.minVal + ", " + mmr.maxVal);
        
        BufferedImage resultImage = createAwtImage(grayMat);
        
        return new Object[] { resultImage, false };
	}
	
	public static Object[] processImage3(Image source) {
		Mat img = openImage(source);
		Mat grayMat = new Mat(img.rows(), img.cols(), CvType.CV_8UC1);
		Imgproc.cvtColor(img, grayMat, Imgproc.COLOR_BGR2GRAY);
		Mat binMat = new Mat(img.rows(), img.cols(), CvType.CV_8UC1);
		Imgproc.threshold(grayMat, binMat, 120, 255, Imgproc.THRESH_TOZERO + Imgproc.THRESH_BINARY);
		
		Mat outputImage = new Mat();	
   	
        Imgproc.matchTemplate(binMat, srcTpl, outputImage, Imgproc.TM_CCOEFF);
        
        MinMaxLocResult mmr = Core.minMaxLoc(outputImage);
        
        Point matchLoc = mmr.maxLoc;
        Imgproc.rectangle(binMat, matchLoc, new Point(matchLoc.x + srcTpl.cols(), matchLoc.y + srcTpl.rows()), 
         											new Scalar(255, 255, 255));

//        Imgproc.rectangle(img, mmr.minLoc, mmr.maxLoc, new Scalar(255, 255, 255));
        System.out.println("similarity: " + mmr.minVal + ", " + mmr.maxVal);
        
        BufferedImage resultImage = createAwtImage(binMat);
        
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
   	
        Imgproc.matchTemplate(srcMat, tplMat, outputImage, Imgproc.TM_CCOEFF);
        
        MinMaxLocResult mmr = Core.minMaxLoc(outputImage);

        Imgproc.rectangle(srcMat, mmr.maxLoc, 
        		 				new Point(mmr.maxLoc.x + tplMat.cols(), mmr.maxLoc.y + tplMat.rows()), 
        						new Scalar(255, 255, 255));
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
		
		resultImage = createAwtImage(binImg);
		
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
	
	private static BufferedImage createAwtImage(Mat mat) {

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
