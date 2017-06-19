package com.minhld.utils;

import java.awt.image.BufferedImage;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.UUID;

public class AppUtils {
	static final DecimalFormat formatter = new DecimalFormat("#,###.00");

	/**
	 * hold the application ID
	 */
	private static String appID = "";

	public static HashMap<String, Integer> innerFramesInfo = new HashMap<>();
	
	public static String getAppID() {
		if (AppUtils.appID.equals("")) {
			AppUtils.appID = AppUtils.getCurrentIP().replaceAll("\\.", "_");
		}
		return AppUtils.appID;
	}
	
	protected static String shortUUID() {
		UUID uuid = UUID.randomUUID();
		long l = ByteBuffer.wrap(uuid.toString().getBytes()).getLong();
		return Long.toString(l, Character.MAX_RADIX);
	}
	
	
	/**
	 * this function searches for available static IP of the being-used network
	 * the IP will be of version-4 address. 
	 * @return
	 */
	public static String getCurrentIP() {
		try {
			Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
			while (nets.hasMoreElements()) {
				NetworkInterface intf = nets.nextElement();
				// ignore the loopback virtual network
				if (!intf.getName().equals("lo")) {
					Enumeration<InetAddress> addrs = intf.getInetAddresses();
					while (addrs.hasMoreElements()) {
						InetAddress net = addrs.nextElement();
						if (net instanceof Inet4Address) {
							// return the first network IP that found
							return net.getHostAddress();
						}
					}
				}
			}
			return "";
		} catch(Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 * save image in the format of buffered binary to file
	 *  
	 * @param imageData
	 */
	public static void saveImage(BufferedImage imageData) {
		
	}
	
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (Exception e) {
			// silently passes
		}
	}
	
	public static String getNumberFormat(double num) {
		return formatter.format(num);
	}
}
