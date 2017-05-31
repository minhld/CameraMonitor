package com.minhld.utils;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import com.minhld.ros.controller.CameraListener;

public class AlgProcFrame extends JInternalFrame {
	private final int WINDOW_DEF_WIDTH = 500;
	private final int WINDOW_DEF_HEIGHT = 500;
	
	private static final long serialVersionUID = 1L;
	private JPanel canvas;
	private Thread nodeThread;

	public AlgProcFrame(String title) {
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
	

	@Override
	public void dispose() {
		super.dispose();
		
		nodeThread.interrupt();
	}
	
	private void startListening(final String title) {
		nodeThread = new Thread() {
			@Override
			public void run() {
				// this will be the name of the subscriber to this topic
				String graphName = ROSUtils.getNodeName(title);
				
				if (title.equals("/rrbot/camera1/image_raw")) {
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
		};
		nodeThread.start();
		
	}
}
