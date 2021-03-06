package net.bubbaland.trivia.client.tabpanel;

// imports for GUI
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;

import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.client.TriviaClient;
import net.bubbaland.trivia.client.TriviaFrame;
import net.bubbaland.trivia.client.TriviaMainPanel;
import net.bubbaland.trivia.client.dialog.NewQuestionDialog;

/**
 * A panel for the workflow tab, which contains most of the trivia operations
 *
 * @author Walter Kolczynski
 *
 */
public class WorkflowPanel extends TriviaMainPanel {

	/** The Constant serialVersionUID. */
	private static final long			serialVersionUID	= -5608314912146842278L;

	// Sub-panels of the workflow panel
	private final SummaryPanel			workflowHeaderPanel;
	private final OpenQuestionsPanel	workflowQlistPanel;
	private final AnswerQueuePanel		workflowQueuePanel;

	/**
	 * Instantiates a new workflow panel.
	 *
	 * @param client
	 *            The local trivia client
	 * @param frame
	 *            The top-level frame containing this panel
	 */
	public WorkflowPanel(final TriviaClient client, final TriviaFrame frame) {

		super(client, frame);

		// Create the sub-panels
		this.workflowHeaderPanel = new SummaryPanel(client, frame);
		this.workflowQlistPanel = new OpenQuestionsPanel(client, frame);
		this.workflowQueuePanel = new AnswerQueuePanel(client, frame);

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

		final JSplitPane splitPane =
				new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.workflowQlistPanel, this.workflowQueuePanel);
		splitPane.setResizeWeight(0.0);
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		this.add(splitPane, constraints);

		// Assign CTRL+O to open a new question
		this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK), "openQuestion");
		this.getActionMap().put("openQuestion", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				final Trivia trivia = client.getTrivia();
				final int nQuestions = trivia.getCurrentRound().getNQuestions();
				final int nextToOpen = trivia.getCurrentRound().nextToOpen();
				new NewQuestionDialog(client, client.getCurrentRoundNumber(), nQuestions, nextToOpen, true);
			}
		});
	}

	@Override
	public void loadProperties(Properties properties) {
		this.workflowHeaderPanel.loadProperties(properties);
		this.workflowQlistPanel.loadProperties(properties);
		this.workflowQueuePanel.loadProperties(properties);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void updateGUI(boolean force) {
		this.workflowHeaderPanel.updateGUIonEDT(force);
		this.workflowQlistPanel.updateGUIonEDT(force);
		this.workflowQueuePanel.updateGUIonEDT(force);
	}

	@Override
	public void changeFrame(TriviaFrame newFrame) {
		super.changeFrame(newFrame);
		this.workflowHeaderPanel.changeFrame(newFrame);
		this.workflowQlistPanel.changeFrame(newFrame);
		this.workflowQueuePanel.changeFrame(newFrame);
	}

}
