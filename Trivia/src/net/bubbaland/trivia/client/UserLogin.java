package net.bubbaland.trivia.client;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * Creates prompt for user name.
 * 
 * @author Walter Kolczynski
 * 
 */
public class UserLogin extends JPanel implements AncestorListener {

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
	public UserLogin(TriviaClient client) {
		super(new GridBagLayout());

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
		label.setFont(label.getFont().deriveFont(FONT_SIZE));
		this.add(label, c);

		c.gridx = 1;
		c.gridy = 0;
		final JTextField userTextField = new JTextField("", 10);
		userTextField.setFont(userTextField.getFont().deriveFont(FONT_SIZE));
		userTextField.setToolTipText("This will be used for both flow tracking and IRC");
		this.add(userTextField, c);
		userTextField.addAncestorListener(this);

		// Display dialog box
		final JOptionPane pane = new JOptionPane(this, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION);
		final JDialog dialog = pane.createDialog(this.getParent(), "User Login");
		dialog.setVisible(true);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		// Set the user name to input value
		final String user = userTextField.getText();
		// if( user.toCharArray().length != 0 ) {
		client.setUser(user);
		// } else {
		// client.exit();
		// new UserLogin(client);
		// }

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.AncestorListener#ancestorAdded(javax.swing.event.AncestorEvent)
	 */
	@Override
	public void ancestorAdded(AncestorEvent event) {
		// Change the focus to the text area when created
		final JComponent component = event.getComponent();
		component.requestFocusInWindow();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.AncestorListener#ancestorMoved(javax.swing.event.AncestorEvent)
	 */
	@Override
	public void ancestorMoved(AncestorEvent event) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.AncestorListener#ancestorRemoved(javax.swing.event.AncestorEvent)
	 */
	@Override
	public void ancestorRemoved(AncestorEvent event) {
	}


}
