package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.math.BigInteger;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;

import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.messages.EditQuestionMessage;

public class EditQuestionDialog extends TriviaDialogPanel implements ActionListener {

	private static final long	serialVersionUID	= 8157338357601793846L;

	/**
	 * GUI elements we will need to change on an action
	 */
	private final JToggleButton	correctButton;
	private final JTextField	submitterTextField;
	private final TriviaClient	client;
	private final int			rNumber, qNumber;
	private final JTextArea		qTextArea, aTextArea;
	private final JSpinner		qValueSpinner;
	private final Color			correctColor;

	public EditQuestionDialog(TriviaClient client, int rNumber, int qNumber) {

		super();

		this.client = client;
		this.rNumber = rNumber;
		this.qNumber = qNumber;
		this.correctColor =
				new Color(new BigInteger(TriviaGUI.PROPERTIES.getProperty("AnswerQueue.Correct.Color"), 16).intValue());

		// Get all of the current data for the question
		final Trivia trivia = client.getTrivia();

		final boolean existingCorrect = trivia.getRound(rNumber).isCorrect(qNumber);
		int existingValue = trivia.getRound(rNumber).getValue(qNumber);
		final String existingQText = trivia.getRound(rNumber).getQuestionText(qNumber);
		final String existingAText = trivia.getRound(rNumber).getAnswerText(qNumber);
		final String existingSubmitter = trivia.getRound(rNumber).getSubmitter(qNumber);
		// final String existingOperator = trivia.getOperator(rNumber, qNumber);

		// Set up layout constraints
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;

		// Round number label
		constraints.gridwidth = 2;
		constraints.gridx = 0;
		constraints.gridy = 0;
		JLabel label = new JLabel("Round: " + rNumber);
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		// Question number label
		constraints.gridx = 2;
		constraints.gridy = 0;
		label = new JLabel("Question: " + qNumber);
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);
		constraints.gridwidth = 1;

		// Value label
		constraints.gridx = 0;
		constraints.gridy = 1;
		label = new JLabel("Value:");
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		// Value spinner
		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.weightx = 0.5;
		existingValue = ( 10 < existingValue ) ? existingValue : 10;
		existingValue = ( 500 > existingValue ) ? existingValue : 500;
		this.qValueSpinner = new JSpinner(new SpinnerNumberModel(existingValue, 10, 500, 5));
		this.qValueSpinner.setFont(this.qValueSpinner.getFont().deriveFont(fontSize));
		this.addEnterOverride(this.qValueSpinner);
		this.add(this.qValueSpinner, constraints);
		constraints.weightx = 0.0;

		// Toggle button to change correctness
		constraints.gridx = 3;
		constraints.gridy = 1;
		this.correctButton = new JToggleButton();
		this.correctButton.setMargin(new Insets(0, 0, 0, 0));
		this.correctButton.addActionListener(this);
		this.add(this.correctButton, constraints);
		constraints.weightx = 0.0;

		// Question label
		constraints.gridwidth = 4;
		constraints.gridx = 0;
		constraints.gridy = 2;
		label = new JLabel("Question:");
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		// Question text
		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.weightx = 1.0;
		constraints.weighty = 0.6;
		this.qTextArea = new JTextArea(existingQText, 4, 50);
		this.qTextArea.setLineWrap(true);
		this.qTextArea.setWrapStyleWord(true);
		this.qTextArea.setFont(this.qTextArea.getFont().deriveFont(textAreaFontSize));
		this.qTextArea.addAncestorListener(this);
		this.addEnterOverride(this.qTextArea);
		JScrollPane scrollPane = new JScrollPane(this.qTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(0, 200));
		this.add(scrollPane, constraints);
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;

		// Answer label
		constraints.gridx = 0;
		constraints.gridy = 4;
		label = new JLabel("Answer:");
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		// Answer text
		constraints.gridx = 0;
		constraints.gridy = 5;
		constraints.weightx = 1.0;
		constraints.weighty = 0.4;
		this.aTextArea = new JTextArea(existingAText, 4, 50);
		this.aTextArea.setLineWrap(true);
		this.aTextArea.setWrapStyleWord(true);
		this.aTextArea.setFont(this.qTextArea.getFont().deriveFont(textAreaFontSize));
		this.addEnterOverride(this.aTextArea);
		scrollPane = new JScrollPane(this.aTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(0, 200));
		this.add(scrollPane, constraints);
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;

		// Submitter label
		constraints.gridwidth = 2;
		constraints.gridx = 0;
		constraints.gridy = 6;
		label = new JLabel("Credit:");
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		// Submitter text field; will be disable if question is incorrect
		constraints.gridx = 2;
		constraints.gridy = 6;
		this.submitterTextField = new JTextField(existingSubmitter);
		this.add(this.submitterTextField, constraints);

		this.correctButton.setText("Correct");

		// Change text on correct button and set editable state of sub/op boxes based on correctness
		if (existingCorrect) {
			this.correctButton.setBackground(this.correctColor);
			this.correctButton.setSelected(true);
			this.submitterTextField.setEditable(true);
			this.submitterTextField.setBackground(Color.WHITE);
		} else {
			this.correctButton.setBackground(null);
			this.correctButton.setSelected(false);
			this.submitterTextField.setEditable(false);
			this.submitterTextField.setBackground(this.getBackground());
		}

		// Display the dialog box
		this.dialog =
				new TriviaDialog(null, "Edit Question", this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		this.dialog.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(this.correctButton)) {
			// If the correctness was changed, update GUI elements appropriately
			if (this.correctButton.isSelected()) {
				this.correctButton.setBackground(this.correctColor);
				this.submitterTextField.setEditable(true);
				this.submitterTextField.setBackground(Color.WHITE);
			} else {
				this.correctButton.setBackground(null);
				this.submitterTextField.setEditable(false);
				this.submitterTextField.setBackground(this.getBackground());
			}
		}

	}

	@Override
	public void windowClosed(WindowEvent event) {
		super.windowClosed(event);
		// If the OK button was pressed, add the proposed answer to the queue
		final int option = ( (Integer) this.dialog.getValue() ).intValue();
		if (option == JOptionPane.OK_OPTION) {
			// Get the input data
			final boolean isCorrect = this.correctButton.isSelected();
			final int qValue = (int) this.qValueSpinner.getValue();
			final String qText = this.qTextArea.getText();
			final String aText = this.aTextArea.getText();
			final String submitter = this.submitterTextField.getText();

			// Edit the question on the server
			( new SwingWorker<Void, Void>() {
				@Override
				public Void doInBackground() {
					EditQuestionDialog.this.client.sendMessage(new EditQuestionMessage(EditQuestionDialog.this.rNumber,
							EditQuestionDialog.this.qNumber, qText, qValue, aText, isCorrect, submitter));
					return null;
				}

				@Override
				public void done() {
					EditQuestionDialog.this.client.log("Question #" + EditQuestionDialog.this.qNumber + " edited.");
				}
			} ).execute();

		}

	}

}
