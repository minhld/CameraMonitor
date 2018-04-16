package com.minhld.ui.supports;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.minhld.utils.Settings;

public class AnalyzerSettingsPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	public AnalyzerSettingsPanel() {
		// setLayout(new BorderLayout());
		setLayout(new FlowLayout(FlowLayout.LEADING));
		
		// ------ HSV Color panel ------
		JPanel hsvColorPanel = new JPanel(new GridLayout(2, 3));
		hsvColorPanel.setPreferredSize(new Dimension(650, 110));
		hsvColorPanel.setBorder(BorderFactory.createTitledBorder("HSV Color"));
		hsvColorPanel.add(new AdjustSlider(Settings.LABEL_COLOR_LOW_H, 1, 255));
		hsvColorPanel.add(new AdjustSlider(Settings.LABEL_COLOR_LOW_S, 1, 255));
		hsvColorPanel.add(new AdjustSlider(Settings.LABEL_COLOR_LOW_V, 1, 255));
		hsvColorPanel.add(new AdjustSlider(Settings.LABEL_COLOR_HIGH_H, 1, 255));
		hsvColorPanel.add(new AdjustSlider(Settings.LABEL_COLOR_HIGH_S, 1, 255));
		hsvColorPanel.add(new AdjustSlider(Settings.LABEL_COLOR_HIGH_V, 1, 255));
		add(hsvColorPanel);
		
		// ------ Hough Lines panel ------
		JPanel linePanel = new JPanel(new GridLayout(2, 3));
		linePanel.setPreferredSize(new Dimension(650, 110));
		linePanel.setBorder(BorderFactory.createTitledBorder("Hough Lines"));
		linePanel.add(new AdjustSlider(Settings.LABEL_CANNY_THRES1, 1, 255));
		linePanel.add(new AdjustSlider(Settings.LABEL_HOUGHLINES_RHO, 1, 255));
		linePanel.add(new AdjustSlider(Settings.LABEL_HOUGHLINES_MIN_LINE_LENGTH, 1, 255));
		linePanel.add(new AdjustSlider(Settings.LABEL_CANNY_THRES2, 1, 255));
		linePanel.add(new AdjustSlider(Settings.LABEL_HOUGHLINES_THRES, 1, 255));
		linePanel.add(new AdjustSlider(Settings.LABEL_HOUGHLINES_MAX_LINE_GAP, 1, 255));
		add(linePanel);

		
		// ------ Threshold panel ------
		JPanel thresholdPanel = new JPanel(new GridLayout(1, 3));
		thresholdPanel.setPreferredSize(new Dimension(650, 70));
		thresholdPanel.setBorder(BorderFactory.createTitledBorder("Thresholds"));
		thresholdPanel.add(new AdjustChecker(Settings.LABEL_DILATE_ENABLE, 1));
		thresholdPanel.add(new AdjustSlider(Settings.LABEL_DILATE_SIZE, 1, 15, 2));
		thresholdPanel.add(new AdjustSlider(Settings.LABEL_THRESHOLD, 1, 255));
		add(thresholdPanel);
				
		// ------ Contour panel ------
		JPanel contourPanel = new JPanel(new GridLayout(1, 3));
		contourPanel.setPreferredSize(new Dimension(650, 70));
		contourPanel.setBorder(BorderFactory.createTitledBorder("Contours"));
		contourPanel.add(new AdjustChecker(Settings.LABEL_CONTOUR_ENABLE, 1, false));
		contourPanel.add(new AdjustSlider(Settings.LABEL_CONTOUR_SIDES, 1, 20));
		contourPanel.add(new AdjustSlider(Settings.LABEL_CONTOUR_AREA_MIN, 200, 900));
		add(contourPanel);
		
		// ------ Gaussian panel ------
		JPanel gaussianPanel = new JPanel(new GridLayout(1, 3));
		gaussianPanel.setPreferredSize(new Dimension(650, 70));
		gaussianPanel.setBorder(BorderFactory.createTitledBorder("Gaussian"));
		gaussianPanel.add(new AdjustChecker(Settings.LABEL_GAUSSIAN_ENABLE, 0, false));
		gaussianPanel.add(new AdjustSlider(Settings.LABEL_GAUSSIAN_SIZE, 1, 15, 2));
		gaussianPanel.add(new AdjustSlider(Settings.LABEL_GAUSSIAN_STANDARD_DEVIATION, 0, 10));
		add(gaussianPanel);
		
		// ------ Distance panel ------
		JPanel distancePanel = new JPanel(new GridLayout(1, 3));
		distancePanel.setPreferredSize(new Dimension(650, 80));
		distancePanel.setBorder(BorderFactory.createTitledBorder("Focal Length"));
		distancePanel.add(new AdjustTexter(Settings.LABEL_STD_DISTANCE));
		distancePanel.add(new AdjustTexter(Settings.LABEL_STD_PIXEL_WIDTH));
		distancePanel.add(new AdjustTexter(Settings.LABEL_STD_WIDTH));
		add(distancePanel);
	}
}
