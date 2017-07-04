package com.minhld.temp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class RealChartTest3 extends JFrame {
	private static final long serialVersionUID = -800431856273352679L;
	
	private static final int SIZE = 300;
    private static final String title = "Scatter Add Demo";
    private static final Random rand = new Random();
    private final XYSeries series = new XYSeries("Original");
    private final XYSeries added = new XYSeries("Moved");

    public RealChartTest3(String s) {
        super(s);
        final ChartPanel chartPanel = createDemoPanel();
        chartPanel.setPreferredSize(new Dimension(SIZE, SIZE));
        this.add(chartPanel, BorderLayout.CENTER);
    }
    
    // JFreeChart jfreechart;
    XYSeriesCollection collection;
    
    private ChartPanel createDemoPanel() {
    	collection = new XYSeriesCollection();
    	series.add(0, 0);
    	collection.addSeries(series);
    	updateData();
    	
    	JFreeChart jfreechart = ChartFactory.createScatterPlot("", "", "", collection, PlotOrientation.VERTICAL, true, true, false);
        XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
        xyPlot.setDomainCrosshairVisible(true);
        xyPlot.setRangeCrosshairVisible(true);
        XYItemRenderer renderer = xyPlot.getRenderer();
        renderer.setSeriesPaint(0, Color.blue);
        renderer.setSeriesPaint(1, Color.green);
        adjustAxis((NumberAxis) xyPlot.getDomainAxis(), true);
        adjustAxis((NumberAxis) xyPlot.getRangeAxis(), false);
        xyPlot.setBackgroundPaint(Color.white);
        
        TimerTask chartUpdaterTask = new TimerTask() {
	    	@Override
	    	public void run() {
	    		updateData();
	    		
	    	}
	    };

	    Timer timer = new Timer();
	    timer.scheduleAtFixedRate(chartUpdaterTask, 0, 500);
        
        return new ChartPanel(jfreechart);
    }

    private void adjustAxis(NumberAxis axis, boolean vertical) {
        axis.setRange(-20.0, 20.0);
        axis.setTickUnit(new NumberTickUnit(5));
        axis.setVerticalTickLabels(vertical);
    }

    private void updateData() {
    	added.clear();
        added.add(rand.nextInt(10), rand.nextInt(10));
    	collection.removeSeries(added);
    	collection.addSeries(added);

    }

    public static void main(String args[]) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
            	RealChartTest3 demo = new RealChartTest3(title);
                demo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                demo.pack();
                demo.setLocationRelativeTo(null);
                demo.setVisible(true);
            }
        });
    }
}
