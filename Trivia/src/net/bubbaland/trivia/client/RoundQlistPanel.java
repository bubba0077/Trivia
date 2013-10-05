package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.*;

import net.bubbaland.trivia.TriviaInterface;

// TODO: Auto-generated Javadoc
/**
 * The Class RoundQlistPanel.
 */
public class RoundQlistPanel extends TriviaPanel {

	/** The Constant serialVersionUID. */
	private static final long		serialVersionUID		= 3589815467416864653L;
	
	/** The Constant HEADER_TEXT_COLOR. */
	protected static final Color	HEADER_TEXT_COLOR		= Color.white;
	
	/** The Constant HEADER_BACKGROUND_COLOR. */
	protected static final Color	HEADER_BACKGROUND_COLOR	= Color.darkGray;

	/** The Constant FONT_SIZE. */
	private static final float		FONT_SIZE				= (float)12.0;

	/** The Constant HEADER_HEIGHT. */
	protected static final int		HEADER_HEIGHT			= 20;

	/** The Constant QNUM_WIDTH. */
	protected static final int		QNUM_WIDTH				= 48;
	
	/** The Constant EARNED_WIDTH. */
	protected static final int		EARNED_WIDTH			= 75;
	
	/** The Constant VALUE_WIDTH. */
	protected static final int		VALUE_WIDTH				= 75;
	
	/** The Constant QUESTION_WIDTH. */
	protected static final int		QUESTION_WIDTH			= 200;
	
	/** The Constant ANSWER_WIDTH. */
	protected static final int		ANSWER_WIDTH			= 150;
	
	/** The Constant SUBOP_WIDTH. */
	protected static final int		SUBOP_WIDTH				= 100;

	/** The round qlist sub panel. */
	private RoundQListSubPanel		roundQlistSubPanel;

	/**
	 * Instantiates a new round qlist panel.
	 *
	 * @param server the server
	 * @param client the client
	 * @param live the live
	 * @param rNumber the r number
	 */
	public RoundQlistPanel( TriviaInterface server, TriviaClient client, boolean live, int rNumber ) {

		super( new GridBagLayout() );		

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;

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
		
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 7;
		constraints.weighty = 1.0;
		this.roundQlistSubPanel = new RoundQListSubPanel( client, live, rNumber );
		JScrollPane roundQlistPane = new JScrollPane( this.roundQlistSubPanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		roundQlistPane.setPreferredSize( new Dimension( 0, 200 ) );
		this.add( roundQlistPane, constraints );
		constraints.weighty = 0.0;


	}
	
	/**
	 * Instantiates a new round qlist panel.
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
	 * Sets the round.
	 *
	 * @param rNumber the new round
	 */
	public void setRound(int rNumber) {
		this.roundQlistSubPanel.setRound(rNumber);
	}

}
