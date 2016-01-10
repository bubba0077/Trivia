package net.bubbaland.trivia.client;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;

import net.bubbaland.trivia.ClientMessage.ClientMessageFactory;
import net.bubbaland.trivia.Trivia;

/**
 * Creates a prompt to enter new question data.
 *
 * @author Walter Kolczynski
 *
 */
public class NewQuestionDialog extends TriviaDialogPanel {

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= 8250659442772286086L;

	private final TriviaClient	client;
	private final int			nQuestions;
	private final int			qNumberStart;
	private final JSpinner		qNumberSpinner;
	private final JSpinner		qValueSpinner;
	private final JTextArea		qTextArea;

	private boolean				newOpen;

	/**
	 * Instantiates a new question entry window.
	 *
	 * @param client
	 *            The local trivia client
	 * @param nQuestions
	 *            the number of questions
	 * @param qNumberStart
	 *            the default question number
	 */
	public NewQuestionDialog(TriviaClient client, int nQuestions, int qNumberStart, final boolean newOpen) {
		this(client, nQuestions, qNumberStart, 10, "", newOpen);
	}

	/**
	 * Instantiates a new question entry window.
	 *
	 * @param client
	 *            The local trivia client
	 * @param nQuestions
	 *            the number of questions
	 * @param qNumberStart
	 *            the default question number
	 * @param qValueStart
	 *            the default question value
	 */
	public NewQuestionDialog(final TriviaClient client, int nQuestions, int qNumberStart, int qValueStart,
			final boolean newOpen) {
		this(client, nQuestions, qNumberStart, qValueStart, "", newOpen);
	}

	/**
	 * Instantiates a new question entry window.
	 *
	 * @param client
	 *            The local trivia client
	 * @param nQuestions
	 *            the number of questions
	 * @param qNumberStart
	 *            the default question number
	 * @param qValueStart
	 *            the default question value
	 * @param qTextStart
	 *            the initial question text
	 */
	public NewQuestionDialog(final TriviaClient client, final int nQuestions, final int qNumberStart,
			final int qValueStart, final String qTextStart, final boolean newOpen) {
		super();

		this.client = client;
		this.nQuestions = nQuestions;
		this.qNumberStart = qNumberStart;
		this.newOpen = newOpen;

		if (this.newOpen) {
			// Open the question on the server temporarily
			( new SwingWorker<Void, Void>() {
				@Override
				public Void doInBackground() {
					NewQuestionDialog.this.client.sendMessage(ClientMessageFactory.open(qNumberStart));
					return null;
				}

				@Override
				public void done() {

				}
			} ).execute();
		}

		// Set up layout constraints
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;

		// Create the question number spinner
		constraints.gridx = 0;
		constraints.gridy = 0;
		JLabel label = new JLabel("Question Number: ");
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		constraints.gridx = 1;
		constraints.gridy = 0;
		this.qNumberSpinner = new JSpinner(new SpinnerNumberModel(qNumberStart, 1, nQuestions, 1));
		this.qNumberSpinner.setFont(this.qNumberSpinner.getFont().deriveFont(fontSize));
		this.addEnterOverride(this.qNumberSpinner);
		this.add(this.qNumberSpinner, constraints);

		// Create the question value spinner
		constraints.gridx = 0;
		constraints.gridy = 1;
		label = new JLabel("Question Value: ");
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		constraints.gridx = 1;
		constraints.gridy = 1;
		this.qValueSpinner = new JSpinner(new SpinnerNumberModel(qValueStart, 0, 1000, 5));
		this.qValueSpinner.setFont(this.qValueSpinner.getFont().deriveFont(fontSize));
		this.addEnterOverride(this.qValueSpinner);
		this.add(this.qValueSpinner, constraints);
		( (JSpinner.NumberEditor) this.qValueSpinner.getEditor() ).getTextField().addAncestorListener(this);
		( (JSpinner.NumberEditor) this.qValueSpinner.getEditor() ).getTextField().addFocusListener(this);

		// Create input area for the question text
		constraints.gridx = 0;
		constraints.gridy = 2;
		label = new JLabel("Question: ");
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.gridwidth = 2;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		this.qTextArea = new JTextArea(qTextStart, 4, 50);
		this.qTextArea.setLineWrap(true);
		this.qTextArea.setWrapStyleWord(true);
		this.qTextArea.setFont(this.qTextArea.getFont().deriveFont(textAreaFontSize));
		this.qTextArea.setToolTipText("Visual Trivia will automatically be linked to appropriate page.");
		this.addEnterOverride(this.qTextArea);

		final JScrollPane scrollPane = new JScrollPane(this.qTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(0, 200));
		this.add(scrollPane, constraints);
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;
		constraints.gridwidth = 1;

		// Display the dialog box
		this.dialog = new TriviaDialog(null, "Open New Question", this, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		this.dialog.setVisible(true);
	}

	@Override
	public void windowClosed(WindowEvent event) {
		super.windowClosed(event);
		// If the OK button was pressed, open the question
		final int option = ( (Integer) this.dialog.getValue() ).intValue();
		// Get the current Trivia data object
		final Trivia trivia = this.client.getTrivia();
		final int currentRound = trivia.getCurrentRoundNumber();

		if (option == JOptionPane.OK_OPTION) {
			// Get the input data
			final int qNumber = (int) this.qNumberSpinner.getValue();
			final int qValue = (int) this.qValueSpinner.getValue();
			final String qText = this.qTextArea.getText();

			// Get the existing question data
			final int existingQValue = trivia.getValue(currentRound, qNumber);
			final String existingQText = trivia.getQuestionText(currentRound, qNumber);

			if (this.qNumberStart == qNumber) {
				// Open question
				( new SwingWorker<Void, Void>() {
					@Override
					public Void doInBackground() {
						NewQuestionDialog.this.client
								.sendMessage(ClientMessageFactory.setQuestion(qNumber, qValue, qText));
						return null;
					}

					@Override
					public void done() {
						NewQuestionDialog.this.client.log("Question #" + qNumber + " submitted.");
					}
				} ).execute();
			} else {
				if (trivia.beenOpen(currentRound, qNumber)) {
					// Overwrite
					this.confirmOverwrite(qNumber, existingQValue, existingQText, qValue, qText);
				} else {
					// Remap
					this.confirmNumberChange(this.qNumberStart, qNumber, qValue, qText);
				}
			}
		} else if (this.newOpen) {
			( new SwingWorker<Void, Void>() {
				@Override
				public Void doInBackground() {
					NewQuestionDialog.this.client
							.sendMessage(ClientMessageFactory.resetQuestion(NewQuestionDialog.this.qNumberStart));
					return null;
				}

				@Override
				public void done() {

				}
			} ).execute();
		}
	}

	private void confirmNumberChange(final int qNumberStart, final int qNumber, final int qValue, final String qText) {
		this.removeAll();

		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;

		constraints.gridx = 0;
		constraints.gridy = 0;
		final JLabel label = new JLabel("Change question number from " + qNumberStart + " to " + qNumber + "?");
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		this.dialog = new TriviaDialog(null, "Confirm Question Number Change " + qNumberStart + " to " + qNumber, this,
				JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		this.dialog.removeWindowListener(this);
		this.dialog.setModal(true);
		this.dialog.setVisible(true);

		final int confirm = ( (Integer) this.dialog.getValue() ).intValue();
		if (confirm != JOptionPane.OK_OPTION) {
			new NewQuestionDialog(this.client, this.nQuestions, qNumberStart, qValue, qText, this.newOpen);
			return;
		}

		// Remap question on server
		( new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() {
				NewQuestionDialog.this.client.sendMessage(ClientMessageFactory.resetQuestion(qNumber));
				NewQuestionDialog.this.client
						.sendMessage(ClientMessageFactory.remapQuestion(NewQuestionDialog.this.qNumberStart, qNumber));
				NewQuestionDialog.this.client.sendMessage(ClientMessageFactory.setQuestion(qNumber, qValue, qText));
				return null;
			}

			@Override
			public void done() {

			}
		} ).execute();
	}

	private void confirmOverwrite(final int qNumber, final int existingQValue, final String existingQText,
			final int qValue, final String qText) {
		this.removeAll();

		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;

		constraints.gridx = 0;
		constraints.gridy = 0;
		JLabel label = new JLabel("Question already open, overwrite?");
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		// Show existing question data
		constraints.gridx = 0;
		constraints.gridy = 1;
		label = new JLabel("Existing question:");
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.weightx = 0.5;
		constraints.weighty = 0.5;
		JTextArea textArea = new JTextArea(existingQText);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(this.qTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(100, 100));
		scrollPane.setViewportView(textArea);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		this.add(scrollPane, constraints);
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;

		constraints.gridx = 0;
		constraints.gridy = 3;
		label = new JLabel("Value: " + existingQValue);
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		// Show new question data
		constraints.gridx = 0;
		constraints.gridy = 4;
		label = new JLabel("Entered question:");
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		constraints.gridx = 0;
		constraints.gridy = 5;
		constraints.weightx = 0.5;
		constraints.weighty = 0.5;
		textArea = new JTextArea(qText);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(true);
		textArea.addAncestorListener(this);
		this.addEnterOverride(textArea);
		scrollPane = new JScrollPane(this.qTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(100, 100));
		scrollPane.setViewportView(textArea);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		this.add(scrollPane, constraints);
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;

		constraints.gridx = 0;
		constraints.gridy = 6;
		label = new JLabel("Value: " + qValue);
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		this.dialog = new TriviaDialog(null, "Confirm Question Overwrite " + qNumber, this, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		this.dialog.removeWindowListener(this);
		this.dialog.setModal(true);
		this.dialog.setVisible(true);

		final int confirm = ( (Integer) this.dialog.getValue() ).intValue();
		if (confirm != JOptionPane.OK_OPTION) {
			new NewQuestionDialog(this.client, this.nQuestions, qNumber, qValue, qText, this.newOpen);
			return;
		}

		( new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() {
				NewQuestionDialog.this.client.sendMessage(ClientMessageFactory.resetQuestion(qNumber));
				NewQuestionDialog.this.client
						.sendMessage(ClientMessageFactory.remapQuestion(NewQuestionDialog.this.qNumberStart, qNumber));
				NewQuestionDialog.this.client.sendMessage(ClientMessageFactory.setQuestion(qNumber, qValue, qText));
				return null;
			}

			@Override
			public void done() {

			}
		} ).execute();
	}
}
