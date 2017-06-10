package com.minhld.opencv;

import java.awt.image.BufferedImage;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.minhld.utils.OpenCVUtils;

import sensor_msgs.Image;

public class ObjectDetector {

	static Mat tplMat;
	
	static {
		init();
	}
	
	public static void init() {
		tplMat = Imgcodecs.imread("samples/tpl8.png");
		Imgproc.cvtColor(tplMat, tplMat, Imgproc.COLOR_BGR2GRAY);
		Imgproc.threshold(tplMat, tplMat, 200, 255, Imgproc.THRESH_BINARY);

	}
	
	public static Object[] processImage(Image source) {
		Mat orgMat = OpenCVUtils.openImage(source);
		Mat modMat = new Mat();
		Imgproc.cvtColor(orgMat, modMat, Imgproc.COLOR_BGR2GRAY);
		Imgproc.threshold(modMat, modMat, 230, 255, Imgproc.THRESH_BINARY);
		
		Mat matchedMat = new Mat();	
        Imgproc.matchTemplate(modMat, tplMat, matchedMat, Imgproc.TM_CCOEFF);
        
        MinMaxLocResult mmr = Core.minMaxLoc(matchedMat);
     
        Point leftTop = new Point(mmr.maxLoc.x - tplMat.cols() / 2, mmr.maxLoc.y - tplMat.rows() / 2);
        Point rightBottom = new Point(mmr.maxLoc.x + tplMat.cols() * 3 / 2, mmr.maxLoc.y + tplMat.rows() * 3 / 2);
        
    	Imgproc.rectangle(orgMat, leftTop, rightBottom, OpenCVUtils.BORDER_COLOR);
        
        System.out.println("similarity: " + mmr.minVal + ", " + mmr.maxVal);
        
        BufferedImage processImage = OpenCVUtils.createAwtImage(modMat);
        BufferedImage resultImage = OpenCVUtils.createAwtImage(orgMat);
        
        
        return new Object[] { processImage, resultImage, false };
	}
}
