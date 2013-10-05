package net.bubbaland.trivia.client;

//imports for GUI
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import net.bubbaland.trivia.TriviaInterface;

// TODO: Auto-generated Javadoc
/**
 * The Class WorkflowQlistPanel.
 */
public class WorkflowQlistPanel extends TriviaPanel {

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID				= 6049067322505905668L;
	
	/** The Constant HEADER_TEXT_COLOR. */
	private static final Color	HEADER_TEXT_COLOR				= Color.white;
	
	/** The Constant HEADER_BACKGROUND_COLOR. */
	private static final Color	HEADER_BACKGROUND_COLOR			= Color.darkGray;
	
	/** The Constant HEADER_FONT_SIZE. */
	private static final float	HEADER_FONT_SIZE				= (float)12.0;
	
	/** The Constant HEADER_HEIGHT. */
	private static final int	HEADER_HEIGHT					= 16;
	
	/** The Constant QNUM_WIDTH. */
	protected static final int	QNUM_WIDTH						= 48;
	
	/** The Constant QUESTION_WIDTH. */
	protected static final int	QUESTION_WIDTH					= 50;
	
	/** The Constant VALUE_WIDTH. */
	protected static final int	VALUE_WIDTH						= 75;
	
	/** The Constant ANSWER_WIDTH. */
	protected static final int	ANSWER_WIDTH					= 72;
	
	/** The Constant CLOSE_WIDTH. */
	protected static final int	CLOSE_WIDTH						= 72;

	/** The workflow q list sub panel. */
	private WorkflowQListSubPanel	workflowQListSubPanel;

	/**
	 * Instantiates a new workflow qlist panel.
	 *
	 * @param server the server
	 * @param client the client
	 */
	public WorkflowQlistPanel( TriviaInterface server, TriviaClient client ) {

		super( new GridBagLayout() );
				
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
		
		this.workflowQListSubPanel = new WorkflowQListSubPanel(server, client);
		JScrollPane scrollPane = new JScrollPane( workflowQListSubPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
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
