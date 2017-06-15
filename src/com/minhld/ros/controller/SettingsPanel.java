package com.minhld.ros.controller;

import java.awt.BorderLayout;
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
		hsvColorPanel.setPreferredSize(new Dimension(590, 110));
		hsvColorPanel.setBorder(BorderFactory.createTitledBorder("HSV Color"));
		hsvColorPanel.add(new AdjustSlider(Settings.LABEL_COLOR_LOW_H, 1, 255));
		hsvColorPanel.add(new AdjustSlider(Settings.LABEL_COLOR_LOW_S, 1, 255));
		hsvColorPanel.add(new AdjustSlider(Settings.LABEL_COLOR_LOW_V, 1, 255));
		hsvColorPanel.add(new AdjustSlider(Settings.LABEL_COLOR_HIGH_H, 1, 255));
		hsvColorPanel.add(new AdjustSlider(Settings.LABEL_COLOR_HIGH_S, 1, 255));
		hsvColorPanel.add(new AdjustSlider(Settings.LABEL_COLOR_HIGH_V, 1, 255));
		// add(hsvColorPanel, BorderLayout.NORTH);
		add(hsvColorPanel);
		
		// ------ HSV Color panel ------
		JPanel contourPanel = new JPanel(new GridLayout(1, 3));
		contourPanel.setPreferredSize(new Dimension(590, 70));
		contourPanel.setBorder(BorderFactory.createTitledBorder("Contours"));
		contourPanel.add(new AdjustChecker(Settings.LABEL_CONTOUR_ENABLE, 1));
		contourPanel.add(new AdjustSlider(Settings.LABEL_CONTOUR_SIDES, 1, 20));
		contourPanel.add(new AdjustSlider(Settings.LABEL_CONTOUR_AREA_MIN, 200, 900));
		// add(contourPanel, BorderLayout.NORTH);
		add(contourPanel);
	}
}
