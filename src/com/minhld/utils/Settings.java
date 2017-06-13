package com.minhld.utils;

import java.util.HashMap;

public class Settings {
	public static final String LABEL_THRESHOLD = "Threshold";
	public static final String LABEL_GAUSSIAN_SIZE = "Gaussian Size";
	public static final String LABEL_CONTOUR_SIDES = "Contour Sides";
	public static final String LABEL_AREA_THRESHOLD = "Threshold Min Area";
	public static final String LABEL_DILATE_SIZE = "Dilate Size";
	public static final String LABEL_VELOCITY = "Velocity";
	public static final String LABEL_NULL = "A";
	
	public static int threshold = 235;
	public static int gaussianSize = 0;
	public static int contourSides = 10;
	public static int areaThreshold = 200;
	public static int dilateSize = 3;
	public static int velocity = 3;
	
	public static String templatePath = "samples/tpl7.png";
	
	static HashMap<String, Integer> settings;
	
	static {
		init();
	}
	
	private static void init() {
		settings = new HashMap<>();
		Settings.settings.put(Settings.LABEL_THRESHOLD, Settings.threshold);
		Settings.settings.put(Settings.LABEL_GAUSSIAN_SIZE, Settings.gaussianSize);
		Settings.settings.put(Settings.LABEL_CONTOUR_SIDES, Settings.contourSides);
		Settings.settings.put(Settings.LABEL_AREA_THRESHOLD, Settings.areaThreshold);
		Settings.settings.put(Settings.LABEL_DILATE_SIZE, Settings.dilateSize);
		Settings.settings.put(Settings.LABEL_VELOCITY, Settings.velocity);
		Settings.settings.put("A", 0);
	}
	
	public static void setValue(String key, int value) {
		Settings.settings.put(key, value);
		if (key.equals(Settings.LABEL_THRESHOLD)) {
			Settings.threshold = value;
		} else if (key.equals(Settings.LABEL_GAUSSIAN_SIZE)) {
			Settings.gaussianSize = value;
		} else if (key.equals(Settings.LABEL_CONTOUR_SIDES)) {
			Settings.contourSides = value;
		} else if (key.equals(Settings.LABEL_AREA_THRESHOLD)) {
			Settings.areaThreshold = value;
		} else if (key.equals(Settings.LABEL_DILATE_SIZE)) { 
			Settings.dilateSize = value;
		} else if (key.equals(Settings.LABEL_VELOCITY)) {
			Settings.velocity = value;
		} else if (key.equals(Settings.LABEL_NULL)) {
			
		}
	}
	
	public static int getValue(String key) {
		// if (!Settings.settings.containsKey(key)) {
		//	 init();
		// }
		return Settings.settings.get(key);
	}
	
}
