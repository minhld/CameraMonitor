package com.minhld.ui.supports;

import java.awt.Color;

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
import org.opencv.core.Point;

public class LocationDrawer {
	static XYSeriesCollection collection;
    static XYSeries xySeries = new XYSeries("Wheelchair");
    static final double RANGE_MAX = 25;
    
    /**
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
    	updateData(new Point());

		JFreeChart jfreechart = ChartFactory.createScatterPlot("", "", "", collection, PlotOrientation.VERTICAL, true, true, false);
        XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
        xyPlot.setDomainCrosshairVisible(true);
        xyPlot.setRangeCrosshairVisible(true);
        XYItemRenderer renderer = xyPlot.getRenderer();
        renderer.setSeriesPaint(0, Color.red);
        renderer.setSeriesPaint(1, Color.blue);
        adjustAxis((NumberAxis) xyPlot.getDomainAxis(), true);
        adjustAxis((NumberAxis) xyPlot.getRangeAxis(), false);
        xyPlot.setBackgroundPaint(Color.white);
        
        return new ChartPanel(jfreechart);
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
     * 
     * @param x
     * @param y
     */
	public static void updateData(Point p) {
		xySeries.clear();
		xySeries.add(p.x, p.y);
    	collection.removeSeries(xySeries);
    	collection.addSeries(xySeries);
    }
	
}
