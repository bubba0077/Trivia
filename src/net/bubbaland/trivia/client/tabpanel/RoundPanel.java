package net.bubbaland.trivia.client.tabpanel;

import java.awt.GridBagConstraints;
import java.util.Properties;

import net.bubbaland.trivia.client.TriviaClient;
import net.bubbaland.trivia.client.TriviaFrame;
import net.bubbaland.trivia.client.TriviaMainPanel;

/**
 * A panel for the current round data.
 *
 * <code>RoundPanel</code> is a panel that displays a summary and question data for the current round. There are two
 * parts: a <code>HeaderPanel</code> to display summary information, and a <RoundQlistPanel> to display the question
 * data.
 *
 * @author Walter Kolczynski
 *
 */
public class RoundPanel extends TriviaMainPanel {

	/** The Constant serialVersionUID. */
	private static final long			serialVersionUID	= 9190017804155701978L;

	// Sub-panels of the round panel
	private final SummaryPanel			roundHeaderPanel;
	private final RoundQuestionsPanel	roundQlistPanel;

	/**
	 * Instantiates a new round panel.
	 *
	 * @param client
	 *            The local trivia client
	 */
	public RoundPanel(TriviaClient client, TriviaFrame parent) {

		super(client, parent);

		// Create the sub-panels
		this.roundHeaderPanel = new SummaryPanel(client, parent);
		this.roundQlistPanel = new RoundQuestionsPanel(client, parent);

		// Set up layout constraints
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;

		// Place the sub-panels
		constraints.gridx = 0;
		constraints.gridy = 0;
		this.add(this.roundHeaderPanel, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weighty = 1.0;
		this.add(this.roundQlistPanel, constraints);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void updateGUI(boolean force) {
		this.roundHeaderPanel.updateGUIonEDT(force);
		this.roundQlistPanel.updateGUIonEDT(force);
	}

	@Override
	protected void loadProperties(Properties properties) {
		this.roundHeaderPanel.loadProperties(properties);
		this.roundQlistPanel.loadProperties(properties);
	}

}
