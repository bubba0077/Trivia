package net.bubbaland.trivia.client;

import java.awt.Dimension;
import java.awt.GridBagConstraints;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

public class ViewAnswerDialog extends TriviaDialogPanel {

	private static final long	serialVersionUID	= 8466638572342233271L;

	/**
	 * Font sizes
	 */
	private static final float	LABEL_FONT_SIZE		= 20.0f;
	private static final float	TEXTAREA_FONT_SIZE	= 16.0f;

	public ViewAnswerDialog(TriviaClient client, int qNumber, int qValue, String qText, String aText) {

		super();

		// Set up layout constraints
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 0.5;
		constraints.weighty = 0.0;

		// Create the question number label
		constraints.gridx = 0;
		constraints.gridy = 0;
		JLabel label = new JLabel("Number: " + qNumber, JLabel.LEFT);
		label.setFont(label.getFont().deriveFont(LABEL_FONT_SIZE));
		this.add(label, constraints);

		// Create the question value label
		constraints.gridx = 1;
		constraints.gridy = 0;
		label = new JLabel("Value: " + qValue, JLabel.RIGHT);
		label.setFont(label.getFont().deriveFont(LABEL_FONT_SIZE));
		this.add(label, constraints);

		// Create the question text area
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 1.0;
		constraints.gridwidth = 2;
		label = new JLabel("Question", JLabel.CENTER);
		label.setFont(label.getFont().deriveFont(LABEL_FONT_SIZE));
		this.add(label, constraints);

		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.weighty = 0.5;
		final JTextArea qTextArea = new JTextArea(qText, 4, 50);
		qTextArea.setEditable(false);
		qTextArea.setLineWrap(true);
		qTextArea.setWrapStyleWord(true);
		qTextArea.setFont(qTextArea.getFont().deriveFont(TEXTAREA_FONT_SIZE));

		JScrollPane scrollPane = new JScrollPane(qTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(0, 200));
		this.add(scrollPane, constraints);
		constraints.weighty = 0.0;


		// Create the answer text area
		constraints.gridx = 0;
		constraints.gridy = 4;
		label = new JLabel("Answer", JLabel.CENTER);
		label.setFont(label.getFont().deriveFont(LABEL_FONT_SIZE));
		this.add(label, constraints);

		constraints.gridx = 0;
		constraints.gridy = 5;
		constraints.weighty = 0.5;
		final JTextArea aTextArea = new JTextArea(aText, 4, 50);
		aTextArea.setEditable(false);
		aTextArea.setLineWrap(true);
		aTextArea.setWrapStyleWord(true);
		aTextArea.setFont(aTextArea.getFont().deriveFont(TEXTAREA_FONT_SIZE));

		scrollPane = new JScrollPane(aTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(0, 200));
		this.add(scrollPane, constraints);
		constraints.weighty = 0.0;

		// Display the dialog box
		this.dialog = new TriviaDialog(null, "View Question", this, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.DEFAULT_OPTION);
		this.dialog.setModal(false);
		this.dialog.setVisible(true);


	}
}
