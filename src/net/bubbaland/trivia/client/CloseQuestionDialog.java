package net.bubbaland.trivia.client;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.rmi.RemoteException;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.TriviaInterface;

/**
 * Creates a dialog box that prompts user for the correct answer when closing a question.
 * 
 * @author Walter Kolczynski
 */
public class CloseQuestionDialog extends TriviaDialog {
	private static final long	serialVersionUID	= 8533094210282632603L;
	
	/**
	 * Font sizes
	 */
	private static final float	LABEL_FONT_SIZE			= 20.0f;
	private static final float	TEXTBOX_FONT_SIZE		= 16.0f;

	public CloseQuestionDialog(TriviaInterface server, TriviaClient client, int qNumber) {
		
		super(new GridBagLayout());		

		// Retrieve current trivia data object
		final Trivia trivia = client.getTrivia();

		// Get the question text
		final String qText = trivia.getQuestionText(qNumber);

		// Set up layout constraints
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.0;
		c.weighty = 0.0;

		// Display question text
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 0;
		JLabel label = new JLabel("Question:");
		label.setFont(label.getFont().deriveFont(LABEL_FONT_SIZE));
		this.add(label, c);

		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0.5;
		c.weighty = 0.5;
		final JTextArea textArea = new JTextArea(qText);
		textArea.setFont(textArea.getFont().deriveFont(TEXTBOX_FONT_SIZE));
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(0, 200));
		this.add(scrollPane, c);
		c.weightx = 0.0;
		c.weighty = 0.0;

		// Create answer text box for input
		c.gridx = 0;
		c.gridy = 2;
		label = new JLabel("Answer: ");
		label.setFont(label.getFont().deriveFont(LABEL_FONT_SIZE));
		this.add(label, c);

		c.gridx = 0;
		c.gridy = 3;
		c.weightx = 0.5;
		c.weighty = 0.5;
		final JTextArea answerTextArea = new JTextArea("", 4, 50);
		answerTextArea.setLineWrap(true);
		answerTextArea.setWrapStyleWord(true);
		answerTextArea.setFont(answerTextArea.getFont().deriveFont(TEXTBOX_FONT_SIZE));
		answerTextArea.addAncestorListener(this);
		scrollPane = new JScrollPane(answerTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(0, 200));
		this.add(scrollPane, c);
		
		// Display the dialog box
		final JOptionPane pane = new JOptionPane(this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		final JDialog dialog = pane.createDialog(this.getParent(), "Close question " + qNumber);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setResizable(true);
		dialog.setVisible(true);

		// If the OK button was pressed, add the proposed answer to the queue
		final int option = ( (Integer) pane.getValue() ).intValue();
		if (option == JOptionPane.OK_OPTION) {
			final String answer = answerTextArea.getText();

			int tryNumber = 0;
			boolean success = false;
			while (tryNumber < TriviaClient.MAX_RETRIES && success == false) {
				tryNumber++;
				try {
					server.close(client.getUser(), qNumber, answer);
					success = true;
				} catch (final RemoteException e) {
					client.log("Couldn't close question on server (try #" + tryNumber + ").");
				}
			}

			if (!success) {
				client.disconnected();
				return;
			}

			client.log("Closed Question #" + qNumber);

		}

	}
			

}
