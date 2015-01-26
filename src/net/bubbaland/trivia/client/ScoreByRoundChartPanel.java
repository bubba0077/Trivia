package net.bubbaland.trivia.client;

import java.awt.GridBagConstraints;
import java.util.Properties;

import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.TriviaChartFactory;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

/**
 * A panel that displays a stacked bar chart showing the score in each round.
 * 
 * @author Walter Kolczynski
 * 
 */
public class ScoreByRoundChartPanel extends TriviaMainPanel {

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= -5408662436975410795L;

	/** Data */
	final private int			nRounds;
	private int[]				values;
	private int[]				earneds;

	/** The chart panel. */
	private ChartPanel			chartPanel;

	/**
	 * Instantiates a new score by round chart panel.
	 * 
	 * @param client
	 *            the client application
	 */
	public ScoreByRoundChartPanel(TriviaClient client, TriviaFrame parent) {
		super(client, parent);

		this.nRounds = client.getTrivia().getNRounds();

		this.values = new int[this.nRounds];
		this.earneds = new int[this.nRounds];

		this.chartPanel = null;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void update(boolean force) {
		// Get the current Trivia data object
		final Trivia trivia = this.client.getTrivia();

		// Get the scores for each round and check if they are updated
		final int[] newValues = new int[this.nRounds];
		final int[] newEarneds = new int[this.nRounds];
		boolean change = false;
		for (int r = 0; r < this.nRounds; r++) {
			newValues[r] = trivia.getValue(r + 1);
			newEarneds[r] = trivia.getEarned(r + 1);
			change = change || ( newValues[r] != this.values[r] ) || ( newEarneds[r] != this.earneds[r] );
		}

		this.values = newValues;
		this.earneds = newEarneds;

		// If the data has changed, remake the chart
		if (change || force) {
			final JFreeChart chart = TriviaChartFactory.makeScoreByRoundChart(trivia);

			// If a chart panel already exists, remove it
			if (this.chartPanel != null) {
				this.remove(this.chartPanel);
			}
			// Add new chart panel
			this.chartPanel = new ChartPanel(chart);

			// Add the chart to the panel
			final GridBagConstraints solo = new GridBagConstraints();
			solo.fill = GridBagConstraints.BOTH;
			solo.anchor = GridBagConstraints.CENTER;
			solo.weightx = 1.0;
			solo.weighty = 1.0;
			solo.gridx = 0;
			solo.gridy = 0;
			this.add(this.chartPanel, solo);

		}

	}

	@Override
	protected void loadProperties(Properties properties) {
	}

}
