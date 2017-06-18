package com.minhld.ros.controller;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.minhld.utils.AdjustChecker;
import com.minhld.utils.AdjustSlider;
import com.minhld.utils.Settings;

public class SettingsPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	public SettingsPanel() {
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
		
	}
}
