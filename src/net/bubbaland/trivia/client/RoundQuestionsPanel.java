package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;

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
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
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
public class RoundQuestionsPanel extends TriviaPanel {

	/** The Constant serialVersionUID. */
	private static final long				serialVersionUID				= 3589815467416864653L;

	/**
	 * Colors
	 */
	private static final Color				HEADER_TEXT_COLOR				= Color.white;
	private static final Color				HEADER_BACKGROUND_COLOR			= Color.darkGray;
	private static final Color				ODD_QUESTION_TEXT_COLOR			= Color.black;

	private static final Color				EVEN_QUESTION_TEXT_COLOR		= Color.black;
	private static final Color				ODD_QUESTION_BACKGROUND_COLOR	= Color.white;
	private static final Color				EVEN_QUESTION_BACKGROUND_COLOR	= Color.lightGray;

	/**
	 * Sizes
	 */
	private static final int				HEADER_HEIGHT					= 20;
	private static final int				QUESTION_HEIGHT					= 66;

	private static final int				QNUM_WIDTH						= 54;
	private static final int				EARNED_WIDTH					= 75;
	private static final int				VALUE_WIDTH						= 75;
	private static final int				QUESTION_WIDTH					= 200;
	private static final int				ANSWER_WIDTH					= 150;
	private static final int				SUBOP_WIDTH						= 100;

	/**
	 * Font sizes
	 */
	private static final float				FONT_SIZE						= (float) 12.0;

	private static final float				LARGE_FONT_SIZE					= (float) 36.0;
	private static final float				SMALL_FONT_SIZE					= (float) 12.0;

	/** The sub-panel holding the questions */
	private final RoundQuestionsSubPanel	roundQlistSubPanel;
	private final JScrollPane				roundQlistPane;

	/**
	 * Instantiates a new question list panel that will show data for the current round.
	 * 
	 * @param server
	 *            the server
	 * @param client
	 *            the client
	 */
	public RoundQuestionsPanel(TriviaClient client) {
		this(client, true, 0);
	}

	/**
	 * Instantiates a new question list panel.
	 * 
	 * @param server
	 *            The remote trivia server
	 * @param client
	 *            The local trivia client
	 * @param live
	 *            whether this panel should always show the current round
	 * @param rNumber
	 *            the round number
	 */
	public RoundQuestionsPanel(TriviaClient client, boolean live, int rNumber) {

		super();

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
		this.enclosedLabel("Q#", QNUM_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints,
				FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);

		constraints.gridx = 1;
		constraints.gridy = 0;
		this.enclosedLabel("Earned", EARNED_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR,
				constraints, FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);

		constraints.gridx = 2;
		constraints.gridy = 0;
		this.enclosedLabel("Value", VALUE_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR,
				constraints, FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
		constraints.weightx = 0.0;

		constraints.gridx = 3;
		constraints.gridy = 0;
		constraints.weightx = 0.6;
		this.enclosedLabel("Question", QUESTION_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR,
				constraints, FONT_SIZE, SwingConstants.LEFT, SwingConstants.CENTER);
		constraints.weightx = 0.0;

		constraints.gridx = 4;
		constraints.gridy = 0;
		constraints.weightx = 0.4;
		this.enclosedLabel("Answer", ANSWER_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR,
				constraints, FONT_SIZE, SwingConstants.LEFT, SwingConstants.CENTER);
		constraints.weightx = 0.0;

		constraints.gridx = 5;
		constraints.gridy = 0;
		this.enclosedLabel("Credit/Op", SUBOP_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR,
				constraints, FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);

		constraints.gridx = 6;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		final int scrollBarWidth;
		if (UIManager.getLookAndFeel().getName().equals("Nimbus")) {
			scrollBarWidth = (int) UIManager.get("ScrollBar.thumbHeight");
		} else {
			scrollBarWidth = ( (Integer) UIManager.get("ScrollBar.width") ).intValue();
		}
		this.enclosedLabel("", scrollBarWidth, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints,
				FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);


		/**
		 * Create the question list sub-panel and place in a scroll pane
		 */
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 7;
		constraints.weighty = 1.0;
		this.roundQlistSubPanel = new RoundQuestionsSubPanel(client, live, rNumber);
		this.roundQlistPane = new JScrollPane(this.roundQlistSubPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		this.roundQlistPane.setPreferredSize(new Dimension(0, 200));
		this.add(this.roundQlistPane, constraints);
		constraints.weighty = 0.0;

	}

	/**
	 * Sets the round to display. This will be overridden with the current round number if this is a "live" panel.
	 * 
	 * @param rNumber
	 *            the new round number
	 */
	public void setRound(int rNumber) {
		this.roundQlistSubPanel.setRound(rNumber);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void update(boolean force) {
		this.roundQlistSubPanel.update(force);
	}


	/**
	 * A panel that displays the question data for a round.
	 */
	private class RoundQuestionsSubPanel extends TriviaPanel implements ActionListener {

		private static final long	serialVersionUID	= 3825357215129662133L;

		/**
		 * GUI Elements that will need to be updated
		 */
		private final JLabel[]		qNumberLabels, earnedLabels, valueLabels;
		private final JTextArea[]	questionTextAreas, answerTextAreas;
		private final JTextPane[]	submitterTextAreas, operatorTextAreas;
		private final JSeparator[]	separators;
		private final JMenuItem		editItem, reopenItem;
		private final JPopupMenu	contextMenu;

		/** Status variables */
		private boolean				speed;

		private final boolean		live;
		private final int			maxQuestions;

		private int					rNumber;

		/** Data source */
		private final TriviaClient	client;

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
		public RoundQuestionsSubPanel(TriviaClient client, boolean live, int rNumber) {
			super();

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
			this.submitterTextAreas = new JTextPane[this.maxQuestions];
			this.operatorTextAreas = new JTextPane[this.maxQuestions];
			this.questionTextAreas = new JTextArea[this.maxQuestions];
			this.answerTextAreas = new JTextArea[this.maxQuestions];
			this.separators = new JSeparator[this.maxQuestions];

			// Set up layout constraints
			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.NORTH;
			constraints.weightx = 0.0;
			constraints.weighty = 0.0;

			for (int q = 0; q < this.maxQuestions; q++) {
				// Set the color for this row
				Color color, bColor;
				if (q % 2 == 1) {
					color = ODD_QUESTION_TEXT_COLOR;
					bColor = ODD_QUESTION_BACKGROUND_COLOR;
				} else {
					color = EVEN_QUESTION_TEXT_COLOR;
					bColor = EVEN_QUESTION_BACKGROUND_COLOR;
				}

				/**
				 * Plot this row
				 */
				constraints.gridheight = 2;

				constraints.gridx = 0;
				constraints.gridy = 2 * q;
				this.qNumberLabels[q] = this.enclosedLabel(( q + 1 ) + "", QNUM_WIDTH, QUESTION_HEIGHT, color, bColor,
						constraints, LARGE_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
				this.qNumberLabels[q].setName(( q + 1 ) + "");
				this.qNumberLabels[q].addMouseListener(new PopupListener(this.contextMenu));

				constraints.gridx = 1;
				constraints.gridy = 2 * q;
				this.earnedLabels[q] = this.enclosedLabel("", EARNED_WIDTH, QUESTION_HEIGHT, color, bColor,
						constraints, LARGE_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
				this.earnedLabels[q].setName(( q + 1 ) + "");
				this.earnedLabels[q].addMouseListener(new PopupListener(this.contextMenu));

				constraints.gridx = 2;
				constraints.gridy = 2 * q;
				this.valueLabels[q] = this.enclosedLabel("", VALUE_WIDTH, QUESTION_HEIGHT, color, bColor, constraints,
						LARGE_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
				this.valueLabels[q].setName(( q + 1 ) + "");
				this.valueLabels[q].addMouseListener(new PopupListener(this.contextMenu));

				constraints.gridx = 3;
				constraints.gridy = 2 * q;
				constraints.weightx = 0.6;
				this.questionTextAreas[q] = this.scrollableTextArea("", QUESTION_WIDTH, QUESTION_HEIGHT, color, bColor,
						constraints, SMALL_FONT_SIZE, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
						ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				this.questionTextAreas[q].setEditable(false);
				this.questionTextAreas[q].setName(( q + 1 ) + "");
				this.questionTextAreas[q].addMouseListener(new PopupListener(this.contextMenu));

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
				this.answerTextAreas[q] = this.scrollableTextArea("", ANSWER_WIDTH, QUESTION_HEIGHT, color, bColor,
						constraints, SMALL_FONT_SIZE, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
						ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				this.answerTextAreas[q].setEditable(false);
				this.answerTextAreas[q].setName(( q + 1 ) + "");
				this.answerTextAreas[q].addMouseListener(new PopupListener(this.contextMenu));
				constraints.weightx = 0.0;
				constraints.weighty = 0.0;

				constraints.gridheight = 1;
				constraints.gridx = 6;
				constraints.gridy = 2 * q;
				StyledDocument document = new DefaultStyledDocument();
				Style defaultStyle = document.getStyle(StyleContext.DEFAULT_STYLE);
				StyleConstants.setAlignment(defaultStyle, StyleConstants.ALIGN_CENTER);
				this.submitterTextAreas[q] = new JTextPane(document);
				DefaultCaret caret = (DefaultCaret) this.submitterTextAreas[q].getCaret();
				caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
				this.submitterTextAreas[q].setEditable(false);
				this.submitterTextAreas[q].setFont(this.submitterTextAreas[q].getFont().deriveFont(SMALL_FONT_SIZE));
				this.submitterTextAreas[q].setPreferredSize(new Dimension(SUBOP_WIDTH, QUESTION_HEIGHT / 3));
				this.submitterTextAreas[q].setMinimumSize(new Dimension(SUBOP_WIDTH, QUESTION_HEIGHT / 3));
				if (UIManager.getLookAndFeel().getName().equals("Nimbus")) {
					UIDefaults defaults = new UIDefaults();
					defaults.put("TextPane[Enabled].backgroundPainter", bColor);
					this.submitterTextAreas[q].putClientProperty("Nimbus.Overrides", defaults);
					this.submitterTextAreas[q].putClientProperty("Nimbus.Overrides.InheritDefaults", true);
				}
				this.submitterTextAreas[q].setBackground(bColor);
				this.submitterTextAreas[q].setForeground(color);
				this.submitterTextAreas[q].setName(( q + 1 ) + "");
				this.submitterTextAreas[q].addMouseListener(new PopupListener(this.contextMenu));

				this.add(this.submitterTextAreas[q], constraints);

				constraints.gridx = 6;
				constraints.gridy = 2 * q + 1;
				document = new DefaultStyledDocument();
				defaultStyle = document.getStyle(StyleContext.DEFAULT_STYLE);
				StyleConstants.setAlignment(defaultStyle, StyleConstants.ALIGN_CENTER);
				this.operatorTextAreas[q] = new JTextPane(document);
				caret = (DefaultCaret) this.operatorTextAreas[q].getCaret();
				caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
				this.operatorTextAreas[q].setEditable(false);
				this.operatorTextAreas[q].setFont(this.operatorTextAreas[q].getFont().deriveFont(SMALL_FONT_SIZE));
				this.operatorTextAreas[q].setPreferredSize(new Dimension(SUBOP_WIDTH, 2 * QUESTION_HEIGHT / 3));
				this.operatorTextAreas[q].setMinimumSize(new Dimension(SUBOP_WIDTH, 2 * QUESTION_HEIGHT / 3));
				if (UIManager.getLookAndFeel().getName().equals("Nimbus")) {
					UIDefaults defaults = new UIDefaults();
					defaults.put("TextPane[Enabled].backgroundPainter", bColor);
					this.operatorTextAreas[q].putClientProperty("Nimbus.Overrides", defaults);
					this.operatorTextAreas[q].putClientProperty("Nimbus.Overrides.InheritDefaults", true);
				}
				this.operatorTextAreas[q].setBackground(bColor);
				this.operatorTextAreas[q].setForeground(color);
				this.operatorTextAreas[q].setName(( q + 1 ) + "");
				this.operatorTextAreas[q].addMouseListener(new PopupListener(this.contextMenu));
				this.add(this.operatorTextAreas[q], constraints);

			}

			/**
			 * Extra row at the bottom to soak up any extra space
			 */
			constraints.gridx = 0;
			constraints.gridy = this.maxQuestions;
			constraints.gridwidth = 7;
			constraints.weighty = 1.0;
			final JPanel blank = new JPanel();
			blank.setBackground(HEADER_BACKGROUND_COLOR);
			blank.setPreferredSize(new Dimension(0, 0));
			this.add(blank, constraints);
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
			String command = event.getActionCommand();
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
							this.client.getServer().open(this.client.getUser(), qNumber,
									trivia.getValue(this.rNumber, qNumber),
									trivia.getQuestionText(this.rNumber, qNumber));
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

		private class PopupListener extends MouseAdapter {

			private final JPopupMenu	menu;

			public PopupListener(JPopupMenu menu) {
				this.menu = menu;
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
		public synchronized void update(boolean force) {

			// Get the current Trivia data structure
			final Trivia trivia = this.client.getTrivia();
			// If this is a live panel, update the round number
			if (this.live) {
				this.rNumber = trivia.getCurrentRoundNumber();
			}
			// Get all of the question data for the current round
			final int nQuestions = trivia.getNQuestions(rNumber);
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
				if (( beenOpens[q] || !this.valueLabels[q].getText().equals(values[q] + "") )) {
					if (beenOpens[q] && !opens[q]) {
						qUpdated[q] = !( this.speed == newSpeed && this.valueLabels[q].getText().equals(values[q] + "")
								&& this.earnedLabels[q].getText().equals(earneds[q] + "")
								&& this.questionTextAreas[q].getText().equals(questions[q])
								&& this.answerTextAreas[q].getText().equals(answers[q])
								&& this.submitterTextAreas[q].getText().equals(submitters[q]) && this.operatorTextAreas[q]
								.getText().equals(operators[q]) );
					} else {
						qUpdated[q] = !( this.speed == newSpeed && this.valueLabels[q].getText().equals(values[q] + "")
								&& this.earnedLabels[q].getText().equals("")
								&& this.questionTextAreas[q].getText().equals(questions[q])
								&& this.answerTextAreas[q].getText().equals(answers[q])
								&& this.submitterTextAreas[q].getText().equals(submitters[q]) && this.operatorTextAreas[q]
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
						this.questionTextAreas[q].setText(questions[q]);
						// this.questionTextAreas[q].setToolTipText(questions[q]);
					} else {
						// Hide values for questions that haven't been asked yet
						this.valueLabels[q].setText("");
						this.questionTextAreas[q].setText("");
						// this.questionTextAreas[q].setToolTipText("");
					}
					if (corrects[q] || ( beenOpens[q] && !opens[q] )) {
						// Only show answers and earned points if the question is correct or closed
						this.earnedLabels[q].setText(earneds[q] + "");
						this.answerTextAreas[q].setText(answers[q]);
						// this.answerTextAreas[q].setToolTipText(answers[q]);
						this.submitterTextAreas[q].setText(submitters[q]);
						this.operatorTextAreas[q].setText(operators[q]);
					} else {
						// Hide answer data for questions that haven't been closed
						this.earnedLabels[q].setText("");
						this.answerTextAreas[q].setText("");
						// this.answerTextAreas[q].setToolTipText("");
						this.submitterTextAreas[q].setText("");
						this.operatorTextAreas[q].setText("");
					}

					// Make sure questions are shown
					this.qNumberLabels[q].getParent().setVisible(true);
					this.earnedLabels[q].getParent().setVisible(true);
					this.valueLabels[q].getParent().setVisible(true);
					this.questionTextAreas[q].setVisible(true);
					this.separators[q].setVisible(true);
					this.answerTextAreas[q].setVisible(true);
					this.questionTextAreas[q].getParent().getParent().setVisible(true);
					this.answerTextAreas[q].getParent().getParent().setVisible(true);
					this.submitterTextAreas[q].setVisible(true);
					this.operatorTextAreas[q].setVisible(true);
				}
			}

			// Hide rows for speed questions in non-speed rounds
			for (int q = nQuestions; q < this.maxQuestions; q++) {
				this.qNumberLabels[q].getParent().setVisible(false);
				this.earnedLabels[q].getParent().setVisible(false);
				this.valueLabels[q].getParent().setVisible(false);
				this.questionTextAreas[q].setVisible(false);
				this.separators[q].setVisible(false);
				this.answerTextAreas[q].setVisible(false);
				this.questionTextAreas[q].getParent().getParent().setVisible(false);
				this.answerTextAreas[q].getParent().getParent().setVisible(false);
				this.submitterTextAreas[q].setVisible(false);
				this.operatorTextAreas[q].setVisible(false);
			}
		}

		// @Override
		// public void windowGainedFocus(WindowEvent e) {
		// }
		//
		// @Override
		// public void windowLostFocus(WindowEvent e) {
		// this.contextMenu.setVisible(false);
		// }
	}

}