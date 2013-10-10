package net.bubbaland.trivia.client;

// imports for GUI
import java.awt.GridBagConstraints;

import net.bubbaland.trivia.TriviaInterface;

/**
 * A panel for the workflow tab, which contains most of the trivia operations
 * 
 * @author Walter Kolczynski
 * 
 */
public class WorkflowPanel extends TriviaPanel {

	/** The Constant serialVersionUID. */
	private static final long			serialVersionUID	= -5608314912146842278L;

	// Sub-panels of the workflow panel
	private final HeaderPanel			workflowHeaderPanel;
	private final WorkflowQlistPanel	workflowQlistPanel;
	private final WorkflowQueuePanel	workflowQueuePanel;

	/**
	 * Instantiates a new workflow panel.
	 * 
	 * @param server
	 *            The remote trivia server
	 * @param client
	 *            The local trivia client
	 */
	public WorkflowPanel(TriviaInterface server, TriviaClient client) {

		super();

		// Create the sub-panels
		this.workflowHeaderPanel = new HeaderPanel(server, client);
		this.workflowQlistPanel = new WorkflowQlistPanel(server, client);
		this.workflowQueuePanel = new WorkflowQueuePanel(server, client);

		// Set up layout constraints
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 0.0;

		// Place the sub-panels
		c.weighty = 0.0;
		c.gridx = 0;
		c.gridy = 0;
		this.add(this.workflowHeaderPanel, c);

		c.gridx = 0;
		c.gridy = 1;
		this.add(this.workflowQlistPanel, c);

		c.weighty = 1.0;
		c.gridx = 0;
		c.gridy = 2;
		this.add(this.workflowQueuePanel, c);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void update() {
		this.workflowHeaderPanel.update();
		this.workflowQlistPanel.update();
		this.workflowQueuePanel.update();
	}

}
