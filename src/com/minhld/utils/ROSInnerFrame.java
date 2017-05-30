package com.minhld.utils;

import java.awt.Dimension;

import javax.swing.JInternalFrame;

import com.minhld.ros.controller.CameraListener;

public class ROSInnerFrame extends JInternalFrame {
	
	private static final long serialVersionUID = 1L;

	public ROSInnerFrame(String title) {
		super(title, true, true, true);
		setPreferredSize(new Dimension(500, 350));
		setBounds(0, 0, 500, 350);
		setVisible(true);
		
		// start listening to a topic
		startListening(title);
	}
	
	private void startListening(final String title) {
		new Thread() {
			@Override
			public void run() {
				// this will be the name of the subscriber to this topic
				String graphName = "minh_monitor/w_" + title;
				
				if (title.equals("/rrbot/camera1/image_raw")) {
					ROSUtils.execute(graphName, new CameraListener(graphName, title));
				}
			}
		}.start();
		
	}
}
