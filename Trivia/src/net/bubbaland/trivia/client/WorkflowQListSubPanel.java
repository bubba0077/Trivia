package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.TriviaInterface;

// TODO: Auto-generated Javadoc
/**
 * The Class WorkflowQListSubPanel.
 */
public class WorkflowQListSubPanel extends TriviaPanel implements ActionListener {
	
	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID				= 6049067322505905668L;

	/** The Constant MIN_QUESTIONS_SHOW. */
	protected static final int	MIN_QUESTIONS_SHOW				= 4;
	
	/** The n questions max. */
	private int nQuestionsMax;
	
	/** The Constant ODD_QUESTION_TEXT_COLOR. */
	private static final Color	ODD_QUESTION_TEXT_COLOR			= Color.black;
	
	/** The Constant EVEN_QUESTION_TEXT_COLOR. */
	private static final Color	EVEN_QUESTION_TEXT_COLOR		= Color.black;
	
	/** The Constant ODD_QUESTION_BACKGROUND_COLOR. */
	private static final Color	ODD_QUESTION_BACKGROUND_COLOR	= Color.white;
	
	/** The Constant EVEN_QUESTION_BACKGROUND_COLOR. */
	private static final Color	EVEN_QUESTION_BACKGROUND_COLOR	= Color.lightGray;

	/** The Constant QNUM_FONT_SIZE. */
	private static final float	QNUM_FONT_SIZE					= (float)32.0;
	
	/** The Constant VALUE_FONT_SIZE. */
	private static final float	VALUE_FONT_SIZE					= (float)32.0;
	
	/** The Constant QUESTION_FONT_SIZE. */
	private static final float	QUESTION_FONT_SIZE				= (float)12.0;

	/** The Constant QUESTION_HEIGHT. */
	protected static final int	QUESTION_HEIGHT					= 46;

	/** The Constant QNUM_WIDTH. */
	private static final int	QNUM_WIDTH						= WorkflowQlistPanel.QNUM_WIDTH;
	
	/** The Constant QUESTION_WIDTH. */
	private static final int	QUESTION_WIDTH					= WorkflowQlistPanel.QUESTION_WIDTH;
	
	/** The Constant VALUE_WIDTH. */
	private static final int	VALUE_WIDTH						= WorkflowQlistPanel.VALUE_WIDTH;
	
	/** The Constant ANSWER_WIDTH. */
	private static final int	ANSWER_WIDTH					= WorkflowQlistPanel.ANSWER_WIDTH;
	
	/** The Constant CLOSE_WIDTH. */
	private static final int	CLOSE_WIDTH						= WorkflowQlistPanel.CLOSE_WIDTH;
	
	/** The Constant ANSWER_BUTTON_HEIGHT. */
	private static final int	ANSWER_BUTTON_HEIGHT			= 32;
	
	/** The Constant ANSWER_BUTTON_WIDTH. */
	private static final int	ANSWER_BUTTON_WIDTH				= 64;
	
	/** The Constant CLOSE_BUTTON_HEIGHT. */
	private static final int	CLOSE_BUTTON_HEIGHT				= 32;
	
	/** The Constant CLOSE_BUTTON_WIDTH. */
	private static final int	CLOSE_BUTTON_WIDTH				= 64;
	
	/** The server. */
	private TriviaInterface		server;
	
	/** The client. */
	private TriviaClient		client;
	
	/** The q value labels. */
	private JLabel[]			qNumberLabels, qValueLabels;
	
	/** The q text areas. */
	private JTextArea[]			qTextAreas;
	
	/** The close buttons. */
	private JButton[]			answerButtons, closeButtons;

	/**
	 * Instantiates a new workflow q list sub panel.
	 *
	 * @param server the server
	 * @param client the client
	 */
	public WorkflowQListSubPanel( TriviaInterface server, TriviaClient client ) {

		super( new GridBagLayout() );

		this.server = server;
		this.client = client;
		
		this.nQuestionsMax = client.getTrivia().getMaxQuestions();
		
		GridBagConstraints solo = new GridBagConstraints();		
		solo.fill = GridBagConstraints.BOTH;
		solo.anchor = GridBagConstraints.CENTER;
		solo.weightx = 1.0; solo.weighty = 1.0;
		solo.gridx = 0; solo.gridy = 0;		
		
		GridBagConstraints buttonConstraints = solo;
		buttonConstraints.fill = GridBagConstraints.NONE;	

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;
		
		this.qNumberLabels = new JLabel[nQuestionsMax];
		this.qValueLabels = new JLabel[nQuestionsMax];
		this.qTextAreas = new JTextArea[nQuestionsMax];
		this.answerButtons = new JButton[nQuestionsMax];
		this.closeButtons = new JButton[nQuestionsMax];
		
		for ( int q = 0; q < nQuestionsMax; q++ ) {
			Color color, bColor;
			if ( q % 2 == 1 ) {
				color = ODD_QUESTION_TEXT_COLOR;
				bColor = ODD_QUESTION_BACKGROUND_COLOR;
			} else {
				color = EVEN_QUESTION_TEXT_COLOR;
				bColor = EVEN_QUESTION_BACKGROUND_COLOR;
			}
			constraints.gridx = 0;
			constraints.gridy = q;
			this.qNumberLabels[q] = enclosedLabel("", QNUM_WIDTH, QUESTION_HEIGHT, color, bColor, constraints, QNUM_FONT_SIZE, JLabel.CENTER, JLabel.CENTER);

			constraints.gridx = 1;
			constraints.gridy = q;
			this.qValueLabels[q] = enclosedLabel("", VALUE_WIDTH, QUESTION_HEIGHT, color, bColor, constraints, VALUE_FONT_SIZE, JLabel.CENTER, JLabel.CENTER);

			constraints.gridx = 2;
			constraints.gridy = q;
			constraints.weightx = 1.0;			
			this.qTextAreas[q] = scrollableTextArea("", QUESTION_WIDTH, QUESTION_HEIGHT, color, bColor, constraints, 
					QUESTION_FONT_SIZE, Component.LEFT_ALIGNMENT, Component.TOP_ALIGNMENT, 
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			this.qTextAreas[q].setEditable( false );
			constraints.weightx = 0.0;

			constraints.gridx = 3;
			constraints.gridy = q;			
			JPanel panel = new JPanel( new GridBagLayout() );
			panel.setPreferredSize( new Dimension( ANSWER_WIDTH, QUESTION_HEIGHT ) );
			panel.setMinimumSize( new Dimension( ANSWER_WIDTH, QUESTION_HEIGHT ) );
			panel.setBackground( bColor );
			this.add( panel, constraints );
			this.answerButtons[q] = new JButton( "" );
			this.answerButtons[q].setMargin( new Insets( 0, 0, 0, 0 ) );
			this.answerButtons[q].setPreferredSize( new Dimension( ANSWER_BUTTON_WIDTH, ANSWER_BUTTON_HEIGHT ) );
			this.answerButtons[q].setMinimumSize( new Dimension( ANSWER_BUTTON_WIDTH, ANSWER_BUTTON_HEIGHT ) );
			panel.add( answerButtons[q], buttonConstraints );
			this.answerButtons[q].addActionListener( this );

			constraints.gridx = 4;
			constraints.gridy = q;
			panel = new JPanel( new GridBagLayout() );
			panel.setPreferredSize( new Dimension( CLOSE_WIDTH, QUESTION_HEIGHT ) );
			panel.setMinimumSize( new Dimension( CLOSE_WIDTH, QUESTION_HEIGHT ) );
			panel.setBackground( bColor );
			this.add( panel, constraints );
			this.closeButtons[q] = new JButton( "Open" );
			this.closeButtons[q].setMargin( new Insets( 0, 0, 0, 0 ) );
			this.closeButtons[q].setPreferredSize( new Dimension( CLOSE_BUTTON_WIDTH, CLOSE_BUTTON_HEIGHT ) );
			this.closeButtons[q].setMinimumSize( new Dimension( CLOSE_BUTTON_WIDTH, CLOSE_BUTTON_HEIGHT ) );
			panel.add( closeButtons[q], buttonConstraints );
			this.closeButtons[q].addActionListener( this );			
						
			if(q > MIN_QUESTIONS_SHOW) {			
				this.qNumberLabels[q].getParent().setVisible( false );
				this.qValueLabels[q].getParent().setVisible( false );
				this.qTextAreas[q].getParent().setVisible( false );
				this.qTextAreas[q].getParent().getParent().setVisible( false );				
				this.answerButtons[q].getParent().setVisible( false );
				this.closeButtons[q].getParent().setVisible( false );
			}
		}

	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public synchronized void actionPerformed(ActionEvent event) {
		JButton source = (JButton)event.getSource();
		for ( int q = 0; q < nQuestionsMax; q++ ) {
			if ( this.answerButtons[q].equals( event.getSource() ) ) {
				answerQuestion( Integer.parseInt( this.qNumberLabels[q].getText() ) );
				return;
			}
		}
		for ( int q = 0; q < nQuestionsMax; q++ ) {
			if ( this.closeButtons[q].equals( event.getSource() ) ) {
				if ( source.getText() == "Close" ) {
					int qNumber = Integer.parseInt( this.qNumberLabels[q].getText() );
					close( qNumber );
				} else {
					open();
				}
				return;
			}
		}

	}

	/**
	 * Answer question.
	 *
	 * @param qNumber the q number
	 */
	private void answerQuestion(int qNumber) {
		new AnswerEntryPanel( server, client, qNumber, client.getUser() );
	}

	/**
	 * Close.
	 *
	 * @param qNumber the q number
	 */
	private void close(int qNumber) {
		int tryNumber = 0;
		boolean success = false;
		while ( tryNumber < TriviaClient.MAX_RETRIES && success == false ) {
			tryNumber++;
			try {
				server.close( qNumber );
				success = true;
			} catch ( RemoteException e ) {
				client.log( "Couldn't retrive question data from server (try #" + tryNumber + ")." );
			}
		}
		
		if(!success) {
			client.disconnected();
			return;
		}
		
		client.log("Closed Question #"+qNumber);
		
	}

	/**
	 * Open.
	 */
	private void open() {
		
		Trivia trivia = client.getTrivia();
		
		int nQuestions = trivia.getNQuestions();
		int	nextToOpen = trivia.nextToOpen();
				
		new QuestionEntryWindow( server, client, nQuestions, nextToOpen );
		
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void update() {
		
		Trivia trivia = client.getTrivia();
		
		String[] openQuestionNumbers = trivia.getOpenQuestionNumbers();
		String[] openQuestionText = trivia.getOpenQuestionText();
		String[] openQuestionValues = trivia.getOpenQuestionValues();

		int nOpen = openQuestionNumbers.length;

		boolean[] qUpdated = new boolean[nOpen];
		boolean anyUpdate = false;

		for ( int q = 0; q < nOpen; q++ ) {
			qUpdated[q] = !( this.qNumberLabels[q].getText().equals( openQuestionNumbers[q] )
					&& this.qValueLabels[q].getText().equals( openQuestionValues[q] ) && this.qTextAreas[q].getText()
					.equals( openQuestionText[q] ) );
			anyUpdate = anyUpdate || qUpdated[q];
		}

		for ( int q = 0; q < nOpen; q++ ) {
			if ( qUpdated[q] ) {
				this.qNumberLabels[q].setText( openQuestionNumbers[q] );
				this.qValueLabels[q].setText( openQuestionValues[q] );
				this.qTextAreas[q].setText( openQuestionText[q] );
				this.qTextAreas[q].setCaretPosition( 0 );
				this.answerButtons[q].setText( "Answer" );
				this.answerButtons[q].setVisible( true );
				this.closeButtons[q].setText( "Close" );
				this.closeButtons[q].setVisible( true );
			}
		}

		for ( int q = nOpen; q < nQuestionsMax; q++ ) {
			this.qNumberLabels[q].setText( "" );
			this.qValueLabels[q].setText( "" );
			this.qTextAreas[q].setText( "" );
			this.answerButtons[q].setText( "" );
			this.answerButtons[q].setVisible( false );
			this.closeButtons[q].setText( "Open" );
			if ( q == nOpen ) {
				this.closeButtons[q].setVisible( true );
			} else {
				this.closeButtons[q].setVisible( false );
			}
		}
		
		for ( int q = 0; q < Math.max( nOpen+1, MIN_QUESTIONS_SHOW ); q++ ) {
			this.qNumberLabels[q].setVisible( true );
			this.qValueLabels[q].setVisible( true );
			this.qTextAreas[q].setVisible( true );
			
			this.qNumberLabels[q].getParent().setVisible( true );
			this.qValueLabels[q].getParent().setVisible( true );
			this.qTextAreas[q].getParent().setVisible( true );				
			this.qTextAreas[q].getParent().getParent().setVisible( true );				
			this.answerButtons[q].getParent().setVisible( true );
			this.closeButtons[q].getParent().setVisible( true );
		}
		
		for ( int q = Math.max( nOpen+1, MIN_QUESTIONS_SHOW ); q < nQuestionsMax; q++ ) {
			this.qNumberLabels[q].setVisible( false );
			this.qValueLabels[q].setVisible( false );
			this.qTextAreas[q].setVisible( false );
			
			this.qNumberLabels[q].getParent().setVisible( false );
			this.qValueLabels[q].getParent().setVisible( false );
			this.qTextAreas[q].getParent().setVisible( false );
			this.qTextAreas[q].getParent().getParent().setVisible( false );				
			this.answerButtons[q].getParent().setVisible( false );
			this.closeButtons[q].getParent().setVisible( false );
		}

	}

}
