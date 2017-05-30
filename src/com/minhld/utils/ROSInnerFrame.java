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
	

	public ROSInnerFrame(String title) {
		super(title, true, true, true);
		Container contentPane = getContentPane();
		
		setPreferredSize(new Dimension(WINDOW_DEF_WIDTH, WINDOW_DEF_HEIGHT));
		setBounds(0, 0, WINDOW_DEF_WIDTH, WINDOW_DEF_HEIGHT);
		setVisible(true);
		
		canvas = new JPanel();
		contentPane.add(canvas);
		
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
					ROSUtils.execute(graphName, new CameraListener(graphName, title, new CameraListener.ImageListener() {
						
						@Override
						public void imageArrived(BufferedImage bImage) {
							Graphics g = canvas.getGraphics();
							if (g != null) {
								g.drawImage(bImage, 0, 0, WINDOW_DEF_WIDTH, WINDOW_DEF_HEIGHT, null);
								
							}
						}
					}));
				}
			}
		}.start();
		
	}
}
