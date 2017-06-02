package com.minhld.utils;

import org.ros.master.client.TopicSystemState;

public class TopicInfo {
	public String name;
	public String type;
	public TopicSystemState topicState;
	
	public TopicInfo(String name, String type, TopicSystemState topicState) {
		this.name = name;
		this.type = type;
		this.topicState = topicState;
	}
	
}
