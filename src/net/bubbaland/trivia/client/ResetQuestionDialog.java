package net.bubbaland.trivia.client;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.rmi.RemoteException;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

public class ResetQuestionDialog extends TriviaDialogPanel {

	private static final long	serialVersionUID	= 6166214835080640219L;

	/**
	 * Font sizes
	 */
	private static final float	LABEL_FONT_SIZE		= 20.0f;

	public ResetQuestionDialog(TriviaClient client, int qNumber, int qValue, String qText) {

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 2;
		JLabel label = new JLabel("Are you sure you want to delete this question?");
		label.setFont(label.getFont().deriveFont(LABEL_FONT_SIZE));
		this.add(label, constraints);
		constraints.gridwidth = 1;

		// Show existing question data
		constraints.gridx = 0;
		constraints.gridy = 1;
		label = new JLabel("Existing question:");
		label.setFont(label.getFont().deriveFont(LABEL_FONT_SIZE));
		this.add(label, constraints);

		constraints.gridx = 1;
		constraints.gridy = 1;
		label = new JLabel("Value: " + qValue, JLabel.RIGHT);
		label.setFont(label.getFont().deriveFont(LABEL_FONT_SIZE));
		this.add(label, constraints);

		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.gridwidth = 2;
		JTextArea textArea = new JTextArea(qText);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(100, 100));
		scrollPane.setViewportView(textArea);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		this.add(scrollPane, constraints);

		TriviaDialog dialog = new TriviaDialog(null, "Confirm Question Reset " + qNumber, this,
				JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		dialog.setVisible(true);

		final int confirm = ( (Integer) dialog.getValue() ).intValue();
		if (confirm == JOptionPane.OK_OPTION) {
			// Reset the question on the server
			int tryNumber = 0;
			boolean success = false;
			while (tryNumber < Integer.parseInt(TriviaClient.PROPERTIES.getProperty("MaxRetries")) && success == false) {
				tryNumber++;
				try {
					client.getServer().resetQuestion(client.getUser(), qNumber);
					success = true;
				} catch (final RemoteException e) {
					client.log("Couldn't open question on server (try #" + tryNumber + ").");
				}
			}

			if (!success) {
				client.disconnected();
				return;
			}

			client.log("Question #" + qNumber + " reset.");


			return;
		}


	}

}
