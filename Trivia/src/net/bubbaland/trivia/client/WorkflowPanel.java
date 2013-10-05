package net.bubbaland.trivia.client;

//imports for GUI
import java.awt.*;

import net.bubbaland.trivia.*;

// TODO: Auto-generated Javadoc
/**
 * The Class WorkflowPanel.
 */
public class WorkflowPanel extends TriviaPanel {

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= -5608314912146842278L;

	/** The workflow header panel. */
	private HeaderPanel			workflowHeaderPanel;
	
	/** The workflow qlist panel. */
	private WorkflowQlistPanel	workflowQlistPanel;
	
	/** The workflow queue panel. */
	private WorkflowQueuePanel	workflowQueuePanel;

	/** The Constant REFRESH_RATE. */
	final public static int		REFRESH_RATE		= 100;

	/**
	 * Instantiates a new workflow panel.
	 *
	 * @param server the server
	 * @param client the client
	 */
	public WorkflowPanel( TriviaInterface server, TriviaClient client ) {

		super( new GridBagLayout() );
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 0.0;

		workflowHeaderPanel = new HeaderPanel( server, client );
		workflowQlistPanel = new WorkflowQlistPanel( server, client );
		workflowQueuePanel = new WorkflowQueuePanel( server, client );
				
		c.weighty = 0.0;
		c.gridx = 0;
		c.gridy = 0;
		this.add( workflowHeaderPanel, c );
		c.gridx = 0;
		c.gridy = 1;
		// c.weighty = 0.1;
		this.add( workflowQlistPanel, c );
		c.gridx = 0;
		c.gridy = 2;
		c.weighty = 1.0;
		this.add( workflowQueuePanel, c );

	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void update() {
		this.workflowHeaderPanel.update();
		this.workflowQlistPanel.update();
		this.workflowQueuePanel.update();
	}

}
