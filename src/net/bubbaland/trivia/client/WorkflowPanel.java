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
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;

		// Place the sub-panels
		constraints.weighty = 0.0;
		constraints.gridx = 0;
		constraints.gridy = 0;
		this.add(this.workflowHeaderPanel, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		this.add(this.workflowQlistPanel, constraints);

		constraints.weighty = 1.0;
		constraints.gridx = 0;
		constraints.gridy = 2;
		this.add(this.workflowQueuePanel, constraints);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void update(boolean force) {
		this.workflowHeaderPanel.update(force);
		this.workflowQlistPanel.update(force);
		this.workflowQueuePanel.update(force);
	}

}
