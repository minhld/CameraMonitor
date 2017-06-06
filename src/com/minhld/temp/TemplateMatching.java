package com.minhld.temp;

import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class TemplateMatching {

	public static void main(String[] args) {
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Mat source=null;
		Mat template=null;
		source = Imgcodecs.imread("samples/realimage1.png");
		template=Imgcodecs.imread("samples/tpl.png");
	
		Mat outputImage=new Mat();	
		int machMethod=Imgproc.TM_CCOEFF;
   
		long start = System.currentTimeMillis();
        Imgproc.matchTemplate(source, template, outputImage, machMethod);
        System.out.println("processing time: " + (System.currentTimeMillis() - start) + "ms");
    
        MinMaxLocResult mmr = Core.minMaxLoc(outputImage);
        Point matchLoc=mmr.maxLoc;

        Imgproc.rectangle(source, matchLoc, new Point(matchLoc.x + template.cols(),
                matchLoc.y + template.rows()), new Scalar(255, 255, 255));

        Imgcodecs.imwrite("samples/temp_match.jpg", source);
        System.out.println("done.");
	}

}
