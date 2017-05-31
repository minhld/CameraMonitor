package com.minhld.utils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.util.Arrays;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import sensor_msgs.Image;

public class OpenCVUtils {
	
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
