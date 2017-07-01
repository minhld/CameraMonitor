package com.minhld.temp;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.minhld.utils.OpenCVUtils;

public class FindPadTest2 extends Thread {
	private final int WINDOW_DEF_WIDTH = 800;
	private JPanel canvas;
	
	public void run() {
		JFrame mainFrame = new JFrame("Find Pad Test");
		Container contentPane = mainFrame.getContentPane();
		
		mainFrame.setPreferredSize(new Dimension(WINDOW_DEF_WIDTH, WINDOW_DEF_WIDTH));
		mainFrame.setBounds(0, 0, WINDOW_DEF_WIDTH, WINDOW_DEF_WIDTH);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setVisible(true);
		
		canvas = new JPanel();
		contentPane.add(canvas);
		
		// load OpenCV
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		
		processImage();
	}
	
	private void processImage() {
		String padPath = "samples/pad2.jpg";
		String scenePath = "samples/";
		
		long startTime = System.currentTimeMillis();
		
		Mat img = Imgcodecs.imread(padPath, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		Mat imgScene = Imgcodecs.imread(scenePath, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

		
		long durr = System.currentTimeMillis() - startTime;
		System.out.println("processing time: " + durr + "ms");
		BufferedImage bImage = OpenCVUtils.createAwtImage(img);
		durr = System.currentTimeMillis() - startTime;
		System.out.println("total time: " + durr + "ms");
		displayImage(bImage);
		
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
