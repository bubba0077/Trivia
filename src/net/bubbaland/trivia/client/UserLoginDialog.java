package net.bubbaland.trivia.client;

import java.awt.GridBagConstraints;
import java.awt.event.WindowEvent;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * Creates prompt for user name.
 * 
 * @author Walter Kolczynski
 * 
 */
public class UserLoginDialog extends TriviaDialogPanel {

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= 7708693892976942384L;

	private final TriviaClient	client;
	private final JTextField	userTextField;

	/**
	 * Instantiates a new user login.
	 * 
	 * @param client
	 *            the client
	 * @param role
	 */
	public UserLoginDialog(TriviaClient client) {
		super();

		this.client = client;

		// Set up layout constraints
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.0;
		c.weighty = 0.0;

		// Prompt for user name
		c.gridx = 0;
		c.gridy = 0;
		final JLabel label = new JLabel("Enter user name: ");
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, c);

		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1.0;
		this.userTextField = new JTextField("", 15);
		userTextField.setFont(userTextField.getFont().deriveFont(fontSize));
		this.add(userTextField, c);
		userTextField.addAncestorListener(this);

		final String userName = client.getUser();
		int options;
		if (userName == null) {
			options = JOptionPane.DEFAULT_OPTION;
		} else {
			userTextField.setText(userName);
			options = JOptionPane.OK_CANCEL_OPTION;
		}

		// Display the dialog box
		this.dialog = new TriviaDialog(null, "User Login", this, JOptionPane.PLAIN_MESSAGE, options);
		if (userName == null) {
			this.dialog.setModal(true);
			this.dialog.removeWindowListener(this);
			this.dialog.setVisible(true);

			final String user = userTextField.getText();

			// If the OK button was pressed, add the proposed answer to the queue
			final int option = ( (Integer) this.dialog.getValue() ).intValue();

			if (option != JOptionPane.CLOSED_OPTION) {
				if (user.toCharArray().length != 0) {
					client.setUser(user);
				} else {
					new UserLoginDialog(client);
				}
			} else {
				if (client.getUser() == null) {
					System.exit(0);
				}
			}

		} else {
			this.dialog.setVisible(true);
		}
	}

	@Override
	public void windowClosed(WindowEvent event) {
		super.windowClosed(event);
		// Set the user name to input value
		final String user = userTextField.getText();

		// If the OK button was pressed, add the proposed answer to the queue
		final int option = ( (Integer) this.dialog.getValue() ).intValue();

		if (option != JOptionPane.CLOSED_OPTION) {
			// Check if name is already being used
			while (client.getIdleUserHash() == null) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException exception) {
					// Nothing to do
				}
			}
			if (client.getIdleUserHash().containsKey(user)) {
				int confirm = (int) JOptionPane.showConfirmDialog(null, "The name \"" + user
						+ "\" has been connected recently. Do you still want to use this name?", "Name Conflict",
						JOptionPane.YES_NO_OPTION);
				if (confirm == JOptionPane.NO_OPTION) {
					new UserLoginDialog(client);
					return;
				}
			}

			if (user.toCharArray().length != 0) {
				client.setUser(user);
			} else {
				new UserLoginDialog(client);
			}
		} else {
			if (client.getUser() == null) {
				System.exit(0);
			}
		}

	}

}
