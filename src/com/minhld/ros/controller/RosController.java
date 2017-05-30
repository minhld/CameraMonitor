package com.minhld.ros.controller;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.minhld.utils.AppUtils;

public class RosController extends Thread {
	JTextField ipText;
	JTextArea infoText;
	
	public void run() {
		JFrame mainFrame = new JFrame("Robot Monitor v1.0");
		Container contentPane = mainFrame.getContentPane();
		
		// set toolbar and buttons
		JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
        toolbar.setBorderPainted(true);
        toolbar.setFloatable( true );

	    JButton button = new JButton("Exit");
	    button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		});
	    toolbar.add(button);
	    toolbar.addSeparator();
	    
	    contentPane.add(toolbar, BorderLayout.NORTH);
	    
	    // set canvas
	    JPanel canvas = new JPanel();
	    // canvas.setBackground(Color.lightGray);
	    contentPane.add(canvas, BorderLayout.CENTER);

	    // set control panel
	    contentPane.add(buildControlPanel(), BorderLayout.EAST);
	    
		// set windows look and feel
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			SwingUtilities.updateComponentTreeUI(mainFrame);
			mainFrame.pack();
		} catch (Exception e) { }
		
		// set window size
		mainFrame.setSize(1280, 860);
		mainFrame.setMinimumSize(new Dimension(1280, 860));
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setVisible(true);
	}
	
	/**
	 * build the right controller panel
	 * 
	 * @return
	 */
	private JPanel buildControlPanel() {
		JPanel controller = new JPanel(new BorderLayout());

		// ------ add Network Configuration panel ------ 
		JPanel networkConfig = new JPanel(new BorderLayout());
		networkConfig.setBorder(BorderFactory.createTitledBorder("ROS Core"));
		
		JPanel p1 = new JPanel(new BorderLayout());
		p1.add(new JLabel("IP: "), BorderLayout.WEST);
		ipText = new JTextField(20); 
		String currentIP = AppUtils.getCurrentIP();
		ipText.setText(currentIP);
		p1.add(ipText);
		networkConfig.add(p1, BorderLayout.NORTH);

		JPanel p2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton startROSButton = new JButton("Start");
//		startROSButton.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				ROSUtils.startRosCore(ipText.getText());
//			}
//		});
		startROSButton.setEnabled(false);
		p2.add(startROSButton);
		JButton endROSButton = new JButton("End");
		endROSButton.setEnabled(false);
//		endROSButton.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				ROSUtils.endRosCore(ipText.getText());
//			}
//		});
		p2.add(endROSButton);
		
		networkConfig.add(p2, BorderLayout.CENTER);

		
		controller.add(networkConfig, BorderLayout.NORTH);

		// ------ add Network Info panel ------ 
		JPanel infoPanel = new JPanel();
		infoPanel.setBorder(BorderFactory.createTitledBorder("Network Log"));

		JTextArea infoText = new JTextArea(30, 46);
		infoText.setBorder(BorderFactory.createLineBorder(Color.gray));
		infoText.setFont(new Font("courier", Font.PLAIN, 11));
		infoText.setEditable(false);
		infoPanel.add(infoText, BorderLayout.CENTER);

		controller.add(infoPanel, BorderLayout.SOUTH);

		return controller;
	}

	public static void main(String args[]) {
		new RosController().start();
	}
}
