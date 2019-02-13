package net.bubbaland.trivia.client.dialog;

import java.awt.GridBagConstraints;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import net.bubbaland.trivia.client.TriviaClient;
import net.bubbaland.trivia.messages.CallInAnswerMessage;
import net.bubbaland.trivia.messages.MarkAnswerCorrectMessage;
import net.bubbaland.trivia.messages.MarkAnswerDuplicateMessage;
import net.bubbaland.trivia.messages.MarkAnswerIncorrectMessage;
import net.bubbaland.trivia.messages.MarkAnswerPartialMessage;
import net.bubbaland.trivia.messages.MarkAnswerUncalledMessage;
import net.bubbaland.trivia.messages.SetOperatorMessage;

/**
 * Creates a dialog that asks for the operator to confirm a correct answer.
 *
 * @author Walter Kolczynski
 */
public class OperatorDialog extends TriviaDialogPanel {

	/** The Constant serialVersionUID. */
	private static final long		serialVersionUID	= -8974614016214193902L;

	private final TriviaClient		client;
	private final int				rNumber, queueIndex;
	private final JComboBox<String>	operatorComboBox;
	private final String			previousStatus;

	/**
	 * Creates a new dialog box and prompts for response
	 *
	 * @param client
	 *            The local trivia client
	 * @param caller
	 *            The user marking the question correct
	 * @param queueIndex
	 *            The queue index of the correct answer
	 * @param statusComboBox
	 *            The status combo box for this answer, so it can be reverted to previous state if dialog is cancelled
	 */
	public OperatorDialog(TriviaClient client, String responseType, int rNumber, int queueIndex,
			JComboBox<String> statusComboBox, String previousStatus) {
		super();

		this.client = client;
		this.rNumber = rNumber;
		this.queueIndex = queueIndex;
		this.previousStatus = previousStatus;

		// Set up layout constraints
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.0;
		c.weighty = 1.0;

		// Prompt for the operator
		c.gridx = 0;
		c.gridy = 1;
		final JLabel label = new JLabel("Operator: ");
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, c);

		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		this.operatorComboBox = new JComboBox<String>();
		this.operatorComboBox.setFont(this.operatorComboBox.getFont().deriveFont(fontSize));
		this.operatorComboBox.addAncestorListener(this);
		ArrayList<String> opList = this.client.getTrivia().getOperators();
		String[] ops = opList.toArray(new String[opList.size()]);
		AutoCompleteSupport.install(operatorComboBox, GlazedLists.eventListOf(ops));
		this.add(this.operatorComboBox, c);

		// Display the dialog box
		this.dialog =
				new TriviaDialog(null, responseType, this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		this.dialog.setVisible(true);
	}

	@Override
	public void windowClosed(WindowEvent event) {
		super.windowClosed(event);
		final int option = ( (Integer) this.dialog.getValue() ).intValue();
		if (option == JOptionPane.OK_OPTION) {
			// If the OK button was pressed, mark the question as correct
			( new SwingWorker<Void, Void>() {
				@Override
				public Void doInBackground() {
					OperatorDialog.this.client.sendMessage(
							new SetOperatorMessage(OperatorDialog.this.client.getTrivia().getCurrentRoundNumber(),
									OperatorDialog.this.queueIndex,
									(String) OperatorDialog.this.operatorComboBox.getSelectedItem()));
					return null;
				}

				@Override
				public void done() {

				}
			} ).execute();

		} else {
			// If the OK button wasn't pressed, reset the status box to the previous status
			switch (previousStatus) {
				case "Duplicate":
					( new SwingWorker<Void, Void>() {
						@Override
						public Void doInBackground() {
							OperatorDialog.this.client.sendMessage(new MarkAnswerDuplicateMessage(rNumber, queueIndex));
							return null;
						}

						@Override
						public void done() {}
					} ).execute();
					break;
				case "Not Called In":
					( new SwingWorker<Void, Void>() {
						@Override
						public Void doInBackground() {
							OperatorDialog.this.client.sendMessage(new MarkAnswerUncalledMessage(rNumber, queueIndex));
							return null;
						}

						@Override
						public void done() {}
					} ).execute();
					break;
				case "Calling":
					( new SwingWorker<Void, Void>() {
						@Override
						public Void doInBackground() {
							OperatorDialog.this.client.sendMessage(new CallInAnswerMessage(rNumber, queueIndex));
							return null;
						}

						@Override
						public void done() {}
					} ).execute();
					break;
				case "Incorrect":
					( new SwingWorker<Void, Void>() {
						@Override
						public Void doInBackground() {
							OperatorDialog.this.client.sendMessage(new MarkAnswerIncorrectMessage(rNumber, queueIndex));
							return null;
						}

						@Override
						public void done() {}
					} ).execute();
					break;
				case "Partial":
					( new SwingWorker<Void, Void>() {
						@Override
						public Void doInBackground() {
							OperatorDialog.this.client.sendMessage(new MarkAnswerPartialMessage(rNumber, queueIndex));
							return null;
						}

						@Override
						public void done() {}
					} ).execute();
					break;
				case "Correct":
					( new SwingWorker<Void, Void>() {
						@Override
						public Void doInBackground() {
							OperatorDialog.this.client.sendMessage(new MarkAnswerCorrectMessage(rNumber, queueIndex));
							return null;
						}

						@Override
						public void done() {}
					} ).execute();
					break;
				default:
					break;
			}
		}

	}
}
