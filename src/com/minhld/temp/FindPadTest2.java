package com.minhld.temp;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.minhld.utils.OpenCVUtils;

public class FindPadTest2 extends Thread {
	private final int WINDOW_DEF_WIDTH = 1500;
	private final int WINDOW_DEF_HEIGHT = 800;
	private JPanel canvas;
	
	public void run() {
		JFrame mainFrame = new JFrame("Find Pad Test");
		Container contentPane = mainFrame.getContentPane();
		
		mainFrame.setPreferredSize(new Dimension(WINDOW_DEF_WIDTH, WINDOW_DEF_HEIGHT));
		mainFrame.setBounds(0, 0, WINDOW_DEF_WIDTH, WINDOW_DEF_HEIGHT);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setVisible(true);
		
		canvas = new JPanel();
		contentPane.add(canvas);
		
		// load OpenCV
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		
		processImage();
	}
	
	private void processImage() {
		String padPath = "samples/pad2.png";
		String scenePath = "samples/realpad2.png";
		
		long startTime = System.currentTimeMillis();
		
//		Mat padImg = Imgcodecs.imread(padPath, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
//		Mat sceneImg = Imgcodecs.imread(scenePath, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		Mat padImg = Imgcodecs.imread(padPath, Imgcodecs.CV_LOAD_IMAGE_COLOR);
		Mat sceneImg = Imgcodecs.imread(scenePath, Imgcodecs.CV_LOAD_IMAGE_COLOR);


		FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.ORB);
		DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
//        DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);

		MatOfKeyPoint objectKeyPoints = new MatOfKeyPoint();
        System.out.println("Detecting key points...");
        featureDetector.detect(padImg, objectKeyPoints);
//        KeyPoint[] keypoints = objectKeyPoints.toArray();
//        System.out.println(keypoints);

        MatOfKeyPoint objectDescriptors = new MatOfKeyPoint();
        System.out.println("Computing descriptors...");
        descriptorExtractor.compute(padImg, objectKeyPoints, objectDescriptors);

        // Create the matrix for output image.
        Mat outputImage = new Mat(padImg.rows(), padImg.cols(), Imgcodecs.CV_LOAD_IMAGE_COLOR);

        
        System.out.println("Drawing key points on object image...");
        Features2d.drawKeypoints(padImg, objectKeyPoints, outputImage, OpenCVUtils.BORDER_COLOR, 0);

        // Match object image with the scene image
        MatOfKeyPoint sceneKeyPoints = new MatOfKeyPoint();
        MatOfKeyPoint sceneDescriptors = new MatOfKeyPoint();
        System.out.println("Detecting key points in background image...");
        featureDetector.detect(sceneImg, sceneKeyPoints);
        System.out.println("Computing descriptors in background image...");
        descriptorExtractor.compute(sceneImg, sceneKeyPoints, sceneDescriptors);

        Mat matchoutput = new Mat(sceneImg.rows() * 2, sceneImg.cols() * 2, Imgcodecs.CV_LOAD_IMAGE_COLOR);
        

        List<MatOfDMatch> matches = new LinkedList<MatOfDMatch>();
        System.out.println("Matching object and scene images...");
        descriptorMatcher.knnMatch(objectDescriptors, sceneDescriptors, matches, 2);

        System.out.println("Calculating good match list...");
        LinkedList<DMatch> goodMatchesList = new LinkedList<DMatch>();

        float nndrRatio = 0.7f;

        for (int i = 0; i < matches.size(); i++) {
            MatOfDMatch matofDMatch = matches.get(i);
            DMatch[] dmatcharray = matofDMatch.toArray();
            DMatch m1 = dmatcharray[0];
            DMatch m2 = dmatcharray[1];

//            if (m1.distance <= m2.distance * nndrRatio) {
                goodMatchesList.addLast(m1);

//            }
        }

//        if (goodMatchesList.size() >= 7) {
            System.out.println("Object Found!!!");

            List<KeyPoint> objKeypointlist = objectKeyPoints.toList();
            List<KeyPoint> scnKeypointlist = sceneKeyPoints.toList();

            LinkedList<Point> objectPoints = new LinkedList<>();
            LinkedList<Point> scenePoints = new LinkedList<>();

            for (int i = 0; i < goodMatchesList.size(); i++) {
                objectPoints.addLast(objKeypointlist.get(goodMatchesList.get(i).queryIdx).pt);
                scenePoints.addLast(scnKeypointlist.get(goodMatchesList.get(i).trainIdx).pt);
            }

            MatOfPoint2f objMatOfPoint2f = new MatOfPoint2f();
            objMatOfPoint2f.fromList(objectPoints);
            MatOfPoint2f scnMatOfPoint2f = new MatOfPoint2f();
            scnMatOfPoint2f.fromList(scenePoints);

            Mat homography = Calib3d.findHomography(objMatOfPoint2f, scnMatOfPoint2f, Calib3d.RANSAC, 3);

            Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
            Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);

            obj_corners.put(0, 0, new double[]{0, 0});
            obj_corners.put(1, 0, new double[]{padImg.cols(), 0});
            obj_corners.put(2, 0, new double[]{padImg.cols(), padImg.rows()});
            obj_corners.put(3, 0, new double[]{0, padImg.rows()});

            System.out.println("Transforming object corners to scene corners...");
            Core.perspectiveTransform(obj_corners, scene_corners, homography);

//            Mat img = Imgproc.imread(bookScene, Imgcodecs.CV_LOAD_IMAGE_COLOR);
//
//            Core.line(img, new Point(scene_corners.get(0, 0)), new Point(scene_corners.get(1, 0)), OpenCVUtils.BORDER_RED_COLOR, 4);
//            Core.line(img, new Point(scene_corners.get(1, 0)), new Point(scene_corners.get(2, 0)), OpenCVUtils.BORDER_RED_COLOR, 4);
//            Core.line(img, new Point(scene_corners.get(2, 0)), new Point(scene_corners.get(3, 0)), OpenCVUtils.BORDER_RED_COLOR, 4);
//            Core.line(img, new Point(scene_corners.get(3, 0)), new Point(scene_corners.get(0, 0)), OpenCVUtils.BORDER_RED_COLOR, 4);

            System.out.println("Drawing matches image...");
            MatOfDMatch goodMatches = new MatOfDMatch();
            goodMatches.fromList(goodMatchesList);

            Features2d.drawMatches(padImg, objectKeyPoints, sceneImg, sceneKeyPoints, goodMatches, matchoutput, OpenCVUtils.BORDER_RED_COLOR, OpenCVUtils.BORDER_COLOR, new MatOfByte(), 2);

    		long durr = System.currentTimeMillis() - startTime;
    		System.out.println("processing time: " + durr + "ms");
    		BufferedImage bImage = OpenCVUtils.createAwtImage(matchoutput);
    		durr = System.currentTimeMillis() - startTime;
    		System.out.println("total time: " + durr + "ms");
    		displayImage(bImage);

//        } else {
//            System.out.println("Object Not Found");
//        }

        System.out.println("Ended....");
		
		
	}
	
//	private BufferedImage createAwtImage(Mat mat) {
//
//	    int type = 0;
//	    if (mat.channels() == 1) {
//	        type = BufferedImage.TYPE_BYTE_GRAY;
//	    } else if (mat.channels() == 3) {
//	        type = BufferedImage.TYPE_3BYTE_BGR;
//	    } else {
//	        return null;
//	    }
//
//	    BufferedImage image = new BufferedImage(mat.width(), mat.height(), type);
//	    WritableRaster raster = image.getRaster();
//	    DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
//	    byte[] data = dataBuffer.getData();
//	    mat.get(0, 0, data);
//
//	    return image;
//	}
	
	private void displayImage(BufferedImage bImage) {
		try {
			Graphics g = canvas.getGraphics();
			if (g != null) {
				int w = canvas.getWidth(), h = canvas.getHeight();
				g.drawImage(bImage, 0, 0, w, h, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) {
		new FindPadTest2().start();
	}
}
