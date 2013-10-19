package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.TriviaInterface;

/**
 * A panel which displays a list of the current open questions.
 * 
 * @author Walter Kolczynski
 * 
 */
public class WorkflowQlistPanel extends TriviaPanel {

	/** The Constant serialVersionUID. */
	private static final long			serialVersionUID				= 6049067322505905668L;

	/**
	 * Colors
	 */
	private static final Color			HEADER_TEXT_COLOR				= Color.WHITE;
	private static final Color			HEADER_BACKGROUND_COLOR			= Color.DARK_GRAY;
	private static final Color			ODD_QUESTION_TEXT_COLOR			= Color.BLACK;
	private static final Color			EVEN_QUESTION_TEXT_COLOR		= Color.BLACK;
	private static final Color			ODD_QUESTION_BACKGROUND_COLOR	= Color.WHITE;
	private static final Color			EVEN_QUESTION_BACKGROUND_COLOR	= Color.LIGHT_GRAY;

	/**
	 * Sizes
	 */
	private static final int			HEADER_HEIGHT					= 16;
	private static final int			QUESTION_HEIGHT					= 46;

	private static final int			QNUM_WIDTH						= 48;
	private static final int			QUESTION_WIDTH					= 50;
	private static final int			VALUE_WIDTH						= 75;
	private static final int			ANSWER_WIDTH					= 72;
	private static final int			CLOSE_WIDTH						= 72;

	/**
	 * Button sizes
	 */
	private static final int			ANSWER_BUTTON_HEIGHT			= 32;
	private static final int			ANSWER_BUTTON_WIDTH				= 64;
	private static final int			CLOSE_BUTTON_HEIGHT				= 32;
	private static final int			CLOSE_BUTTON_WIDTH				= 64;

	/**
	 * Font sizes
	 */
	private static final float			HEADER_FONT_SIZE				= (float) 12.0;
	private static final float			QNUM_FONT_SIZE					= (float) 32.0;
	private static final float			VALUE_FONT_SIZE					= (float) 32.0;
	private static final float			QUESTION_FONT_SIZE				= (float) 12.0;

	/** The number of open questions to show at one time */
	private static final int			DEFAULT_QUESTIONS_SHOW			= 4;

	/** Sub-panel that will hold the open questions */
	private final WorkflowQListSubPanel	workflowQListSubPanel;

	/**
	 * Instantiates a new workflow question list panel.
	 * 
	 * @param server
	 *            The remote trivia server
	 * @param client
	 *            The local trivia client
	 */
	public WorkflowQlistPanel(TriviaInterface server, TriviaClient client) {

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
		this.enclosedLabel("Q#", QNUM_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints,
				HEADER_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);

		constraints.gridx = 1;
		constraints.gridy = 0;
		this.enclosedLabel("Value", VALUE_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR,
				constraints, HEADER_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);

		constraints.gridx = 2;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		this.enclosedLabel("Question", QUESTION_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR,
				constraints, HEADER_FONT_SIZE, SwingConstants.LEFT, SwingConstants.CENTER);
		constraints.weightx = 0.0;

		constraints.gridx = 3;
		constraints.gridy = 0;
		this.enclosedLabel("", ANSWER_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints,
				HEADER_FONT_SIZE, SwingConstants.LEFT, SwingConstants.CENTER);

		constraints.gridx = 4;
		constraints.gridy = 0;
		;
		this.enclosedLabel("", CLOSE_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints,
				HEADER_FONT_SIZE, SwingConstants.LEFT, SwingConstants.CENTER);

		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 5;
		constraints.weighty = 1.0;

		/**
		 * Create the subpanel that will hold the actual questions and put it in a scroll pane
		 */
		this.workflowQListSubPanel = new WorkflowQListSubPanel(server, client);
		final JScrollPane scrollPane = new JScrollPane(this.workflowQListSubPanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(0, DEFAULT_QUESTIONS_SHOW * QUESTION_HEIGHT + 3));
		scrollPane.setMinimumSize(new Dimension(0, QUESTION_HEIGHT + 3));
		this.add(scrollPane, constraints);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void update(boolean force) {
		this.workflowQListSubPanel.update(force);
	}

	/**
	 * Panel which displays a list of the current open questions.
	 */
	private class WorkflowQListSubPanel extends TriviaPanel implements ActionListener, MouseListener, ChangeListener {

		/** The Constant serialVersionUID. */
		private static final long		serialVersionUID	= 6049067322505905668L;

		// Maximum number of questions in a round
		private final int				nQuestionsMax;

		/**
		 * GUI Elements that will need to be updated
		 */
		private final JLabel[]			qNumberLabels, qValueLabels;
		private final JTextArea[]		qTextAreas;
		private final JButton[]			answerButtons, closeButtons;

		private final JMenuItem			editItem;

		/**
		 * Data sources
		 */
		private final TriviaInterface	server;
		private final TriviaClient		client;

		/**
		 * Instantiates a new workflow q list sub panel.
		 * 
		 * @param server
		 *            the server
		 * @param client
		 *            the client
		 */
		public WorkflowQListSubPanel(TriviaInterface server, TriviaClient client) {

			super();

			this.server = server;
			this.client = client;
			this.nQuestionsMax = client.getTrivia().getMaxQuestions();

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
				Color color, bColor;
				if (q % 2 == 1) {
					color = ODD_QUESTION_TEXT_COLOR;
					bColor = ODD_QUESTION_BACKGROUND_COLOR;
				} else {
					color = EVEN_QUESTION_TEXT_COLOR;
					bColor = EVEN_QUESTION_BACKGROUND_COLOR;
				}
				constraints.gridx = 0;
				constraints.gridy = q;
				this.qNumberLabels[q] = this.enclosedLabel("", QNUM_WIDTH, QUESTION_HEIGHT, color, bColor, constraints,
						QNUM_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
				this.qNumberLabels[q].addMouseListener(this);

				constraints.gridx = 1;
				constraints.gridy = q;
				this.qValueLabels[q] = this.enclosedLabel("", VALUE_WIDTH, QUESTION_HEIGHT, color, bColor, constraints,
						VALUE_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
				this.qValueLabels[q].addMouseListener(this);

				constraints.gridx = 2;
				constraints.gridy = q;
				constraints.weightx = 1.0;
				this.qTextAreas[q] = this.scrollableTextArea("", QUESTION_WIDTH, QUESTION_HEIGHT, color, bColor,
						constraints, QUESTION_FONT_SIZE, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
						ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				this.qTextAreas[q].setEditable(false);
				this.qTextAreas[q].addMouseListener(this);
				constraints.weightx = 0.0;

				constraints.gridx = 3;
				constraints.gridy = q;
				JPanel panel = new JPanel(new GridBagLayout());
				panel.setPreferredSize(new Dimension(ANSWER_WIDTH, QUESTION_HEIGHT));
				panel.setMinimumSize(new Dimension(ANSWER_WIDTH, QUESTION_HEIGHT));
				panel.setBackground(bColor);
				this.add(panel, constraints);
				this.answerButtons[q] = new JButton("");
				this.answerButtons[q].setMargin(new Insets(0, 0, 0, 0));
				this.answerButtons[q].setPreferredSize(new Dimension(ANSWER_BUTTON_WIDTH, ANSWER_BUTTON_HEIGHT));
				this.answerButtons[q].setMinimumSize(new Dimension(ANSWER_BUTTON_WIDTH, ANSWER_BUTTON_HEIGHT));
				panel.add(this.answerButtons[q], buttonConstraints);
				this.answerButtons[q].addActionListener(this);

				constraints.gridx = 4;
				constraints.gridy = q;
				panel = new JPanel(new GridBagLayout());
				panel.setPreferredSize(new Dimension(CLOSE_WIDTH, QUESTION_HEIGHT));
				panel.setMinimumSize(new Dimension(CLOSE_WIDTH, QUESTION_HEIGHT));
				panel.setBackground(bColor);
				this.add(panel, constraints);
				this.closeButtons[q] = new JButton("Open");
				this.closeButtons[q].setMargin(new Insets(0, 0, 0, 0));
				this.closeButtons[q].setPreferredSize(new Dimension(CLOSE_BUTTON_WIDTH, CLOSE_BUTTON_HEIGHT));
				this.closeButtons[q].setMinimumSize(new Dimension(CLOSE_BUTTON_WIDTH, CLOSE_BUTTON_HEIGHT));
				panel.add(this.closeButtons[q], buttonConstraints);
				this.closeButtons[q].addActionListener(this);

				if (q > DEFAULT_QUESTIONS_SHOW) {
					this.qNumberLabels[q].getParent().setVisible(false);
					this.qValueLabels[q].getParent().setVisible(false);
					this.qTextAreas[q].getParent().setVisible(false);
					this.qTextAreas[q].getParent().getParent().setVisible(false);
					this.answerButtons[q].getParent().setVisible(false);
					this.closeButtons[q].getParent().setVisible(false);
				}
			}

			/**
			 * Create a blank spacer row at the bottom
			 */
			constraints.gridx = 0;
			constraints.gridy = this.nQuestionsMax;
			constraints.gridwidth = 5;
			constraints.weightx = 1.0;
			constraints.weighty = 1.0;
			final JPanel blank = new JPanel();
			blank.setBackground(HEADER_BACKGROUND_COLOR);
			blank.setPreferredSize(new Dimension(0, 0));
			this.add(blank, constraints);

			/**
			 * Build context menu
			 */
			final JPopupMenu contextMenu = new JPopupMenu();
			this.editItem = new JMenuItem("Edit");
			this.editItem.addMouseListener(this);
			contextMenu.add(this.editItem);
			this.add(contextMenu);

			this.client.getBook().addChangeListener(this);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public synchronized void actionPerformed(ActionEvent event) {
			final JButton source = (JButton) event.getSource();
			for (int q = 0; q < this.nQuestionsMax; q++) {
				if (this.answerButtons[q].equals(event.getSource())) {
					// Answer button q was pressed
					this.answerQuestion(Integer.parseInt(this.qNumberLabels[q].getText()));
					return;
				}
			}
			for (int q = 0; q < this.nQuestionsMax; q++) {
				if (this.closeButtons[q].equals(event.getSource())) {
					if (source.getText() == "Close") {
						// Close button q was pressed
						final int qNumber = Integer.parseInt(this.qNumberLabels[q].getText());
						new CloseQuestionDialog(this.server, this.client, qNumber);
						// this.close(qNumber);
					} else {
						// Open button was pressed
						this.open();
					}
					return;
				}
			}

		}

		@Override
		public void mouseClicked(MouseEvent event) {
			final JComponent source = (JComponent) event.getSource();
			final String sourceName = source.getName();
			if (source.equals(this.editItem)) {
				// Edit chosen from context menu
				this.editItem.getParent().setVisible(false);
				final Trivia trivia = this.client.getTrivia();
				final int rNumber = trivia.getCurrentRoundNumber();
				final int qNumber = Integer.parseInt(sourceName);
				final int nQuestions = trivia.getNQuestions();
				final int qValue = trivia.getValue(rNumber, qNumber);
				final String qText = trivia.getQuestionText(rNumber, qNumber);
				new OpenQuestionDialog(this.server, this.client, nQuestions, qNumber, qValue, qText);
			} else {
				// Right-click pressed, show context menu
				if (event.getButton() == 3 && !sourceName.equals("")) {
					this.editItem.getParent().setLocation(event.getXOnScreen(), event.getYOnScreen());
					this.editItem.setName(source.getName());
					this.editItem.getParent().setVisible(true);
				} else {
					this.editItem.getParent().setVisible(false);
				}
			}

		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void stateChanged(ChangeEvent event) {
			// Make sure the menu is hidden when changing tabs
			this.editItem.getParent().setVisible(false);
		}

		/**
		 * Propose an answer for the designated question. Creates a prompt to submit the answer.
		 * 
		 * @param qNumber
		 *            the question number
		 */
		private void answerQuestion(int qNumber) {
			new AnswerEntryPanel(this.server, this.client, qNumber, this.client.getUser());
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
				new OpenQuestionDialog(this.server, this.client, nQuestions, nextToOpen, trivia.getValue(rNumber,
						nextToOpen - 1));
			} else {
				new OpenQuestionDialog(this.server, this.client, nQuestions, nextToOpen);
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
					this.qTextAreas[q].setCaretPosition(0);
					this.answerButtons[q].setText("Answer");
					this.answerButtons[q].setVisible(true);
					this.closeButtons[q].setText("Close");
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
				this.answerButtons[q].setText("");
				this.answerButtons[q].setVisible(false);
				this.closeButtons[q].setText("Open");
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
				this.qTextAreas[q].getParent().setVisible(false);
				this.qTextAreas[q].getParent().getParent().setVisible(false);
				this.answerButtons[q].getParent().setVisible(false);
				this.closeButtons[q].getParent().setVisible(false);
			}

		}

	}

}
