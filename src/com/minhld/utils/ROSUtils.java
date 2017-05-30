package com.minhld.utils;

import org.ros.RosCore;

public class ROSUtils {
	static final int ROS_SVR_PORT = 11311;
	
	static RosCore rosCore;
	
	/**
	 * start a ROS core
	 * @param rosServerIP
	 */
	public static void startRosCore(String rosServerIP) {
		// initiate ROS-core
		rosCore = RosCore.newPublic();
		rosCore.start();
		
		try {
			rosCore.awaitStart();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * shutdown a ROS core
	 * 
	 * @param rosServerIP
	 */
	public static void endRosCore(String rosServerIP) {
		rosCore = RosCore.newPublic(rosServerIP, ROSUtils.ROS_SVR_PORT);
		rosCore.shutdown();
	}
}
