package com.minhld.utils;

import java.awt.Dimension;

import javax.swing.JInternalFrame;

public class ROSInnerFrame extends JInternalFrame {
	
	private static final long serialVersionUID = 1L;

	public ROSInnerFrame(String title) {
		super(title, true, true, true);
		setPreferredSize(new Dimension(500, 350));
		setBounds(0, 0, 500, 350);
		setVisible(true);
	}
	
	private void startListening(String title) {
		
	}
}
