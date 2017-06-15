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
	public static final String LABEL_CONTOUR_ENABLE = "contour-enable";
	
	public static final String LABEL_DILATE_SIZE = "dilate-size";
	public static final String LABEL_VELOCITY = "velocity";
	
	public static final String LABEL_COLOR_LOW_H = "low-h-color";
	public static final String LABEL_COLOR_LOW_S = "low-s-color";
	public static final String LABEL_COLOR_LOW_V = "low-v-color";
	public static final String LABEL_COLOR_HIGH_H = "high-h-color";
	public static final String LABEL_COLOR_HIGH_S = "high-s-color";
	public static final String LABEL_COLOR_HIGH_V = "high-v-color";
	
	public static final String LABEL_NULL = "A";
	
	public static int threshold = 235;
	public static int colorThreshold = 100;
	public static int gaussianSize = 0;
	
	public static int contourSides = 9;
	public static int contourAreaMin = 300;
	public static int contourEnable = 1;
	
	public static int dilateSize = 3;
	public static int velocity = 3;
	
	public static int lowHColor = 235;
	public static int lowSColor = 100;
	public static int lowVColor = 0;
	public static int highHColor = 9;
	public static int highSColor = 300;
	public static int highVColor = 3;

	
	public static int TEMPLATE_WIDTH = 180;
	public static int TEMPLATE_HEIGHT = 100;
	
	public static String templatePath = "samples/tpl7.png";
	
//	static HashMap<String, Integer> settings;
	static Properties settings;
	
	
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
		} else if (key.equals(Settings.LABEL_CONTOUR_ENABLE)) { 
			Settings.contourEnable = value;
		} else if (key.equals(Settings.LABEL_DILATE_SIZE)) { 
			Settings.dilateSize = value;
		} else if (key.equals(Settings.LABEL_VELOCITY)) {
			Settings.velocity = value;
		} else if (key.equals(Settings.LABEL_COLOR_LOW_H)) {
			Settings.lowHColor = value;
		} else if (key.equals(Settings.LABEL_COLOR_LOW_S)) {
			Settings.lowSColor = value;
		} else if (key.equals(Settings.LABEL_COLOR_LOW_V)) {
			Settings.lowVColor = value;
		} else if (key.equals(Settings.LABEL_COLOR_HIGH_H)) {
			Settings.highHColor = value;
		} else if (key.equals(Settings.LABEL_COLOR_HIGH_S)) {
			Settings.highSColor = value;
		} else if (key.equals(Settings.LABEL_COLOR_HIGH_V)) {
			Settings.highVColor = value;
		} else if (key.equals(Settings.LABEL_NULL)) {
			
		}  
	}
	
	/**
	 * get integer value from setting parameter list
	 *  
	 * @param key
	 * @return
	 */
	public static int getValue(String key) {
		String value = (String) Settings.settings.get(key);
		return Integer.parseInt(value);
	}
	
	/**
	 * get string value from parameter list
	 * 
	 * @param key
	 * @return
	 */
	public static String getStringValue(String key) {
		return (String) Settings.settings.get(key);
	}
	
	/**
	 * load properties list from outside configuration file
	 * 
	 * @param file
	 */
	public static void loadProps(String file) {
		try {
			File f = new File(file);
			Settings.settings = new Properties();
			Settings.settings.load(new FileInputStream(f));
			
		} catch (IOException ioEx) {
			ioEx.printStackTrace();
		}
	}
	
	/**
	 * save properties to the outside file
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
	
}
