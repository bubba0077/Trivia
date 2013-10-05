package net.bubbaland.trivia;

import java.awt.*;
import java.rmi.RemoteException;

import javax.swing.*;

import net.bubbaland.trivia.server.TriviaInterface;

// TODO: Auto-generated Javadoc
/**
 * The Class RoundQListSubPanel.
 */
public class RoundQListSubPanel extends TriviaPanel {

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID				= 4030677705507400609L;

	/** The Constant ODD_QUESTION_TEXT_COLOR. */
	private static final Color	ODD_QUESTION_TEXT_COLOR			= Color.black;
	
	/** The Constant EVEN_QUESTION_TEXT_COLOR. */
	private static final Color	EVEN_QUESTION_TEXT_COLOR		= Color.black;
	
	/** The Constant ODD_QUESTION_BACKGROUND_COLOR. */
	private static final Color	ODD_QUESTION_BACKGROUND_COLOR	= Color.white;
	
	/** The Constant EVEN_QUESTION_BACKGROUND_COLOR. */
	private static final Color	EVEN_QUESTION_BACKGROUND_COLOR	= Color.lightGray;

	/** The Constant LARGE_FONT_SIZE. */
	private static final float	LARGE_FONT_SIZE					= (float)36.0;
	
	/** The Constant SMALL_FONT_SIZE. */
	private static final float	SMALL_FONT_SIZE					= (float)12.0;

	/** The Constant QUESTION_HEIGHT. */
	private static final int	QUESTION_HEIGHT					= 50;

	/** The Constant QNUM_WIDTH. */
	private static final int	QNUM_WIDTH						= RoundQlistPanel.QNUM_WIDTH;
	
	/** The Constant EARNED_WIDTH. */
	private static final int	EARNED_WIDTH					= RoundQlistPanel.EARNED_WIDTH;
	
	/** The Constant VALUE_WIDTH. */
	private static final int	VALUE_WIDTH						= RoundQlistPanel.VALUE_WIDTH;
	
	/** The Constant QUESTION_WIDTH. */
	private static final int	QUESTION_WIDTH					= RoundQlistPanel.QUESTION_WIDTH;
	
	/** The Constant ANSWER_WIDTH. */
	private static final int	ANSWER_WIDTH					= RoundQlistPanel.ANSWER_WIDTH;
	
	/** The Constant SUBOP_WIDTH. */
	private static final int	SUBOP_WIDTH						= RoundQlistPanel.SUBOP_WIDTH;

	/** The operator labels. */
	private JLabel[]			qNumberLabels, earnedLabels, valueLabels, submitterLabels, operatorLabels;
	
	/** The answer text areas. */
	private JTextArea[]			questionTextAreas, answerTextAreas;
	
	/** The live. */
	private boolean				speed, live;

	/** The r number. */
	private int					maxQuestions, rNumber;
	
	/** The server. */
	private TriviaInterface		server;
	
	/** The client. */
	private TriviaClient		client;
	
	/**
	 * Instantiates a new round q list sub panel.
	 *
	 * @param server the server
	 * @param client the client
	 * @param live the live
	 * @param rNumber the r number
	 */
	public RoundQListSubPanel( TriviaInterface server, TriviaClient client, boolean live, int rNumber ) {
		super( new GridBagLayout() );

		this.server = server;
		this.speed = false;
		this.live = live;
		this.rNumber = rNumber;

		this.maxQuestions = 0;
		int tryNumber = 0;
		boolean success = false;
		while ( tryNumber < TriviaClient.MAX_RETRIES && success == false ) {
			tryNumber++;
			try {
				this.maxQuestions = server.getMaxQuestions();
				success = true;
			} catch ( RemoteException e ) {
				client.log( "Couldn't retrive question data from server (try #" + tryNumber + ")." );
			}
		}

		if ( !success ) {
			client.disconnected();
			return;
		}

		this.qNumberLabels = new JLabel[maxQuestions];
		this.earnedLabels = new JLabel[maxQuestions];
		this.valueLabels = new JLabel[maxQuestions];
		this.submitterLabels = new JLabel[maxQuestions];
		this.operatorLabels = new JLabel[maxQuestions];
		this.questionTextAreas = new JTextArea[maxQuestions];
		this.answerTextAreas = new JTextArea[maxQuestions];

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;

		for ( int q = 0; q < maxQuestions; q++ ) {
			Color color, bColor;
			if ( q % 2 == 1 ) {
				color = ODD_QUESTION_TEXT_COLOR;
				bColor = ODD_QUESTION_BACKGROUND_COLOR;
			} else {
				color = EVEN_QUESTION_TEXT_COLOR;
				bColor = EVEN_QUESTION_BACKGROUND_COLOR;
			}

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

		constraints.gridx = 0;
		constraints.gridy = this.maxQuestions;
		constraints.gridwidth = 6;
		constraints.weighty = 1.0;
		JPanel blank = new JPanel();
		blank.setBackground( HeaderPanel.BACKGROUND_COLOR_NORMAL );
		blank.setPreferredSize( new Dimension(0,0) );
		this.add( blank, constraints );
	}

	/**
	 * Instantiates a new round q list sub panel.
	 *
	 * @param server the server
	 * @param client the client
	 */
	public RoundQListSubPanel( TriviaInterface server, TriviaClient client ) {

		this(server, client, true, 0);

	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void update(){

		int currentRound = 0;
		int nQuestions = 0;
		boolean newSpeed = false;
		boolean[] beenOpens = new boolean[maxQuestions];
		boolean[] opens = new boolean[maxQuestions];
		boolean[] corrects = new boolean[maxQuestions];
		int[] earneds = new int[maxQuestions];
		int[] values = new int[maxQuestions];
		String[] questions = new String[maxQuestions];
		String[] answers = new String[maxQuestions];
		String[] submitters = new String[maxQuestions];
		String[] operators = new String[maxQuestions];

		int tryNumber = 0;
		boolean success = false;
		while ( tryNumber < TriviaClient.MAX_RETRIES && success == false ) {
			tryNumber++;
			try {
				if(this.live) {
					currentRound = server.getRoundNumber();
				} else {
					currentRound = rNumber;
				}
				newSpeed = server.isSpeed(currentRound);
				nQuestions = server.getNQuestions();
				beenOpens = server.eachBeenOpen( currentRound );
				opens = server.eachOpen( currentRound );
				corrects = server.eachCorrect( currentRound );
				earneds = server.getEachEarned( currentRound );
				values = server.getEachValue( currentRound );
				questions = server.getEachQuestionText( currentRound );
				answers = server.getEachAnswerText( currentRound );
				submitters = server.getEachSubmitter( currentRound );
				operators = server.getEachOperator( currentRound );

				success = true;
			} catch ( RemoteException e ) {
				client.log( "Couldn't retrive question data from server (try #" + tryNumber + ")." );
			}
		}

		if ( !success ) {
			client.disconnected();
			return;
		}

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
					valueLabels[q].setText( values[q] + "" );
					questionTextAreas[q].setText( questions[q] );
					questionTextAreas[q].setCaretPosition( 0 );
				} else {
					valueLabels[q].setText( "" );
					questionTextAreas[q].setText( "" );
				}
				if ( corrects[q] || ( beenOpens[q] && !opens[q] ) ) {
					earnedLabels[q].setText( earneds[q] + "" );
					answerTextAreas[q].setText( answers[q] );
					answerTextAreas[q].setCaretPosition( 0 );
					submitterLabels[q].setText( submitters[q] );
					operatorLabels[q].setText( operators[q] );
				} else {
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
	 * Sets the round.
	 *
	 * @param rNumber the new round
	 */
	public void setRound(int rNumber) {
		this.rNumber = rNumber;		
	}

}
