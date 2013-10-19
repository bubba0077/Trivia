package net.bubbaland.trivia.client;

// imports for GUI
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;

import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.TriviaInterface;

/**
 * A panel for the workflow tab, which contains most of the trivia operations
 * 
 * @author Walter Kolczynski
 * 
 */
@SuppressWarnings("unused")
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
	public WorkflowPanel(final TriviaInterface server, final TriviaClient client) {

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

		constraints.weighty = 1.0;
		constraints.gridx = 0;
		constraints.gridy = 1;

		final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.workflowQlistPanel,
				this.workflowQueuePanel);
		splitPane.setResizeWeight(0.0);
		this.add(splitPane, constraints);

		// Assign CTRL+O to open a new question
		this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK), "openQuestion");
		this.getActionMap().put("openQuestion", new AbstractAction() {
			private static final long	serialVersionUID	= 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				final Trivia trivia = client.getTrivia();
				final int nQuestions = trivia.getNQuestions();
				final int nextToOpen = trivia.nextToOpen();
				new OpenQuestionDialog(server, client, nQuestions, nextToOpen);
			}
		});
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
