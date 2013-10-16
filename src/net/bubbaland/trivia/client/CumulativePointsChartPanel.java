package net.bubbaland.trivia.client;

import java.awt.GridBagConstraints;
import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.TriviaCharts;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

/**
 * A panel with a stacked XY plot that shows the cumulative score by round.
 * 
 * @author Walter Kolczynski
 * 
 */
public class CumulativePointsChartPanel extends TriviaPanel {

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= -6171512617495834445L;

	/** The chart panel */
	private ChartPanel			chartPanel;

	/** Data */
	final private int			nRounds;
	private int[]			values, earneds;

	/** The client */
	final private TriviaClient	client;

	/**
	 * Instantiates a new chart panel
	 * 
	 * @param client
	 *            The local trivia client
	 * 
	 */
	public CumulativePointsChartPanel(TriviaClient client) {
		super();

		this.client = client;
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

		// Read score data and determine if there have been any changes
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

		// If there has been a change, remake the chart
		if (change || force) {
			// Create the Stacked XY plot
			final JFreeChart chart = TriviaCharts.CumulativePointsChartFactory(trivia);

			// Remove the old chart if it exists
			if (this.chartPanel != null) {
				this.remove(this.chartPanel);
			}

			// Create a new chart panel
			this.chartPanel = new ChartPanel(chart);

			// Add the new chart to the panel
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

}
