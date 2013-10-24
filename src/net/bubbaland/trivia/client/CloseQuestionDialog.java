package net.bubbaland.trivia.client;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.rmi.RemoteException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.TriviaInterface;

/**
 * Creates a dialog box that prompts user for the correct answer when closing a question.
 *
 * @author Walter Kolczynski
 */
public class CloseQuestionDialog extends TriviaDialogPanel {
	private static final long	serialVersionUID	= 8533094210282632603L;

	/**
	 * Font sizes
	 */
	private static final float	LABEL_FONT_SIZE		= 20.0f;
	private static final float	TEXTBOX_FONT_SIZE	= 16.0f;


	public CloseQuestionDialog(TriviaInterface server, TriviaClient client, int qNumber) {

		super();

		// Retrieve current trivia data object
		final Trivia trivia = client.getTrivia();

		// Get the question text
		final String qText = trivia.getQuestionText(qNumber);

		// Set up layout constraints
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;

		// Display question text
		constraints.gridwidth = 2;
		constraints.gridx = 0;
		constraints.gridy = 0;
		JLabel label = new JLabel("Question:");
		label.setFont(label.getFont().deriveFont(LABEL_FONT_SIZE));
		this.add(label, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 0.5;
		constraints.weighty = 0.5;
		final JTextArea textArea = new JTextArea(qText);
		textArea.setFont(textArea.getFont().deriveFont(TEXTBOX_FONT_SIZE));
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(0, 200));
		this.add(scrollPane, constraints);
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;

		// Create answer text box for input
		constraints.gridx = 0;
		constraints.gridy = 2;
		label = new JLabel("Answer: ");
		label.setFont(label.getFont().deriveFont(LABEL_FONT_SIZE));
		this.add(label, constraints);

		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.weightx = 0.5;
		constraints.weighty = 0.5;
		final JTextArea answerTextArea = new JTextArea("", 4, 50);
		answerTextArea.setLineWrap(true);
		answerTextArea.setWrapStyleWord(true);
		answerTextArea.setFont(answerTextArea.getFont().deriveFont(TEXTBOX_FONT_SIZE));
		answerTextArea.addAncestorListener(this);
		this.addEnterOverride(answerTextArea);
		// answerTextArea.addKeyListener(this);
		scrollPane = new JScrollPane(answerTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(0, 200));
		this.add(scrollPane, constraints);

		// Display the dialog box
		this.dialog = new TriviaDialog(client.getFrame(), "Close question " + qNumber, this, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		this.dialog.setName("Close Question");
		this.dialog.setVisible(true);

		// If the OK button was pressed, add the proposed answer to the queue
		final int option = ( (Integer) dialog.getValue() ).intValue();
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
