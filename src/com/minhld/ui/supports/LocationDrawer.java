package com.minhld.ui.supports;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;


import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import org.opencv.core.Point;

import com.minhld.ros.controller.LocationInstructor.GPSLocation;

public class LocationDrawer {
	static XYSeriesCollection collection;
    static XYSeries xySeries = new XYSeries("Wheelchair");
    static XYPlot xyPlot;
    static final double RANGE_MAX = 25;
    
    /**
     * setup and install the coordinate system with jFreeChart
     * coming along with a container. 
     * 
     * @return
     */
	public static ChartPanel createLocationSystem() {
    	collection = new XYSeriesCollection();
    	// add the pad series
    	XYSeries padSeries = new XYSeries("Charging Pad");
    	padSeries.add(0, 0);
    	collection.addSeries(padSeries);
    	
    	// add the XY-series to update location of the object (wheel-chair)
    	collection.addSeries(xySeries);
    	updateData(new Point(), 0);

    	// setup the chart
		JFreeChart jfreechart = ChartFactory.createScatterPlot("", "", "", collection, PlotOrientation.VERTICAL, true, true, false);
        xyPlot = (XYPlot) jfreechart.getPlot();
        xyPlot.setDomainCrosshairVisible(true);
        xyPlot.setRangeCrosshairVisible(true);
        XYItemRenderer renderer = xyPlot.getRenderer();
        renderer.setSeriesPaint(0, Color.red);
        renderer.setSeriesPaint(1, Color.blue);
        // renderer.setSeriesShape(1, new Ellipse2D.Double(-3, -3, 3, 3));
        adjustAxis((NumberAxis) xyPlot.getDomainAxis(), true);
        adjustAxis((NumberAxis) xyPlot.getRangeAxis(), false);
        xyPlot.setBackgroundPaint(Color.white);
        
        // setup the container of the chart
        ChartPanel parent = new ChartPanel(jfreechart);
        parent.setPopupMenu(null);
        parent.setDomainZoomable(false);
        parent.setRangeZoomable(false);
        return parent;
	}
	
	/**
	 * define properties for each axis 
	 * 
	 * @param axis
	 * @param vertical
	 */
    private static void adjustAxis(NumberAxis axis, boolean vertical) {
        axis.setRange(-RANGE_MAX, RANGE_MAX);
        axis.setTickUnit(new NumberTickUnit(5));
        axis.setVerticalTickLabels(vertical);
    }
	
    /**
     * update the location of the object 
     * 
     */
	public static void updateData(GPSLocation gpsPoint) {
		if (!gpsPoint.hasData) return;
		
		Point p = gpsPoint.center;
		double error = gpsPoint.radius;
		updateData(p, error);
    }
    
    /**
     * update the location of the object 
     * 
     */
	public static void updateData(Point p, double error) {
		xySeries.clear();
		xySeries.add(p.x, p.y);
		
    	collection.removeSeries(xySeries);
    	collection.addSeries(xySeries);
    	
    	if (xyPlot != null) {
    		xyPlot.clearAnnotations();
    		XYShapeAnnotation circle = new XYShapeAnnotation(
							new Ellipse2D.Double(p.x - error, p.y - error, error * 2, error * 2),
    						new BasicStroke(1), Color.blue);
    		xyPlot.addAnnotation(circle);
    	}
    }
	
}
