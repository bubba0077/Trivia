package net.bubbaland.trivia.client.dialog;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import net.bubbaland.trivia.client.TriviaClient;
import net.bubbaland.trivia.messages.ResetQuestionMessage;

public class ResetQuestionDialog extends TriviaDialogPanel {

	private static final long	serialVersionUID	= 6166214835080640219L;

	private final TriviaClient	client;
	private final int			rNumber, qNumber;

	public ResetQuestionDialog(TriviaClient client, int rNumber, int qNumber, int qValue, String qText) {

		this.client = client;
		this.rNumber = rNumber;
		this.qNumber = qNumber;

		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 2;
		JLabel label = new JLabel("Are you sure you want to delete this question?");
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);
		constraints.gridwidth = 1;

		// Show existing question data
		constraints.gridx = 0;
		constraints.gridy = 1;
		label = new JLabel("Existing question:");
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		constraints.gridx = 1;
		constraints.gridy = 1;
		label = new JLabel("Value: " + qValue, SwingConstants.RIGHT);
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.gridwidth = 2;
		final JTextArea textArea = new JTextArea(qText);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		final JScrollPane scrollPane = new JScrollPane(textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(100, 100));
		scrollPane.setViewportView(textArea);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		this.add(scrollPane, constraints);

		this.dialog = new TriviaDialog(null, "Confirm Question Reset " + qNumber, this, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		this.dialog.setVisible(true);
	}

	@Override
	public void windowClosed(WindowEvent event) {
		super.windowClosed(event);
		final int confirm = ( (Integer) this.dialog.getValue() ).intValue();
		if (this.client.getTrivia().getCurrentRoundNumber() != this.rNumber) {
			this.client.log("Refusing to change open status of question from previous round");
			JOptionPane.showMessageDialog(this, "Cannot reset question from a previous round");
			return;
		}
		if (confirm == JOptionPane.OK_OPTION) {
			( new SwingWorker<Void, Void>() {
				@Override
				public Void doInBackground() {
					ResetQuestionDialog.this.client.sendMessage(new ResetQuestionMessage(
							ResetQuestionDialog.this.rNumber, ResetQuestionDialog.this.qNumber));
					return null;
				}

				@Override
				public void done() {
					ResetQuestionDialog.this.client.log("Question #" + ResetQuestionDialog.this.qNumber + " reset.");
				}
			} ).execute();
			return;
		}
	}

}
