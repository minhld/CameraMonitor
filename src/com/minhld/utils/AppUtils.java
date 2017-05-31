package com.minhld.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class AppUtils {
	
	public static void saveImage() {
		
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
}
