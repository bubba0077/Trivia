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
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import net.bubbaland.trivia.Trivia;

/**
 * A panel that displays the scores from each round.
 * 
 * @author Walter Kolczynski
 * 
 */
public class ScoreByRoundPanel extends TriviaMainPanel {

	/** The Constant serialVersionUID. */
	private static final long			serialVersionUID	= -2047479093232798581L;

	/**
	 * GUI Elements to update
	 */
	final private JLabel				roundLabel0, roundEarnedLabel0, roundValueLabel0, roundPercentLabel0,
			totalEarnedLabel0, totalValueLabel0, totalPercentLabel0, announcedLabel0, placeLabel0, discrepancyLabel0;
	final private JLabel				roundLabel1, roundEarnedLabel1, roundValueLabel1, roundPercentLabel1,
			totalEarnedLabel1, totalValueLabel1, totalPercentLabel1, announcedLabel1, placeLabel1, discrepancyLabel1;

	/** The scroll panel that will hold the round data */
	final private InternalScrollPanel	internalScrollPanel;

	/**
	 * Instantiates a new score by round panel.
	 * 
	 * @param client
	 *            The local trivia client
	 * 
	 */
	public ScoreByRoundPanel(TriviaClient client) {

		super();

		// Set up the layout constraints
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.NORTH;

		constraints.weightx = 0.0;
		constraints.weighty = 0.0;

		/**
		 * Create the header
		 */
		constraints.gridx = 0;
		constraints.gridy = 0;
		this.roundLabel0 = this.enclosedLabel("", constraints, SwingConstants.CENTER, SwingConstants.CENTER);
		constraints.gridx = 0;
		constraints.gridy = 1;
		this.roundLabel1 = this.enclosedLabel("Hour", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

		constraints.gridx = 1;
		constraints.gridy = 0;
		this.roundEarnedLabel0 = this.enclosedLabel("", constraints, SwingConstants.CENTER, SwingConstants.CENTER);
		constraints.gridx = 1;
		constraints.gridy = 1;
		this.roundEarnedLabel1 = this
				.enclosedLabel("Earned", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

		constraints.gridx = 2;
		constraints.gridy = 0;
		this.roundValueLabel0 = this.enclosedLabel("", constraints, SwingConstants.CENTER, SwingConstants.CENTER);
		constraints.gridx = 2;
		constraints.gridy = 1;
		this.roundValueLabel1 = this.enclosedLabel("Possible", constraints, SwingConstants.CENTER,
				SwingConstants.CENTER);

		constraints.gridx = 3;
		constraints.gridy = 0;
		this.roundPercentLabel0 = this.enclosedLabel("", constraints, SwingConstants.CENTER, SwingConstants.CENTER);
		constraints.gridx = 3;
		constraints.gridy = 1;
		this.roundPercentLabel1 = this.enclosedLabel("Percent", constraints, SwingConstants.CENTER,
				SwingConstants.CENTER);

		constraints.gridx = 4;
		constraints.gridy = 0;
		this.totalEarnedLabel0 = this.enclosedLabel("Cumulative", constraints, SwingConstants.CENTER,
				SwingConstants.CENTER);
		constraints.gridx = 4;
		constraints.gridy = 1;
		this.totalEarnedLabel1 = this.enclosedLabel("Score", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

		constraints.gridx = 5;
		constraints.gridy = 0;
		this.totalValueLabel0 = this.enclosedLabel("Cumulative", constraints, SwingConstants.CENTER,
				SwingConstants.CENTER);
		constraints.gridx = 5;
		constraints.gridy = 1;
		this.totalValueLabel1 = this.enclosedLabel("Possible", constraints, SwingConstants.CENTER,
				SwingConstants.CENTER);

		constraints.gridx = 6;
		constraints.gridy = 0;
		this.totalPercentLabel0 = this.enclosedLabel("Percent", constraints, SwingConstants.CENTER,
				SwingConstants.CENTER);
		constraints.gridx = 6;
		constraints.gridy = 1;
		this.totalPercentLabel1 = this
				.enclosedLabel("Total", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

		constraints.gridx = 7;
		constraints.gridy = 0;
		this.announcedLabel0 = this.enclosedLabel("Announced", constraints, SwingConstants.CENTER,
				SwingConstants.CENTER);
		constraints.gridx = 7;
		constraints.gridy = 1;
		this.announcedLabel1 = this.enclosedLabel("Score", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

		constraints.gridx = 8;
		constraints.gridy = 0;
		this.placeLabel0 = this.enclosedLabel("", constraints, SwingConstants.CENTER, SwingConstants.CENTER);
		constraints.gridx = 8;
		constraints.gridy = 1;
		this.placeLabel1 = this.enclosedLabel("Place", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

		constraints.weightx = 1.0;
		constraints.gridx = 9;
		constraints.gridy = 0;
		this.discrepancyLabel0 = this.enclosedLabel("", constraints, SwingConstants.CENTER, SwingConstants.CENTER);
		constraints.gridx = 9;
		constraints.gridy = 1;
		this.discrepancyLabel1 = this.enclosedLabel("Discrepancy Notes", constraints, SwingConstants.CENTER,
				SwingConstants.CENTER);
		constraints.weightx = 0.00;

		/**
		 * Create a scroll panel with the round data the round data
		 */
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 11;
		this.internalScrollPanel = new InternalScrollPanel(client);
		final JScrollPane scrollPane = new JScrollPane(this.internalScrollPanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(200, 200));
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		this.add(scrollPane, constraints);

		this.loadProperties(TriviaClient.PROPERTIES);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public void update(boolean force) {
		this.internalScrollPanel.update(force);
	}

	@Override
	protected void loadProperties(Properties properties) {
		/**
		 * Colors
		 */
		final Color headerBackgroundColor = new Color(new BigInteger(
				properties.getProperty("ScoreByRound.Header.BackgroundColor"), 16).intValue());
		final Color roundColor = new Color(
				new BigInteger(properties.getProperty("ScoreByRound.Round.Color"), 16).intValue());
		final Color earnedColor = new Color(new BigInteger(properties.getProperty("Earned.Color"), 16).intValue());
		final Color valueColor = new Color(new BigInteger(properties.getProperty("Value.Color"), 16).intValue());
		final Color percentColor = new Color(
				new BigInteger(properties.getProperty("ScoreByRound.Percent.Color"), 16).intValue());
		final Color announcedColor = new Color(new BigInteger(properties.getProperty("Announced.Color"), 16).intValue());
		final Color discrepancyColor = new Color(new BigInteger(
				properties.getProperty("ScoreByRound.Discrepancy.Color"), 16).intValue());

		/**
		 * Sizes
		 */
		final int headerHeight = Integer.parseInt(properties.getProperty("ScoreByRound.Header.Height"));

		final int roundWidth = Integer.parseInt(properties.getProperty("ScoreByRound.Round.Width"));
		final int roundEarnedWidth = Integer.parseInt(properties.getProperty("ScoreByRound.Round.Earned.Width"));
		final int roundValueWidth = Integer.parseInt(properties.getProperty("ScoreByRound.Round.Value.Width"));
		final int roundPercentWidth = Integer.parseInt(properties.getProperty("ScoreByRound.Round.Percent.Width"));
		final int totalEarnedWidth = Integer.parseInt(properties.getProperty("ScoreByRound.Total.Earned.Width"));
		final int totalValueWidth = Integer.parseInt(properties.getProperty("ScoreByRound.Total.Value.Width"));
		final int totalPercentWidth = Integer.parseInt(properties.getProperty("ScoreByRound.Total.Percent.Width"));
		final int announcedWidth = Integer.parseInt(properties.getProperty("ScoreByRound.Announced.Width"));
		final int placeWidth = Integer.parseInt(properties.getProperty("ScoreByRound.Place.Width"));
		final int discrepancyWidth = Integer.parseInt(properties.getProperty("ScoreByRound.Discrepancy.Width"));
		/**
		 * Font sizes
		 */
		final float headerFontSize = Float.parseFloat(properties.getProperty("ScoreByRound.Header.FontSize"));

		setLabelProperties(this.roundLabel0, roundWidth, headerHeight / 2, roundColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.roundLabel1, roundWidth, headerHeight / 2, roundColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.roundEarnedLabel0, roundEarnedWidth, headerHeight / 2, earnedColor,
				headerBackgroundColor, headerFontSize);
		setLabelProperties(this.roundEarnedLabel1, roundEarnedWidth, headerHeight / 2, earnedColor,
				headerBackgroundColor, headerFontSize);
		setLabelProperties(this.roundValueLabel0, roundValueWidth, headerHeight / 2, valueColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.roundValueLabel1, roundValueWidth, headerHeight / 2, valueColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.roundPercentLabel0, roundPercentWidth, headerHeight / 2, percentColor,
				headerBackgroundColor, headerFontSize);
		setLabelProperties(this.roundPercentLabel1, roundPercentWidth, headerHeight / 2, percentColor,
				headerBackgroundColor, headerFontSize);
		setLabelProperties(this.totalEarnedLabel0, totalEarnedWidth, headerHeight / 2, earnedColor,
				headerBackgroundColor, headerFontSize);
		setLabelProperties(this.totalEarnedLabel1, totalEarnedWidth, headerHeight / 2, earnedColor,
				headerBackgroundColor, headerFontSize);
		setLabelProperties(this.totalValueLabel0, totalValueWidth, headerHeight / 2, valueColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.totalValueLabel1, totalValueWidth, headerHeight / 2, valueColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.totalPercentLabel0, totalPercentWidth, headerHeight / 2, percentColor,
				headerBackgroundColor, headerFontSize);
		setLabelProperties(this.totalPercentLabel1, totalPercentWidth, headerHeight / 2, percentColor,
				headerBackgroundColor, headerFontSize);
		setLabelProperties(this.announcedLabel0, announcedWidth, headerHeight / 2, announcedColor,
				headerBackgroundColor, headerFontSize);
		setLabelProperties(this.announcedLabel1, announcedWidth, headerHeight / 2, announcedColor,
				headerBackgroundColor, headerFontSize);
		setLabelProperties(this.placeLabel0, placeWidth, headerHeight / 2, announcedColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.placeLabel1, placeWidth, headerHeight / 2, announcedColor, headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.discrepancyLabel0, discrepancyWidth, headerHeight / 2, discrepancyColor,
				headerBackgroundColor, headerFontSize);
		setLabelProperties(this.discrepancyLabel1, discrepancyWidth, headerHeight / 2, discrepancyColor,
				headerBackgroundColor, headerFontSize);

		this.internalScrollPanel.loadProperties(properties);

	}

	/**
	 * Scroll panel that contains the score data for every round
	 */
	private class InternalScrollPanel extends TriviaMainPanel implements ActionListener {

		/** The Constant serialVersionUID. */
		private static final long	serialVersionUID	= 7121481355244434308L;

		/**
		 * GUI elements that update
		 */
		final private JLabel[]		roundLabels, earnedLabels, valueLabels, percentLabels, cumulativeEarnedLabels,
				cumulativeValueLabels, percentTotalLabels, announcedScoreLabels, placeLabels, discrepancyLabels;
		private final JMenuItem		editItem;
		private final JPopupMenu	contextMenu;
		private final JPanel		spacer;

		/** The nunber of rounds */
		final private int			nRounds;

		/** Data sources */
		final private TriviaClient	client;

		/**
		 * Instantiates a new internal scroll panel.
		 * 
		 * @param server
		 *            the server
		 * @param client
		 *            the client application
		 */
		public InternalScrollPanel(TriviaClient client) {
			super();

			this.client = client;

			this.nRounds = client.getTrivia().getNRounds();

			/**
			 * Build context menu
			 */
			this.contextMenu = new JPopupMenu();
			this.editItem = new JMenuItem("Edit");
			this.editItem.setActionCommand("Edit");
			this.editItem.addActionListener(this);
			this.contextMenu.add(this.editItem);
			this.add(this.contextMenu);

			// Set up layout constraints
			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.NORTH;
			constraints.weighty = 0.0;

			this.roundLabels = new JLabel[this.nRounds];
			this.earnedLabels = new JLabel[this.nRounds];
			this.valueLabels = new JLabel[this.nRounds];
			this.percentLabels = new JLabel[this.nRounds];
			this.cumulativeEarnedLabels = new JLabel[this.nRounds];
			this.cumulativeValueLabels = new JLabel[this.nRounds];
			this.percentTotalLabels = new JLabel[this.nRounds];
			this.announcedScoreLabels = new JLabel[this.nRounds];
			this.placeLabels = new JLabel[this.nRounds];
			this.discrepancyLabels = new JLabel[this.nRounds];

			// Create the labels for each round
			for (int r = 0; r < this.nRounds; r++) {

				constraints.weightx = 0.0;
				constraints.weighty = 0.0;

				constraints.gridx = 0;
				constraints.gridy = r;
				this.roundLabels[r] = this.enclosedLabel(( r + 1 ) + "", constraints, SwingConstants.CENTER,
						SwingConstants.CENTER);

				constraints.gridx = 1;
				constraints.gridy = r;
				this.earnedLabels[r] = this
						.enclosedLabel("", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

				constraints.gridx = 2;
				constraints.gridy = r;
				this.valueLabels[r] = this.enclosedLabel("", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

				constraints.gridx = 3;
				constraints.gridy = r;
				this.percentLabels[r] = this
						.enclosedLabel("", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

				constraints.gridx = 4;
				constraints.gridy = r;
				this.cumulativeEarnedLabels[r] = this.enclosedLabel("", constraints, SwingConstants.CENTER,
						SwingConstants.CENTER);

				constraints.gridx = 5;
				constraints.gridy = r;
				this.cumulativeValueLabels[r] = this.enclosedLabel("", constraints, SwingConstants.CENTER,
						SwingConstants.CENTER);

				constraints.gridx = 6;
				constraints.gridy = r;
				this.percentTotalLabels[r] = this.enclosedLabel("", constraints, SwingConstants.RIGHT,
						SwingConstants.CENTER);

				constraints.gridx = 7;
				constraints.gridy = r;
				this.announcedScoreLabels[r] = this.enclosedLabel("", constraints, SwingConstants.CENTER,
						SwingConstants.CENTER);

				constraints.gridx = 8;
				constraints.gridy = r;
				this.placeLabels[r] = this.enclosedLabel("", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

				constraints.weightx = 1.0;
				constraints.gridx = 9;
				constraints.gridy = r;

				this.discrepancyLabels[r] = this.enclosedLabel("", constraints, SwingConstants.LEFT,
						SwingConstants.CENTER);
				this.discrepancyLabels[r].setName(( r + 1 ) + "");
				this.discrepancyLabels[r].addMouseListener(new PopupListener(this.contextMenu));
			}

			// Add a blank row at the bottom as a spacer
			constraints.weightx = 1.0;
			constraints.weighty = 1.0;
			constraints.gridx = 0;
			constraints.gridy = this.nRounds;
			constraints.gridwidth = 11;
			this.spacer = new JPanel(new GridBagLayout());
			this.spacer.setPreferredSize(new Dimension(0, 0));
			this.add(this.spacer, constraints);

		}

		@Override
		public void actionPerformed(ActionEvent event) {
			final int rNumber = Integer.parseInt(this.contextMenu.getName());
			new DiscrepancyDialog(this.client, rNumber);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.bubbaland.trivia.TriviaPanel#update()
		 */
		@Override
		public void update(boolean force) {
			// Get the current Trivia data object
			final Trivia trivia = this.client.getTrivia();

			int cumulativeEarned = 0;
			int cumulativeValue = 0;
			for (int r = 0; r < this.nRounds; r++) {
				// Read in the data
				final int earned = trivia.getEarned(r + 1);
				final int value = trivia.getValue(r + 1);
				final int announced = trivia.getAnnouncedPoints(r + 1);
				final int place = trivia.getAnnouncedPlace(r + 1);
				final String discrepancy = trivia.getDiscrepancyText(r + 1);
				final boolean isAnnounced = trivia.isAnnounced(r + 1);
				cumulativeEarned += earned;
				cumulativeValue += value;

				final boolean updated;
				if (value == 0) {
					updated = !( this.earnedLabels[r].getText().equals("") && this.valueLabels[r].getText().equals("")
							&& this.cumulativeEarnedLabels[r].getText().equals("")
							&& this.cumulativeValueLabels[r].getText().equals("")
							&& ( this.announcedScoreLabels[r].getText().equals("") )
							&& ( this.placeLabels[r].getText().equals("") ) && this.discrepancyLabels[r].getText()
							.equals(discrepancy) );
				} else if (!isAnnounced) {
					updated = !( this.earnedLabels[r].getText().equals(earned + "")
							&& this.valueLabels[r].getText().equals(value + "")
							&& this.cumulativeEarnedLabels[r].getText().equals(cumulativeEarned + "")
							&& this.cumulativeValueLabels[r].getText().equals(cumulativeValue + "")
							&& this.announcedScoreLabels[r].getText().equals("")
							&& this.placeLabels[r].getText().equals("") && this.discrepancyLabels[r].getText().equals(
							discrepancy) );
				} else {
					updated = !( this.earnedLabels[r].getText().equals(earned + "")
							&& this.valueLabels[r].getText().equals(value + "")
							&& this.cumulativeEarnedLabels[r].getText().equals(cumulativeEarned + "")
							&& this.cumulativeValueLabels[r].getText().equals(cumulativeValue + "")
							&& this.announcedScoreLabels[r].getText().equals(announced + "")
							&& this.placeLabels[r].getText().equals(TriviaClient.ordinalize(place)) && this.discrepancyLabels[r]
							.getText().equals(discrepancy) );
				}


				if (updated || force) {
					if (value != 0) {
						// If the round has started, update all of the labels for the round
						final String percent = String.format("%04.1f", ( earned * 100.0 / value )) + "%";
						final String percentTotal = String.format("%04.1f",
								( cumulativeEarned * 100.0 / cumulativeValue )) + "%";
						this.earnedLabels[r].setText(earned + "");
						this.valueLabels[r].setText(value + "");
						this.percentLabels[r].setText(percent);
						this.cumulativeEarnedLabels[r].setText(cumulativeEarned + "");
						this.cumulativeValueLabels[r].setText(cumulativeValue + "");
						this.percentTotalLabels[r].setText(percentTotal);
						if (isAnnounced) {
							this.announcedScoreLabels[r].setText(announced + "");
							this.placeLabels[r].setText(TriviaClient.ordinalize(place));
						}
						this.discrepancyLabels[r].setText(discrepancy);
					} else {
						this.earnedLabels[r].setText("");
						this.valueLabels[r].setText("");
						this.percentLabels[r].setText("");
						this.cumulativeEarnedLabels[r].setText("");
						this.cumulativeValueLabels[r].setText("");
						this.percentTotalLabels[r].setText("");
						this.announcedScoreLabels[r].setText("");
						this.placeLabels[r].setText("");
						this.discrepancyLabels[r].setText("");
					}
				}

			}

		}

		@Override
		protected void loadProperties(Properties properties) {
			/**
			 * Colors
			 */
			final Color headerBackgroundColor = new Color(new BigInteger(
					properties.getProperty("ScoreByRound.Header.BackgroundColor"), 16).intValue());
			final Color backgroundColor = new Color(new BigInteger(
					properties.getProperty("ScoreByRound.BackgroundColor"), 16).intValue());
			final Color altBackgroundColor = new Color(new BigInteger(
					properties.getProperty("ScoreByRound.AltBackgroundColor"), 16).intValue());
			final Color roundColor = new Color(
					new BigInteger(properties.getProperty("ScoreByRound.Round.Color"), 16).intValue());
			final Color earnedColor = new Color(new BigInteger(properties.getProperty("Earned.Color"), 16).intValue());
			final Color valueColor = new Color(new BigInteger(properties.getProperty("Value.Color"), 16).intValue());
			final Color percentColor = new Color(new BigInteger(properties.getProperty("ScoreByRound.Percent.Color"),
					16).intValue());
			final Color announcedColor = new Color(
					new BigInteger(properties.getProperty("Announced.Color"), 16).intValue());
			final Color discrepancyColor = new Color(new BigInteger(
					properties.getProperty("ScoreByRound.Discrepancy.Color"), 16).intValue());

			/**
			 * Sizes
			 */
			final int rowHeight = Integer.parseInt(properties.getProperty("ScoreByRound.Row.Height"));

			final int roundWidth = Integer.parseInt(properties.getProperty("ScoreByRound.Round.Width"));
			final int roundEarnedWidth = Integer.parseInt(properties.getProperty("ScoreByRound.Round.Earned.Width"));
			final int roundValueWidth = Integer.parseInt(properties.getProperty("ScoreByRound.Round.Value.Width"));
			final int roundPercentWidth = Integer.parseInt(properties.getProperty("ScoreByRound.Round.Percent.Width"));
			final int totalEarnedWidth = Integer.parseInt(properties.getProperty("ScoreByRound.Total.Earned.Width"));
			final int totalValueWidth = Integer.parseInt(properties.getProperty("ScoreByRound.Total.Value.Width"));
			final int totalPercentWidth = Integer.parseInt(properties.getProperty("ScoreByRound.Total.Percent.Width"));
			final int announcedWidth = Integer.parseInt(properties.getProperty("ScoreByRound.Announced.Width"));
			final int placeWidth = Integer.parseInt(properties.getProperty("ScoreByRound.Place.Width"));
			final int discrepancyWidth = Integer.parseInt(properties.getProperty("ScoreByRound.Discrepancy.Width"));
			/**
			 * Font sizes
			 */
			final float fontSize = Float.parseFloat(properties.getProperty("ScoreByRound.FontSize"));
			final float discrepancyFontSize = Float.parseFloat(properties
					.getProperty("ScoreByRound.Discrepancy.FontSize"));

			/** The number of open questions to show at one time */
			final int altColorInterval = Integer.parseInt(properties.getProperty("ScoreByRound.AltInterval"));

			// Create the labels for each round
			for (int r = 0; r < this.nRounds; r++) {
				final Color bColor = ( ( r + 1 ) % altColorInterval == 0 ) ? altBackgroundColor : backgroundColor;
				setLabelProperties(this.roundLabels[r], roundWidth, rowHeight, roundColor, bColor, fontSize);
				setLabelProperties(this.earnedLabels[r], roundEarnedWidth, rowHeight, earnedColor, bColor, fontSize);
				setLabelProperties(this.valueLabels[r], roundValueWidth, rowHeight, valueColor, bColor, fontSize);
				setLabelProperties(this.percentLabels[r], roundPercentWidth, rowHeight, percentColor, bColor, fontSize);
				setLabelProperties(this.cumulativeEarnedLabels[r], totalEarnedWidth, rowHeight, earnedColor, bColor,
						fontSize);
				setLabelProperties(this.cumulativeValueLabels[r], totalValueWidth, rowHeight, valueColor, bColor,
						fontSize);
				setLabelProperties(this.percentTotalLabels[r], totalPercentWidth, rowHeight, percentColor, bColor,
						fontSize);
				setLabelProperties(this.announcedScoreLabels[r], announcedWidth, rowHeight, announcedColor, bColor,
						fontSize);
				setLabelProperties(this.placeLabels[r], placeWidth, rowHeight, announcedColor, bColor, fontSize);
				setLabelProperties(this.discrepancyLabels[r], discrepancyWidth, rowHeight, discrepancyColor, bColor,
						discrepancyFontSize);
			}
			this.spacer.setBackground(headerBackgroundColor);

		}

		private class PopupListener extends MouseAdapter {

			private final JPopupMenu	menu;

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
				final Trivia trivia = InternalScrollPanel.this.client.getTrivia();
				final int rNumber = Integer.parseInt(source.getName());
				if (event.isPopupTrigger() && trivia.isAnnounced(rNumber)) {
					this.menu.setName(source.getName());
					this.menu.show(source, event.getX(), event.getY());
				}
			}

		}


	}


}