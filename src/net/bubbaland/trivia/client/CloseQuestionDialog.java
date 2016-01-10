package net.bubbaland.trivia.client;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.WindowEvent;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;

import net.bubbaland.trivia.ClientMessage.ClientMessageFactory;
import net.bubbaland.trivia.Trivia;

/**
 * Creates a dialog box that prompts user for the correct answer when closing a question.
 *
 * @author Walter Kolczynski
 */
public class CloseQuestionDialog extends TriviaDialogPanel {
	private static final long	serialVersionUID	= 8533094210282632603L;

	private final TriviaClient	client;
	private final int			qNumber;
	private final JTextArea		answerTextArea;

	public CloseQuestionDialog(TriviaClient client, int qNumber) {

		super();

		this.client = client;
		this.qNumber = qNumber;

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
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 0.5;
		constraints.weighty = 0.5;
		final JTextArea textArea = new JTextArea(qText);
		textArea.setFont(textArea.getFont().deriveFont(textAreaFontSize));
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
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.weightx = 0.5;
		constraints.weighty = 0.5;
		this.answerTextArea = new JTextArea("", 4, 50);
		this.answerTextArea.setLineWrap(true);
		this.answerTextArea.setWrapStyleWord(true);
		this.answerTextArea.setFont(this.answerTextArea.getFont().deriveFont(textAreaFontSize));
		this.answerTextArea.addAncestorListener(this);
		this.addEnterOverride(this.answerTextArea);
		// answerTextArea.addKeyListener(this);
		scrollPane = new JScrollPane(this.answerTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(0, 200));
		this.add(scrollPane, constraints);

		// Display the dialog box
		this.dialog = new TriviaDialog(null, "Close question " + qNumber, this, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		this.dialog.setName("Close Question");
		this.dialog.setVisible(true);
	}

	@Override
	public void windowClosed(WindowEvent event) {
		super.windowClosed(event);
		// If the OK button was pressed, add the proposed answer to the queue
		final int option = ( (Integer) this.dialog.getValue() ).intValue();
		if (option == JOptionPane.OK_OPTION) {
			final String answer = this.answerTextArea.getText();
			( new SwingWorker<Void, Void>() {
				@Override
				public Void doInBackground() {
					CloseQuestionDialog.this.client
							.sendMessage(ClientMessageFactory.close(CloseQuestionDialog.this.qNumber, answer));
					return null;
				}

				@Override
				public void done() {
					CloseQuestionDialog.this.client.log("Closed Question #" + CloseQuestionDialog.this.qNumber);
				}
			} ).execute();
		}
	}


}
