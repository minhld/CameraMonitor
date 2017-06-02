package com.minhld.utils;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import com.minhld.ros.controller.CameraListener;

public class ROSInnerFrame extends JInternalFrame {
	private final int WINDOW_DEF_WIDTH = 500;
	private final int WINDOW_DEF_HEIGHT = 500;
	
	private static final long serialVersionUID = 1L;
	private JPanel canvas;
	private Thread nodeThread;
	private TopicInfo topicInfo;
	
	public ROSInnerFrame(TopicInfo topicInfo) {
		super("", true, true, true);
		this.setTitle(topicInfo.name); 
		
		this.topicInfo = topicInfo;
		
		Container contentPane = getContentPane();
		
		setPreferredSize(new Dimension(WINDOW_DEF_WIDTH, WINDOW_DEF_HEIGHT));
		setBounds(0, 0, WINDOW_DEF_WIDTH, WINDOW_DEF_HEIGHT);
		setVisible(true);
		
		canvas = new JPanel();
		contentPane.add(canvas);
		
		// start listening to a topic
		startListening(this.topicInfo.name);
	}
	

	@Override
	public void dispose() {
		super.dispose();
		
		// remove the topic out of the watching list
		String nodeName = ROSUtils.getNodeName(this.topicInfo.name);
		ROSUtils.shutdownNode(nodeName);
		nodeThread.interrupt();
	}
	
	private void startListening(final String title) {
		nodeThread = new Thread() {
			@Override
			public void run() {
				// this will be the name of the subscriber to this topic
				String graphName = ROSUtils.getNodeName(title);
				
				if (topicInfo.type.equals("sensor_msgs/Image")) {
					initImageNode(graphName);
				}
			}
		};
		nodeThread.start();
		
	}
	
	private void initImageNode(String graphName) {
		ROSUtils.execute(graphName, new CameraListener(graphName, title, new CameraListener.ImageListener() {
			@Override
			public void imageArrived(BufferedImage bImage) {
				Graphics g = canvas.getGraphics();
				if (g != null) {
					int w = canvas.getWidth(), h = canvas.getHeight();
					g.drawImage(bImage, 0, 0, w, h, null);
					
				}
			}
		}));
	}
}
