package com.minhld.utils;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class EzSlider extends JPanel {
	private static final long serialVersionUID = 8183929984967684854L;
	public static final int FLAG_ODD_STEP = 1;
	
	String title;
	JLabel titleLabel;
	JSlider bar;
	
	public EzSlider(String label, int min, int max) {
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(200, 80));

		this.title = label;
		int currVal = Settings.getValue(this.title);
		
		titleLabel = new JLabel(this.title);
		titleLabel.setText(this.title + ": " + currVal);
		add(titleLabel, BorderLayout.NORTH);
		
		bar = new JSlider(min, max);
		bar.setOrientation(JSlider.HORIZONTAL);
		bar.setSize(200, 15);
		bar.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				// update setting object
				int val = bar.getValue();
				Settings.setValue(EzSlider.this.title, val);
				
				// update label of the slider 
				EzSlider.this.titleLabel.setText(EzSlider.this.title + ": " + val);
			}
		});
		// bar.setPaintTicks(true);
		// bar.setPaintLabels(true);
		bar.setValue(currVal);
		bar.setFocusable(false);
		add(bar, BorderLayout.CENTER);
	}
	
	public EzSlider(String label, int min, int max, int slideFlag) {
		this(label, min, max);
		if (slideFlag == FLAG_ODD_STEP) {
//			bar.setMinorTickSpacing(2);
//			bar.setSnapToTicks(true);
		}
	}
}
