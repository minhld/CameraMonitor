package com.minhld.ros.controller;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JPanel;

public class UISupport {
	static Properties uiProps;
	
	public static int getUIProp(String key) {
		String val = (String) uiProps.getProperty(key);
		return Integer.parseInt(val);
	}
	
	public static void loadUIProps() {
        String osName = (String) System.getProperties().get("os.name");
        String osType = (osName.toLowerCase().contains("mac") ? "mac" : "others");
        loadUIProps(osType);
	}
	
	public static void loadUIProps(String osType) {
		String propFile = "config/ui-" + osType + ".props";
		
		try {
			File f = new File(propFile);
			UISupport.uiProps = new Properties();
			UISupport.uiProps.load(new FileInputStream(f));
		} catch (IOException ioEx) {
			ioEx.printStackTrace();
		}
	}
	
	/**
	 * draw an image to the panel - this function to draw 
	 * on-the-fly image on a canvas of the panel 
	 * 
	 * @param panel
	 * @param img
	 */
	public static void drawImage(JPanel panel, BufferedImage img) {
		Graphics g = panel.getGraphics();
		if (g != null) {
			g.drawImage(img, 0, 0, panel.getWidth(), panel.getHeight(), null);
		}
	}
	
	/**
	 * draw an image to fit the canvas with respect to the image's ratio
	 * 
	 * @param panel
	 * @param img
	 */
	public static void drawRatioImage(JPanel panel, BufferedImage img) {
		// find the relative size of being-drawn object
		double panelRate = (double) panel.getWidth() / (double) panel.getHeight();
		double imgRate = (double) img.getWidth() / (double) img.getHeight();
		int width = 0, height = 0;
		if (imgRate > panelRate) {
			width = panel.getWidth();
			height = (int) (width / imgRate);
		} else {
			height = panel.getHeight();
			width = (int) (height * imgRate);
		}
		
		int startX = (panel.getWidth() - width) / 2;
		int startY = (panel.getHeight() - height) / 2;
		
		// start drawing
		Graphics g = panel.getGraphics();
		if (g != null) {
			g.drawImage(img, startX, startY, width, height, null);
		}
	}
	
	/**
	 * draw an image by clearing the area before drawing
	 * (this method is used when drawing stream images with different sizes
	 * so the previous image will be erased before a new smaller/bigger image
	 * is taken place) 
	 * 
	 * @param panel
	 * @param img
	 * @param w
	 * @param h
	 */
	public static void drawClearImage(JPanel panel, BufferedImage img, int w, int h) {
		Graphics g = panel.getGraphics();
		if (g != null) {
			// g.clearRect(0, 0, Settings.TEMPLATE_WIDTH, Settings.TEMPLATE_HEIGHT);
			g.drawImage(img, 0, 0, w / 2, h / 2, null);
			
		}
	}
}
