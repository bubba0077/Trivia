package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.*;

import net.bubbaland.trivia.TriviaInterface;

/**
 * Panel that shows all of the questions for a round.
 */
public class RoundQlistPanel extends TriviaPanel {

	/** The Constant serialVersionUID. */
	private static final long		serialVersionUID		= 3589815467416864653L;
	
	/** Font size */
	private static final float		FONT_SIZE				= (float)12.0;

	/**
	 * Colors
	 */
	protected static final Color	HEADER_TEXT_COLOR		= Color.white;
	protected static final Color	HEADER_BACKGROUND_COLOR	= Color.darkGray;

	/**
	 * Sizes
	 */
	protected static final int		HEADER_HEIGHT			= 20;
	
	protected static final int		QNUM_WIDTH				= 48;
	protected static final int		EARNED_WIDTH			= 75;
	protected static final int		VALUE_WIDTH				= 75;
	protected static final int		QUESTION_WIDTH			= 200;
	protected static final int		ANSWER_WIDTH			= 150;
	protected static final int		SUBOP_WIDTH				= 100;

	
	/** The sub-panel holding the questions */
	private final RoundQListSubPanel		roundQlistSubPanel;

	/**
	 * Instantiates a new question list panel.
	 *
	 * @param server the server
	 * @param client the client application
	 * @param live whether this panel should always show the current round 
	 * @param rNumber the round number
	 */
	public RoundQlistPanel( TriviaInterface server, TriviaClient client, boolean live, int rNumber ) {

		super( new GridBagLayout() );		

		// Set up the layout constraints
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;

		/**
		 * Create the header row
		 */
		constraints.gridx = 0;
		constraints.gridy = 0;
		enclosedLabel("Q#", QNUM_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints, FONT_SIZE, JLabel.CENTER, JLabel.CENTER);
		
		constraints.gridx = 1;
		constraints.gridy = 0;
		enclosedLabel("Earned", EARNED_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints, FONT_SIZE, JLabel.CENTER, JLabel.CENTER);
		
		constraints.gridx = 2;
		constraints.gridy = 0;
		enclosedLabel("Value", VALUE_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints, FONT_SIZE, JLabel.CENTER, JLabel.CENTER);
		constraints.weightx = 0.0;

		constraints.gridx = 3;
		constraints.gridy = 0;
		constraints.weightx = 0.6;
		enclosedLabel("Question", QUESTION_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints, FONT_SIZE, JLabel.LEFT, JLabel.CENTER);
		constraints.weightx = 0.0;

		constraints.gridx = 4;
		constraints.gridy = 0;
		constraints.weightx = 0.4;
		enclosedLabel("Answer", ANSWER_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints, FONT_SIZE, JLabel.LEFT, JLabel.CENTER);
		constraints.weightx = 0.0;

		constraints.gridx = 5;
		constraints.gridy = 0;
		enclosedLabel("Credit/Op", SUBOP_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints, FONT_SIZE, JLabel.CENTER, JLabel.CENTER);
		
		constraints.gridx = 6;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		int scrollBarWidth = ( (Integer)UIManager.get( "ScrollBar.width" ) ).intValue();
		enclosedLabel("", scrollBarWidth, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints, FONT_SIZE, JLabel.CENTER, JLabel.CENTER);
		
		
		/**
		 * Create the question list sub-panel and place in a scroll pane
		 */
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 7;
		constraints.weighty = 1.0;
		this.roundQlistSubPanel = new RoundQListSubPanel( client, live, rNumber );
		final JScrollPane roundQlistPane = new JScrollPane( this.roundQlistSubPanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		roundQlistPane.setPreferredSize( new Dimension( 0, 200 ) );
		this.add( roundQlistPane, constraints );
		constraints.weighty = 0.0;

	}
	
	/**
	 * Instantiates a new question list panel that will show data for the current round.
	 *
	 * @param server the server
	 * @param client the client
	 */
	public RoundQlistPanel( TriviaInterface server, TriviaClient client) {		
		this(server, client, true, 0);		
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void update() {
		this.roundQlistSubPanel.update();
	}
	
	/**
	 * Sets the round to display. This will be overridden with the current round number if this is a "live" panel.
	 *
	 * @param rNumber the new round number
	 */
	public void setRound(int rNumber) {
		this.roundQlistSubPanel.setRound(rNumber);
	}

}
