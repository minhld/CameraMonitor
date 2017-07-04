package com.minhld.temp;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knowm.xchart.BubbleChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.knowm.xchart.style.Styler.LegendPosition;
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

public class RealChartTest extends Thread {
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
		
		canvas = new JPanel(new BorderLayout());
		contentPane.add(canvas);
		
		canvas.add(new JLabel("Add XChart"), BorderLayout.NORTH);
		
		processImage();
	}
	
    // Series
    List<Double> xData = new LinkedList<Double>();
    List<Double> yData = new LinkedList<Double>();
    // XYChart chart;
    BubbleChart chart;
    
	private void processImage() {
	    // Create Chart
	    // chart = new XYChartBuilder().width(800).height(600).build();
 
	    // Customize Chart
	    // chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Scatter);
	    // chart.getStyler().setChartTitleVisible(false);
	    // chart.getStyler().setLegendPosition(LegendPosition.InsideSW);
	    // chart.getStyler().setMarkerSize(16);

		chart = new BubbleChart(500, 400, ChartTheme.GGPlot2);
	    chart.setTitle("Real-time Bubble Chart");
	    chart.setXAxisTitle("X");
	    chart.setYAxisTitle("Y");

	    
	    // Series
//	    Random random = new Random();
//	    int size = 5;
//	    for (int i = 0; i < size; i++) {
//	      xData.add(random.nextGaussian() / 1000);
//	      yData.add(-1000000 + random.nextGaussian());
//	    }
	    xData = getRandomData(5);
	    yData = getRandomData(5);
	    
	    chart.addSeries("Gaussian Blob", null, xData, yData);
	    
	    new SwingWrapper(chart).displayChart();
//	    XChartPanel<XYChart> panel = new XChartPanel<XYChart>(chart);
//	    canvas.add(panel, BorderLayout.CENTER);
	    
	    TimerTask chartUpdaterTask = new TimerTask() {
	    	@Override
	    	public void run() {
		        updateData();
		        // chart.updateXYSeries("Gaussian Blob", null, xData, yData);
		        chart.updateBubbleSeries("Gaussian Blob", null, xData, yData);
	    	}
	    };

	    Timer timer = new Timer();
	    timer.scheduleAtFixedRate(chartUpdaterTask, 0, 500);
	}
	
	public void updateData() {

		// Get some new data
		List<Double> newData = getRandomData(1);
		yData.addAll(newData);
		// Limit the total number of points
		while (yData.size() > 20) {
			yData.remove(0);
		}

		// Get some new data
		newData = getRandomData(1);
		xData.addAll(newData);
		// Limit the total number of points
		while (xData.size() > 20) {
			xData.remove(0);
		}

	}
	
	private List<Double> getRandomData(int numPoints) {

		List<Double> data = new CopyOnWriteArrayList<>();
		for (int i = 0; i < numPoints; i++) {
			data.add(Math.random() * 100);
		}
		return data;
	}
	
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
		new RealChartTest().start();
	}
}
