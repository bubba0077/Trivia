package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.rmi.RemoteException;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.TriviaInterface;

/**
 * A panel that shows all of the questions for a round.
 * 
 * <code>RoundQlistPanel</code> is a panel that contains a list of all the question data for a round. The panel consists of a header row and then a sub-panel placed inside a scroll pane. The sub-panel is created via an inner class. Each row of the sub-panel contains GUI elements displaying data for one question. Unused rows (for a speed round) are created but hidden until necessary.
 * 
 * @author Walter Kolczynski
 * 
 */
public class RoundQlistPanel extends TriviaPanel {

	/**
	 * A panel that displays the question data for a round.
	 */
	private class RoundQListSubPanel extends TriviaPanel implements MouseListener, ChangeListener {

		private static final long	serialVersionUID	= 3825357215129662133L;

		/**
		 * GUI Elements that will need to be updated
		 */
		private final JLabel[]		qNumberLabels, earnedLabels, valueLabels, submitterLabels, operatorLabels;
		private final JTextArea[]	questionTextAreas, answerTextAreas;
		private final JMenuItem		editItem, reopenItem;

		/** Status variables */
		private boolean				speed;

		private final boolean		live;
		private final int			maxQuestions;

		private int					rNumber;

		/** Data source */
		private final TriviaInterface server;
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
		public RoundQListSubPanel(TriviaInterface server, TriviaClient client, boolean live, int rNumber) {
			super();

			this.server = server;
			this.client = client;
			this.speed = false;
			this.live = live;
			this.rNumber = rNumber;

			this.maxQuestions = client.getTrivia().getMaxQuestions();

			this.qNumberLabels = new JLabel[this.maxQuestions];
			this.earnedLabels = new JLabel[this.maxQuestions];
			this.valueLabels = new JLabel[this.maxQuestions];
			this.submitterLabels = new JLabel[this.maxQuestions];
			this.operatorLabels = new JLabel[this.maxQuestions];
			this.questionTextAreas = new JTextArea[this.maxQuestions];
			this.answerTextAreas = new JTextArea[this.maxQuestions];

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
				this.qNumberLabels[q].setName((q+1)+"");
				this.qNumberLabels[q].addMouseListener(this);

				constraints.gridx = 1;
				constraints.gridy = 2 * q;
				this.earnedLabels[q] = this.enclosedLabel("", EARNED_WIDTH, QUESTION_HEIGHT, color, bColor,
						constraints, LARGE_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
				this.earnedLabels[q].setName((q+1)+"");
				this.earnedLabels[q].addMouseListener(this);

				constraints.gridx = 2;
				constraints.gridy = 2 * q;
				this.valueLabels[q] = this.enclosedLabel("", VALUE_WIDTH, QUESTION_HEIGHT, color, bColor, constraints,
						LARGE_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
				this.valueLabels[q].setName((q+1)+"");
				this.valueLabels[q].addMouseListener(this);

				constraints.gridx = 3;
				constraints.gridy = 2 * q;
				constraints.weightx = 0.6;
				this.questionTextAreas[q] = this.scrollableTextArea("", QUESTION_WIDTH, QUESTION_HEIGHT, color, bColor,
						constraints, SMALL_FONT_SIZE, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				this.questionTextAreas[q].setEditable(false);
				this.questionTextAreas[q].setName((q+1)+"");
				this.questionTextAreas[q].addMouseListener(this);

				constraints.weightx = 0.0;
				constraints.weighty = 0.0;

				constraints.gridx = 4;
				constraints.gridy = 2 * q;
				constraints.weightx = 0.4;
				this.answerTextAreas[q] = this.scrollableTextArea("", ANSWER_WIDTH, QUESTION_HEIGHT, color, bColor,
						constraints, SMALL_FONT_SIZE, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				this.answerTextAreas[q].setEditable(false);
				this.answerTextAreas[q].setName((q+1)+"");
				this.answerTextAreas[q].addMouseListener(this);
				constraints.weightx = 0.0;
				constraints.weighty = 0.0;
				
				constraints.gridheight = 1;

				constraints.gridx = 5;
				constraints.gridy = 2 * q;
				this.submitterLabels[q] = this.enclosedLabel("", SUBOP_WIDTH, QUESTION_HEIGHT / 2, color, bColor,
						constraints, SMALL_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
				this.submitterLabels[q].setName((q+1)+"");
				this.submitterLabels[q].addMouseListener(this);

				constraints.gridx = 5;
				constraints.gridy = 2 * q + 1;
				this.operatorLabels[q] = this.enclosedLabel("", SUBOP_WIDTH, QUESTION_HEIGHT / 2, color, bColor,
						constraints, SMALL_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
				this.operatorLabels[q].setName((q+1)+"");
				this.operatorLabels[q].addMouseListener(this);

			}

			/**
			 * Extra row at the bottom to soak up any extra space
			 */
			constraints.gridx = 0;
			constraints.gridy = this.maxQuestions;
			constraints.gridwidth = 6;
			constraints.weighty = 1.0;
			final JPanel blank = new JPanel();
			blank.setBackground(HeaderPanel.BACKGROUND_COLOR);
			blank.setPreferredSize(new Dimension(0, 0));
			this.add(blank, constraints);
			
			/**
			 * Build context menu
			 */
			JPopupMenu contextMenu = new JPopupMenu();
			editItem = new JMenuItem("Edit");
			editItem.addMouseListener(this);
			contextMenu.add(editItem);
			this.add(contextMenu);			
			
			if(live) {
				reopenItem = new JMenuItem("Reopen");
				reopenItem.addMouseListener(this);
				contextMenu.add(reopenItem);
				this.add(contextMenu);
			} else {
				reopenItem = null;
			}
			
			this.client.getBook().addChangeListener(this);
			
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

			final Trivia trivia = this.client.getTrivia();
			if (this.live) {
				this.rNumber = trivia.getCurrentRoundNumber();
			}
			final int nQuestions = trivia.getNQuestions();
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
				if (beenOpens[q]) {
					if (opens[q]) {
						qUpdated[q] = !( this.speed == newSpeed && this.valueLabels[q].getText().equals(values[q] + "") && this.questionTextAreas[q]
								.getText().equals(questions[q]) && this.earnedLabels[q].getText().equals("") );

					} else {
						qUpdated[q] = !( this.speed == newSpeed && this.valueLabels[q].getText().equals(values[q] + "")
								&& this.earnedLabels[q].getText().equals(earneds[q] + "")
								&& this.questionTextAreas[q].getText().equals(questions[q])
								&& this.answerTextAreas[q].getText().equals(answers[q])
								&& this.submitterLabels[q].getText().equals(submitters[q]) && this.operatorLabels[q]
								.getText().equals(operators[q]) );
					}
				} else {
					qUpdated[q] = this.speed == newSpeed;
				}
			}

			for (int q = 0; q < nQuestions; q++) {
				if (qUpdated[q] || force) {
					this.speed = newSpeed;
					if (beenOpens[q]) {
						// Only show questions that have been asked
						this.valueLabels[q].setText(values[q] + "");
						this.questionTextAreas[q].setText(questions[q]);
						this.questionTextAreas[q].setCaretPosition(0);
					} else {
						// Hide questions that haven't been asked yet
						this.valueLabels[q].setText("");
						this.questionTextAreas[q].setText("");
					}
					if (corrects[q] || ( beenOpens[q] && !opens[q] )) {
						// Only show answers and earned points if the question is correct or closed
						this.earnedLabels[q].setText(earneds[q] + "");
						this.answerTextAreas[q].setText(answers[q]);
						this.answerTextAreas[q].setCaretPosition(0);
						this.submitterLabels[q].setText(submitters[q]);
						this.operatorLabels[q].setText(operators[q]);
					} else {
						// Hide answer data for questions that haven't been closed
						this.earnedLabels[q].setText("");
						this.answerTextAreas[q].setText("");
						this.submitterLabels[q].setText("");
						this.operatorLabels[q].setText("");
					}

					this.qNumberLabels[q].getParent().setVisible(true);
					this.earnedLabels[q].getParent().setVisible(true);
					this.valueLabels[q].getParent().setVisible(true);
					this.questionTextAreas[q].setVisible(true);
					this.answerTextAreas[q].setVisible(true);
					this.questionTextAreas[q].getParent().getParent().setVisible(true);
					this.answerTextAreas[q].getParent().getParent().setVisible(true);
					this.submitterLabels[q].getParent().setVisible(true);
					this.operatorLabels[q].getParent().setVisible(true);
				}
			}

			// Hide rows for speed questions in non-speed rounds
			for (int q = nQuestions; q < this.maxQuestions; q++) {
				this.qNumberLabels[q].getParent().setVisible(false);
				this.earnedLabels[q].getParent().setVisible(false);
				this.valueLabels[q].getParent().setVisible(false);
				this.questionTextAreas[q].setVisible(false);
				this.answerTextAreas[q].setVisible(false);
				this.questionTextAreas[q].getParent().getParent().setVisible(false);
				this.answerTextAreas[q].getParent().getParent().setVisible(false);
				this.submitterLabels[q].getParent().setVisible(false);
				this.operatorLabels[q].getParent().setVisible(false);
			}

		}

		@Override
		public void mouseClicked(MouseEvent event) {
			final JComponent source = (JComponent) event.getSource();
			final Trivia trivia = client.getTrivia();
			final int qNumber = Integer.parseInt(source.getName());
			if (this.live) {	this.rNumber = trivia.getCurrentRoundNumber();	}			
			if(source.equals(editItem)) {
				editItem.getParent().setVisible(false);
				new EditQuestionDialog(this.server, this.client, this.rNumber, qNumber);				
			} else if (source.equals(reopenItem)) {
				editItem.getParent().setVisible(false);
				
				// Repen the question on the server
				int tryNumber = 0;
				boolean success = false;
				while (tryNumber < TriviaClient.MAX_RETRIES && success == false) {
					tryNumber++;
					try {
						server.open(client.getUser(), qNumber, trivia.getValue(rNumber, qNumber), trivia.getQuestionText(rNumber, qNumber));
						success = true;
					} catch (final RemoteException e) {
						client.log("Couldn't reopen question on server (try #" + tryNumber + ").");
					}
				}

				if (!success) {
					client.disconnected();
					return;
				}

				client.log("Question #" + qNumber + " reopened.");								
				
			} else {
				if(event.getButton() == 3 && trivia.beenOpen( this.rNumber, qNumber )) {
					editItem.getParent().setLocation(event.getXOnScreen(), event.getYOnScreen());
					editItem.setName(source.getName());
					if(live) {	reopenItem.setName(source.getName()); }
					editItem.getParent().setVisible(true);
				} else {
					editItem.getParent().setVisible(false);
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent event) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent event) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseEntered(MouseEvent event) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent event) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			editItem.getParent().setVisible(false);
			
		}

	}

	/** The Constant serialVersionUID. */
	private static final long			serialVersionUID				= 3589815467416864653L;
	/**
	 * Colors
	 */
	private static final Color			HEADER_TEXT_COLOR				= Color.white;
	private static final Color			HEADER_BACKGROUND_COLOR			= Color.darkGray;
	
	private static final Color			ODD_QUESTION_TEXT_COLOR			= Color.black;
	private static final Color			EVEN_QUESTION_TEXT_COLOR		= Color.black;
	private static final Color			ODD_QUESTION_BACKGROUND_COLOR	= Color.white;
	private static final Color			EVEN_QUESTION_BACKGROUND_COLOR	= Color.lightGray;
	
	/**
	 * Sizes
	 */
	private static final int			HEADER_HEIGHT					= 20;

	private static final int			QUESTION_HEIGHT					= 50;
	private static final int			QNUM_WIDTH						= 48;
	private static final int			EARNED_WIDTH					= 75;
	private static final int			VALUE_WIDTH						= 75;
	private static final int			QUESTION_WIDTH					= 200;
	private static final int			ANSWER_WIDTH					= 150;

	private static final int			SUBOP_WIDTH						= 100;
	
	/**
	 * Font sizes
	 */
	private static final float			FONT_SIZE						= (float) 12.0;
	private static final float			LARGE_FONT_SIZE					= (float) 36.0;

	private static final float			SMALL_FONT_SIZE					= (float) 12.0;

	/** The sub-panel holding the questions */
	private final RoundQListSubPanel	roundQlistSubPanel;

	/**
	 * Instantiates a new question list panel that will show data for the current round.
	 * 
	 * @param server
	 *            the server
	 * @param client
	 *            the client
	 */
	public RoundQlistPanel(TriviaInterface server, TriviaClient client) {
		this(server, client, true, 0);
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
	public RoundQlistPanel(TriviaInterface server, TriviaClient client, boolean live, int rNumber) {

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
		final int scrollBarWidth = ( (Integer) UIManager.get("ScrollBar.width") ).intValue();
		this.enclosedLabel("", scrollBarWidth, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints,
				FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);


		/**
		 * Create the question list sub-panel and place in a scroll pane
		 */
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 7;
		constraints.weighty = 1.0;
		this.roundQlistSubPanel = new RoundQListSubPanel(server, client, live, rNumber);
		final JScrollPane roundQlistPane = new JScrollPane(this.roundQlistSubPanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		roundQlistPane.setPreferredSize(new Dimension(0, 200));
		this.add(roundQlistPane, constraints);
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

}
