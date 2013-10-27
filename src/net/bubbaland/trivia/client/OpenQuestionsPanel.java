package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import net.bubbaland.trivia.Trivia;

/**
 * A panel which displays a list of the current open questions.
 * 
 * @author Walter Kolczynski
 * 
 */
public class OpenQuestionsPanel extends TriviaPanel {

	/** The Constant serialVersionUID. */
	private static final long			serialVersionUID	= 6049067322505905668L;

	private final JLabel				qNumberLabel, valueLabel, questionLabel, answerColumn, closeColumn;
	private final JScrollPane			scrollPane;

	/** Sub-panel that will hold the open questions */
	private final OpenQuestionsSubPanel	openQuestionsSubPanel;

	/**
	 * Instantiates a new workflow question list panel.
	 * 
	 * @param server
	 *            The remote trivia server
	 * @param client
	 *            The local trivia client
	 */
	public OpenQuestionsPanel(TriviaClient client) {

		super();

		// Set up layout constraints
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;

		/**
		 * Create the header row
		 */
		constraints.gridx = 0;
		constraints.gridy = 0;
		this.qNumberLabel = this.enclosedLabel("Q#", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

		constraints.gridx = 1;
		constraints.gridy = 0;
		this.valueLabel = this.enclosedLabel("Value", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

		constraints.gridx = 2;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		this.questionLabel = this.enclosedLabel("Question", constraints, SwingConstants.LEFT, SwingConstants.CENTER);
		constraints.weightx = 0.0;

		constraints.gridx = 3;
		constraints.gridy = 0;
		this.answerColumn = this.enclosedLabel("", constraints, SwingConstants.LEFT, SwingConstants.CENTER);

		constraints.gridx = 4;
		constraints.gridy = 0;
		;
		this.closeColumn = this.enclosedLabel("", constraints, SwingConstants.LEFT, SwingConstants.CENTER);

		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 5;
		constraints.weighty = 1.0;

		/**
		 * Create the subpanel that will hold the actual questions and put it in a scroll pane
		 */
		this.openQuestionsSubPanel = new OpenQuestionsSubPanel(client);
		this.scrollPane = new JScrollPane(this.openQuestionsSubPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		this.scrollPane.setBorder(BorderFactory.createEmptyBorder());
		this.add(scrollPane, constraints);

		loadProperties();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void update(boolean force) {
		this.openQuestionsSubPanel.update(force);
	}

	/**
	 * Panel which displays a list of the current open questions.
	 */
	private class OpenQuestionsSubPanel extends TriviaPanel implements ActionListener {

		/** The Constant serialVersionUID. */
		private static final long	serialVersionUID	= 6049067322505905668L;

		// Maximum number of questions in a round
		private final int			nQuestionsMax;

		/**
		 * GUI Elements that will need to be updated
		 */
		private final JLabel[]		qNumberLabels, qValueLabels;
		private final JTextArea[]	qTextAreas;
		private final JButton[]		answerButtons, closeButtons;
		private final JPopupMenu	contextMenu;
		private final JPanel		spacer;

		/**
		 * Data sources
		 */
		private final TriviaClient	client;

		/**
		 * Instantiates a new workflow q list sub panel.
		 * 
		 * @param server
		 *            the server
		 * @param client
		 *            the client
		 */
		public OpenQuestionsSubPanel(TriviaClient client) {

			super();

			this.client = client;
			this.nQuestionsMax = client.getTrivia().getMaxQuestions();

			/**
			 * Build context menu
			 */
			this.contextMenu = new JPopupMenu();

			JMenuItem viewItem = new JMenuItem("View");
			viewItem.setActionCommand("View");
			viewItem.addActionListener(this);
			this.contextMenu.add(viewItem);

			JMenuItem editItem = new JMenuItem("Edit");
			editItem.setActionCommand("Edit");
			editItem.addActionListener(this);
			this.contextMenu.add(editItem);

			JMenuItem resetItem = new JMenuItem("Delete");
			resetItem.setActionCommand("Delete");
			resetItem.addActionListener(this);
			this.contextMenu.add(resetItem);

			this.add(this.contextMenu);

			// Set up layout constraints
			final GridBagConstraints buttonConstraints = new GridBagConstraints();
			buttonConstraints.fill = GridBagConstraints.BOTH;
			buttonConstraints.anchor = GridBagConstraints.CENTER;
			buttonConstraints.weightx = 1.0;
			buttonConstraints.weighty = 1.0;
			buttonConstraints.gridx = 0;
			buttonConstraints.gridy = 0;
			buttonConstraints.fill = GridBagConstraints.NONE;

			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.NORTH;
			constraints.weightx = 0.0;
			constraints.weighty = 0.0;

			/**
			 * Create the GUI elements
			 */
			this.qNumberLabels = new JLabel[this.nQuestionsMax];
			this.qValueLabels = new JLabel[this.nQuestionsMax];
			this.qTextAreas = new JTextArea[this.nQuestionsMax];
			this.answerButtons = new JButton[this.nQuestionsMax];
			this.closeButtons = new JButton[this.nQuestionsMax];

			for (int q = 0; q < this.nQuestionsMax; q++) {
				constraints.gridx = 0;
				constraints.gridy = q;
				this.qNumberLabels[q] = this.enclosedLabel("", constraints, SwingConstants.CENTER,
						SwingConstants.CENTER);
				this.qNumberLabels[q].addMouseListener(new PopupListener(this.contextMenu));

				constraints.gridx = 1;
				constraints.gridy = q;
				this.qValueLabels[q] = this
						.enclosedLabel("", constraints, SwingConstants.CENTER, SwingConstants.CENTER);
				this.qValueLabels[q].addMouseListener(new PopupListener(this.contextMenu));

				constraints.gridx = 2;
				constraints.gridy = q;
				constraints.weightx = 1.0;
				this.qTextAreas[q] = this.scrollableTextArea("", constraints,
						ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
						ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				this.qTextAreas[q].setEditable(false);
				this.qTextAreas[q].addMouseListener(new PopupListener(this.contextMenu));
				constraints.weightx = 0.0;

				constraints.gridx = 3;
				constraints.gridy = q;
				JPanel panel = new JPanel(new GridBagLayout());
				this.add(panel, constraints);
				this.answerButtons[q] = new JButton("");
				this.answerButtons[q].setMargin(new Insets(0, 0, 0, 0));
				panel.add(this.answerButtons[q], buttonConstraints);
				this.answerButtons[q].setActionCommand("Answer");
				this.answerButtons[q].addActionListener(this);

				constraints.gridx = 4;
				constraints.gridy = q;
				panel = new JPanel(new GridBagLayout());
				this.add(panel, constraints);
				this.closeButtons[q] = new JButton("Open");
				this.closeButtons[q].setMargin(new Insets(0, 0, 0, 0));
				panel.add(this.closeButtons[q], buttonConstraints);
				this.closeButtons[q].setActionCommand("Open");
				this.closeButtons[q].addActionListener(this);

			}

			/**
			 * Create a blank spacer row at the bottom
			 */
			constraints.gridx = 0;
			constraints.gridy = this.nQuestionsMax;
			constraints.gridwidth = 5;
			constraints.weightx = 1.0;
			constraints.weighty = 1.0;
			this.spacer = new JPanel();
			// blank.setBackground(HEADER_BACKGROUND_COLOR);
			this.spacer.setPreferredSize(new Dimension(0, 0));
			this.add(this.spacer, constraints);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public synchronized void actionPerformed(ActionEvent event) {
			Trivia trivia = this.client.getTrivia();
			final int rNumber = trivia.getCurrentRoundNumber();
			String command = event.getActionCommand();
			final int qNumber;
			switch (command) {
				case "Answer":
					qNumber = Integer.parseInt(( (Component) event.getSource() ).getName());
					this.answerQuestion(qNumber);
					break;
				case "Close":
					qNumber = Integer.parseInt(( (Component) event.getSource() ).getName());
					new CloseQuestionDialog(this.client, qNumber);
					break;
				case "Open":
					this.open();
					break;
				case "Edit":
					qNumber = Integer.parseInt(this.contextMenu.getName());
					int qValue = trivia.getValue(rNumber, qNumber);
					String qText = trivia.getQuestionText(rNumber, qNumber);
					int nQuestions = trivia.getNQuestions();
					new NewQuestionDialog(this.client, nQuestions, qNumber, qValue, qText);
					break;
				case "Delete":
					qNumber = Integer.parseInt(this.contextMenu.getName());
					qValue = trivia.getValue(rNumber, qNumber);
					qText = trivia.getQuestionText(rNumber, qNumber);
					new ResetQuestionDialog(this.client, qNumber, qValue, qText);
					break;
				case "View":
					qNumber = Integer.parseInt(this.contextMenu.getName());
					qValue = trivia.getValue(rNumber, qNumber);
					qText = trivia.getQuestionText(rNumber, qNumber);
					new ViewQuestionDialog(this.client, qNumber, qValue, qText);
					break;
				default:
					break;
			}
		}

		/**
		 * Propose an answer for the designated question. Creates a prompt to submit the answer.
		 * 
		 * @param qNumber
		 *            the question number
		 */
		private void answerQuestion(int qNumber) {
			new AnswerEntryPanel(this.client, qNumber, this.client.getUser());
		}

		/**
		 * Open a new question. Creates a prompt to enter the question.
		 */
		private void open() {

			final Trivia trivia = this.client.getTrivia();

			final int nQuestions = trivia.getNQuestions();
			final int nextToOpen = trivia.nextToOpen();
			final int rNumber = trivia.getCurrentRoundNumber();

			if (trivia.isSpeed(rNumber) && nextToOpen > 1) {
				new NewQuestionDialog(this.client, nQuestions, nextToOpen, trivia.getValue(rNumber, nextToOpen - 1));
			} else {
				new NewQuestionDialog(this.client, nQuestions, nextToOpen);
			}

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.bubbaland.trivia.TriviaPanel#update()
		 */
		@Override
		public synchronized void update(boolean force) {

			// Get the local copy of the Trivia data
			final Trivia trivia = this.client.getTrivia();

			// Get the data for any open questions
			final int[] openQuestionNumbers = trivia.getOpenQuestionNumbers();
			final String[] openQuestionText = trivia.getOpenQuestionText();
			final String[] openQuestionValues = trivia.getOpenQuestionValues();

			final int nOpen = openQuestionNumbers.length;

			// Check if there were any changes to the list of open questions
			final boolean[] qUpdated = new boolean[nOpen];
			boolean anyUpdate = false;
			for (int q = 0; q < nOpen; q++) {
				qUpdated[q] = !( this.qNumberLabels[q].getText().equals(openQuestionNumbers[q] + "")
						&& this.qValueLabels[q].getText().equals(openQuestionValues[q] + "") && this.qTextAreas[q]
						.getText().equals(openQuestionText[q]) );
				anyUpdate = anyUpdate || qUpdated[q];
			}

			// Show data for open questions
			for (int q = 0; q < nOpen; q++) {
				if (qUpdated[q] || force) {
					this.qNumberLabels[q].setText(openQuestionNumbers[q] + "");
					this.qValueLabels[q].setText(openQuestionValues[q]);
					this.qTextAreas[q].setText(openQuestionText[q]);
					// this.qTextAreas[q].setToolTipText(openQuestionText[q]);
					this.answerButtons[q].setText("Answer");
					this.answerButtons[q].setName(openQuestionNumbers[q] + "");
					this.answerButtons[q].setVisible(true);
					this.closeButtons[q].setText("Close");
					this.closeButtons[q].setActionCommand("Close");
					this.closeButtons[q].setName(openQuestionNumbers[q] + "");
					this.closeButtons[q].setVisible(true);

					this.qNumberLabels[q].setName(openQuestionNumbers[q] + "");
					this.qValueLabels[q].setName(openQuestionNumbers[q] + "");
					this.qTextAreas[q].setName(openQuestionNumbers[q] + "");

				}
			}

			// Blank unused lines and hide buttons (except one Open button)
			for (int q = nOpen; q < this.nQuestionsMax; q++) {
				this.qNumberLabels[q].setText("");
				this.qValueLabels[q].setText("");
				this.qTextAreas[q].setText("");
				// this.qTextAreas[q].setToolTipText("");
				this.answerButtons[q].setText("");
				this.answerButtons[q].setName("");
				this.answerButtons[q].setVisible(false);
				this.closeButtons[q].setText("Open");
				this.closeButtons[q].setActionCommand("Open");
				this.closeButtons[q].setName("");
				if (q == nOpen && trivia.nUnopened() > 0) {
					this.closeButtons[q].setVisible(true);
				} else {
					this.closeButtons[q].setVisible(false);
				}
				this.qNumberLabels[q].setName("");
				this.qValueLabels[q].setName("");
				this.qTextAreas[q].setName("");
			}

			int nQuestionsShow;
			if (trivia.nUnopened() > 0) {
				nQuestionsShow = nOpen + 1;
			} else {
				nQuestionsShow = nOpen;
			}

			// Show rows equal to the greater of the number of questions to show and the number of open questions
			for (int q = 0; q < nQuestionsShow; q++) {
				this.qNumberLabels[q].setVisible(true);
				this.qValueLabels[q].setVisible(true);
				this.qTextAreas[q].setVisible(true);

				this.qNumberLabels[q].getParent().setVisible(true);
				this.qValueLabels[q].getParent().setVisible(true);
				this.qTextAreas[q].setVisible(true);
				this.qTextAreas[q].getParent().setVisible(true);
				this.qTextAreas[q].getParent().getParent().setVisible(true);
				this.answerButtons[q].getParent().setVisible(true);
				this.closeButtons[q].getParent().setVisible(true);
			}

			// Hide the rest of the rows
			for (int q = nQuestionsShow; q < this.nQuestionsMax; q++) {
				this.qNumberLabels[q].setVisible(false);
				this.qValueLabels[q].setVisible(false);
				this.qTextAreas[q].setVisible(false);

				this.qNumberLabels[q].getParent().setVisible(false);
				this.qValueLabels[q].getParent().setVisible(false);
				this.qTextAreas[q].setVisible(false);
				this.qTextAreas[q].getParent().setVisible(false);
				this.qTextAreas[q].getParent().getParent().setVisible(false);
				this.answerButtons[q].getParent().setVisible(false);
				this.closeButtons[q].getParent().setVisible(false);
			}

		}

		private class PopupListener extends MouseAdapter {

			private final JPopupMenu	menu;

			public PopupListener(JPopupMenu menu) {
				this.menu = menu;
			}

			private void checkForPopup(MouseEvent event) {
				final JComponent source = (JComponent) event.getSource();
				if (event.isPopupTrigger() && !source.getName().equals("")) {
					menu.setName(source.getName());
					menu.show(source, event.getX(), event.getY());
				}
			}

			public void mousePressed(MouseEvent e) {
				checkForPopup(e);
			}

			public void mouseReleased(MouseEvent e) {
				checkForPopup(e);
			}

			public void mouseClicked(MouseEvent e) {
				checkForPopup(e);
			}

		}

		private void loadProperties() {
			/**
			 * Colors
			 */
			final Color headerBackgroundColor = new Color(Integer.parseInt(
					TriviaClient.PROPERTIES.getProperty("OpenQuestions.Header.BackgroundColor"), 16));
			final Color oddRowColor = new Color(Integer.parseInt(
					TriviaClient.PROPERTIES.getProperty("OpenQuestions.OddRow.Color"), 16));
			final Color evenRowColor = new Color(Integer.parseInt(
					TriviaClient.PROPERTIES.getProperty("OpenQuestions.EvenRow.Color"), 16));
			final Color oddRowBackgroundColor = new Color(Integer.parseInt(
					TriviaClient.PROPERTIES.getProperty("OpenQuestions.OddRow.BackgroundColor"), 16));
			final Color evenRowBackgroundColor = new Color(Integer.parseInt(
					TriviaClient.PROPERTIES.getProperty("OpenQuestions.EvenRow.BackgroundColor"), 16));

			/**
			 * Sizes
			 */
			final int rowHeight = Integer.parseInt(TriviaClient.PROPERTIES.getProperty("OpenQuestions.Row.Height"));

			final int qNumWidth = Integer.parseInt(TriviaClient.PROPERTIES.getProperty("OpenQuestions.QNumber.Width"));
			final int questionWidth = Integer.parseInt(TriviaClient.PROPERTIES
					.getProperty("OpenQuestions.Question.Width"));
			final int valueWidth = Integer.parseInt(TriviaClient.PROPERTIES.getProperty("OpenQuestions.Value.Width"));
			final int answerWidth = Integer.parseInt(TriviaClient.PROPERTIES
					.getProperty("OpenQuestions.AnswerCol.Width"));
			final int closeWidth = Integer
					.parseInt(TriviaClient.PROPERTIES.getProperty("OpenQuestions.CloseCol.Width"));

			/**
			 * Button sizes
			 */
			final int answerButtonHeight = Integer.parseInt(TriviaClient.PROPERTIES
					.getProperty("OpenQuestions.AnswerButton.Height"));
			final int answerButtonWidth = Integer.parseInt(TriviaClient.PROPERTIES
					.getProperty("OpenQuestions.AnswerButton.Width"));
			final int closeButtonHeight = Integer.parseInt(TriviaClient.PROPERTIES
					.getProperty("OpenQuestions.CloseButton.Height"));
			final int closeButtonWidth = Integer.parseInt(TriviaClient.PROPERTIES
					.getProperty("OpenQuestions.CloseButton.Width"));

			/**
			 * Font sizes
			 */
			final float qNumFontSize = Float.parseFloat(TriviaClient.PROPERTIES
					.getProperty("OpenQuestions.QNumber.FontSize"));
			final float valueFontSize = Float.parseFloat(TriviaClient.PROPERTIES
					.getProperty("OpenQuestions.Value.FontSize"));
			final float questionFontSize = Float.parseFloat(TriviaClient.PROPERTIES
					.getProperty("OpenQuestions.Question.FontSize"));

			for (int q = 0; q < this.nQuestionsMax; q++) {
				Color color, bColor;
				if (q % 2 == 1) {
					color = oddRowColor;
					bColor = oddRowBackgroundColor;
				} else {
					color = evenRowColor;
					bColor = evenRowBackgroundColor;
				}

				setLabelProperties(this.qNumberLabels[q], qNumWidth, rowHeight, color, bColor, qNumFontSize);
				setLabelProperties(this.qValueLabels[q], valueWidth, rowHeight, color, bColor, valueFontSize);
				setTextAreaProperties(this.qTextAreas[q], questionWidth, rowHeight, color, bColor, questionFontSize);
				setPanelProperties((JPanel) this.answerButtons[q].getParent(), answerWidth, rowHeight, bColor);
				setPanelProperties((JPanel) this.closeButtons[q].getParent(), closeWidth, rowHeight, bColor);
				this.answerButtons[q].setPreferredSize(new Dimension(answerButtonWidth, answerButtonHeight));
				this.closeButtons[q].setPreferredSize(new Dimension(closeButtonWidth, closeButtonHeight));
			}

			this.spacer.setBackground(headerBackgroundColor);
		}

	}

	protected void loadProperties() {
		/**
		 * Colors
		 */
		final Color headerColor = new Color(Integer.parseInt(
				TriviaClient.PROPERTIES.getProperty("OpenQuestions.Header.Color"), 16));
		final Color headerBackgroundColor = new Color(Integer.parseInt(
				TriviaClient.PROPERTIES.getProperty("OpenQuestions.Header.BackgroundColor"), 16));

		/**
		 * Sizes
		 */
		final int headerHeight = Integer.parseInt(TriviaClient.PROPERTIES.getProperty("OpenQuestions.Header.Height"));
		final int rowHeight = Integer.parseInt(TriviaClient.PROPERTIES.getProperty("OpenQuestions.Row.Height"));

		final int qNumWidth = Integer.parseInt(TriviaClient.PROPERTIES.getProperty("OpenQuestions.QNumber.Width"));
		final int valueWidth = Integer.parseInt(TriviaClient.PROPERTIES.getProperty("OpenQuestions.Value.Width"));
		final int questionWidth = Integer.parseInt(TriviaClient.PROPERTIES.getProperty("OpenQuestions.Question.Width"));
		final int answerWidth = Integer.parseInt(TriviaClient.PROPERTIES.getProperty("OpenQuestions.AnswerCol.Width"));
		final int closeWidth = Integer.parseInt(TriviaClient.PROPERTIES.getProperty("OpenQuestions.CloseCol.Width"));

		/**
		 * Font sizes
		 */
		final float headerFontSize = Float.parseFloat(TriviaClient.PROPERTIES
				.getProperty("OpenQuestions.Header.FontSize"));

		/** The number of open questions to show at one time */
		final int questionsShow = Integer.parseInt(TriviaClient.PROPERTIES.getProperty("OpenQuestions.QuestionsShow"));

		setLabelProperties(this.qNumberLabel, qNumWidth, headerHeight, headerColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.valueLabel, valueWidth, headerHeight, headerColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.questionLabel, questionWidth, headerHeight, headerColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.answerColumn, answerWidth, headerHeight, headerColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.closeColumn, closeWidth, headerHeight, headerColor, headerBackgroundColor,
				headerFontSize);

		this.scrollPane.setPreferredSize(new Dimension(0, questionsShow * rowHeight + 3));
		this.scrollPane.setMinimumSize(new Dimension(0, rowHeight + 3));

		this.openQuestionsSubPanel.loadProperties();
	}


}
