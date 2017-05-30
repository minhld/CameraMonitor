package com.minhld.ros.controller;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.DesktopPaneUI;

import com.birosoft.liquid.LiquidLookAndFeel;
import com.minhld.utils.AppUtils;
import com.minhld.utils.ROSInnerFrame;
import com.minhld.utils.ROSUtils;

public class RosController extends Thread {
	JFrame mainFrame;
	JTextField ipText;
	JTextArea infoText;
	JDesktopPane frameContainer;
	JList<String> topicList;
	
	public void run() {
		mainFrame = new JFrame("Robot Monitor v1.0");
		Container contentPane = mainFrame.getContentPane();
		
		// ------ set Tool-bar and Buttons ------ 
		contentPane.add(buildToolBar(), BorderLayout.NORTH);
	    
	    // ------ set the View panel ------ 
	    contentPane.add(buildViewPanel(), BorderLayout.CENTER);

	    // ------ set Control panel ------ 
	    contentPane.add(buildControlPanel(), BorderLayout.EAST);
	    
		// ------ set Windows Look-n-Feel ------ 
		try {
			UIManager.setLookAndFeel(new LiquidLookAndFeel());
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
	
	private JToolBar buildToolBar() {
		JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
//      toolbar.setBorderPainted(true);
		toolbar.setFloatable(false);

		JButton refreshBtn = new JButton("Refresh");
		refreshBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addTopicsToList();
			}
		});
		toolbar.add(refreshBtn);
		toolbar.addSeparator();
		return toolbar;
	}
	
	private JDesktopPane buildViewPanel() {
		frameContainer = new JDesktopPane();
		frameContainer.setBackground(new Color(220, 220, 220));
		frameContainer.setAutoscrolls(true);
		
		frameContainer.setUI(new DesktopPaneUI() {
		    @Override
		        public void installUI(JComponent c) {
		            try {
		            	LiquidLookAndFeel liquid = new LiquidLookAndFeel();
		                UIManager.setLookAndFeel(liquid);
		            } catch (Exception e) {
		                e.printStackTrace();
		            }
		            super.installUI(c);
		        }   
		    });

		return frameContainer;
	}
	
	/**
	 * build the right controller panel
	 * 
	 * @return
	 */
	@SuppressWarnings({ "rawtypes" })
	private JPanel buildControlPanel() {
		JPanel controller = new JPanel(new BorderLayout());

		// ------ add Network Configuration panel ------ 
		JPanel networkConfig = new JPanel(new BorderLayout());
		networkConfig.setBorder(BorderFactory.createTitledBorder("ROS Core"));
		
		JPanel p1 = new JPanel(new BorderLayout());
		p1.add(new JLabel("Server IP: "), BorderLayout.WEST);
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

		// ------ add ROS Topic List panel ------
		JPanel topicPanel = new JPanel(new BorderLayout());
		topicPanel.setBorder(BorderFactory.createTitledBorder("ROS Topics"));
		
		topicList = new JList<String>(); 
		topicList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		topicList.setLayoutOrientation(JList.VERTICAL);
		topicList.setVisibleRowCount(-1);
		topicList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
		        JList list = (JList) e.getSource();
	            
	            String selectedTopic = (String) list.getSelectedValue();
		        if (e.getClickCount() == 2) {
		        	// Double-click detected
		        	int index = list.locationToIndex(e.getPoint());
		            
		        	// open corresponding window for a topic
		            createFrame(selectedTopic, index);
		        } else if (e.getClickCount() == 1) {
		        	// single-click detected
		        	String topicInfo = ROSUtils.getTopicInfo(selectedTopic);
		        	infoText.setText(topicInfo);
		        }
			}
		});
		JScrollPane listScroller = new JScrollPane(topicList, 
							JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		listScroller.setPreferredSize(new Dimension(300, 250));
		topicPanel.add(listScroller, BorderLayout.CENTER);
		controller.add(topicPanel, BorderLayout.CENTER);

		// crawl topic list and add to the swing view list
		addTopicsToList();
		
		// ------ add Network Info panel ------ 
		JPanel infoPanel = new JPanel();
		infoPanel.setBorder(BorderFactory.createTitledBorder("Network Log"));

		infoText = new JTextArea(25, 46);
		infoText.setBorder(BorderFactory.createLineBorder(Color.gray));
		infoText.setFont(new Font("courier", Font.PLAIN, 11));
		infoText.setEditable(false);
		JScrollPane infoScroller = new JScrollPane(infoText, 
							JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//		infoScroller.setPreferredSize(new Dimension(300, 250));
		infoPanel.add(infoScroller, BorderLayout.CENTER);

		controller.add(infoPanel, BorderLayout.SOUTH);

		return controller;
	}
	
	private void createFrame(String title, int index) {
		if (ROSUtils.addDisplayTopic(title)) {
			ROSInnerFrame f = new ROSInnerFrame(title);
			frameContainer.add(f, index);
		} else {
			JOptionPane.showMessageDialog(mainFrame, "This topic has been added", "Monitor", JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * add a topic list to the swing list
	 */
	private void addTopicsToList() {
		String[] topics = ROSUtils.getTopicNameList(ipText.getText(), true);
		
		if (topics.length == 0) {
			JOptionPane.showMessageDialog(mainFrame, "ROS Server is unable to connect.");
		} 
		topicList.removeAll();
		topicList.setListData(topics);
	}
	
	public static void main(String args[]) {
		new RosController().start();
	}
}
