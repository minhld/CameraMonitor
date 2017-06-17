package com.minhld.ros.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

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
}
