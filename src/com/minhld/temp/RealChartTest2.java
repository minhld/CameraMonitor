package com.minhld.temp;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.LegendPosition;

public class RealChartTest2 extends Thread {
	private final int WINDOW_DEF_WIDTH = 1500;
	private final int WINDOW_DEF_HEIGHT = 800;
	private JPanel canvas;
	JFrame mainFrame;
	XChartPanel<XYChart> panel;
	
	public void run() {
		mainFrame = new JFrame("Find Pad Test");
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
    XYChart chart;
    // BubbleChart chart;
    
	private void processImage() {
	    // Create Chart
	    chart = new XYChartBuilder().width(800).height(600).build();
 
	    // Customize Chart
	    chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Scatter);
	    chart.getStyler().setChartTitleVisible(false);
	    chart.getStyler().setLegendPosition(LegendPosition.InsideSW);
	    chart.getStyler().setMarkerSize(16);
	    
		xData.add(0d);
		yData.add(0d);
	    
	    chart.addSeries("Gaussian Blob", null, xData, yData);
	    
	    panel = new XChartPanel<XYChart>(chart);
	    canvas.add(panel, BorderLayout.CENTER);
	    
	    TimerTask chartUpdaterTask = new TimerTask() {
	    	@Override
	    	public void run() {
		        updateData();
		        chart.updateXYSeries("Gaussian Blob", null, xData, yData);
		        panel.repaint();
	    	}
	    };

	    Timer timer = new Timer();
	    timer.scheduleAtFixedRate(chartUpdaterTask, 0, 500);
	}
	
	public void updateData() {

		if (xData.size() == 2) {
			xData.remove(1);
		}
		xData.add(Math.random() * 5);

		if (yData.size() == 2) {
			yData.remove(1);
		}
		yData.add(Math.random() * 5);

	}
	
	public static void main(String args[]) {
		new RealChartTest2().start();
	}
}
