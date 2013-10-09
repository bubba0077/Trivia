package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import net.bubbaland.trivia.TriviaInterface;

/**
 * Panel which displays a list of the current open questions.
 */
public class WorkflowQlistPanel extends TriviaPanel {

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID				= 6049067322505905668L;
	
	/**
	 * Colors
	 */
	private static final Color	HEADER_TEXT_COLOR				= Color.white;
	private static final Color	HEADER_BACKGROUND_COLOR			= Color.darkGray;
	
	/** Font size for header row */
	private static final float	HEADER_FONT_SIZE				= (float)12.0;
		
	/**
	 * Sizes
	 */	
	private static final int	HEADER_HEIGHT					= 16;
	
	protected static final int	QNUM_WIDTH						= 48;
	protected static final int	QUESTION_WIDTH					= 50;
	protected static final int	VALUE_WIDTH						= 75;
	protected static final int	ANSWER_WIDTH					= 72;
	protected static final int	CLOSE_WIDTH						= 72;

	/** Sub-panel that will hold the open questions */
	private final WorkflowQListSubPanel	workflowQListSubPanel;

	/**
	 * Instantiates a new workflow question list panel.
	 *
	 * @param server the server
	 * @param client the client application
	 */
	public WorkflowQlistPanel( TriviaInterface server, TriviaClient client ) {

		super( new GridBagLayout() );
		
		// Set up layout constraints
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;

		/**
		 * Create the header row
		 */		
		constraints.gridx = 0;
		constraints.gridy = 0;
		enclosedLabel("Q#", QNUM_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.CENTER, JLabel.CENTER);
		
		constraints.gridx = 1;
		constraints.gridy = 0;
		enclosedLabel("Value", VALUE_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.CENTER, JLabel.CENTER);
		
		constraints.gridx = 2;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		enclosedLabel("Question", QUESTION_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.LEFT, JLabel.CENTER);
		constraints.weightx = 0.0;
		
		constraints.gridx = 3;
		constraints.gridy = 0;
		enclosedLabel("", ANSWER_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.LEFT, JLabel.CENTER);
		
		constraints.gridx = 4;
		constraints.gridy = 0;;
		enclosedLabel("", CLOSE_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.LEFT, JLabel.CENTER);
		
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 5;
		constraints.weighty = 1.0;
		
		/**
		 * Create the subpanel that will hold the actual questions and put it in a scroll pane 
		 */		
		this.workflowQListSubPanel = new WorkflowQListSubPanel(server, client);
		final JScrollPane scrollPane = new JScrollPane( workflowQListSubPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		scrollPane.setPreferredSize( new Dimension( 0, WorkflowQListSubPanel.MIN_QUESTIONS_SHOW * WorkflowQListSubPanel.QUESTION_HEIGHT + 3 ) );
		scrollPane.setMinimumSize( new Dimension( 0, WorkflowQListSubPanel.MIN_QUESTIONS_SHOW * WorkflowQListSubPanel.QUESTION_HEIGHT + 3 ) );
		this.add( scrollPane, constraints );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void update() {
		this.workflowQListSubPanel.update();
	}

}
