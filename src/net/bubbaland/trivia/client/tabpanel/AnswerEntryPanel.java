package net.bubbaland.trivia.client.tabpanel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.WindowEvent;
import java.util.Hashtable;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.client.TriviaClient;
import net.bubbaland.trivia.client.dialog.TriviaDialog;
import net.bubbaland.trivia.client.dialog.TriviaDialogPanel;
import net.bubbaland.trivia.messages.ProposeAnswerMessage;

/**
 * Creates a dialog box that prompts user to propose an answer.
 *
 * @author Walter Kolczynski
 */
public class AnswerEntryPanel extends TriviaDialogPanel {

	private static final long	serialVersionUID	= -5797789908178154492L;

	private final String		user;
	private final int			rNumber, qNumber;
	private final JTextArea		answerTextArea;
	private final JCheckBox		bruteForceCheckbox;
	private final JSlider		confidenceSlider;
	private final TriviaClient	client;

	/**
	 * Creates a dialog box and prompt for response
	 *
	 * @param client
	 *            The local trivia client
	 * @param qNumber
	 *            The question number
	 * @param user
	 *            The user's name
	 */
	public AnswerEntryPanel(TriviaClient client, int rNumber, int qNumber, String user) {

		super();

		this.client = client;
		this.rNumber = rNumber;
		this.qNumber = qNumber;
		this.user = user;

		// Retrieve current trivia data object
		final Trivia trivia = client.getTrivia();

		// Get the question text
		final String qText = trivia.getCurrentRound().getQuestion(qNumber).getQuestionText();

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
		scrollPane = new JScrollPane(this.answerTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(0, 200));
		this.add(scrollPane, constraints);
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;

		// Create brute force check box
		constraints.gridwidth = 2;
		constraints.gridx = 0;
		constraints.gridy = 4;
		this.bruteForceCheckbox = new JCheckBox("Treat separate lines as multiple guesses");
		this.add(this.bruteForceCheckbox, constraints);

		// Create confidence slider
		constraints.gridwidth = 1;
		constraints.gridx = 0;
		constraints.gridy = 5;
		label = new JLabel("Confidence", SwingConstants.RIGHT);
		label.setVerticalAlignment(SwingConstants.CENTER);
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		constraints.gridx = 1;
		constraints.gridy = 5;
		constraints.insets = new Insets(sliderPaddingBottom, sliderPaddingLeft, sliderPaddingRight, sliderPaddingTop);
		this.confidenceSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 5, 0);
		this.confidenceSlider.setMajorTickSpacing(1);
		final Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(new Integer(0), new JLabel("�"));
		labelTable.put(new Integer(1), new JLabel("Guess"));
		labelTable.put(new Integer(5), new JLabel("Sure"));
		this.confidenceSlider.setLabelTable(labelTable);
		this.confidenceSlider.setPaintLabels(true);
		this.confidenceSlider.setPaintTicks(true);
		this.add(this.confidenceSlider, constraints);

		// Display the dialog box
		this.dialog = new TriviaDialog(null, "Submit Answer for Question " + qNumber, this, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		this.dialog.setName("Answer Question");
		this.dialog.setVisible(true);
	}

	private void sendAnswer(final String answer, final int confidence) {
		( new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() {
				AnswerEntryPanel.this.client.sendMessage(new ProposeAnswerMessage(AnswerEntryPanel.this.rNumber,
						AnswerEntryPanel.this.qNumber, answer, confidence));
				return null;
			}

			@Override
			public void done() {
				AnswerEntryPanel.this.client.log("Submitted an answer for Question #" + AnswerEntryPanel.this.qNumber);
			}
		} ).execute();
	}

	@Override
	public void windowClosed(WindowEvent event) {
		super.windowClosed(event);
		// If the OK button was pressed, add the proposed answer to the queue
		final int option = ( (Integer) this.dialog.getValue() ).intValue();
		if (option == JOptionPane.OK_OPTION) {
			final String answer = this.answerTextArea.getText();
			final int confidence = this.confidenceSlider.getValue();

			if (answer.equals("")) {
				new AnswerEntryPanel(this.client, this.rNumber, this.qNumber, this.user);
				return;
			}

			if (this.bruteForceCheckbox.isSelected()) {
				final String[] answers = answer.split("\\r?\\n");
				for (final String a : answers) {
					if (!a.equals("")) {
						this.sendAnswer(a, confidence);
					}
				}
			} else {
				this.sendAnswer(answer, confidence);
			}


		}
	}
}