package com.minhld.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import org.ros.RosCore;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.response.Response;
import org.ros.master.client.SystemState;
import org.ros.master.client.TopicSystemState;
import org.ros.namespace.GraphName;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;

public class ROSUtils {
	static final int ROS_SVR_PORT = 11311;
	
	/**
	 * ROS core - usage suspended
	 */
	static RosCore rosCore;
	
	static String rosServerIP = "";
	
	/**
	 * list of current topics - used for outer references
	 */
	public static TreeMap<String, TopicSystemState> topics;
	
	public static List<String> displayingTopics = new ArrayList<>(); 
	
	/**
	 * store a node executor instance
	 */
	private static NodeMainExecutor executor = DefaultNodeMainExecutor.newDefault();
	
	
	public static void execute(String name, NodeMain node) {
		NodeConfiguration config = NodeConfiguration.newPrivate();
	    try {
			config.setMasterUri(new URI("http://" + ROSUtils.rosServerIP + ":11311"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	    config.setNodeName(name);
	    executor.execute(node, config);
	}
	
	public static boolean addDisplayTopic(String title) {
		if (!ROSUtils.displayingTopics.contains(title)) {
			ROSUtils.displayingTopics.add(title);
			return true;
		}
		return false;
	}
	
	/**
	 * get list of current topics. The list will be stored in the topic tree-map
	 * for sorting by title.
	 * 
	 * @param rosServerIP
	 * @param refresh
	 * @return
	 */
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
		
		ROSUtils.rosServerIP = rosServerIP;
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
