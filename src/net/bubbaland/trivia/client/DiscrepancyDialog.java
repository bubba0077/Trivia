package net.bubbaland.trivia.client;

import java.awt.GridBagConstraints;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import net.bubbaland.trivia.Trivia;

public class DiscrepancyDialog extends TriviaDialogPanel {
	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= 7708693892976942384L;

	/** The Constant FONT_SIZE. */
	private static final float	FONT_SIZE			= 20.0f;

	/**
	 * Instantiates a new user login.
	 * 
	 * @param client
	 *            the client
	 */
	public DiscrepancyDialog(TriviaClient client, int rNumber) {
		super();

		Trivia trivia = client.getTrivia();
		String oldText = trivia.getDiscrepancyText(rNumber);

		// Set up layout constraints
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;

		//
		constraints.gridx = 0;
		constraints.gridy = 0;
		final JLabel label = new JLabel("Enter discrepancy note for round: " + rNumber);
		label.setFont(label.getFont().deriveFont(FONT_SIZE));
		this.add(label, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		final JTextField discrepancyTextField = new JTextField(oldText, 10);
		discrepancyTextField.setFont(discrepancyTextField.getFont().deriveFont(FONT_SIZE));
		this.add(discrepancyTextField, constraints);
		discrepancyTextField.addAncestorListener(this);

		// Display the dialog box
		this.dialog = new TriviaDialog(null, "Score Discrepancy", this, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		this.dialog.setVisible(true);

		// If the OK button was pressed, add the proposed answer to the queue
		final int option = ( (Integer) this.dialog.getValue() ).intValue();

		if (option == JOptionPane.OK_OPTION) {
			int tryNumber = 0;
			boolean success = false;
			while (tryNumber < Integer.parseInt(TriviaClient.PROPERTIES.getProperty("MaxRetries")) && success == false) {
				tryNumber++;
				try {
					client.getServer().setDiscrepancyText(client.getUser(), rNumber, discrepancyTextField.getText());
					success = true;
				} catch (final Exception exception) {
					client.log("Couldn't set discrepancy text on server (try #" + tryNumber + ").");
				}
			}

			if (!success) {
				client.disconnected();
				return;
			}

			return;
		}

	}
}
