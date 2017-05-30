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
	JTextField ipText;
	JTextArea infoText;
	JDesktopPane frameContainer;
	
	public void run() {
		JFrame mainFrame = new JFrame("Robot Monitor v1.0");
		Container contentPane = mainFrame.getContentPane();
		
		// ------ set Tool-bar and Buttons ------ 
		JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
//        toolbar.setBorderPainted(true);
        toolbar.setFloatable(false);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

			}
		});
	    toolbar.add(refreshBtn);
	    toolbar.addSeparator();
	    
	    contentPane.add(toolbar, BorderLayout.NORTH);
	    
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
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
		
		
		String[] topics = ROSUtils.getTopicNameList(ipText.getText(), true);
		
		if (topics.length == 0) {
			JOptionPane.showMessageDialog(controller, "ROS Server is unable to connect.");
		} 
		
		JList topicList = new JList(topics); //data has type Object[]
		topicList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		topicList.setLayoutOrientation(JList.VERTICAL);
		topicList.setVisibleRowCount(-1);
		topicList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
		        JList list = (JList) e.getSource();
		        if (e.getClickCount() == 2) {
		            // Double-click detected
		            int index = list.locationToIndex(e.getPoint());

		            // open corresponding window for a topic
		            String selectedTopic = (String) list.getSelectedValue();
		            createFrame(selectedTopic, index);
		        }		        
			}
		});
		JScrollPane listScroller = new JScrollPane(topicList, 
							JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		listScroller.setPreferredSize(new Dimension(300, 250));
		topicPanel.add(listScroller, BorderLayout.CENTER);
		controller.add(topicPanel, BorderLayout.CENTER);

		// ------ add Network Info panel ------ 
		JPanel infoPanel = new JPanel();
		infoPanel.setBorder(BorderFactory.createTitledBorder("Network Log"));

		JTextArea infoText = new JTextArea(25, 46);
		infoText.setBorder(BorderFactory.createLineBorder(Color.gray));
		infoText.setFont(new Font("courier", Font.PLAIN, 11));
		infoText.setEditable(false);
		infoPanel.add(infoText, BorderLayout.CENTER);

		controller.add(infoPanel, BorderLayout.SOUTH);

		return controller;
	}
	
	private void createFrame(String title, int index) {
		if (ROSUtils.addDisplayTopic(title)) {
			ROSInnerFrame f = new ROSInnerFrame(title);
			frameContainer.add(f, index);
		} else {
			JOptionPane.showMessageDialog(frameContainer, "This topic has been added", "Monitor", JOptionPane.WARNING_MESSAGE);
		}
	}

	public static void main(String args[]) {
		new RosController().start();
	}
}
