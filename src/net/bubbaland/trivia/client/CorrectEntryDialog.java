package net.bubbaland.trivia.client;

import java.awt.GridBagConstraints;
import java.awt.event.WindowEvent;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import net.bubbaland.trivia.ClientMessage.ClientMessageFactory;

/**
 * Creates a dialog that asks for the operator to confirm a correct answer.
 * 
 * @author Walter Kolczynski
 */
public class CorrectEntryDialog extends TriviaDialogPanel {

	/** The Constant serialVersionUID. */
	private static final long		serialVersionUID	= -8974614016214193902L;

	private final TriviaClient		client;
	private final int				queueIndex;
	private final JComboBox<String>	statusComboBox;
	private final JTextField		operatorTextField;

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
	public CorrectEntryDialog(TriviaClient client, int queueIndex, JComboBox<String> statusComboBox) {
		super();

		this.client = client;
		this.queueIndex = queueIndex;
		this.statusComboBox = statusComboBox;

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
		this.operatorTextField = new JTextField("", 15);
		operatorTextField.setFont(operatorTextField.getFont().deriveFont(fontSize));
		operatorTextField.addAncestorListener(this);
		this.add(operatorTextField, c);

		// Display the dialog box
		this.dialog = new TriviaDialog(null, "Mark question correct", this, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		this.dialog.setVisible(true);
	}

	@Override
	public void windowClosed(WindowEvent event) {
		super.windowClosed(event);
		final int option = ( (Integer) this.dialog.getValue() ).intValue();
		if (option == JOptionPane.OK_OPTION) {
			// If the OK button was pressed, mark the question as correct
			( new SwingWorker<Void, Void>() {
				public Void doInBackground() {
					CorrectEntryDialog.this.client.sendMessage(ClientMessageFactory.markCorrect(queueIndex,
							operatorTextField.getText()));
					return null;
				}

				public void done() {

				}
			} ).execute();

		} else {
			// If the OK button wasn't pressed, reset the status box to the previous status
			statusComboBox.setSelectedItem(client.getTrivia().getAnswerQueueStatus(queueIndex));
		}

	}
}
