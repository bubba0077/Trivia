package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.bubbaland.trivia.ScoreEntry;

public class StandingsPanel extends TriviaMainPanel implements ChangeListener {

	/** The Constant serialVersionUID. */
	final private static long		serialVersionUID	= -5094201314926851039L;

	/**
	 * GUI Elements that will need to be updated
	 */
	private final JLabel			roundLabel, placeLabel, teamNameLabel, scoreLabel, pointsBackLabel, spacer;
	private final JSpinner			roundSpinner;
	private final StandingsSubPanel	standingsSubPanel;
	private final JScrollPane		scrollPane;

	/**
	 * Data
	 */
	private int						nRounds, lastAnnounced;

	public StandingsPanel(TriviaClient client, TriviaFrame parent) {
		super(client, parent);

		this.lastAnnounced = 0;
		this.nRounds = client.getTrivia().getNRounds();

		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;

		final GridBagConstraints solo = new GridBagConstraints();
		solo.fill = GridBagConstraints.BOTH;
		solo.anchor = GridBagConstraints.NORTH;
		solo.weightx = 1.0;
		solo.weighty = 1.0;
		solo.gridx = 0;
		solo.gridy = 0;

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 3;
		constraints.weightx = 1.0;
		this.roundLabel = this.enclosedLabel("Standings for Round ", constraints, SwingConstants.RIGHT,
				SwingConstants.CENTER);
		constraints.weightx = 0.0;
		constraints.gridwidth = 1;

		constraints.gridx = 3;
		constraints.gridy = 0;
		constraints.gridwidth = 2;
		final JPanel panel = new JPanel(new GridBagLayout());
		this.add(panel, constraints);
		final String[] rNumbers = new String[this.nRounds];
		for (int r = 0; r < this.nRounds; r++) {
			rNumbers[r] = ( r + 1 ) + "";
		}
		this.roundSpinner = new JSpinner(new SpinnerNumberModel(1, 1, this.nRounds, 1));
		this.roundSpinner.addChangeListener(this);
		panel.add(this.roundSpinner, solo);
		constraints.gridwidth = 1;

		constraints.gridx = 0;
		constraints.gridy = 1;
		this.placeLabel = this.enclosedLabel("Pl", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.weightx = 1.0;
		this.teamNameLabel = this.enclosedLabel("Team Name", constraints, SwingConstants.LEFT, SwingConstants.CENTER);
		constraints.gridwidth = 1;
		constraints.weightx = 0.0;

		constraints.gridx = 2;
		constraints.gridy = 1;
		this.scoreLabel = this.enclosedLabel("Score", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

		constraints.gridx = 3;
		constraints.gridy = 1;
		this.pointsBackLabel = this.enclosedLabel("PB", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

		constraints.gridx = 4;
		constraints.gridy = 1;
		this.spacer = this.enclosedLabel("", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 5;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		final JPanel scrollPanel = new JPanel(new GridBagLayout());
		this.scrollPane = new JScrollPane(scrollPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		this.scrollPane.setBorder(BorderFactory.createEmptyBorder());
		this.scrollPane.getVerticalScrollBar().setUnitIncrement(5);
		this.add(this.scrollPane, constraints);
		this.standingsSubPanel = new StandingsSubPanel();
		scrollPanel.add(this.standingsSubPanel, constraints);

		this.loadProperties(TriviaGUI.PROPERTIES);

	}

	private class StandingsSubPanel extends TriviaMainPanel {

		private static final long	serialVersionUID	= -2312875502979435229L;

		private int					nTeams;

		private JLabel[]			placeLabels, teamLabels, scoreLabels, pointsBackLabels;
		private Color				foregroundColor, highlightColor, altBackgroundColor, backgroundColor;
		private int					rowHeight, scoreWidth, placeWidth, teamWidth, pointsBackWidth, altColorInterval,
				lastRNumber;
		private float				fontSize;

		public StandingsSubPanel() {
			super(StandingsPanel.this.client, StandingsPanel.this.frame);
			this.nTeams = 0;
			this.lastRNumber = 0;

			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.NORTH;
			constraints.weightx = 0.0;
			constraints.weighty = 0.0;

			this.placeLabels = new JLabel[0];
			this.teamLabels = new JLabel[0];
			this.scoreLabels = new JLabel[0];
			this.pointsBackLabels = new JLabel[0];

			this.loadProperties(TriviaGUI.PROPERTIES);

			updateGUI();

		}

		@Override
		public void updateGUI(boolean forceUpdate) {
			if (StandingsPanel.this.client.getTrivia().getNTeams() != this.nTeams) {
				resetRows();
			}

			final int rNumber = ( (Integer) StandingsPanel.this.roundSpinner.getValue() ).intValue();

			if (rNumber == this.lastRNumber && !forceUpdate) {
				return;
			}

			ScoreEntry[] standings = StandingsPanel.this.client.getTrivia().getStandings(rNumber);

			if (standings == null) {
				for (int t = 0; t < nTeams; t++) {
					this.placeLabels[t].setText(( t + 1 ) + "");
					this.teamLabels[t].setText("");
					this.scoreLabels[t].setText("");
					this.pointsBackLabels[t].setText("");
				}
				return;
			}

			Arrays.sort(standings);

			final int maxScore = standings[0].getScore();

			for (int t = 0; t < this.nTeams; t++) {
				ScoreEntry entry = standings[t];
				String teamName = entry.getTeamName();
				final int score = entry.getScore();
				if (t > 0 && entry.getPlace() == standings[t - 1].getPlace()) {
					this.placeLabels[t].setText("");
				} else {
					this.placeLabels[t].setText(entry.getPlace() + "");
				}
				this.teamLabels[t].setText(teamName);
				this.scoreLabels[t].setText(score + "");
				final int pointsBack = maxScore - score;
				this.pointsBackLabels[t].setText(pointsBack + "");
				if (teamName.equalsIgnoreCase(StandingsPanel.this.client.getTrivia().getTeamName())) {
					this.placeLabels[t].setForeground(this.highlightColor);
					this.teamLabels[t].setForeground(this.highlightColor);
					this.scoreLabels[t].setForeground(this.highlightColor);
					this.pointsBackLabels[t].setForeground(this.highlightColor);
				} else {
					this.placeLabels[t].setForeground(this.foregroundColor);
					this.teamLabels[t].setForeground(this.foregroundColor);
					this.scoreLabels[t].setForeground(this.foregroundColor);
					this.pointsBackLabels[t].setForeground(this.foregroundColor);
				}
			}


		}

		private void resetRows() {
			this.nTeams = StandingsPanel.this.client.getTrivia().getNTeams();

			if (this.placeLabels != null) {
				for (JLabel placeLabel : this.placeLabels) {
					this.remove(placeLabel.getParent());
				}
			}

			if (this.teamLabels != null) {
				for (JLabel teamLabel : this.teamLabels) {
					this.remove(teamLabel.getParent());
				}
			}

			if (this.scoreLabels != null) {
				for (JLabel scoreLabel : this.scoreLabels) {
					this.remove(scoreLabel.getParent());
				}
			}

			if (this.pointsBackLabels != null) {
				for (JLabel pointsBackLabel : this.pointsBackLabels) {
					this.remove(pointsBackLabel.getParent());
				}
			}

			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.NORTH;
			constraints.weightx = 0.0;
			constraints.weighty = 0.0;

			this.placeLabels = new JLabel[this.nTeams];
			this.teamLabels = new JLabel[this.nTeams];
			this.scoreLabels = new JLabel[this.nTeams];
			this.pointsBackLabels = new JLabel[this.nTeams];

			for (int r = 0; r < nTeams; r++) {
				constraints.gridx = 0;
				constraints.gridy = r;
				this.placeLabels[r] = this.enclosedLabel("" + ( r + 1 ), constraints, SwingConstants.CENTER,
						SwingConstants.CENTER);

				constraints.gridx = 1;
				constraints.gridy = r;
				constraints.weightx = 1.0;
				this.teamLabels[r] = this.enclosedLabel("", constraints, SwingConstants.LEFT, SwingConstants.CENTER);
				constraints.weightx = 0.0;

				constraints.gridx = 2;
				constraints.gridy = r;
				this.scoreLabels[r] = this.enclosedLabel("", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

				constraints.gridx = 3;
				constraints.gridy = r;
				this.pointsBackLabels[r] = this.enclosedLabel("", constraints, SwingConstants.RIGHT,
						SwingConstants.CENTER);

				final Color bColor = ( ( r + 1 ) % altColorInterval == 0 ) ? altBackgroundColor : backgroundColor;
				setLabelProperties(this.placeLabels[r], placeWidth, rowHeight, foregroundColor, bColor, fontSize);
				setLabelProperties(this.teamLabels[r], teamWidth, rowHeight, foregroundColor, bColor, fontSize);
				setLabelProperties(this.scoreLabels[r], scoreWidth, rowHeight, foregroundColor, bColor, fontSize);
				setLabelProperties(this.pointsBackLabels[r], pointsBackWidth, rowHeight, foregroundColor, bColor,
						fontSize);
			}
		}

		@Override
		protected void loadProperties(Properties properties) {
			/**
			 * Colors
			 */
			this.foregroundColor = new Color(
					new BigInteger(properties.getProperty("Standings.ForegroundColor"), 16).intValue());
			this.highlightColor = new Color(
					new BigInteger(properties.getProperty("Standings.HighlightColor"), 16).intValue());
			this.backgroundColor = new Color(
					new BigInteger(properties.getProperty("Standings.BackgroundColor"), 16).intValue());
			this.altBackgroundColor = new Color(
					new BigInteger(properties.getProperty("Standings.AltBackgroundColor"), 16).intValue());

			/**
			 * Sizes
			 */

			this.rowHeight = Integer.parseInt(properties.getProperty("Standings.Row.Height"));
			this.scoreWidth = Integer.parseInt(properties.getProperty("Standings.Score.Width"));
			this.placeWidth = Integer.parseInt(properties.getProperty("Standings.Place.Width"));
			this.teamWidth = Integer.parseInt(properties.getProperty("Standings.Team.Width"));
			this.pointsBackWidth = Integer.parseInt(properties.getProperty("Standings.PointsBack.Width"));

			/**
			 * Font sizes
			 */
			this.fontSize = Float.parseFloat(properties.getProperty("Standings.FontSize"));

			/** The number of open questions to show at one time */
			this.altColorInterval = Integer.parseInt(properties.getProperty("Standings.AltInterval"));

			for (int r = 0; r < this.nTeams; r++) {
				final Color bColor = ( ( r + 1 ) % altColorInterval == 0 ) ? altBackgroundColor : backgroundColor;
				setLabelProperties(this.placeLabels[r], placeWidth, rowHeight, foregroundColor, bColor, fontSize);
				setLabelProperties(this.teamLabels[r], teamWidth, rowHeight, foregroundColor, bColor, fontSize);
				setLabelProperties(this.scoreLabels[r], scoreWidth, rowHeight, foregroundColor, bColor, fontSize);
				setLabelProperties(this.pointsBackLabels[r], pointsBackWidth, rowHeight, foregroundColor, bColor,
						fontSize);
			}
		}
	}

	@Override
	public void updateGUI(boolean forceUpdate) {
		if (this.client.getTrivia().getLastAnnounced() != this.lastAnnounced) {
			this.lastAnnounced = this.client.getTrivia().getLastAnnounced();
			this.roundSpinner.setValue(this.lastAnnounced - 1);
			forceUpdate = true;
		}
		this.standingsSubPanel.updateGUI(forceUpdate);
	}

	@Override
	protected void loadProperties(Properties properties) {
		/**
		 * Colors
		 */
		final Color foregroundColor = new Color(
				new BigInteger(properties.getProperty("Standings.Header.ForegroundColor"), 16).intValue());
		final Color headerBackgroundColor = new Color(
				new BigInteger(properties.getProperty("Standings.Header.BackgroundColor"), 16).intValue());

		/**
		 * Sizes
		 */

		final int headerHeight = Integer.parseInt(properties.getProperty("Standings.Header.Height"));
		final int roundWidth = Integer.parseInt(properties.getProperty("Standings.Header.Round.Width"));
		final int scoreWidth = Integer.parseInt(properties.getProperty("Standings.Score.Width"));
		final int placeWidth = Integer.parseInt(properties.getProperty("Standings.Place.Width"));
		final int teamWidth = Integer.parseInt(properties.getProperty("Standings.Team.Width"));
		final int pointsBackWidth = Integer.parseInt(properties.getProperty("Standings.PointsBack.Width"));

		final int selectorWidth = Integer.parseInt(properties.getProperty("Standings.Header.Selector.Width"));

		/**
		 * Font sizes
		 */
		final float headerFontSize = Float.parseFloat(properties.getProperty("History.Header.FontSize"));

		setLabelProperties(this.roundLabel, roundWidth, headerHeight / 2, foregroundColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.placeLabel, placeWidth, headerHeight / 2, foregroundColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.teamNameLabel, teamWidth, headerHeight / 2, foregroundColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.scoreLabel, scoreWidth, headerHeight / 2, foregroundColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.pointsBackLabel, pointsBackWidth, headerHeight / 2, foregroundColor,
				headerBackgroundColor, headerFontSize);
		this.roundSpinner.setFont(this.roundSpinner.getFont().deriveFont(headerFontSize));
		setPanelProperties((JPanel) this.roundSpinner.getParent(), selectorWidth, headerHeight / 2,
				headerBackgroundColor);

		final int scrollBarWidth;
		if (UIManager.getLookAndFeel().getName().equals("Nimbus")) {
			scrollBarWidth = (int) UIManager.get("ScrollBar.thumbHeight");
		} else {
			scrollBarWidth = ( (Integer) UIManager.get("ScrollBar.width") ).intValue();
		}
		setLabelProperties(this.spacer, scrollBarWidth, headerHeight / 2, foregroundColor, headerBackgroundColor,
				headerFontSize);

		this.standingsSubPanel.loadProperties(properties);

	}

	@Override
	public void stateChanged(ChangeEvent e) {
		this.updateGUI(false);
	}


}
