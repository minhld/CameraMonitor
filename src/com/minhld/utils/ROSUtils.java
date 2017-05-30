package com.minhld.utils;

import java.net.URI;
import java.util.Collection;
import java.util.TreeMap;

import org.ros.RosCore;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.response.Response;
import org.ros.master.client.SystemState;
import org.ros.master.client.TopicSystemState;
import org.ros.namespace.GraphName;

public class ROSUtils {
	static final int ROS_SVR_PORT = 11311;
	
	static RosCore rosCore;
	
	static TreeMap<String, TopicSystemState> topics;
	
	public static String[] getTopicNameList(String rosServerIP, boolean refresh) {
		if (refresh || topics == null) {
			// update the topic list again
			try {
				getTopics(rosServerIP);
			} catch(Exception e) {
//				e.printStackTrace();
				return new String[0];
			}
		}
		return topics.keySet().toArray(new String[] {});
	}
	
	public static void getTopics(String rosServerIP) throws Exception {
		topics = new TreeMap<String, TopicSystemState>();
		
		String rosURI = "http://" + rosServerIP + ":" + ROSUtils.ROS_SVR_PORT;
		MasterClient masterClient = new MasterClient(new URI(rosURI));
		Response<SystemState> systemState = masterClient.getSystemState(GraphName.of("hello"));
		Collection<TopicSystemState> topicSystemStateList = systemState.getResult().getTopics();
		
		for (TopicSystemState topic : topicSystemStateList) {
			topics.put(topic.getTopicName(), topic);
		}
	}
	
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
