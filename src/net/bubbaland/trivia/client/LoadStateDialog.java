package net.bubbaland.trivia.client;

import java.awt.GridBagConstraints;
import java.rmi.RemoteException;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * Creates a dialog that lists the saves and prompts for one to load.
 * 
 * @author Walter Kolczynski
 * 
 */
public class LoadStateDialog extends TriviaDialogPanel {

	private static final long	serialVersionUID	= -3297076605620744620L;

	public LoadStateDialog(TriviaClient client) {

		super();

		// Try to communicate with server
		String[] saveList = null;
		int tryNumber = 0;
		boolean success = false;
		while (tryNumber < Integer.parseInt(TriviaClient.PROPERTIES.getProperty("MaxRetries")) && success == false) {
			tryNumber++;
			try {
				// Set the announced values
				saveList = client.getServer().listSaves();
				success = true;
			} catch (final RemoteException e) {
				// Retry if the connection is broken
				client.log("Couldn't get list of save from server (try #" + tryNumber + ").");
			}
		}

		if (!success) {
			// Couldn't retrieve data from server, create a disconnected dialog box
			client.disconnected();
			return;
		}

		// Set up base layout constraints
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;


		// Add warning
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 2;
		JLabel label = new JLabel("Warning: This will overwrite all current data.");
		label.setFont(label.getFont().deriveFont(fontSize));
		label.setForeground(warningColor);
		this.add(label, constraints);
		constraints.gridwidth = 1;

		// Instruction label
		constraints.gridx = 0;
		constraints.gridy = 1;
		label = new JLabel("Choose file to load:");
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		constraints.gridx = 1;
		constraints.gridy = 1;
		final JComboBox<String> chooser = new JComboBox<String>(saveList);
		chooser.addAncestorListener(this);
		this.add(chooser, constraints);

		// Display the dialog box
		this.dialog = new TriviaDialog(null, "Load saved state", this, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		this.dialog.setVisible(true);

		// If the OK button was pressed, add the proposed answer to the queue
		final int option = ( (Integer) this.dialog.getValue() ).intValue();

		if (option == JOptionPane.OK_OPTION) {
			final String saveFile = (String) chooser.getSelectedItem();

			// Try to communicate with server
			tryNumber = 0;
			success = false;
			while (tryNumber < Integer.parseInt(TriviaClient.PROPERTIES.getProperty("MaxRetries")) && success == false) {
				tryNumber++;
				try {
					// Set the announced values
					client.getServer().loadState(client.getUser(), saveFile);
					success = true;
				} catch (final RemoteException e) {
					// Retry if the connection is broken
					client.log("Couldn't load save file on server (try #" + tryNumber + ").");
				}
			}

			if (!success) {
				// Couldn't retrieve data from server, create a disconnected dialog box
				client.disconnected();
				return;
			}

		}
	}

}