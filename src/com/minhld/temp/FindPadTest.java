package com.minhld.temp;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class FindPadTest extends Thread {
	private final int WINDOW_DEF_WIDTH = 800;
	private JPanel canvas;
	
	public void run() {
		JFrame mainFrame = new JFrame("Find Pad Test");
		Container contentPane = mainFrame.getContentPane();
		
		mainFrame.setPreferredSize(new Dimension(WINDOW_DEF_WIDTH, WINDOW_DEF_WIDTH));
		mainFrame.setBounds(0, 0, WINDOW_DEF_WIDTH, WINDOW_DEF_WIDTH);
		mainFrame.setVisible(true);
		
		canvas = new JPanel();
		contentPane.add(canvas);
		
		// load image out
		loadImage();
	}
	
	private void processImage() {
		
	}
	
	private void loadImage() {
		String imagePath = "/home/lee/matlab_ws/multiobjects.png";
		try {
			BufferedImage bImage = ImageIO.read(new File(imagePath));
			Graphics g = canvas.getGraphics();
			if (g != null) {
				int w = canvas.getWidth(), h = canvas.getHeight();
				g.drawImage(bImage, 0, 0, w, h, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) {
		new FindPadTest().start();
	}
}
