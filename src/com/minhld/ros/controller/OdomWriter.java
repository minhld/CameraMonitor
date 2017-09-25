package com.minhld.ros.controller;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

import nav_msgs.Odometry;

/**
 * this class is to publish odometry information to ROS
 * 
 * @author lee
 *
 */
public class OdomWriter extends AbstractNodeMain {
	public static final String topicTitle = "/wc_odom";
	Publisher<Odometry> publisher;
	
	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of(topicTitle);
	}

	@Override
	public void onStart(ConnectedNode connectedNode) {
		publisher = connectedNode.newPublisher(topicTitle, Odometry._TYPE);
	}
	
	public void publish(Odometry msg) {
		publisher.publish(msg);
	}
	
	public void publish() {
		
	}
}
