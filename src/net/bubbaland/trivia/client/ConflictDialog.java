package net.bubbaland.trivia.client;

import java.awt.GridBagConstraints;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import net.bubbaland.trivia.Trivia;

public class ConflictDialog extends TriviaDialogPanel {

	private static final long	serialVersionUID	= 542530522891342681L;

	public ConflictDialog(final TriviaClient client) {

		super();

		final Trivia trivia = client.getTrivia();
		final int currentRound = trivia.getCurrentRoundNumber();
		final int calculated = trivia.getCumulativeEarned(currentRound - 1);
		final int announced = trivia.getAnnouncedPoints(currentRound - 1);

		// Set up layout constraints
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;

		constraints.gridwidth = 4;
		constraints.gridx = 0;
		constraints.gridy = 0;
		JLabel label = new JLabel("Announced score does not match internal calculations.", JLabel.CENTER);
		label.setFont(label.getFont().deriveFont(fontSize));
		label.setForeground(warningColor);
		this.add(label, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		label = new JLabel("Call the point dispute line or correct the question data.", JLabel.CENTER);
		label.setFont(label.getFont().deriveFont(fontSize));
		label.setForeground(warningColor);
		this.add(label, constraints);

		constraints.gridx = 0;
		constraints.gridy = 2;
		label = new JLabel("Point dispute line: (320) 308-4748", JLabel.CENTER);
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);
		constraints.gridwidth = 1;

		constraints.gridx = 0;
		constraints.gridy = 3;
		label = new JLabel("    ", JLabel.CENTER);
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);
		constraints.gridwidth = 1;

		constraints.weightx = 0.5;
		constraints.gridx = 0;
		constraints.gridy = 4;
		label = new JLabel("", JLabel.RIGHT);
		this.add(label, constraints);
		constraints.weightx = 0.0;

		constraints.gridx = 1;
		constraints.gridy = 4;
		label = new JLabel("Calculated score:    ", JLabel.RIGHT);
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		constraints.gridx = 2;
		constraints.gridy = 4;
		label = new JLabel(calculated + "", JLabel.RIGHT);
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		constraints.weightx = 0.5;
		constraints.gridx = 3;
		constraints.gridy = 4;
		label = new JLabel("", JLabel.RIGHT);
		this.add(label, constraints);
		constraints.weightx = 0.0;

		constraints.gridx = 1;
		constraints.gridy = 5;
		label = new JLabel("Announced score:    ", JLabel.RIGHT);
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		constraints.gridx = 2;
		constraints.gridy = 5;
		label = new JLabel(announced + "", JLabel.RIGHT);
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		new TriviaDialog(null, "Score Conflict", this, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION)
				.setVisible(true);

	}
}
