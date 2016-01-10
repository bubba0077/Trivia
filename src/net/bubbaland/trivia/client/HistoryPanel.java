package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.math.BigInteger;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

import net.bubbaland.trivia.Trivia;

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
public class HistoryPanel extends TriviaMainPanel implements ItemListener {

	/** The Constant serialVersionUID. */
	final private static long			serialVersionUID	= -5094201314926851039L;

	/**
	 * GUI Elements that will need to be updated
	 */
	private final JComboBox<String>		roundSelector;
	private final RoundQuestionsPanel	roundQuestionPanel;
	private final AnswerQueuePanel		answerQueuePanel;
	private final JLabel				roundScoreLabel, totalScoreLabel, placeScoreLabel, roundLabel, totalLabel,
			placeLabel, blank0, blank1;

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
	@SuppressWarnings("unchecked")
	public HistoryPanel(TriviaClient client, TriviaFrame parent) {

		super(client, parent);

		this.nRounds = client.getTrivia().getNRounds();

		this.roundQuestionPanel = new RoundQuestionsPanel(client, parent, false, 1);
		this.answerQueuePanel = new AnswerQueuePanel(client, parent, 1);

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
		this.roundSelector = new JComboBox<String>(rNumbers);
		this.roundSelector
				.setRenderer(new RoundCellRenderer((ListCellRenderer<String>) this.roundSelector.getRenderer()));
		this.roundSelector.addItemListener(this);
		panel.add(this.roundSelector, solo);

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

		/**
		 * Add a question list panel to show the selected round data
		 */
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 9;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;

		final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.roundQuestionPanel,
				this.answerQueuePanel);
		splitPane.setResizeWeight(0.0);
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		this.add(splitPane, constraints);

		this.loadProperties(TriviaGUI.PROPERTIES);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public synchronized void itemStateChanged(ItemEvent e) {
		final JComboBox<String> source = (JComboBox<String>) e.getSource();
		final int rNumber = Integer.parseInt((String) source.getSelectedItem());
		this.roundQuestionPanel.setRound(rNumber);
		this.answerQueuePanel.setRoundNumber(rNumber);
		this.updateGUI();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void updateGUI(boolean force) {
		final Trivia trivia = this.client.getTrivia();
		final int rNumber = Integer.parseInt((String) this.roundSelector.getSelectedItem());

		this.roundScoreLabel.setText(trivia.getEarned(rNumber) + " / " + trivia.getValue(rNumber));
		this.totalScoreLabel.setText(trivia.getCumulativeEarned(rNumber) + " / " + trivia.getCumulativeValue(rNumber));
		if (trivia.isAnnounced(rNumber)) {
			this.placeScoreLabel.setText(
					TriviaGUI.ordinalize(trivia.getAnnouncedPlace(rNumber)) + " / " + trivia.getNTeams() + " ");
		} else {
			this.placeScoreLabel.setText("-- / " + trivia.getNTeams() + " ");
		}

		this.roundQuestionPanel.updateGUI(force);
		this.answerQueuePanel.updateGUI(force);
	}

	@Override
	protected void loadProperties(Properties properties) {
		/**
		 * Colors
		 */
		final Color headerBackgroundColor = new Color(
				new BigInteger(properties.getProperty("History.Header.BackgroundColor"), 16).intValue());
		final Color selectorColor = new Color(
				new BigInteger(properties.getProperty("History.Header.Selector.Color"), 16).intValue());
		final Color selectorBackgroundColor = new Color(
				new BigInteger(properties.getProperty("History.Header.Selector.BackgroundColor"), 16).intValue());
		final Color roundColor = new Color(
				new BigInteger(properties.getProperty("History.Header.Round.Color"), 16).intValue());
		final Color totalColor = new Color(
				new BigInteger(properties.getProperty("History.Header.Total.Color"), 16).intValue());
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

		final int selectorHeight = Integer.parseInt(properties.getProperty("History.Header.Selector.Height"));
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

		setComboBoxProperties(this.roundSelector, selectorWidth, selectorHeight, selectorColor, selectorBackgroundColor,
				headerBackgroundColor, headerFontSize);

		this.roundQuestionPanel.loadProperties(properties);
	}

	@Override
	public void changeFrame(TriviaFrame newFrame) {
		super.changeFrame(newFrame);
		this.roundQuestionPanel.changeFrame(newFrame);
	}

	private class RoundCellRenderer implements ListCellRenderer<String> {
		private final ListCellRenderer<String> wrapped;

		public RoundCellRenderer(ListCellRenderer<String> listCellRenderer) {
			this.wrapped = listCellRenderer;
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
				boolean isSelected, boolean cellHasFocus) {
			final String displayName = String.valueOf(value); // customize here
			final Component renderer = this.wrapped.getListCellRendererComponent(list, displayName, index, isSelected,
					cellHasFocus);
			if (renderer instanceof JLabel) {
				( (JLabel) renderer ).setHorizontalAlignment(SwingConstants.RIGHT);
				if (isSelected) {
					( (JLabel) renderer ).setForeground(HistoryPanel.this.roundSelector.getBackground());
					( (JLabel) renderer ).setBackground(HistoryPanel.this.roundSelector.getForeground());
				} else {
					( (JLabel) renderer ).setForeground(HistoryPanel.this.roundSelector.getForeground());
					( (JLabel) renderer ).setBackground(HistoryPanel.this.roundSelector.getBackground());
				}
			}
			return renderer;
		}
	}
}
