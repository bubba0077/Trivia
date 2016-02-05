package net.bubbaland.trivia.client;

import java.awt.GridBagConstraints;
import java.awt.event.WindowEvent;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;

import net.bubbaland.trivia.ClientMessage.ClientMessageFactory;

public class TeamNumberDialog extends TriviaDialogPanel {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 6302510179287469396L;
	private final TriviaClient	client;
	private final JSpinner		teamNumberSpinner;

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
	public TeamNumberDialog(TriviaClient client) {
		super();

		this.client = client;

		// Set up layout constraints
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.0;
		c.weighty = 1.0;

		// Prompt for the operator
		c.gridx = 0;
		c.gridy = 1;
		final JLabel label = new JLabel("Team Number: ");
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, c);

		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		this.teamNumberSpinner = new JSpinner(new SpinnerNumberModel(client.getTrivia().getTeamNumber(), 0, 100, 1));
		this.teamNumberSpinner.setFont(this.teamNumberSpinner.getFont().deriveFont(fontSize));
		this.teamNumberSpinner.addAncestorListener(this);
		this.add(this.teamNumberSpinner, c);

		// Display the dialog box
		this.dialog = new TriviaDialog(null, "Set Team Number", this, JOptionPane.PLAIN_MESSAGE,
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
				@Override
				public Void doInBackground() {
					TeamNumberDialog.this.client.sendMessage(ClientMessageFactory.setTeamNumber(
							( (Integer) TeamNumberDialog.this.teamNumberSpinner.getValue() ).intValue()));
					return null;
				}

				@Override
				public void done() {

				}
			} ).execute();

		}
	}

}
