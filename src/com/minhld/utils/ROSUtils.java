package com.minhld.utils;

import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.ros.RosCore;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.response.Response;
import org.ros.master.client.SystemState;
import org.ros.master.client.TopicSystemState;
import org.ros.master.client.TopicType;
import org.ros.namespace.GraphName;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;

import sensor_msgs.Image;

public class ROSUtils {
	static final int ROS_SVR_PORT = 11311;
	
	private static MasterClient masterClient;
	
	/**
	 * ROS core - usage suspended
	 */
	static RosCore rosCore;
	
	static String rosServerIP = "";
	
	/**
	 * list of current topics - used for outer references
	 */
	public static TreeMap<String, TopicInfo> topics;
	
	/**
	 * list of displaying topics, including node information
	 */
	public static HashMap<String, NodeMain> displayingTopics = new HashMap<String, NodeMain>(); 
	
	/**
	 * store a node executor instance
	 */
	private static NodeMainExecutor executor = DefaultNodeMainExecutor.newDefault();
	
	/**
	 * entry point of a server. This function should be called first
	 * and foremost when user chooses to connect with a new server. 
	 * 
	 * @param serverIP
	 */
	public static void startWithServer(String serverIP) throws Exception {
		ROSUtils.rosServerIP = serverIP;
		String rosURI = "http://" + ROSUtils.rosServerIP + ":" + ROSUtils.ROS_SVR_PORT;
		masterClient = new MasterClient(new URI(rosURI));
	}
	
	/**
	 * convert a ROS compressed Image to a 8-bit RGB image 
	 * in the BufferedImage format
	 * 
	 * @param imgMsg
	 * @return
	 */
	public static BufferedImage messageToBufferedImage(Image imgMsg){
        int width = (int) imgMsg.getWidth();
        int height = (int) imgMsg.getHeight();       
        DataBuffer buffer = new DataBufferByte(imgMsg.getData().array(), width * height);
        SampleModel sampleModel = new ComponentSampleModel(DataBuffer.TYPE_BYTE, width, height, 3, width*3, new int[]{2,1,0});
        Raster raster = Raster.createRaster(sampleModel, buffer, null);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        image.setData(raster);
        return image;
    }
	
	
	/**
	 * create a new node for subscribing/publishing
	 * @param nodeName
	 * @param node
	 */
	public static void execute(String nodeName, NodeMain node) {
		NodeConfiguration config = NodeConfiguration.newPrivate();
	    try {
			config.setMasterUri(new URI("http://" + ROSUtils.rosServerIP + ":11311"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	    config.setNodeName(nodeName);
	    ROSUtils.executor.execute(node, config);
	    
	    // add node to the watching list
	    ROSUtils.displayingTopics.put(nodeName, node);
	}
	
	/**
	 * shutdown all nodes that relevant to a server
	 */
	public static void shutdownAllNodes() {
		// disconnect all current opening nodes
		NodeMain node;
		for (String nodeName : ROSUtils.displayingTopics.keySet()) {
			node = ROSUtils.displayingTopics.get(nodeName);
			ROSUtils.executor.shutdownNodeMain(node);
		}
		
		// remove information of all opening nodes
		ROSUtils.displayingTopics.clear();
	}
	
	/**
	 * shutdown a node by its name
	 * 
	 * @param nodeName
	 */
	public static void shutdownNode(String nodeName) {
		NodeMain currentNode = ROSUtils.displayingTopics.get(nodeName);
		ROSUtils.executor.shutdownNodeMain(currentNode);
	    
		// remove node to the watching list
	    ROSUtils.displayingTopics.remove(nodeName);
	}
	
	/**
	 * check if a topic is opened in the inner frame or not.
	 * if it is opened, it wont be opened again to avoid complexity 
	 * and memory consumption.
	 * 
	 * @param name
	 * @return
	 */
	public static boolean checkWatchingTopic(String name) {
		return ROSUtils.displayingTopics.containsKey(name);
	}
	
	/**
	 * get info of a topic including name, list of publishers and subscribers
	 * 
	 * @param topicName
	 * @return
	 */
	public static String getTopicInfo(String topicName) {
		
		StringBuffer topicInfo = new StringBuffer();
		TopicInfo topic = topics.get(topicName);
		TopicSystemState topicState = topic.topicState;
		
		topicInfo.append("Info of " + topicName + "\n");
		topicInfo.append("Type: " + topic.type + "\n\n");
		topicInfo.append("Publishers: \n");
		for (String pubName : topicState.getPublishers()) {
			topicInfo.append("\t" + pubName + "\n");
		}
		
		topicInfo.append("Subscribers: \n");
		for (String subName : topicState.getSubscribers()) {
			topicInfo.append("\t" + subName + "\n");
		}
		
		return topicInfo.toString();
	}
	
	/**
	 * 
	 * 
	 * @param serverIP
	 * @param refresh
	 * @return
	 */
	public static String[] getTopicNameList(String serverIP, boolean refresh) {
		ROSUtils.rosServerIP = serverIP;
		return getTopicNameList(refresh);
	}
	
	
	/**
	 * get list of current topics. The list will be stored in the topic tree-map
	 * for sorting by title.
	 * 
	 * @param rosServerIP
	 * @param refresh
	 * @return
	 */
	public static String[] getTopicNameList(boolean refresh) {
		if (refresh || topics == null) {
			// update the topic list again
			try {
				getTopics();
			} catch(Exception e) {
				return new String[0];
			}
		}
		return topics.keySet().toArray(new String[] {});
	}
	
	/**
	 * get list of available topics that are currently in ROS system
	 * 
	 * @param rosServerIP
	 * @throws Exception
	 */
	public static void getTopics() throws Exception {
		topics = new TreeMap<String, TopicInfo>();
		
		GraphName anyName = GraphName.of("any");
		Response<SystemState> systemState = masterClient.getSystemState(anyName);
		Response<List<TopicType>> topicType = masterClient.getTopicTypes(anyName);
		Collection<TopicSystemState> topicSystemStateList = systemState.getResult().getTopics();
		
		// get list of topic types
		List<TopicType> topicTypeList = topicType.getResult();
		HashMap<String, String> typeList = new HashMap<>();
		for (TopicType type : topicTypeList) {
			typeList.put(type.getName(), type.getMessageType());
		}
		
		// 
		TopicInfo topicInfo;
		String topicName;
		for (TopicSystemState topic : topicSystemStateList) {
			topicName = topic.getTopicName();
			topicInfo = new TopicInfo(topicName, typeList.get(topicName), topic);
			topics.put(topicName, topicInfo);
		}
	}
	
	/**
	 * start a ROS core - temporarily disabled
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
	 * shutdown a ROS core - temporarily disabled
	 * 
	 * @param rosServerIP
	 */
	public static void endRosCore(String rosServerIP) {
		rosCore = RosCore.newPublic(rosServerIP, ROSUtils.ROS_SVR_PORT);
		rosCore.shutdown();
	}
	
	
	/**
	 * get watcher node name from topic title. I added a prefix to the title
	 * so that it will reflect the nodes that made by this software
	 * 
	 * @param title
	 * @return
	 */
	public static String getNodeName(String title) {
		return "monitor/w_" + title;
	}
	
	/**
	 * get talker node name from topic title
	 * 
	 * @param title
	 * @return
	 */
	public static String getTalkerName(String title) {
		return "monitor/t_" + title;
	}
}
