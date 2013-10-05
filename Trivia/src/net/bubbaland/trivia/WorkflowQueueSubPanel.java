package net.bubbaland.trivia;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.rmi.RemoteException;
import java.util.Arrays;

import javax.swing.*;

import net.bubbaland.trivia.server.TriviaInterface;

// TODO: Auto-generated Javadoc
/**
 * The Class WorkflowQueueSubPanel.
 */
public class WorkflowQueueSubPanel extends TriviaPanel implements ItemListener {

	/** The Constant serialVersionUID. */
	private static final long		serialVersionUID	= -5462544756397828556L;

	/** The Constant BACKGROUND_COLOR. */
	private static final Color		BACKGROUND_COLOR	= Color.BLACK;
	
	/** The Constant NOT_CALLED_IN_COLOR. */
	private static final Color		NOT_CALLED_IN_COLOR	= Color.WHITE;
	
	/** The Constant CALLING_COLOR. */
	private static final Color		CALLING_COLOR		= Color.CYAN;
	
	/** The Constant INCORRECT_COLOR. */
	private static final Color		INCORRECT_COLOR		= Color.RED;
	
	/** The Constant PARTIAL_COLOR. */
	private static final Color		PARTIAL_COLOR		= Color.ORANGE;
	
	/** The Constant CORRECT_COLOR. */
	private static final Color		CORRECT_COLOR		= Color.GREEN;

	/** The Constant TIME_WIDTH. */
	private static final int		TIME_WIDTH			= WorkflowQueuePanel.TIME_WIDTH;
	
	/** The Constant QNUM_WIDTH. */
	private static final int		QNUM_WIDTH			= WorkflowQueuePanel.QNUM_WIDTH;
	
	/** The Constant ANSWER_WIDTH. */
	private static final int		ANSWER_WIDTH		= WorkflowQueuePanel.ANSWER_WIDTH;
	
	/** The Constant CONFIDENCE_WIDTH. */
	private static final int		CONFIDENCE_WIDTH	= WorkflowQueuePanel.CONFIDENCE_WIDTH;
	
	/** The Constant SUBMITTER_WIDTH. */
	private static final int		SUB_CALLER_WIDTH	= WorkflowQueuePanel.SUB_CALLER_WIDTH;
	
	/** The Constant OPERATOR_WIDTH. */
	private static final int		OPERATOR_WIDTH		= WorkflowQueuePanel.OPERATOR_WIDTH;	
	
	/** The Constant STATUS_WIDTH. */
	private static final int		STATUS_WIDTH		= WorkflowQueuePanel.STATUS_WIDTH;

	/** The Constant ANSWER_HEIGHT. */
	protected static final int		ANSWER_HEIGHT		= 40;
	
	/** The Constant DEFAULT_N_ANSWERS_SHOW. */
	protected static final int		DEFAULT_N_ANSWERS_SHOW = 4;

	/** The Constant LARGE_FONT_SIZE. */
	private static final float		LARGE_FONT_SIZE		= (float)24.0;
	
	/** The Constant SMALL_FONT_SIZE. */
	private static final float		SMALL_FONT_SIZE		= (float)12.0;

	/** The Constant MAX_QUEUE_LENGTH. */
	private static final int		MAX_QUEUE_LENGTH	= 500;

	/** The Constant STATUSES. */
	private static final String[]	STATUSES			= { "Not Called In", "Calling", "Incorrect", "Partial",
			"Correct"									};

	/** The server. */
	private TriviaInterface			server;
	
	/** The client. */
	private TriviaClient			client;
	
	/** The last status. */
	private volatile String[]		lastStatus;
	
	/** The caller labels. */
	private JLabel[]				queuenumberLabels, timestampLabels, qNumberLabels, confidenceLabels, submitterLabels, operatorLabels,
			callerLabels;
	
	/** The status combo boxes. */
	private JComboBox<String>[]		statusComboBoxes;
	
	/** The answer text areas. */
	private JTextArea[]				answerTextAreas;

	/**
	 * Instantiates a new workflow queue sub panel.
	 *
	 * @param server the server
	 * @param client the client
	 */
	@SuppressWarnings( "unchecked" )
	public WorkflowQueueSubPanel( TriviaInterface server, TriviaClient client ) {

		super( new GridBagLayout() );

		this.server = server;
		this.client = client;

		GridBagConstraints solo = new GridBagConstraints();		
		solo.fill = GridBagConstraints.BOTH;
		solo.anchor = GridBagConstraints.CENTER;
		solo.weightx = 1.0; solo.weighty = 1.0;
		solo.gridx = 0; solo.gridy = 0;
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;
		
		this.queuenumberLabels = new JLabel[MAX_QUEUE_LENGTH];
		this.timestampLabels = new JLabel[MAX_QUEUE_LENGTH];
		this.qNumberLabels = new JLabel[MAX_QUEUE_LENGTH];
		this.confidenceLabels = new JLabel[MAX_QUEUE_LENGTH];
		this.submitterLabels = new JLabel[MAX_QUEUE_LENGTH];
		this.operatorLabels = new JLabel[MAX_QUEUE_LENGTH];
		this.callerLabels = new JLabel[MAX_QUEUE_LENGTH];
		this.statusComboBoxes = new JComboBox[MAX_QUEUE_LENGTH];
		this.answerTextAreas = new JTextArea[MAX_QUEUE_LENGTH];
		this.lastStatus = new String[MAX_QUEUE_LENGTH];

		for ( int a = 0; a < MAX_QUEUE_LENGTH; a++ ) {

			constraints.gridheight = 1;
			constraints.gridx = 0;
			constraints.gridy = 2*a;
			this.queuenumberLabels[a] = enclosedLabel("#"+(a+1), TIME_WIDTH, ANSWER_HEIGHT/2, NOT_CALLED_IN_COLOR, BACKGROUND_COLOR, constraints, SMALL_FONT_SIZE, JLabel.CENTER, JLabel.CENTER);

			constraints.gridx = 0;
			constraints.gridy = 2*a + 1;
			this.timestampLabels[a] = enclosedLabel("", TIME_WIDTH, ANSWER_HEIGHT/2, NOT_CALLED_IN_COLOR, BACKGROUND_COLOR, constraints, SMALL_FONT_SIZE, JLabel.CENTER, JLabel.CENTER);

			constraints.gridheight = 2;
			constraints.gridx = 1;
			constraints.gridy = 2*a;
			this.qNumberLabels[a] = enclosedLabel("", QNUM_WIDTH, ANSWER_HEIGHT, NOT_CALLED_IN_COLOR, BACKGROUND_COLOR, constraints, LARGE_FONT_SIZE, JLabel.CENTER, JLabel.CENTER);
			
			constraints.gridx = 2;
			constraints.gridy = 2*a;
			constraints.weightx = 1.0;
			this.answerTextAreas[a] = scrollableTextArea("", ANSWER_WIDTH, ANSWER_HEIGHT, NOT_CALLED_IN_COLOR, BACKGROUND_COLOR, constraints, SMALL_FONT_SIZE, Component.LEFT_ALIGNMENT, Component.TOP_ALIGNMENT, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);		
			this.answerTextAreas[a].setEditable( false );			
			constraints.weightx = 0.0;

			constraints.gridx = 3;
			constraints.gridy = 2*a;
			this.confidenceLabels[a] = enclosedLabel("", CONFIDENCE_WIDTH, ANSWER_HEIGHT, NOT_CALLED_IN_COLOR, BACKGROUND_COLOR, constraints, SMALL_FONT_SIZE, JLabel.CENTER, JLabel.CENTER);

			constraints.gridheight = 1;
	        constraints.gridx = 4;
			constraints.gridy = 2*a;
			this.submitterLabels[a] = enclosedLabel("", SUB_CALLER_WIDTH, ANSWER_HEIGHT/2, NOT_CALLED_IN_COLOR, BACKGROUND_COLOR, constraints, SMALL_FONT_SIZE, JLabel.CENTER, JLabel.CENTER);

			constraints.gridx = 4;
			constraints.gridy = 2*a + 1;
			this.operatorLabels[a] = enclosedLabel("", OPERATOR_WIDTH, ANSWER_HEIGHT/2, NOT_CALLED_IN_COLOR, BACKGROUND_COLOR, constraints, SMALL_FONT_SIZE, JLabel.CENTER, JLabel.CENTER);
			constraints.gridheight = 2;
	        
			constraints.gridx = 5;
			constraints.gridy = 2*a;
			this.callerLabels[a] = enclosedLabel("", SUB_CALLER_WIDTH, ANSWER_HEIGHT, NOT_CALLED_IN_COLOR, BACKGROUND_COLOR, constraints, SMALL_FONT_SIZE, JLabel.CENTER, JLabel.CENTER);

			constraints.gridx = 6;
			constraints.gridy = 2*a;
			JPanel panel = new JPanel( new GridBagLayout() );
			panel.setBackground( BACKGROUND_COLOR );
			panel.setPreferredSize( new Dimension( STATUS_WIDTH, ANSWER_HEIGHT ) );
			panel.setMinimumSize( new Dimension( STATUS_WIDTH, ANSWER_HEIGHT ) );
			this.add( panel, constraints );
			this.statusComboBoxes[a] = new JComboBox<String>( STATUSES );
			this.statusComboBoxes[a].setName( a + "" );
			this.statusComboBoxes[a].addItemListener( this );
			this.statusComboBoxes[a].setBackground( BACKGROUND_COLOR );
			panel.add( this.statusComboBoxes[a] );
			
			this.lastStatus[a] = "new";

		}

		constraints.gridx = 0;
		constraints.gridy = MAX_QUEUE_LENGTH;
		constraints.gridwidth = 8;
		
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		JPanel blank = new JPanel();
		blank.setBackground( BACKGROUND_COLOR );
		blank.setPreferredSize(new Dimension(0,0));
		this.add( blank, constraints );

		update();

	}

	/* (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	@Override
	@SuppressWarnings( "unchecked" )
	public synchronized void itemStateChanged(ItemEvent event) {
		JComponent source = (JComponent)event.getSource();
		if ( source instanceof JComboBox<?> && event.getStateChange() == ItemEvent.SELECTED ) {
			String newStatus = (String)( ( (JComboBox<String>)source ).getSelectedItem() );
			int queueIndex = Integer.parseInt( ( (JComboBox<String>)source ).getName() );

			int tryNumber = 0;
			boolean success = false;
			while ( tryNumber < TriviaClient.MAX_RETRIES && success == false ) {
				tryNumber++;
				try {
					switch ( newStatus ) {
						case "Not Called In":
							server.markUncalled( queueIndex );
							break;
						case "Calling":
							server.callIn( queueIndex, client.getUser() );
							break;
						case "Incorrect":
							server.markIncorrect( queueIndex, client.getUser() );
							break;
						case "Partial":
							server.markPartial( queueIndex, client.getUser() );
							break;
						case "Correct":
							new CorrectEntryPanel( server, client, client.getUser(), queueIndex, ( (JComboBox<String>)source ) );
							break;
						default:
							break;
					}
					success = true;
				} catch ( RemoteException e ) {
					client.log( "Couldn't change answer status on server (try #" + tryNumber + ")." );
				}
			}

			if ( !success ) {
				client.disconnected();
				return;
			}
			
			client.log( "Changed status of answer #" + (queueIndex+1) + " in the queue to " + newStatus );

		}

	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void update() {

		String[] newTimestamps = null;
		int[] newQNumbers = null;
		String[] newAnswers = null;
		int[] newConfidences = null;
		String[] newSubmitters = null;
		String[] newOperators = null;
		String[] newCallers = null;
		String[] newStatuses = null;

		int tryNumber = 0;
		boolean success = false;
		while ( tryNumber < TriviaClient.MAX_RETRIES && success == false ) {
			tryNumber++;
			try {
				newTimestamps = server.getAnswerQueueTimestamps();
				newQNumbers = server.getAnswerQueueQNumbers();
				newAnswers = server.getAnswerQueueAnswers();
				newConfidences = server.getAnswerQueueConfidences();
				newSubmitters = server.getAnswerQueueSubmitters();
				newOperators = server.getAnswerQueueOperators();
				newCallers = server.getAnswerQueueCallers();
				newStatuses = server.getAnswerQueueStatuses();
				success = true;
			} catch ( RemoteException e ) {
				client.log( "Couldn't retrive answer queue data from server (try #" + tryNumber + ")." );
			}
		}

		if ( !success ) {
			client.disconnected();
			return;
		}

		int queueSize = newTimestamps.length;
		boolean[] qUpdated = new boolean[queueSize];

		for ( int a = 0; a < queueSize; a++ ) {
			qUpdated[a] = !( this.timestampLabels[a].getText().equals( newTimestamps[a] )
					&& this.qNumberLabels[a].getText().equals( newQNumbers[a] + "" )
					&& this.answerTextAreas[a].getText().equals( newAnswers[a] )
					&& this.confidenceLabels[a].getText().equals( newConfidences[a] + "" )
					&& this.submitterLabels[a].getText().equals( newSubmitters[a] )
					&& this.operatorLabels[a].getText().equals( newOperators[a] )
					&& this.callerLabels[a].getText().equals( newCallers[a] ) && this.lastStatus[a]
					.equals( newStatuses[a] ) );
		}

		for ( int a = 0; a < queueSize; a++ ) {

			this.lastStatus[a] = newStatuses[a];

			if ( qUpdated[a] ) {

				Color color = NOT_CALLED_IN_COLOR;
				int statusIndex = Arrays.asList( STATUSES ).indexOf( newStatuses[a] );
				switch ( newStatuses[a] ) {
					case "Not Called In":
						color = NOT_CALLED_IN_COLOR;
						break;
					case "Calling":
						color = CALLING_COLOR;
						break;
					case "Incorrect":
						color = INCORRECT_COLOR;
						break;
					case "Partial":
						color = PARTIAL_COLOR;
						break;
					case "Correct":
						color = CORRECT_COLOR;
						break;
					default:
						color = NOT_CALLED_IN_COLOR;
						break;
				}

				this.timestampLabels[a].setText( newTimestamps[a] );
				this.timestampLabels[a].setForeground( color );

				this.qNumberLabels[a].setText( newQNumbers[a] + "" );
				this.qNumberLabels[a].setForeground( color );

				this.answerTextAreas[a].setText( newAnswers[a] );
				this.answerTextAreas[a].setForeground( color );
				this.answerTextAreas[a].setCaretPosition( 0 );

				this.confidenceLabels[a].setText( newConfidences[a] + "" );
				this.confidenceLabels[a].setForeground( color );

				this.submitterLabels[a].setText( newSubmitters[a] );
				this.submitterLabels[a].setForeground( color );

				this.operatorLabels[a].setText( newOperators[a] );
				this.operatorLabels[a].setForeground( color );

				this.callerLabels[a].setText( newCallers[a] );
				this.callerLabels[a].setForeground( color );

				ItemListener[] listeners = this.statusComboBoxes[a].getItemListeners();
				for ( ItemListener listener : listeners ) {
					this.statusComboBoxes[a].removeItemListener( listener );
				}
				this.statusComboBoxes[a].setForeground( color );
				this.statusComboBoxes[a].setSelectedIndex( statusIndex );
				for ( ItemListener listener : listeners ) {
					this.statusComboBoxes[a].addItemListener( listener );
				}
				
				this.queuenumberLabels[a].setVisible( true );
				this.timestampLabels[a].setVisible( true );
				this.qNumberLabels[a].setVisible( true );
				this.answerTextAreas[a].setVisible( true );
				this.answerTextAreas[a].getParent().setVisible( true );
				this.answerTextAreas[a].getParent().getParent().setVisible( true );
				this.confidenceLabels[a].setVisible( true );
				this.submitterLabels[a].setVisible( true );
				this.operatorLabels[a].setVisible( true );
				this.callerLabels[a].setVisible( true );
				this.statusComboBoxes[a].setVisible( true );
				
				this.queuenumberLabels[a].getParent().setVisible( true );
				this.timestampLabels[a].getParent().setVisible( true );
				this.qNumberLabels[a].getParent().setVisible( true );
				this.answerTextAreas[a].getParent().setVisible( true );
				this.answerTextAreas[a].getParent().getParent().setVisible( true );
				this.confidenceLabels[a].getParent().setVisible( true );
				this.submitterLabels[a].getParent().setVisible( true );
				this.operatorLabels[a].getParent().setVisible( true );
				this.callerLabels[a].getParent().setVisible( true );
				this.statusComboBoxes[a].getParent().setVisible( true );

			}

		}

		for ( int a = queueSize; a < MAX_QUEUE_LENGTH; a++ ) {
			this.queuenumberLabels[a].setVisible( false );
			this.timestampLabels[a].setVisible( false );
			this.qNumberLabels[a].setVisible( false );
			this.answerTextAreas[a].setVisible( false );
			this.confidenceLabels[a].setVisible( false );
			this.submitterLabels[a].setVisible( false );
			this.operatorLabels[a].setVisible( false );
			this.callerLabels[a].setVisible( false );
			this.statusComboBoxes[a].setVisible( false );
			
			this.queuenumberLabels[a].getParent().setVisible( false );
			this.timestampLabels[a].getParent().setVisible( false );
			this.qNumberLabels[a].getParent().setVisible( false );
			this.answerTextAreas[a].getParent().setVisible( false );
			this.answerTextAreas[a].getParent().getParent().setVisible( false );
			this.confidenceLabels[a].getParent().setVisible( false );
			this.submitterLabels[a].getParent().setVisible( false );
			this.operatorLabels[a].getParent().setVisible( false );
			this.callerLabels[a].getParent().setVisible( false );
			this.statusComboBoxes[a].getParent().setVisible( false );
			
		}

	}

}
