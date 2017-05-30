package com.minhld.utils;

import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
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

import sensor_msgs.Image;

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
	
	public static String getTopicInfo(String topicName) {
		StringBuffer topicInfo = new StringBuffer();
		TopicSystemState topicState = topics.get(topicName);
		
		topicInfo.append("Info of " + topicName + "\n");
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
	 * create a new node for subscribing/publishing
	 * @param name
	 * @param node
	 */
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
	
	/**
	 * check if a topic is opened in the inner frame or not.
	 * if it is opened, it wont be opened again to avoid complexity 
	 * and memory consumption.
	 * 
	 * @param title
	 * @return
	 */
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
