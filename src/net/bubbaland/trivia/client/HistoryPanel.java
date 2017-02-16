package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigInteger;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.messages.SetShowHostMessage;
import net.bubbaland.trivia.messages.SetShowNameMessage;

/**
 * A panel to select and display any round.
 *
 * The <code>History</code> class is a panel that allows you to display the question data for any round. There are two
 * parts: a top area that has a selector to choose the desired round, and a <code>RoundQlistPanel</code> that displays
 * the question data for the chosen round.
 *
 * @author Walter Kolczynski
 *
 */
public class HistoryPanel extends TriviaMainPanel implements ActionListener, ChangeListener {

	/** The Constant serialVersionUID. */
	final private static long			serialVersionUID	= -5094201314926851039L;

	/**
	 * GUI Elements that will need to be updated
	 */
	private final JSpinner				roundSpinner;
	private final RoundQuestionsPanel	roundQuestionPanel;
	private final AnswerQueuePanel		answerQueuePanel;
	private final JLabel				roundScoreLabel, totalScoreLabel, placeScoreLabel, roundLabel, totalLabel,
			placeLabel, blank0, blank1, showLabel, showNameLabel, hostLabel, showHostLabel;
	private final JPopupMenu			showNameMenu, showHostMenu;
	private final JTextField			showNameTextField, showHostTextField;

	/**
	 * Data
	 */
	private final int					nRounds;

	/**
	 * Instantiates a new history panel.
	 *
	 * @param client
	 *            The local trivia client
	 */
	public HistoryPanel(TriviaClient client, TriviaFrame parent) {

		super(client, parent);

		this.nRounds = client.getTrivia().getNRounds();

		this.roundQuestionPanel = new RoundQuestionsPanel(client, parent, false, 1);
		this.answerQueuePanel = new AnswerQueuePanel(client, parent, 1);

		/**
		 * Build context menus
		 */
		this.showNameMenu = new JPopupMenu();
		this.showNameTextField = new JTextField();
		this.showNameTextField.setPreferredSize(new Dimension(100, 25));
		this.showNameTextField.setActionCommand("Set Show Name");
		this.showNameTextField.addActionListener(this);
		this.showNameMenu.add(this.showNameTextField);
		this.add(this.showNameMenu);

		this.showHostMenu = new JPopupMenu();
		this.showHostTextField = new JTextField();
		this.showHostTextField.setPreferredSize(new Dimension(100, 25));
		this.showHostTextField.setActionCommand("Set Show Host");
		this.showHostTextField.addActionListener(this);
		this.showHostMenu.add(this.showHostTextField);
		this.add(this.showHostMenu);

		// Set up layout constraints
		final GridBagConstraints solo = new GridBagConstraints();
		solo.fill = GridBagConstraints.BOTH;
		solo.anchor = GridBagConstraints.NORTH;
		solo.weightx = 1.0;
		solo.weighty = 1.0;
		solo.gridx = 0;
		solo.gridy = 0;

		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;

		/**
		 * Create the top row with the selector combo box
		 */
		constraints.weightx = 0.0;
		constraints.gridx = 0;
		constraints.gridy = 0;
		this.roundLabel = this.enclosedLabel(" Round:", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);
		constraints.weightx = 0.0;

		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.weightx = 0.0;
		final JPanel panel = new JPanel(new GridBagLayout());
		this.add(panel, constraints);
		final String[] rNumbers = new String[this.nRounds];
		for (int r = 0; r < this.nRounds; r++) {
			rNumbers[r] = ( r + 1 ) + "";
		}
		this.roundSpinner = new JSpinner(new SpinnerNumberModel(1, 1, this.nRounds, 1));
		this.roundSpinner.addChangeListener(this);
		panel.add(this.roundSpinner, solo);

		constraints.gridx = 2;
		constraints.gridy = 0;
		this.roundScoreLabel = this.enclosedLabel("", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

		constraints.weightx = 1.0;
		constraints.gridx = 3;
		constraints.gridy = 0;
		this.blank0 = this.enclosedLabel("", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);
		constraints.weightx = 0.0;

		constraints.gridx = 4;
		constraints.gridy = 0;
		this.totalLabel = this.enclosedLabel("Total: ", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

		constraints.gridx = 5;
		constraints.gridy = 0;
		this.totalScoreLabel = this.enclosedLabel("", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

		constraints.weightx = 1.0;
		constraints.gridx = 6;
		constraints.gridy = 0;
		this.blank1 = this.enclosedLabel("", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);
		constraints.weightx = 0.0;

		constraints.gridx = 7;
		constraints.gridy = 0;
		this.placeLabel = this.enclosedLabel("Place: ", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

		constraints.gridx = 8;
		constraints.gridy = 0;
		this.placeScoreLabel = this.enclosedLabel("", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

		constraints.gridx = 0;
		constraints.gridy = 1;
		this.showLabel = this.enclosedLabel("Show: ", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);
		this.showLabel.addMouseListener(new PopupListener(this.showNameMenu));

		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.gridwidth = 3;
		this.showNameLabel = this.enclosedLabel("", constraints, SwingConstants.LEFT, SwingConstants.CENTER);
		this.showNameLabel.addMouseListener(new PopupListener(this.showNameMenu));
		constraints.gridwidth = 1;

		constraints.gridx = 4;
		constraints.gridy = 1;
		this.hostLabel = this.enclosedLabel("Host: ", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);
		this.hostLabel.addMouseListener(new PopupListener(this.showHostMenu));

		constraints.gridx = 5;
		constraints.gridy = 1;
		constraints.gridwidth = 4;
		this.showHostLabel = this.enclosedLabel("", constraints, SwingConstants.LEFT, SwingConstants.CENTER);
		this.showHostLabel.addMouseListener(new PopupListener(this.showHostMenu));
		constraints.gridwidth = 1;

		/**
		 * Add a question list panel to show the selected round data
		 */
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 9;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;

		final JSplitPane splitPane =
				new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.roundQuestionPanel, this.answerQueuePanel);
		splitPane.setResizeWeight(0.0);
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		this.add(splitPane, constraints);

		this.loadProperties(TriviaGUI.PROPERTIES);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void updateGUI(boolean force) {
		final Trivia trivia = this.client.getTrivia();
		final int rNumber = ( (Integer) this.roundSpinner.getValue() ).intValue();

		this.roundScoreLabel
				.setText(trivia.getRound(rNumber).getEarned() + " / " + trivia.getRound(rNumber).getValue());
		this.totalScoreLabel.setText(trivia.getCumulativeEarned(rNumber) + " / " + trivia.getCumulativeValue(rNumber));
		if (trivia.getRound(rNumber).isAnnounced()) {
			this.placeScoreLabel.setText(
					TriviaGUI.ordinalize(trivia.getRound(rNumber).getPlace()) + " / " + trivia.getNTeams() + " ");
		} else {
			this.placeScoreLabel.setText("-- / " + trivia.getNTeams() + " ");
		}

		this.showNameLabel.setText(trivia.getRound(rNumber).getShowName());
		this.showHostLabel.setText(trivia.getRound(rNumber).getShowHost());

		this.roundQuestionPanel.updateGUI(force);
		this.answerQueuePanel.updateGUI(force);
	}

	@Override
	protected void loadProperties(Properties properties) {
		/**
		 * Colors
		 */
		final Color headerBackgroundColor =
				new Color(new BigInteger(properties.getProperty("History.Header.BackgroundColor"), 16).intValue());
		final Color roundColor =
				new Color(new BigInteger(properties.getProperty("History.Header.Round.Color"), 16).intValue());
		final Color totalColor =
				new Color(new BigInteger(properties.getProperty("History.Header.Total.Color"), 16).intValue());
		final Color placeColor = new Color(new BigInteger(properties.getProperty("Announced.Color"), 16).intValue());

		/**
		 * Sizes
		 */
		final int headerHeight = Integer.parseInt(properties.getProperty("History.Header.Height"));

		final int roundLabelWidth = Integer.parseInt(properties.getProperty("History.Header.Round.Label.Width"));
		final int roundScoreWidth = Integer.parseInt(properties.getProperty("History.Header.Round.Score.Width"));
		final int totalLabelWidth = Integer.parseInt(properties.getProperty("History.Header.Total.Label.Width"));
		final int totalScoreWidth = Integer.parseInt(properties.getProperty("History.Header.Total.Score.Width"));
		final int placeLabelWidth = Integer.parseInt(properties.getProperty("History.Header.Place.Label.Width"));
		final int placeWidth = Integer.parseInt(properties.getProperty("History.Header.Place.Width"));

		final int selectorWidth = Integer.parseInt(properties.getProperty("History.Header.Selector.Width"));

		/**
		 * Font sizes
		 */
		final float headerFontSize = Float.parseFloat(properties.getProperty("History.Header.FontSize"));

		setLabelProperties(this.roundLabel, roundLabelWidth, headerHeight, roundColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.roundScoreLabel, roundScoreWidth, headerHeight, roundColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.totalLabel, totalLabelWidth, headerHeight, totalColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.totalScoreLabel, totalScoreWidth, headerHeight, totalColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.placeLabel, placeLabelWidth, headerHeight, placeColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.placeScoreLabel, placeWidth, headerHeight, placeColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.blank0, -1, headerHeight, roundColor, headerBackgroundColor, headerFontSize);
		setLabelProperties(this.blank1, -1, headerHeight, roundColor, headerBackgroundColor, headerFontSize);
		this.roundSpinner.setFont(this.roundSpinner.getFont().deriveFont(headerFontSize));
		setPanelProperties((JPanel) this.roundSpinner.getParent(), selectorWidth, headerHeight, headerBackgroundColor);

		// setComboBoxProperties(this.roundSpinner, selectorWidth, selectorHeight, selectorColor,
		// selectorBackgroundColor,
		// headerBackgroundColor, headerFontSize);

		setLabelProperties(this.showLabel, roundLabelWidth, headerHeight, roundColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.showNameLabel, selectorWidth + roundScoreWidth, headerHeight, roundColor,
				headerBackgroundColor, headerFontSize);
		setLabelProperties(this.hostLabel, totalScoreWidth, headerHeight, roundColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.showHostLabel, placeLabelWidth + placeWidth, headerHeight, roundColor,
				headerBackgroundColor, headerFontSize);

		this.roundQuestionPanel.loadProperties(properties);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public synchronized void actionPerformed(ActionEvent event) {
		final int rNumber = ( (Integer) HistoryPanel.this.roundSpinner.getValue() ).intValue();
		switch (event.getActionCommand()) {
			case "Set Show Name":
				final String showName = this.showNameTextField.getText();
				( new SwingWorker<Void, Void>() {
					@Override
					public Void doInBackground() {
						HistoryPanel.this.client.sendMessage(new SetShowNameMessage(rNumber, showName));
						return null;
					}

					@Override
					public void done() {

					}
				} ).execute();
				this.client.log("Changed show name to " + showName + " for round " + rNumber);
				break;
			case "Set Show Host":
				final String showHost = this.showHostTextField.getText();
				( new SwingWorker<Void, Void>() {
					@Override
					public Void doInBackground() {
						HistoryPanel.this.client.sendMessage(new SetShowHostMessage(rNumber, showHost));
						return null;
					}

					@Override
					public void done() {

					}
				} ).execute();
				this.client.log("Changed host name to " + showHost);
				break;
		}
	}

	@Override
	public void changeFrame(TriviaFrame newFrame) {
		super.changeFrame(newFrame);
		this.roundQuestionPanel.changeFrame(newFrame);
	}

	private class PopupListener extends MouseAdapter {

		private final JPopupMenu menu;

		public PopupListener(JPopupMenu menu) {
			this.menu = menu;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			this.checkForPopup(e);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			this.checkForPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			this.checkForPopup(e);
		}

		private void checkForPopup(MouseEvent event) {
			final JComponent source = (JComponent) event.getSource();
			final int rNumber = ( (Integer) HistoryPanel.this.roundSpinner.getValue() ).intValue();
			HistoryPanel.this.showNameTextField
					.setText(HistoryPanel.this.client.getTrivia().getRound(rNumber).getShowName());
			HistoryPanel.this.showHostTextField
					.setText(HistoryPanel.this.client.getTrivia().getRound(rNumber).getShowHost());
			if (event.isPopupTrigger()) {
				this.menu.show(source, event.getX(), event.getY());
			}
		}

	}

	@Override
	public void stateChanged(ChangeEvent event) {
		final int rNumber = ( (Integer) this.roundSpinner.getValue() ).intValue();
		this.roundQuestionPanel.setRound(rNumber);
		this.answerQueuePanel.setRoundNumber(rNumber);
		this.updateGUI();
	}
}
