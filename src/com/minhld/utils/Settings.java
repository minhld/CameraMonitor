package com.minhld.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Settings {
	public static final String SETTING_ORG = "settings";
	public static final String SETTING_RED = "settings-red";
	
	public static final String LABEL_THRESHOLD = "threshold";
	public static final String LABEL_COLOR_THRESHOLD = "color-threshold";
	public static final String LABEL_GAUSSIAN_SIZE = "gaussian-size";
	public static final String LABEL_CONTOUR_SIDES = "contour-sides";
	public static final String LABEL_CONTOUR_AREA_MIN = "contour-area-min";
	public static final String LABEL_DILATE_SIZE = "dilate-size";
	public static final String LABEL_VELOCITY = "velocity";
	
	public static final String LABEL_NULL = "A";
	
	public static int threshold = 235;
	public static int colorThreshold = 100;
	public static int gaussianSize = 0;
	public static int contourSides = 9;
	public static int contourAreaMin = 300;
	public static int dilateSize = 3;
	public static int velocity = 3;
	
	public static String templatePath = "samples/tpl7.png";
	
//	static HashMap<String, Integer> settings;
	static Properties settings;
	
	static {
//		init();
	}
	
//	private static void init() {
//		settings = new HashMap<>();
//		Settings.settings.put(Settings.LABEL_THRESHOLD, Settings.threshold);
//		Settings.settings.put(Settings.LABEL_COLOR_THRESHOLD, Settings.colorThreshold);
//		Settings.settings.put(Settings.LABEL_GAUSSIAN_SIZE, Settings.gaussianSize);
//		Settings.settings.put(Settings.LABEL_CONTOUR_SIDES, Settings.contourSides);
//		Settings.settings.put(Settings.LABEL_CONTOUR_AREA_MIN, Settings.contourAreaMin);
//		Settings.settings.put(Settings.LABEL_DILATE_SIZE, Settings.dilateSize);
//		Settings.settings.put(Settings.LABEL_VELOCITY, Settings.velocity);
//		loadProps("config/settings.props");
//		Settings.settings.put(LABEL_NULL, 0);
//	}
	
	public static void init(String configName) {
		String configPath = "config/" + configName + ".props";
		loadProps(configPath);
	}
	
	public static void setValue(String key, int value) {
		Settings.settings.put(key, Integer.toString(value));
		
		if (key.equals(Settings.LABEL_THRESHOLD)) {
			Settings.threshold = value;
		} else if (key.equals(Settings.LABEL_COLOR_THRESHOLD)) {
			Settings.colorThreshold = value;
		} else if (key.equals(Settings.LABEL_GAUSSIAN_SIZE)) {
			Settings.gaussianSize = value;
		} else if (key.equals(Settings.LABEL_CONTOUR_SIDES)) {
			Settings.contourSides = value;
		} else if (key.equals(Settings.LABEL_CONTOUR_AREA_MIN)) {
			Settings.contourAreaMin = value;
		} else if (key.equals(Settings.LABEL_DILATE_SIZE)) { 
			Settings.dilateSize = value;
		} else if (key.equals(Settings.LABEL_VELOCITY)) {
			Settings.velocity = value;
		} else if (key.equals(Settings.LABEL_NULL)) {
			
		}
	}
	
	public static int getValue(String key) {
		String value = (String) Settings.settings.get(key);
		return Integer.parseInt(value);
	}
	
	public static void loadProps(String file) {
		try {
			File f = new File(file);
			Settings.settings = new Properties();
			Settings.settings.load(new FileInputStream(f));
			
//			Enumeration enuKeys = props.keys();
//			while (enuKeys.hasMoreElements()) {
//				String key = (String) enuKeys.nextElement();
//				int value = Integer.parseInt(props.getProperty(key));
//				Settings.settings.put(key, value);
//			}
		} catch (IOException ioEx) {
			ioEx.printStackTrace();
		}
	}
	
	/**
	 * save properties to the file
	 * 
	 * @param configName
	 */
	public static void saveProps(String configName) {
		String configPath = "config/" + configName + ".props";

		try {
			FileOutputStream fos = new FileOutputStream(configPath, false);
			Settings.settings.store(fos, "ROS CONTROLLER SETTINGS");
			fos.flush();
			fos.close();
		} catch (IOException ioEx) {
			ioEx.printStackTrace();
		}
	}
	
//	/**
//	 * Get property value in integer format. Make sure the property
//	 * is in integer or else it throws exception
//	 * 
//	 * @param propName
//	 * @return
//	 */
//	public static int getProp(String propName) {
//		return Settings.settings.get(propName);
//	}
//	
}
