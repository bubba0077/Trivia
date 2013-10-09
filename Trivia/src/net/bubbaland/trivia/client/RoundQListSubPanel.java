package net.bubbaland.trivia.client;

import java.awt.*;
import javax.swing.*;

import net.bubbaland.trivia.Trivia;

// TODO: Auto-generated Javadoc
/**
 * The Class RoundQListSubPanel.
 */
public class RoundQListSubPanel extends TriviaPanel {

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID				= 4030677705507400609L;

	/**
	 * Font sizes
	 */
	private static final float	LARGE_FONT_SIZE					= (float)36.0;
	private static final float	SMALL_FONT_SIZE					= (float)12.0;

	/**
	 * Colors
	 */
	private static final Color	ODD_QUESTION_TEXT_COLOR			= Color.black;
	private static final Color	EVEN_QUESTION_TEXT_COLOR		= Color.black;
	private static final Color	ODD_QUESTION_BACKGROUND_COLOR	= Color.white;
	private static final Color	EVEN_QUESTION_BACKGROUND_COLOR	= Color.lightGray;

	/**
	 * Sizes (most are taken from the parent panel)
	 */
	private static final int	QUESTION_HEIGHT					= 50;
	
	private static final int	QNUM_WIDTH						= RoundQlistPanel.QNUM_WIDTH;
	private static final int	EARNED_WIDTH					= RoundQlistPanel.EARNED_WIDTH;
	private static final int	VALUE_WIDTH						= RoundQlistPanel.VALUE_WIDTH;
	private static final int	QUESTION_WIDTH					= RoundQlistPanel.QUESTION_WIDTH;
	private static final int	ANSWER_WIDTH					= RoundQlistPanel.ANSWER_WIDTH;
	private static final int	SUBOP_WIDTH						= RoundQlistPanel.SUBOP_WIDTH;

	/**
	 * GUI Elements that will need to be updated
	 */
	private final JLabel[]			qNumberLabels, earnedLabels, valueLabels, submitterLabels, operatorLabels;
	private final JTextArea[]			questionTextAreas, answerTextAreas;
	
	/** Status variables */
	private boolean				speed, live;
	private int					maxQuestions, rNumber;
	
	/** Data source */
	private final TriviaClient		client;
	
	/**
	 * Instantiates a new question list sub-panel.
	 *
	 * @param server the server
	 * @param client the client application
	 * @param live whether this panel should always show the current round 
	 * @param rNumber the round number
	 */
	public RoundQListSubPanel( TriviaClient client, boolean live, int rNumber ) {
		super( new GridBagLayout() );

		this.client = client;
		this.speed = false;
		this.live = live;
		this.rNumber = rNumber;

		this.maxQuestions = client.getTrivia().getMaxQuestions();

		this.qNumberLabels = new JLabel[maxQuestions];
		this.earnedLabels = new JLabel[maxQuestions];
		this.valueLabels = new JLabel[maxQuestions];
		this.submitterLabels = new JLabel[maxQuestions];
		this.operatorLabels = new JLabel[maxQuestions];
		this.questionTextAreas = new JTextArea[maxQuestions];
		this.answerTextAreas = new JTextArea[maxQuestions];
		
		// Set up layout constraints		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;

		for ( int q = 0; q < maxQuestions; q++ ) {
			// Set the color for this row
			Color color, bColor;			
			if ( q % 2 == 1 ) {
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
			this.qNumberLabels[q] = enclosedLabel(( q + 1 ) + "", QNUM_WIDTH, QUESTION_HEIGHT, color, bColor, constraints, LARGE_FONT_SIZE, JLabel.CENTER, JLabel.CENTER);

			constraints.gridx = 1;
			constraints.gridy = 2 * q;
			this.earnedLabels[q] = enclosedLabel("", EARNED_WIDTH, QUESTION_HEIGHT, color, bColor, constraints, LARGE_FONT_SIZE, JLabel.CENTER, JLabel.CENTER);

			constraints.gridx = 2;
			constraints.gridy = 2 * q;
			this.valueLabels[q] = enclosedLabel("", VALUE_WIDTH, QUESTION_HEIGHT, color, bColor, constraints, LARGE_FONT_SIZE, JLabel.CENTER, JLabel.CENTER);

			constraints.gridx = 3;
			constraints.gridy = 2 * q;
			constraints.weightx = 0.6;
			this.questionTextAreas[q] = scrollableTextArea("", QUESTION_WIDTH, QUESTION_HEIGHT, color, bColor, constraints, 
					SMALL_FONT_SIZE, Component.LEFT_ALIGNMENT, Component.TOP_ALIGNMENT, 
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			this.questionTextAreas[q].setEditable( false );
			
			constraints.weightx = 0.0;
			constraints.weighty = 0.0;

			constraints.gridx = 4;
			constraints.gridy = 2 * q;
			constraints.weightx = 0.4;			
			this.answerTextAreas[q] = scrollableTextArea("", ANSWER_WIDTH, QUESTION_HEIGHT, color, bColor, constraints, 
					SMALL_FONT_SIZE, Component.LEFT_ALIGNMENT, Component.TOP_ALIGNMENT, 
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			this.answerTextAreas[q].setEditable( false );			
			constraints.weightx = 0.0;
			constraints.weighty = 0.0;

			constraints.gridheight = 1;

			constraints.gridx = 5;
			constraints.gridy = 2 * q;
			this.submitterLabels[q] = enclosedLabel("", SUBOP_WIDTH, QUESTION_HEIGHT/2, color, bColor, constraints, SMALL_FONT_SIZE, JLabel.CENTER, JLabel.CENTER);

			constraints.gridx = 5;
			constraints.gridy = 2 * q + 1;
			this.operatorLabels[q] = enclosedLabel("", SUBOP_WIDTH, QUESTION_HEIGHT/2, color, bColor, constraints, SMALL_FONT_SIZE, JLabel.CENTER, JLabel.CENTER);

		}

		/**
		 * Extra row at the bottom to soak up any extra space
		 */
		constraints.gridx = 0;
		constraints.gridy = this.maxQuestions;
		constraints.gridwidth = 6;
		constraints.weighty = 1.0;
		final JPanel blank = new JPanel();
		blank.setBackground( HeaderPanel.BACKGROUND_COLOR_NORMAL );
		blank.setPreferredSize( new Dimension(0,0) );
		this.add( blank, constraints );
	}

	/**
	 * Instantiates a new question list sub-panel that will show data for the current round.
	 *
	 * @param server the server
	 * @param client the client
	 */
	public RoundQListSubPanel( TriviaClient client ) {
		this( client, true, 0);
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void update(){

		final Trivia trivia = client.getTrivia();
		int currentRound = 0;
		
		if(this.live) {
			currentRound = trivia.getRoundNumber();
		} else {
			currentRound = this.rNumber;
		}
		final int nQuestions = trivia.getNQuestions();
		final boolean newSpeed = trivia.isSpeed(currentRound);
		final boolean[] beenOpens = trivia.eachBeenOpen( currentRound );
		final boolean[] opens = trivia.eachOpen( currentRound );
		final boolean[] corrects = trivia.eachCorrect( currentRound );
		final int[] earneds = trivia.getEachEarned( currentRound );
		final int[] values = trivia.getEachValue( currentRound );
		final String[] questions = trivia.getEachQuestionText( currentRound );
		final String[] answers = trivia.getEachAnswerText( currentRound );
		final String[] submitters = trivia.getEachSubmitter( currentRound );
		final String[] operators = trivia.getEachOperator( currentRound );

		// Determine which questions have been updated
		boolean[] qUpdated = new boolean[nQuestions];
		for ( int q = 0; q < nQuestions; q++ ) {
			if ( beenOpens[q] ) {
				if ( opens[q] ) {
					qUpdated[q] = !( this.speed == newSpeed && this.valueLabels[q].getText().equals( values[q] + "" ) && this.questionTextAreas[q]
							.getText().equals( questions[q] ) );

				} else {
					qUpdated[q] = !( this.speed == newSpeed && this.valueLabels[q].getText().equals( values[q] + "" )
							&& this.earnedLabels[q].getText().equals( earneds[q] + "" )
							&& this.questionTextAreas[q].getText().equals( questions[q] )
							&& this.answerTextAreas[q].getText().equals( answers[q] )
							&& this.submitterLabels[q].getText().equals( submitters[q] ) && this.operatorLabels[q]
							.getText().equals( operators[q] ) );
				}
			} else {
				qUpdated[q] = this.speed == newSpeed;
			}
		}
				
		for ( int q = 0; q < nQuestions; q++ ) {
			if ( qUpdated[q] ) {
				this.speed = newSpeed;
				if ( beenOpens[q] ) {
					// Only show questions that have been asked
					valueLabels[q].setText( values[q] + "" );
					questionTextAreas[q].setText( questions[q] );
					questionTextAreas[q].setCaretPosition( 0 );
				} else {
					// Hide questions that haven't been asked yet
					valueLabels[q].setText( "" );
					questionTextAreas[q].setText( "" );
				}
				if ( corrects[q] || ( beenOpens[q] && !opens[q] ) ) {
					// Only show answers and earned points if the question is correct or closed
					earnedLabels[q].setText( earneds[q] + "" );
					answerTextAreas[q].setText( answers[q] );
					answerTextAreas[q].setCaretPosition( 0 );
					submitterLabels[q].setText( submitters[q] );
					operatorLabels[q].setText( operators[q] );
				} else {
					// Hide answer data for questions that haven't been closed
					earnedLabels[q].setText( "" );
					answerTextAreas[q].setText( "" );
					submitterLabels[q].setText( "" );
					operatorLabels[q].setText( "" );
				}

				qNumberLabels[q].getParent().setVisible( true );
				earnedLabels[q].getParent().setVisible( true );
				valueLabels[q].getParent().setVisible( true );
				questionTextAreas[q].setVisible( true );
				answerTextAreas[q].setVisible( true );
				questionTextAreas[q].getParent().getParent().setVisible( true );
				answerTextAreas[q].getParent().getParent().setVisible( true );
				submitterLabels[q].getParent().setVisible( true );
				operatorLabels[q].getParent().setVisible( true );
			}
		}

		// Hide rows for speed questions in non-speed rounds
		for ( int q = nQuestions; q < this.maxQuestions; q++ ) {
			qNumberLabels[q].getParent().setVisible( false );
			earnedLabels[q].getParent().setVisible( false );
			valueLabels[q].getParent().setVisible( false );
			questionTextAreas[q].setVisible( false );
			answerTextAreas[q].setVisible( false );
			questionTextAreas[q].getParent().getParent().setVisible( false );
			answerTextAreas[q].getParent().getParent().setVisible( false );
			submitterLabels[q].getParent().setVisible( false );
			operatorLabels[q].getParent().setVisible( false );
		}

	}

	/**
	 * Sets the round to display. This will be overridden with the current round number if this is a "live" panel.
	 *
	 * @param rNumber the new round
	 */
	public void setRound(int rNumber) {
		this.rNumber = rNumber;		
	}

}
