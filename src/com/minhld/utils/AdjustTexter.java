package com.minhld.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class AdjustTexter extends JPanel {
	private static final long serialVersionUID = 1L;
	
	String title;
	JTextField text;
	
	public AdjustTexter(String label) {
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(0, 5, 5, 5));
		setPreferredSize(new Dimension(200, 75));

		this.title = label;
		int currVal = Settings.getValue(this.title);

		JLabel titleLabel = new JLabel(this.title);
		titleLabel.setPreferredSize(new Dimension(150, 30));
		add(titleLabel, BorderLayout.NORTH);

		text = new JTextField("" + currVal);
		text.setPreferredSize(new Dimension(150, 35));
		text.setBorder(new LineBorder(Color.GRAY));
		// text.setFocusable(false);
		text.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateFieldState();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateFieldState();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				updateFieldState();
			}
			
			protected void updateFieldState() {
                String textStr = text.getText();
                int val = 0;
                try {
                	val = Integer.parseInt(textStr);
                	text.setBorder(new LineBorder(Color.GRAY));
                } catch (NumberFormatException nfe) {
                	text.setBorder(new LineBorder(Color.RED));
                }
                Settings.setValue(AdjustTexter.this.title, val);
            }
		});
		add(text, BorderLayout.CENTER);
	}
}
