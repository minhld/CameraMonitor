package com.minhld.ros.controller;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;


import geometry_msgs.Twist;

public class VelocityTalker extends AbstractNodeMain {
	private Publisher<geometry_msgs.Twist> pub;
	
	private String publisherName;
	private String topicTitle;

	
	public VelocityTalker(String publisherName, String topicTitle) {
		this.publisherName = publisherName;
		this.topicTitle = topicTitle;
	}
	
	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of(this.publisherName);
	}
	
	@Override
	public void onStart(ConnectedNode connectedNode) {
		pub = connectedNode.newPublisher(this.topicTitle, Twist._TYPE);
	}
	
	public void move(double vel, double rot) {
        Twist twist = pub.newMessage();
        twist.getLinear().setX(vel);
        twist.getAngular().setZ(rot);
        pub.publish(twist);
    }
}
