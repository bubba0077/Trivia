package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.text.DefaultCaret;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import net.bubbaland.trivia.Trivia;

/**
 * A panel that shows all of the questions for a round.
 * 
 * <code>RoundQlistPanel</code> is a panel that contains a list of all the question data for a round. The panel consists
 * of a header row and then a sub-panel placed inside a scroll pane. The sub-panel is created via an inner class. Each
 * row of the sub-panel contains GUI elements displaying data for one question. Unused rows (for a speed round) are
 * created but hidden until necessary.
 * 
 * @author Walter Kolczynski
 * 
 */
public class RoundQuestionsPanel extends TriviaMainPanel {

	/** The Constant serialVersionUID. */
	private static final long				serialVersionUID	= 3589815467416864653L;

	private final JLabel					qNumberLabel, earnedLabel, valueLabel, questionLabel, answerLabel,
			subOpLabel, blank;

	/** The sub-panel holding the questions */
	private final RoundQuestionsSubPanel	roundQuestionsSubPanel;
	private final JScrollPane				roundQlistPane;

	/**
	 * Instantiates a new question list panel that will show data for the current round.
	 * 
	 * @param client
	 *            the client
	 */
	public RoundQuestionsPanel(TriviaClient client, TriviaFrame parent) {
		this(client, parent, true, 0);
	}

	/**
	 * Instantiates a new question list panel.
	 * 
	 * @param client
	 *            The local trivia client
	 * @param live
	 *            whether this panel should always show the current round
	 * @param rNumber
	 *            the round number
	 */
	public RoundQuestionsPanel(TriviaClient client, TriviaFrame parent, boolean live, int rNumber) {

		super(client, parent);

		// Set up the layout constraints
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
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
		this.earnedLabel = this.enclosedLabel("Earned", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

		constraints.gridx = 2;
		constraints.gridy = 0;
		this.valueLabel = this.enclosedLabel("Value", constraints, SwingConstants.CENTER, SwingConstants.CENTER);
		constraints.weightx = 0.0;

		constraints.gridx = 3;
		constraints.gridy = 0;
		constraints.weightx = 0.6;
		this.questionLabel = this.enclosedLabel("Question", constraints, SwingConstants.LEFT, SwingConstants.CENTER);
		constraints.weightx = 0.0;

		constraints.gridx = 4;
		constraints.gridy = 0;
		constraints.weightx = 0.4;
		this.answerLabel = this.enclosedLabel("Answer", constraints, SwingConstants.LEFT, SwingConstants.CENTER);
		constraints.weightx = 0.0;

		constraints.gridx = 5;
		constraints.gridy = 0;
		this.subOpLabel = this.enclosedLabel("Credit/Op", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

		constraints.gridx = 6;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		this.blank = this.enclosedLabel("", constraints, SwingConstants.CENTER, SwingConstants.CENTER);


		/**
		 * Create the question list sub-panel and place in a scroll pane
		 */
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 7;
		constraints.weighty = 1.0;
		this.roundQuestionsSubPanel = new RoundQuestionsSubPanel(client, parent, live, rNumber);
		this.roundQlistPane = new JScrollPane(this.roundQuestionsSubPanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		this.roundQlistPane.setBorder(BorderFactory.createEmptyBorder());
		this.roundQlistPane.setPreferredSize(new Dimension(0, 200));
		this.add(this.roundQlistPane, constraints);
		constraints.weighty = 0.0;

		this.loadProperties(TriviaClient.PROPERTIES);
	}

	/**
	 * Sets the round to display. This will be overridden with the current round number if this is a "live" panel.
	 * 
	 * @param rNumber
	 *            the new round number
	 */
	public void setRound(int rNumber) {
		this.roundQuestionsSubPanel.setRound(rNumber);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void updateGUI(boolean force) {
		this.roundQuestionsSubPanel.updateGUI(force);
	}


	@Override
	protected void loadProperties(Properties properties) {
		/**
		 * Colors
		 */
		final Color headerColor = new Color(
				new BigInteger(properties.getProperty("RoundQuestions.Header.Color"), 16).intValue());
		final Color headerBackgroundColor = new Color(new BigInteger(
				properties.getProperty("RoundQuestions.Header.BackgroundColor"), 16).intValue());
		/**
		 * Sizes
		 */
		final int headerHeight = Integer.parseInt(properties.getProperty("RoundQuestions.Header.Height"));

		final int qNumWidth = Integer.parseInt(properties.getProperty("RoundQuestions.QNumber.Width"));
		final int earnedWidth = Integer.parseInt(properties.getProperty("RoundQuestions.Earned.Width"));
		final int valueWidth = Integer.parseInt(properties.getProperty("RoundQuestions.Value.Width"));
		final int questionWidth = Integer.parseInt(properties.getProperty("RoundQuestions.Question.Width"));
		final int answerWidth = Integer.parseInt(properties.getProperty("RoundQuestions.Answer.Width"));
		final int subOpWidth = Integer.parseInt(properties.getProperty("RoundQuestions.SubOp.Width"));

		/**
		 * Font sizes
		 */
		final float headerFontSize = Float.parseFloat(properties.getProperty("RoundQuestions.Header.FontSize"));

		setLabelProperties(this.qNumberLabel, qNumWidth, headerHeight, headerColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.earnedLabel, earnedWidth, headerHeight, headerColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.valueLabel, valueWidth, headerHeight, headerColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.questionLabel, questionWidth, headerHeight, headerColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.answerLabel, answerWidth, headerHeight, headerColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.subOpLabel, subOpWidth, headerHeight, headerColor, headerBackgroundColor,
				headerFontSize);
		final int scrollBarWidth;
		if (UIManager.getLookAndFeel().getName().equals("Nimbus")) {
			scrollBarWidth = (int) UIManager.get("ScrollBar.thumbHeight");
		} else {
			scrollBarWidth = ( (Integer) UIManager.get("ScrollBar.width") ).intValue();
		}
		setLabelProperties(this.blank, scrollBarWidth, headerHeight, headerColor, headerBackgroundColor, headerFontSize);

		this.roundQuestionsSubPanel.loadProperties(properties);


	}

	/**
	 * A panel that displays the question data for a round.
	 */
	private class RoundQuestionsSubPanel extends TriviaMainPanel implements ActionListener {

		private static final long		serialVersionUID	= 3825357215129662133L;

		/**
		 * GUI Elements that will need to be updated
		 */
		private final JLabel[]			qNumberLabels, earnedLabels, valueLabels;
		private final QuestionPane[]	questionTextPane;
		private final JTextArea[]		answerTextAreas;
		private final JTextPane[]		submitterTextPanes, operatorTextPanes;
		private final JSeparator[]		separators;
		private final JMenuItem			editItem, reopenItem;
		private final JPopupMenu		contextMenu;
		private final JPanel			spacer;

		/** Status variables */
		private boolean					speed;

		private final boolean			live;
		private final int				maxQuestions;

		private int						rNumber;

		/** Data source */
		private final TriviaClient		client;

		/**
		 * Instantiates a new question list sub-panel.
		 * 
		 * @param server
		 *            the server
		 * @param client
		 *            the client application
		 * @param live
		 *            whether this panel should always show the current round
		 * @param rNumber
		 *            the round number
		 */
		public RoundQuestionsSubPanel(TriviaClient client, TriviaFrame parent, boolean live, int rNumber) {
			super(client, parent);

			this.client = client;
			this.speed = false;
			this.live = live;
			this.rNumber = rNumber;

			this.maxQuestions = client.getTrivia().getMaxQuestions();

			/**
			 * Build context menu
			 */
			this.contextMenu = new JPopupMenu();
			this.editItem = new JMenuItem("Edit");
			this.editItem.setActionCommand("Edit");
			this.editItem.addActionListener(this);
			this.contextMenu.add(this.editItem);

			if (live) {
				this.reopenItem = new JMenuItem("Reopen");
				this.reopenItem.setActionCommand("Reopen");
				this.reopenItem.addActionListener(this);
				this.contextMenu.add(this.reopenItem);
			} else {
				this.reopenItem = null;
			}
			this.add(this.contextMenu);


			this.qNumberLabels = new JLabel[this.maxQuestions];
			this.earnedLabels = new JLabel[this.maxQuestions];
			this.valueLabels = new JLabel[this.maxQuestions];
			this.submitterTextPanes = new JTextPane[this.maxQuestions];
			this.operatorTextPanes = new JTextPane[this.maxQuestions];
			this.questionTextPane = new QuestionPane[this.maxQuestions];
			this.answerTextAreas = new JTextArea[this.maxQuestions];
			this.separators = new JSeparator[this.maxQuestions];

			// Set up layout constraints
			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.NORTH;
			constraints.weightx = 0.0;
			constraints.weighty = 0.0;

			for (int q = 0; q < this.maxQuestions; q++) {
				/**
				 * Plot this row
				 */
				constraints.gridheight = 2;

				constraints.gridx = 0;
				constraints.gridy = 2 * q;
				this.qNumberLabels[q] = this.enclosedLabel(( q + 1 ) + "", constraints, SwingConstants.CENTER,
						SwingConstants.CENTER);
				this.qNumberLabels[q].setName(( q + 1 ) + "");
				this.qNumberLabels[q].addMouseListener(new PopupListener(this.contextMenu));

				constraints.gridx = 1;
				constraints.gridy = 2 * q;
				this.earnedLabels[q] = this
						.enclosedLabel("", constraints, SwingConstants.CENTER, SwingConstants.CENTER);
				this.earnedLabels[q].setName(( q + 1 ) + "");
				this.earnedLabels[q].addMouseListener(new PopupListener(this.contextMenu));

				constraints.gridx = 2;
				constraints.gridy = 2 * q;
				this.valueLabels[q] = this.enclosedLabel("", constraints, SwingConstants.CENTER, SwingConstants.CENTER);
				this.valueLabels[q].setName(( q + 1 ) + "");
				this.valueLabels[q].addMouseListener(new PopupListener(this.contextMenu));

				constraints.gridx = 3;
				constraints.gridy = 2 * q;
				constraints.weightx = 0.6;
				this.questionTextPane[q] = this.hyperlinkedTextPane("", constraints,
						ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
						ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				this.questionTextPane[q].setEditable(false);
				this.questionTextPane[q].setName(( q + 1 ) + "");
				this.questionTextPane[q].addMouseListener(new PopupListener(this.contextMenu));

				constraints.weightx = 0.0;
				constraints.weighty = 0.0;
				constraints.gridx = 4;
				constraints.gridy = 2 * q;
				constraints.weightx = 0.0;
				this.separators[q] = new JSeparator(SwingConstants.VERTICAL);
				this.add(this.separators[q], constraints);

				constraints.gridx = 5;
				constraints.gridy = 2 * q;
				constraints.weightx = 0.4;
				this.answerTextAreas[q] = this.scrollableTextArea("", constraints,
						ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
						ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				this.answerTextAreas[q].setEditable(false);
				this.answerTextAreas[q].setName(( q + 1 ) + "");
				this.answerTextAreas[q].addMouseListener(new PopupListener(this.contextMenu));
				constraints.weightx = 0.0;
				constraints.weighty = 0.0;

				constraints.gridheight = 1;
				constraints.gridx = 6;
				constraints.gridy = 2 * q;
				this.submitterTextPanes[q] = scrollableTextPane("", constraints,
						ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
						ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				StyleConstants.setAlignment(( (StyledDocument) this.submitterTextPanes[q].getDocument() )
						.getStyle(StyleContext.DEFAULT_STYLE), StyleConstants.ALIGN_CENTER);
				DefaultCaret caret = (DefaultCaret) this.submitterTextPanes[q].getCaret();
				caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
				this.submitterTextPanes[q].setEditable(false);
				this.submitterTextPanes[q].setName(( q + 1 ) + "");
				this.submitterTextPanes[q].addMouseListener(new PopupListener(this.contextMenu));

				constraints.gridx = 6;
				constraints.gridy = 2 * q + 1;
				this.operatorTextPanes[q] = scrollableTextPane("", constraints,
						ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
						ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				StyleConstants.setAlignment(( (StyledDocument) this.operatorTextPanes[q].getDocument() )
						.getStyle(StyleContext.DEFAULT_STYLE), StyleConstants.ALIGN_CENTER);
				caret = (DefaultCaret) this.operatorTextPanes[q].getCaret();
				caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
				this.operatorTextPanes[q].setEditable(false);
				this.operatorTextPanes[q].setName(( q + 1 ) + "");
				this.operatorTextPanes[q].addMouseListener(new PopupListener(this.contextMenu));

			}

			/**
			 * Extra row at the bottom to soak up any extra space
			 */
			constraints.gridx = 0;
			constraints.gridy = this.maxQuestions;
			constraints.gridwidth = 7;
			constraints.weighty = 1.0;
			this.spacer = new JPanel();
			this.spacer.setPreferredSize(new Dimension(0, 0));
			this.add(this.spacer, constraints);
		}


		/**
		 * Process mouse clicks
		 */
		@Override
		public void actionPerformed(ActionEvent event) {
			final Trivia trivia = this.client.getTrivia();
			final int qNumber = Integer.parseInt(this.contextMenu.getName());
			if (this.live) {
				this.rNumber = trivia.getCurrentRoundNumber();
			}
			final String command = event.getActionCommand();
			switch (command) {
				case "Edit":
					new EditQuestionDialog(this.client, this.rNumber, qNumber);
					break;
				case "Reopen":
					// Repen the question on the server
					int tryNumber = 0;
					boolean success = false;
					while (tryNumber < Integer.parseInt(TriviaClient.PROPERTIES.getProperty("MaxRetries"))
							&& success == false) {
						tryNumber++;
						try {
							this.client.getServer().reopen(this.client.getUser(), qNumber);
							success = true;
						} catch (final RemoteException e) {
							this.client.log("Couldn't reopen question on server (try #" + tryNumber + ").");
						}
					}

					if (!success) {
						this.client.disconnected();
						return;
					}

					this.client.log("Question #" + qNumber + " reopened.");
					break;
				default:
					break;
			}
		}

		/**
		 * Sets the round to display. This will be overridden with the current round number if this is a "live" panel.
		 * 
		 * @param rNumber
		 *            the new round
		 */
		public void setRound(int rNumber) {
			this.rNumber = rNumber;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.bubbaland.trivia.TriviaPanel#update()
		 */
		@Override
		public synchronized void updateGUI(boolean force) {

			// Get the current Trivia data structure
			final Trivia trivia = this.client.getTrivia();
			// If this is a live panel, update the round number
			if (this.live) {
				this.rNumber = trivia.getCurrentRoundNumber();
			}
			// Get all of the question data for the current round
			final int nQuestions = trivia.getNQuestions(this.rNumber);
			final boolean newSpeed = trivia.isSpeed(this.rNumber);
			final boolean[] beenOpens = trivia.eachBeenOpen(this.rNumber);
			final boolean[] opens = trivia.eachOpen(this.rNumber);
			final boolean[] corrects = trivia.eachCorrect(this.rNumber);
			final int[] earneds = trivia.getEachEarned(this.rNumber);
			final int[] values = trivia.getEachValue(this.rNumber);
			final String[] questions = trivia.getEachQuestionText(this.rNumber);
			final String[] answers = trivia.getEachAnswerText(this.rNumber);
			final String[] submitters = trivia.getEachSubmitter(this.rNumber);
			final String[] operators = trivia.getEachOperator(this.rNumber);

			// Determine which questions have been updated
			final boolean[] qUpdated = new boolean[nQuestions];
			for (int q = 0; q < nQuestions; q++) {
				if (( beenOpens[q] || !this.valueLabels[q].getText().equals("") )) {
					if (beenOpens[q] && !opens[q]) {
						qUpdated[q] = !( this.speed == newSpeed && this.valueLabels[q].getText().equals(values[q] + "")
								&& this.earnedLabels[q].getText().equals(earneds[q] + "")
								&& this.questionTextPane[q].textEquals(questions[q])
								&& this.answerTextAreas[q].getText().equals(answers[q])
								&& this.submitterTextPanes[q].getText().equals(submitters[q]) && this.operatorTextPanes[q]
								.getText().equals(operators[q]) );
					} else {
						qUpdated[q] = !( this.speed == newSpeed && this.valueLabels[q].getText().equals(values[q] + "")
								&& this.earnedLabels[q].getText().equals("")
								&& this.questionTextPane[q].textEquals(questions[q])
								&& this.answerTextAreas[q].getText().equals(answers[q])
								&& this.submitterTextPanes[q].getText().equals(submitters[q]) && this.operatorTextPanes[q]
								.getText().equals(operators[q]) );
					}
				} else {
					qUpdated[q] = !( this.speed == newSpeed );
				}
			}

			this.speed = newSpeed;
			for (int q = 0; q < nQuestions; q++) {
				if (qUpdated[q] || force) {
					if (beenOpens[q]) {
						// Only show values for questions that have been asked
						this.valueLabels[q].setText(values[q] + "");
						this.questionTextPane[q].setText(questions[q]);
						// this.questionTextAreas[q].setToolTipText(questions[q]);
					} else {
						// Hide values for questions that haven't been asked yet
						this.valueLabels[q].setText("");
						this.questionTextPane[q].setText("");
						// this.questionTextAreas[q].setToolTipText("");
					}
					if (corrects[q] || ( beenOpens[q] && !opens[q] )) {
						// Only show answers and earned points if the question is correct or closed
						this.earnedLabels[q].setText(earneds[q] + "");
						this.answerTextAreas[q].setText(answers[q]);
						// this.answerTextAreas[q].setToolTipText(answers[q]);
						this.submitterTextPanes[q].setText(submitters[q]);
						this.operatorTextPanes[q].setText(operators[q]);
					} else {
						// Hide answer data for questions that haven't been closed
						this.earnedLabels[q].setText("");
						this.answerTextAreas[q].setText("");
						// this.answerTextAreas[q].setToolTipText("");
						this.submitterTextPanes[q].setText("");
						this.operatorTextPanes[q].setText("");
					}

					// Make sure questions are shown
					this.qNumberLabels[q].getParent().setVisible(true);
					this.earnedLabels[q].getParent().setVisible(true);
					this.valueLabels[q].getParent().setVisible(true);
					this.questionTextPane[q].setVisible(true);
					this.separators[q].setVisible(true);
					this.answerTextAreas[q].setVisible(true);
					this.questionTextPane[q].getParent().getParent().setVisible(true);
					this.answerTextAreas[q].getParent().getParent().setVisible(true);
					this.submitterTextPanes[q].setVisible(true);
					this.submitterTextPanes[q].getParent().getParent().setVisible(true);
					this.operatorTextPanes[q].setVisible(true);
					this.operatorTextPanes[q].getParent().getParent().setVisible(true);
				}
			}

			// Hide rows for speed questions in non-speed rounds
			for (int q = nQuestions; q < this.maxQuestions; q++) {
				this.qNumberLabels[q].getParent().setVisible(false);
				this.earnedLabels[q].getParent().setVisible(false);
				this.valueLabels[q].getParent().setVisible(false);
				this.questionTextPane[q].setVisible(false);
				this.separators[q].setVisible(false);
				this.answerTextAreas[q].setVisible(false);
				this.questionTextPane[q].getParent().getParent().setVisible(false);
				this.answerTextAreas[q].getParent().getParent().setVisible(false);
				this.submitterTextPanes[q].setVisible(false);
				this.submitterTextPanes[q].getParent().getParent().setVisible(false);
				this.operatorTextPanes[q].setVisible(false);
				this.operatorTextPanes[q].getParent().getParent().setVisible(false);
			}
		}


		@Override
		protected void loadProperties(Properties properties) {
			/**
			 * Colors
			 */
			final Color headerBackgroundColor = new Color(new BigInteger(
					properties.getProperty("RoundQuestions.Header.BackgroundColor"), 16).intValue());
			final Color oddColor = new Color(
					new BigInteger(properties.getProperty("RoundQuestions.OddRow.Color"), 16).intValue());
			final Color evenColor = new Color(
					new BigInteger(properties.getProperty("RoundQuestions.EvenRow.Color"), 16).intValue());
			final Color oddBackgroundColor = new Color(new BigInteger(
					properties.getProperty("RoundQuestions.OddRow.BackgroundColor"), 16).intValue());
			final Color evenBackgroundColor = new Color(new BigInteger(
					properties.getProperty("RoundQuestions.EvenRow.BackgroundColor"), 16).intValue());

			/**
			 * Sizes
			 */
			final int rowHeight = Integer.parseInt(properties.getProperty("RoundQuestions.Row.Height"));

			final int qNumWidth = Integer.parseInt(properties.getProperty("RoundQuestions.QNumber.Width"));
			final int earnedWidth = Integer.parseInt(properties.getProperty("RoundQuestions.Earned.Width"));
			final int valueWidth = Integer.parseInt(properties.getProperty("RoundQuestions.Value.Width"));
			final int questionWidth = Integer.parseInt(properties.getProperty("RoundQuestions.Question.Width"));
			final int answerWidth = Integer.parseInt(properties.getProperty("RoundQuestions.Answer.Width"));
			final int subOpWidth = Integer.parseInt(properties.getProperty("RoundQuestions.SubOp.Width"));

			/**
			 * Font sizes
			 */
			final float fontSize = Float.parseFloat(properties.getProperty("RoundQuestions.FontSize"));
			final float largeFontSize = Float.parseFloat(properties.getProperty("RoundQuestions.LargeFontSize"));

			for (int q = 0; q < this.maxQuestions; q++) {
				// Set the color for this row
				Color color, bColor;
				if (q % 2 == 1) {
					color = oddColor;
					bColor = oddBackgroundColor;
				} else {
					color = evenColor;
					bColor = evenBackgroundColor;
				}
				setLabelProperties(this.qNumberLabels[q], qNumWidth, rowHeight, color, bColor, largeFontSize);
				setLabelProperties(this.earnedLabels[q], earnedWidth, rowHeight, color, bColor, largeFontSize);
				setLabelProperties(this.valueLabels[q], valueWidth, rowHeight, color, bColor, largeFontSize);
				setTextPaneProperties(this.questionTextPane[q], questionWidth, rowHeight, color, bColor, fontSize);
				setTextAreaProperties(this.answerTextAreas[q], answerWidth, rowHeight, color, bColor, fontSize);
				setTextPaneProperties(this.submitterTextPanes[q], subOpWidth, rowHeight / 3, color, bColor, fontSize);
				setTextPaneProperties(this.operatorTextPanes[q], subOpWidth, 2 * rowHeight / 3, color, bColor, fontSize);
			}
			this.spacer.setBackground(headerBackgroundColor);

		}

		private class PopupListener extends MouseAdapter {

			private final JPopupMenu	menu;

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
				final Trivia trivia = RoundQuestionsSubPanel.this.client.getTrivia();
				final int qNumber = Integer.parseInt(source.getName());
				if (RoundQuestionsSubPanel.this.live) {
					RoundQuestionsSubPanel.this.rNumber = trivia.getCurrentRoundNumber();
				}
				if (event.isPopupTrigger() && trivia.beenOpen(RoundQuestionsSubPanel.this.rNumber, qNumber)) {
					if (RoundQuestionsSubPanel.this.live) {
						RoundQuestionsSubPanel.this.reopenItem.setVisible(!trivia.isOpen(qNumber));
					}
					this.menu.setName(source.getName());
					this.menu.show(source, event.getX(), event.getY());
				}
			}

		}
	}

}
