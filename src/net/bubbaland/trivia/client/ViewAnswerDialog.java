package net.bubbaland.trivia.client;

import java.awt.GridBagConstraints;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

/**
 * Create a dialog to display answer data.
 * 
 * @author Walter Kolczynski
 * 
 */
@SuppressWarnings("unused")
public class ViewAnswerDialog extends TriviaDialogPanel {

	private static final long	serialVersionUID	= 8466638572342233271L;

	public ViewAnswerDialog(int rNumber, int qNumber, int qValue, String qText, String aText) {

		super();

		// Set up layout constraints
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 0.5;
		constraints.weighty = 0.0;

		// Create the question number label
		constraints.gridx = 0;
		constraints.gridy = 0;
		JLabel label = new JLabel("Number: " + qNumber, SwingConstants.LEFT);
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		// Create the question value label
		constraints.gridx = 1;
		constraints.gridy = 0;
		label = new JLabel("Value: " + qValue, SwingConstants.RIGHT);
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		// Create the question text area
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 1.0;
		constraints.gridwidth = 2;
		label = new JLabel("Question", SwingConstants.CENTER);
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.weighty = 0.5;
		final QuestionPane qTextArea = this.hyperlinkedTextPane(rNumber, qText, constraints,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		qTextArea.setEditable(false);
		setTextPaneProperties(qTextArea, 300, 200, this.getForeground(), this.getBackground(), textAreaFontSize);
		constraints.weighty = 0.0;

		// Create the answer text area
		constraints.gridx = 0;
		constraints.gridy = 4;
		label = new JLabel("Answer", SwingConstants.CENTER);
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		constraints.gridx = 0;
		constraints.gridy = 5;
		constraints.weighty = 0.5;
		final JTextArea aTextArea = scrollableTextArea(aText, 300, 200, this.getForeground(), this.getBackground(),
				constraints, textAreaFontSize, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		aTextArea.setEditable(false);
		aTextArea.setFont(aTextArea.getFont().deriveFont(textAreaFontSize));
		constraints.weighty = 0.0;

		// Display the dialog box
		this.dialog = new TriviaDialog(null, "View Proposed Answer", this, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.DEFAULT_OPTION);
		this.dialog.setModal(false);
		this.dialog.setVisible(true);
	}
}
