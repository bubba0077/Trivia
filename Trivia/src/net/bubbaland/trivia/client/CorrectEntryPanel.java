package net.bubbaland.trivia.client;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.rmi.RemoteException;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import net.bubbaland.trivia.TriviaInterface;

/**
 * Creates a dialog that asks for the operator to confirm a correct answer.
 * 
 * @author Walter Kolczynski
 */
public class CorrectEntryPanel extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= -8974614016214193902L;

	// Font size
	private static final float	FONT_SIZE			= 20.0f;

	/**
	 * Creates a new dialog box and prompts for response
	 * 
	 * @param server
	 *            The remote trivia server
	 * @param client
	 *            The local trivia client
	 * @param caller
	 *            The user marking the question correct
	 * @param queueIndex
	 *            The queue index of the correct answer
	 * @param statusComboBox
	 *            The status combo box for this answer, so it can be reverted to previous state if dialog is cancelled
	 */
	public CorrectEntryPanel(TriviaInterface server, TriviaClient client, String caller, int queueIndex,
			JComboBox<String> statusComboBox) {
		super(new GridBagLayout());

		// Set up layout constraints
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.0;
		c.weighty = 0.0;

		// Prompt for the operator
		c.gridx = 0;
		c.gridy = 1;
		final JLabel label = new JLabel("Operator: ");
		label.setFont(label.getFont().deriveFont(FONT_SIZE));
		this.add(label, c);

		c.gridx = 1;
		c.gridy = 1;
		final JTextField operatorTextField = new JTextField("", 15);
		operatorTextField.setFont(operatorTextField.getFont().deriveFont(FONT_SIZE));
		this.add(operatorTextField, c);

		// Display the dialog box
		final JOptionPane pane = new JOptionPane(this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		final JDialog dialog = pane.createDialog(this.getParent(), "Mark question correct");
		dialog.setVisible(true);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		final int option = ( (Integer) pane.getValue() ).intValue();
		if (option == JOptionPane.OK_OPTION) {
			// If the OK button was pressed, mark the question as correct
			int tryNumber = 0;
			boolean success = false;
			while (tryNumber < TriviaClient.MAX_RETRIES && success == false) {
				tryNumber++;
				try {
					server.markCorrect(queueIndex, caller, operatorTextField.getText());
					success = true;
				} catch (final RemoteException e) {
					client.log("Couldn't change answer status on server (try #" + tryNumber + ").");
				}
			}

			if (!success) {
				client.disconnected();
				return;
			}

			client.log("Marked answer # " + ( queueIndex + 1 ) + "correct");

		} else {
			// If the OK button wasn't pressed, reset the status box to the previous status
			statusComboBox.setSelectedItem(client.getTrivia().getAnswerQueueStatus(queueIndex));
		}
	}
}
