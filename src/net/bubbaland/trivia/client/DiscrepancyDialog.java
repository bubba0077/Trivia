package net.bubbaland.trivia.client;

import java.awt.GridBagConstraints;
import java.awt.event.WindowEvent;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import net.bubbaland.trivia.Trivia;

public class DiscrepancyDialog extends TriviaDialogPanel {
	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= 7708693892976942384L;

	private final TriviaClient	client;
	private final int			rNumber;
	private final JTextField	discrepancyTextField;

	/**
	 * Instantiates a new user login.
	 * 
	 * @param client
	 *            the client
	 */
	public DiscrepancyDialog(TriviaClient client, int rNumber) {
		super();

		this.client = client;
		this.rNumber = rNumber;

		final Trivia trivia = client.getTrivia();
		final String oldText = trivia.getDiscrepancyText(rNumber);

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
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		this.discrepancyTextField = new JTextField(oldText, 10);
		discrepancyTextField.setFont(discrepancyTextField.getFont().deriveFont(fontSize));
		this.add(discrepancyTextField, constraints);
		discrepancyTextField.addAncestorListener(this);

		// Display the dialog box
		this.dialog = new TriviaDialog(null, "Score Discrepancy", this, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		this.dialog.setVisible(true);
	}

	@Override
	public void windowClosed(WindowEvent event) {
		super.windowClosed(event);
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
