package com.minhld.temp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class RealChartTest3 extends JFrame {
	private static final int N = 1;
    private static final int SIZE = 345;
    private static final String title = "Scatter Add Demo";
    private static final Random rand = new Random();
    private final XYSeries series = new XYSeries("Original");
    private final XYSeries added = new XYSeries("Moved");

    public RealChartTest3(String s) {
        super(s);
        final ChartPanel chartPanel = createDemoPanel();
        chartPanel.setPreferredSize(new Dimension(SIZE, SIZE));
        this.add(chartPanel, BorderLayout.CENTER);
        JPanel control = new JPanel();
        control.add(new JButton(new AbstractAction("Move") {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < N / 2; i++) {
                    XYDataItem item = series.remove(0);
                    if (item != null) {
                        added.add(item);
                    }
                }
			}
		}));
    }

    private ChartPanel createDemoPanel() {
    
        JFreeChart jfreechart = ChartFactory.createScatterPlot(
            title, "", "", createSampleData(),
            PlotOrientation.VERTICAL, true, true, false);
        XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
        xyPlot.setDomainCrosshairVisible(true);
        xyPlot.setRangeCrosshairVisible(true);
        XYItemRenderer renderer = xyPlot.getRenderer();
        renderer.setSeriesPaint(0, Color.blue);
        renderer.setSeriesPaint(1, Color.red);
        adjustAxis((NumberAxis) xyPlot.getDomainAxis(), true);
        adjustAxis((NumberAxis) xyPlot.getRangeAxis(), false);
        xyPlot.setBackgroundPaint(Color.white);
        return new ChartPanel(jfreechart);
    }

    private void adjustAxis(NumberAxis axis, boolean vertical) {
        axis.setRange(-20.0, 20.0);
        axis.setTickUnit(new NumberTickUnit(5));
        axis.setVerticalTickLabels(vertical);
    }

    private XYDataset createSampleData() {
        XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
        for (int i = 0; i < N * N; i++) {
            series.add(rand.nextInt(10), rand.nextInt(10));
            added.add(0, 0);
        }
        xySeriesCollection.addSeries(series);
        xySeriesCollection.addSeries(added);
        return xySeriesCollection;
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
