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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.function.IntPredicate;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.User;
import net.bubbaland.trivia.messages.SetEffortMessage;
import net.bubbaland.trivia.Question;

/**
 * A panel which displays a list of the current open questions.
 *
 * @author Walter Kolczynski
 *
 */
public class OpenQuestionsPanel extends TriviaMainPanel {

	/** The Constant serialVersionUID. */
	private static final long				serialVersionUID	= 6049067322505905668L;

	private final JLabel					qNumberLabel, effortLabel, valueLabel, questionLabel, visualTriviaLabel;
	private final JLabel[]					statusLabels;

	private JLabel							spacerLabel;
	private JLabel[]						visualTriviaLabels;

	private final JScrollPane				scrollPane;
	private final TriviaPanel				visualTriviaPanel;

	// Set up layout constraints
	private static final GridBagConstraints	buttonConstraints	= new GridBagConstraints();

	static {
		buttonConstraints.anchor = GridBagConstraints.CENTER;
		buttonConstraints.weightx = 1.0;
		buttonConstraints.weighty = 1.0;
		buttonConstraints.gridx = 0;
		buttonConstraints.gridy = 0;
		buttonConstraints.fill = GridBagConstraints.NONE;
	}


	/** Sub-panel that will hold the open questions */
	private final OpenQuestionsSubPanel	openQuestionsSubPanel;
	private Color						headerColor, headerBackgroundColor, footerBackgroundColor, correctColor,
			incorrectColor, usedColor, unusedColor, activeColor;

	private int							footerHeight, statusWidth, visualStatusWidth;
	private float						footerFontSize;

	/**
	 * Instantiates a new workflow question list panel.
	 *
	 * @param client
	 *            The local trivia client
	 */
	public OpenQuestionsPanel(TriviaClient client, TriviaFrame parent) {

		super(client, parent);

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
		this.effortLabel = this.enclosedLabel("W", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

		constraints.gridx = 2;
		constraints.gridy = 0;
		this.valueLabel = this.enclosedLabel("Value", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

		constraints.gridx = 3;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		this.questionLabel = this.enclosedLabel("Question", constraints, SwingConstants.LEFT, SwingConstants.CENTER);
		constraints.weightx = 0.0;

		final int nQuestions = this.client.getTrivia().getMaxQuestions();
		this.statusLabels = new JLabel[nQuestions];
		for (int q = 1; q <= nQuestions; q++) {
			constraints.gridx = 3 + q;
			this.statusLabels[q - 1] =
					this.enclosedLabel("", constraints, SwingConstants.CENTER, SwingConstants.CENTER);
		}

		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 4 + nQuestions;
		constraints.weighty = 1.0;

		/**
		 * Create the subpanel that will hold the actual questions and put it in a scroll pane
		 */
		this.openQuestionsSubPanel = new OpenQuestionsSubPanel(client, parent);
		this.scrollPane = new JScrollPane(this.openQuestionsSubPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		this.scrollPane.setBorder(BorderFactory.createEmptyBorder());
		this.add(this.scrollPane, constraints);

		constraints.gridwidth = 1;
		constraints.weighty = 0.0;

		/**
		 * Create the footer row
		 */
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 4 + nQuestions;
		this.visualTriviaPanel = new TriviaPanel(new GridBagLayout());
		this.add(visualTriviaPanel, constraints);

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		this.visualTriviaLabel = visualTriviaPanel.enclosedLabel(" Visual Trivia:", constraints, SwingConstants.LEFT,
				SwingConstants.CENTER);
		this.visualTriviaLabels = null;

		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		this.spacerLabel = visualTriviaPanel.enclosedLabel("", constraints, SwingConstants.LEFT, SwingConstants.CENTER);

		this.loadProperties(TriviaGUI.PROPERTIES);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void updateGUI(boolean force) {
		final Trivia trivia = this.client.getTrivia();
		final int currentRound = trivia.getCurrentRoundNumber();
		final int nMaxQuestions = trivia.getMaxQuestions();
		final int nQuestions = trivia.getCurrentRound().getNQuestions();
		final int diff = nMaxQuestions - nQuestions;
		for (int q = 0; q < diff; q++) {
			this.statusLabels[q].setText("");
		}

		for (int q = diff; q < nMaxQuestions; q++) {
			final int qNumber = q + 1 - diff;
			this.statusLabels[q].setText(qNumber + "");
			if (trivia.getCurrentRound().getQuestion(qNumber).beenOpen()) {
				if (trivia.getCurrentRound().isOpen(qNumber)) {
					this.statusLabels[q].setForeground(this.headerColor);
				} else {
					if (trivia.getCurrentRound().isCorrect(qNumber)) {
						this.statusLabels[q].setForeground(this.correctColor);
					} else {
						this.statusLabels[q].setForeground(this.incorrectColor);
					}
				}
			} else {
				this.statusLabels[q].setForeground(this.headerBackgroundColor);
			}
		}

		final boolean[] visualTriviaUsed = trivia.getVisualTriviaUsed();
		final int nVisualTrivia = trivia.getNVisual();
		if (this.visualTriviaLabels == null || visualTriviaUsed.length != this.visualTriviaLabels.length) {
			if (this.visualTriviaLabels != null) {
				for (JLabel label : this.visualTriviaLabels) {
					this.visualTriviaPanel.remove(label);
					this.visualTriviaPanel.remove(label.getParent());
				}
			}

			this.visualTriviaPanel.remove(this.spacerLabel);
			this.visualTriviaPanel.remove(this.spacerLabel.getParent());

			this.visualTriviaLabels = new JLabel[nVisualTrivia];
			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.NORTH;
			constraints.weightx = 0.0;
			constraints.weighty = 0.0;

			for (int v = 0; v < nVisualTrivia; v++) {
				constraints.gridx = 1 + v;
				constraints.gridy = 0;
				this.visualTriviaLabels[v] = this.visualTriviaPanel.enclosedLabel(( v + 1 ) + "",
						this.visualStatusWidth, this.footerHeight, this.unusedColor, this.footerBackgroundColor,
						constraints, this.footerFontSize, SwingConstants.RIGHT, SwingConstants.CENTER);
			}

			constraints.gridx = nVisualTrivia + 1;
			constraints.gridy = 0;
			constraints.weightx = 1.0;
			this.spacerLabel = visualTriviaPanel.enclosedLabel("", 0, this.footerHeight, this.footerBackgroundColor,
					this.footerBackgroundColor, constraints, this.footerFontSize, SwingConstants.LEFT,
					SwingConstants.CENTER);
			this.visualTriviaPanel.updateUI();
		}

		for (int v = 0; v < nVisualTrivia; v++) {
			Color color = visualTriviaUsed[v] ? this.usedColor : this.unusedColor;
			for (Question q : trivia.getCurrentRound().getOpenQuestions()) {
				if (q.getVisualTrivia() == v + 1) {
					color = this.activeColor;
				}
			}

			this.visualTriviaLabels[v].setForeground(color);

		}

		this.openQuestionsSubPanel.updateGUI(force);
	}

	@Override
	protected void loadProperties(Properties properties) {
		/**
		 * Colors
		 */
		this.headerColor =
				new Color(new BigInteger(properties.getProperty("OpenQuestions.Header.Color"), 16).intValue());
		this.headerBackgroundColor = new Color(
				new BigInteger(properties.getProperty("OpenQuestions.Header.BackgroundColor"), 16).intValue());
		this.usedColor =
				new Color(new BigInteger(properties.getProperty("OpenQuestions.Footer.Used.Color"), 16).intValue());
		this.unusedColor =
				new Color(new BigInteger(properties.getProperty("OpenQuestions.Footer.Unused.Color"), 16).intValue());
		this.activeColor =
				new Color(new BigInteger(properties.getProperty("OpenQuestions.Footer.Active.Color"), 16).intValue());
		this.footerBackgroundColor = new Color(
				new BigInteger(properties.getProperty("OpenQuestions.Footer.BackgroundColor"), 16).intValue());
		this.correctColor =
				new Color(new BigInteger(TriviaGUI.PROPERTIES.getProperty("AnswerQueue.Correct.Color"), 16).intValue());
		this.incorrectColor = new Color(
				new BigInteger(TriviaGUI.PROPERTIES.getProperty("AnswerQueue.Incorrect.Color"), 16).intValue());

		/**
		 * Sizes
		 */

		final int headerHeight = Integer.parseInt(properties.getProperty("OpenQuestions.Header.Height"));
		final int rowHeight = Integer.parseInt(properties.getProperty("OpenQuestions.Row.Height"));

		final int qNumWidth = Integer.parseInt(properties.getProperty("OpenQuestions.QNumber.Width"));
		final int effortWidth = Integer.parseInt(properties.getProperty("OpenQuestions.Effort.Width"));
		final int valueWidth = Integer.parseInt(properties.getProperty("OpenQuestions.Value.Width"));
		final int questionWidth = Integer.parseInt(properties.getProperty("OpenQuestions.Question.Width"));
		this.statusWidth = Integer.parseInt(properties.getProperty("OpenQuestions.Header.Status.Width"));
		final int visualTriviaWidth =
				Integer.parseInt(properties.getProperty("OpenQuestions.Footer.VisualTrivia.Width"));
		this.visualStatusWidth =
				Integer.parseInt(properties.getProperty("OpenQuestions.Footer.VisualTrivia.Status.Width"));

		/**
		 * Font sizes
		 */
		final float headerFontSize = Float.parseFloat(properties.getProperty("OpenQuestions.Header.FontSize"));
		this.footerFontSize = Float.parseFloat(properties.getProperty("OpenQuestions.Footer.FontSize"));


		/** The number of open questions to show at one time */
		final int questionsShow = Integer.parseInt(properties.getProperty("OpenQuestions.QuestionsShow"));

		setLabelProperties(this.qNumberLabel, qNumWidth, headerHeight, headerColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.effortLabel, effortWidth, headerHeight, headerColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.valueLabel, valueWidth, headerHeight, headerColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.questionLabel, questionWidth, headerHeight, headerColor, headerBackgroundColor,
				headerFontSize);
		setPanelProperties(this.visualTriviaPanel, 5, headerHeight, headerBackgroundColor);
		setLabelProperties(this.visualTriviaLabel, visualTriviaWidth, headerHeight, unusedColor, footerBackgroundColor,
				footerFontSize);
		for (final JLabel label : this.statusLabels) {
			setLabelProperties(label, this.statusWidth, footerHeight, footerBackgroundColor, footerBackgroundColor,
					footerFontSize);
		}
		this.scrollPane.setPreferredSize(new Dimension(0, questionsShow * rowHeight + 3));
		this.scrollPane.setMinimumSize(new Dimension(0, rowHeight + 3));

		this.openQuestionsSubPanel.loadProperties(properties);
	}

	/**
	 * Panel which displays a list of the current open questions.
	 */
	private class OpenQuestionsSubPanel extends TriviaMainPanel implements ActionListener {

		/** The Constant serialVersionUID. */
		private static final long		serialVersionUID	= 6049067322505905668L;

		// Maximum number of questions in a round
		private final int				nQuestionsMax;

		/**
		 * GUI Elements that will need to be updated
		 */
		private final JLabel[]			effortLabels;
		private final JLabel[]			qValueLabels;
		private final QuestionPane[]	qTextPanes;
		private final JButton[]			answerButtons, closeButtons;
		private final JToggleButton[]	qNumberButtons;
		private final JPopupMenu		contextMenu;
		private final JPanel			spacer;
		private final ArrayList<User[]>	lastEffort;

		/**
		 * Data sources
		 */
		private final TriviaClient		client;

		/**
		 * Instantiates a new workflow q list sub panel.
		 *
		 * @param server
		 *            the server
		 * @param client
		 *            the client
		 */
		public OpenQuestionsSubPanel(TriviaClient client, TriviaFrame parent) {

			super(client, parent);

			this.client = client;
			this.nQuestionsMax = client.getTrivia().getMaxQuestions();
			this.lastEffort = new ArrayList<User[]>();

			for (int q = 0; q < this.nQuestionsMax; q++) {
				this.lastEffort.add(null);
			}

			/**
			 * Build context menu
			 */
			this.contextMenu = new JPopupMenu();

			final JMenuItem viewItem = new JMenuItem("View");
			viewItem.setActionCommand("View");
			viewItem.addActionListener(this);
			this.contextMenu.add(viewItem);

			final JMenuItem editItem = new JMenuItem("Edit");
			editItem.setActionCommand("Edit");
			editItem.addActionListener(this);
			this.contextMenu.add(editItem);

			final JMenuItem resetItem = new JMenuItem("Delete");
			resetItem.setActionCommand("Delete");
			resetItem.addActionListener(this);
			this.contextMenu.add(resetItem);

			this.add(this.contextMenu);

			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.NORTH;
			constraints.weightx = 0.0;
			constraints.weighty = 0.0;

			/**
			 * Create the GUI elements
			 */
			this.qNumberButtons = new JToggleButton[this.nQuestionsMax];
			this.effortLabels = new JLabel[this.nQuestionsMax];
			this.qValueLabels = new JLabel[this.nQuestionsMax];
			this.qTextPanes = new QuestionPane[this.nQuestionsMax];
			this.answerButtons = new JButton[this.nQuestionsMax];
			this.closeButtons = new JButton[this.nQuestionsMax];

			for (int q = 0; q < this.nQuestionsMax; q++) {
				constraints.gridx = 0;
				constraints.gridy = q;
				JPanel panel = new JPanel(new GridBagLayout());
				this.add(panel, constraints);
				this.qNumberButtons[q] = new JToggleButton("");
				this.qNumberButtons[q].setBorder(BorderFactory.createEmptyBorder());
				this.qNumberButtons[q].setMargin(new Insets(0, 0, 0, 0));
				panel.add(this.qNumberButtons[q], buttonConstraints);
				this.qNumberButtons[q].setActionCommand("Set Effort");
				this.qNumberButtons[q].addActionListener(this);
				this.qNumberButtons[q].addMouseListener(new PopupListener(this.contextMenu));

				constraints.gridx = 1;
				constraints.gridy = q;
				this.effortLabels[q] =
						this.enclosedLabel("", constraints, SwingConstants.CENTER, SwingConstants.BOTTOM);

				constraints.gridx = 2;
				constraints.gridy = q;
				this.qValueLabels[q] =
						this.enclosedLabel("", constraints, SwingConstants.CENTER, SwingConstants.CENTER);
				this.qValueLabels[q].addMouseListener(new PopupListener(this.contextMenu));

				constraints.gridx = 3;
				constraints.gridy = q;
				constraints.weightx = 1.0;
				this.qTextPanes[q] = this.hyperlinkedTextPane(this.client, "", constraints,
						ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
						ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				this.qTextPanes[q].setEditable(false);
				this.qTextPanes[q].addMouseListener(new PopupListener(this.contextMenu));
				constraints.weightx = 0.0;

				constraints.gridx = 4;
				constraints.gridy = q;
				panel = new JPanel(new GridBagLayout());
				this.add(panel, constraints);
				this.answerButtons[q] = new JButton("");
				this.answerButtons[q].setBorder(BorderFactory.createEmptyBorder());
				this.answerButtons[q].setMargin(new Insets(0, 0, 0, 0));
				panel.add(this.answerButtons[q], buttonConstraints);
				this.answerButtons[q].setActionCommand("Answer");
				this.answerButtons[q].addActionListener(this);

				constraints.gridx = 5;
				constraints.gridy = q;
				panel = new JPanel(new GridBagLayout());
				this.add(panel, constraints);
				this.closeButtons[q] = new JButton("Open");
				this.closeButtons[q].setBorder(BorderFactory.createEmptyBorder());
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
			constraints.gridwidth = 6;
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
			final Trivia trivia = this.client.getTrivia();
			final int rNumber = trivia.getCurrentRoundNumber();
			final String command = event.getActionCommand();
			final int qNumber;
			switch (command) {
				case "Answer":
					qNumber = Integer.parseInt(( (Component) event.getSource() ).getName());
					this.answerQuestion(qNumber);
					break;
				case "Close":
					qNumber = Integer.parseInt(( (Component) event.getSource() ).getName());
					new CloseQuestionDialog(this.client, rNumber, qNumber);
					break;
				case "Open":
					this.open();
					break;
				case "Edit":
					qNumber = Integer.parseInt(this.contextMenu.getName());
					int qValue = trivia.getRound(rNumber).getValue(qNumber);
					String qText = trivia.getRound(rNumber).getQuestionText(qNumber);
					final int nQuestions = trivia.getRound(rNumber).getNQuestions();
					new NewQuestionDialog(this.client, rNumber, nQuestions, qNumber, qValue, qText, false);
					break;
				case "Delete":
					qNumber = Integer.parseInt(this.contextMenu.getName());
					qValue = trivia.getRound(rNumber).getValue(qNumber);
					qText = trivia.getRound(rNumber).getQuestionText(qNumber);
					new ResetQuestionDialog(this.client, rNumber, qNumber, qValue, qText);
					break;
				case "View":
					qNumber = Integer.parseInt(this.contextMenu.getName());
					qValue = trivia.getRound(rNumber).getValue(qNumber);
					qText = trivia.getRound(rNumber).getQuestionText(qNumber);
					new ViewQuestionDialog(rNumber, qNumber, qValue, qText);
					break;
				case "Set Effort":
					JToggleButton source = (JToggleButton) event.getSource();
					qNumber = source.isSelected() ? Integer.parseInt(source.getName()) : 0;
					this.client.getUser().setEffort(qNumber);

					( new SwingWorker<Void, Void>() {
						@Override
						public Void doInBackground() {
							OpenQuestionsPanel.this.client.sendMessage(new SetEffortMessage(rNumber, qNumber));
							return null;
						}

						@Override
						public void done() {
							String logMessage =
									qNumber == 0 ? "Stopped working on Question" : "Started working on Question #"
											+ qNumber;
							OpenQuestionsPanel.this.client.log(logMessage);
						}
					} ).execute();
					break;
				default:
					break;
			}
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see net.bubbaland.trivia.TriviaPanel#update()
		 */
		@Override
		public synchronized void updateGUI(boolean force) {

			// Get the local copy of the Trivia data
			final Trivia trivia = this.client.getTrivia();

			// Get the data for any open questions
			final int[] openQuestionNumbers = trivia.getCurrentRound().getOpenQuestionNumbers();
			final String[] openQuestionText = trivia.getCurrentRound().getOpenQuestionText();
			final String[] openQuestionValues = trivia.getCurrentRound().getOpenQuestionValues();
			final Question[] openQuestions = trivia.getCurrentRound().getOpenQuestions();
			final User[] userList = client.getUserList();
			final int nOpen = openQuestionNumbers.length;

			// Check if there were any changes to the list of open questions
			final boolean[] qUpdated = new boolean[nOpen];
			boolean anyUpdate = false;
			for (int q = 0; q < nOpen; q++) {
				qUpdated[q] = !( this.qNumberButtons[q].getText().equals(openQuestionNumbers[q] + "")
						&& this.qValueLabels[q].getText().equals(openQuestionValues[q] + "")
						&& this.qTextPanes[q].textEquals(openQuestionText[q])
						&& this.lastEffort.get(q) == openQuestions[q].getEffort(userList) );
				anyUpdate = anyUpdate || qUpdated[q];
			}


			if (!Arrays.stream(openQuestionNumbers).anyMatch(new IntPredicate() {
				@Override
				public boolean test(int i) {
					return i == OpenQuestionsSubPanel.this.client.getUser().getEffort();
				}
			})) {
				this.client.getUser().setEffort(0);
			}

			// Show data for open questions
			for (int q = 0; q < nOpen; q++) {
				if (qUpdated[q] || force) {
					this.lastEffort.set(q, openQuestions[q].getEffort(userList));
					final int nEffort = openQuestions[q].getEffort(userList) == null ? 0 : openQuestions[q]
							.getEffort(userList).length;
					String lastEffortString =
							"<html><div align=center>" + nEffort + ( nEffort == 1 ? " User" : " Users" )
									+ " Working on Q" + openQuestionNumbers[q] + "</div>";
					if (this.lastEffort.get(q) != null) {
						for (User user : this.lastEffort.get(q)) {
							lastEffortString = lastEffortString + user.getUserName() + "<BR/>";
						}
					}
					lastEffortString = lastEffortString + "</html>";

					this.qNumberButtons[q].setText(openQuestionNumbers[q] + "");
					this.qNumberButtons[q].setEnabled(true);
					this.qNumberButtons[q].setSelected(this.client.getUser().getEffort() == openQuestionNumbers[q]);
					this.effortLabels[q].setText(nEffort + "");
					this.effortLabels[q].setToolTipText(lastEffortString);
					this.qValueLabels[q].setText(openQuestionValues[q]);
					this.qTextPanes[q].setText(openQuestionText[q]);
					this.answerButtons[q].setText("Answer");
					this.answerButtons[q].setName(openQuestionNumbers[q] + "");
					this.answerButtons[q].setVisible(true);
					this.closeButtons[q].setText("Close");
					this.closeButtons[q].setActionCommand("Close");
					this.closeButtons[q].setName(openQuestionNumbers[q] + "");
					this.closeButtons[q].setVisible(true);

					this.qNumberButtons[q].setName(openQuestionNumbers[q] + "");
					this.qValueLabels[q].setName(openQuestionNumbers[q] + "");
					this.qTextPanes[q].setName(openQuestionNumbers[q] + "");
				}
			}

			// Blank unused lines and hide buttons (except one Open button)
			for (int q = nOpen; q < this.nQuestionsMax; q++) {
				this.effortLabels[q].setText("");
				this.qValueLabels[q].setText("");
				this.answerButtons[q].setText("");
				this.answerButtons[q].setName("");
				this.answerButtons[q].setVisible(false);
				this.closeButtons[q].setText("Open");
				this.closeButtons[q].setActionCommand("Open");
				this.closeButtons[q].setName("");
				this.qNumberButtons[q].setEnabled(false);
				if (q == nOpen && trivia.getCurrentRound().nUnopened() > 0) {
					this.qNumberButtons[q].setText(trivia.getCurrentRound().nextToOpen() + "");
					this.effortLabels[q].setToolTipText(null);
					this.qTextPanes[q].setText("Next to open");
					this.closeButtons[q].setVisible(true);
				} else {
					this.qNumberButtons[q].setText("");
					this.qTextPanes[q].setText("");
					this.closeButtons[q].setVisible(false);
				}
				this.qNumberButtons[q].setName("");
				this.qValueLabels[q].setName("");
				this.qTextPanes[q].setName("");
			}

			int nQuestionsShow;
			if (trivia.getCurrentRound().nUnopened() > 0) {
				nQuestionsShow = nOpen + 1;
			} else {
				nQuestionsShow = nOpen;
			}

			// Show rows equal to the greater of the number of questions to show and the number of open questions
			for (int q = 0; q < nQuestionsShow; q++) {
				this.qNumberButtons[q].setVisible(true);
				this.effortLabels[q].setVisible(true);
				this.qValueLabels[q].setVisible(true);
				this.qTextPanes[q].setVisible(true);

				this.qNumberButtons[q].getParent().setVisible(true);
				this.effortLabels[q].getParent().setVisible(true);
				this.qValueLabels[q].getParent().setVisible(true);
				this.qTextPanes[q].setVisible(true);
				this.qTextPanes[q].getParent().setVisible(true);
				this.qTextPanes[q].getParent().getParent().setVisible(true);
				this.answerButtons[q].getParent().setVisible(true);
				this.closeButtons[q].getParent().setVisible(true);
			}

			// Hide the rest of the rows
			for (int q = nQuestionsShow; q < this.nQuestionsMax; q++) {
				this.qNumberButtons[q].setVisible(false);
				this.effortLabels[q].setVisible(false);
				this.qValueLabels[q].setVisible(false);
				this.qTextPanes[q].setVisible(false);

				this.qNumberButtons[q].getParent().setVisible(false);
				this.effortLabels[q].getParent().setVisible(false);
				this.qValueLabels[q].getParent().setVisible(false);
				this.qTextPanes[q].setVisible(false);
				this.qTextPanes[q].getParent().setVisible(false);
				this.qTextPanes[q].getParent().getParent().setVisible(false);
				this.answerButtons[q].getParent().setVisible(false);
				this.closeButtons[q].getParent().setVisible(false);
			}

		}

		@Override
		protected void loadProperties(Properties properties) {
			/**
			 * Colors
			 */
			final Color headerBackgroundColor = new Color(
					new BigInteger(properties.getProperty("OpenQuestions.Header.BackgroundColor"), 16).intValue());
			final Color oddRowColor =
					new Color(new BigInteger(properties.getProperty("OpenQuestions.OddRow.Color"), 16).intValue());
			final Color evenRowColor =
					new Color(new BigInteger(properties.getProperty("OpenQuestions.EvenRow.Color"), 16).intValue());
			final Color oddRowBackgroundColor = new Color(
					new BigInteger(properties.getProperty("OpenQuestions.OddRow.BackgroundColor"), 16).intValue());
			final Color evenRowBackgroundColor = new Color(
					new BigInteger(properties.getProperty("OpenQuestions.EvenRow.BackgroundColor"), 16).intValue());

			/**
			 * Sizes
			 */

			final int rowHeight = Integer.parseInt(properties.getProperty("OpenQuestions.Row.Height"));
			final int qNumWidth = Integer.parseInt(properties.getProperty("OpenQuestions.QNumber.Width"));
			final int effortWidth = Integer.parseInt(properties.getProperty("OpenQuestions.Effort.Width"));
			final int questionWidth = Integer.parseInt(properties.getProperty("OpenQuestions.Question.Width"));
			final int valueWidth = Integer.parseInt(properties.getProperty("OpenQuestions.Value.Width"));
			final int answerWidth = Integer.parseInt(properties.getProperty("OpenQuestions.AnswerCol.Width"));
			final int closeWidth = Integer.parseInt(properties.getProperty("OpenQuestions.CloseCol.Width"));

			/**
			 * Button sizes
			 */
			final int answerButtonHeight =
					Integer.parseInt(properties.getProperty("OpenQuestions.AnswerButton.Height"));
			final int answerButtonWidth = Integer.parseInt(properties.getProperty("OpenQuestions.AnswerButton.Width"));
			final int closeButtonHeight = Integer.parseInt(properties.getProperty("OpenQuestions.CloseButton.Height"));
			final int closeButtonWidth = Integer.parseInt(properties.getProperty("OpenQuestions.CloseButton.Width"));

			/**
			 * Font sizes
			 */
			final float qNumFontSize = Float.parseFloat(properties.getProperty("OpenQuestions.QNumber.FontSize"));
			final float effortFontSize = Float.parseFloat(properties.getProperty("OpenQuestions.Effort.FontSize"));
			final float valueFontSize = Float.parseFloat(properties.getProperty("OpenQuestions.Value.FontSize"));
			final float questionFontSize = Float.parseFloat(properties.getProperty("OpenQuestions.Question.FontSize"));

			for (int q = 0; q < this.nQuestionsMax; q++) {
				Color color, bColor;
				if (q % 2 == 1) {
					color = oddRowColor;
					bColor = oddRowBackgroundColor;
				} else {
					color = evenRowColor;
					bColor = evenRowBackgroundColor;
				}

				setPanelProperties((JPanel) this.qNumberButtons[q].getParent(), qNumWidth, rowHeight, bColor);
				setButtonProperties(this.qNumberButtons[q], qNumWidth, rowHeight, null, qNumFontSize);
				setLabelProperties(this.effortLabels[q], effortWidth, rowHeight, color, bColor, effortFontSize);
				setLabelProperties(this.qValueLabels[q], valueWidth, rowHeight, color, bColor, valueFontSize);
				setTextPaneProperties(this.qTextPanes[q], questionWidth, rowHeight, color, bColor, questionFontSize);
				setPanelProperties((JPanel) this.answerButtons[q].getParent(), answerWidth, rowHeight, bColor);
				setPanelProperties((JPanel) this.closeButtons[q].getParent(), closeWidth, rowHeight, bColor);
				setButtonProperties(this.answerButtons[q], answerButtonWidth, answerButtonHeight, null,
						this.answerButtons[q].getFont().getSize2D());
				setButtonProperties(this.closeButtons[q], closeButtonWidth, closeButtonHeight, null,
						this.closeButtons[q].getFont().getSize2D());
			}

			this.spacer.setBackground(headerBackgroundColor);
		}

		/**
		 * Propose an answer for the designated question. Creates a prompt to submit the answer.
		 *
		 * @param qNumber
		 *            the question number
		 */
		private void answerQuestion(int qNumber) {
			new AnswerEntryPanel(this.client, this.client.getCurrentRoundNumber(), qNumber,
					this.client.getUser().getUserName());
		}

		/**
		 * Open a new question. Creates a prompt to enter the question.
		 */
		private void open() {

			final Trivia trivia = this.client.getTrivia();

			final int nQuestions = trivia.getCurrentRound().getNQuestions();
			final int nextToOpen = trivia.getCurrentRound().nextToOpen();
			final int rNumber = trivia.getCurrentRoundNumber();

			if (trivia.getRound(rNumber).isSpeed() && nextToOpen > 1) {
				new NewQuestionDialog(this.client, rNumber, nQuestions, nextToOpen,
						trivia.getRound(rNumber).getValue(nextToOpen - 1), true);
			} else {
				new NewQuestionDialog(this.client, rNumber, nQuestions, nextToOpen, true);
			}

		}

		private class PopupListener extends MouseAdapter {

			private final JPopupMenu menu;

			public PopupListener(JPopupMenu menu) {
				this.menu = menu;
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				this.checkForPopup(e);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				this.checkForPopup(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				this.checkForPopup(e);
			}

			private void checkForPopup(MouseEvent event) {
				final JComponent source = (JComponent) event.getSource();
				if (event.isPopupTrigger() && !source.getName().equals("")) {
					this.menu.setName(source.getName());
					this.menu.show(source, event.getX(), event.getY());
				}
			}
		}
	}

}
